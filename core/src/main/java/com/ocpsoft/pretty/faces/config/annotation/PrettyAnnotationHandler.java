package com.ocpsoft.pretty.faces.config.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLAction.PhaseId;
import com.ocpsoft.pretty.faces.annotation.URLActions;
import com.ocpsoft.pretty.faces.annotation.URLBeanName;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLQueryParameter;
import com.ocpsoft.pretty.faces.annotation.URLValidator;
import com.ocpsoft.pretty.faces.config.PrettyConfigBuilder;
import com.ocpsoft.pretty.faces.config.mapping.PathValidator;
import com.ocpsoft.pretty.faces.config.mapping.QueryParameter;
import com.ocpsoft.pretty.faces.config.mapping.UrlAction;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import com.ocpsoft.pretty.faces.el.ConstantExpression;
import com.ocpsoft.pretty.faces.el.LazyBeanNameFinder;
import com.ocpsoft.pretty.faces.el.LazyExpression;
import com.ocpsoft.pretty.faces.el.PrettyExpression;

@SuppressWarnings("unchecked")
public class PrettyAnnotationHandler
{

   /**
    * The logger
    */
   private static final Log log = LogFactory.getLog(PrettyAnnotationHandler.class);

   /**
    * A map assigning mapping IDs to {@link UrlMapping} instances
    */
   private final Map<String, UrlMapping> urlMappings = new HashMap<String, UrlMapping>();

   /**
    * A map to resolve the bean name for a {@link Class} object
    */
   private final Map<Class, String> beanNameMap = new HashMap<Class, String>();

   /**
    * The {@link ActionSpec} objects generated from annotation scanning
    */
   private final List<ActionSpec> urlActions = new ArrayList<ActionSpec>();

   /**
    * The {@link QueryParamSpec} objects generated from annotation scanning
    */
   private final List<QueryParamSpec> queryParamList = new ArrayList<QueryParamSpec>();

   /**
    * Reference to the {@link LazyBeanNameFinder}
    */
   private final LazyBeanNameFinder beanNameFinder;

   /**
    * Constructor
    */
   public PrettyAnnotationHandler(LazyBeanNameFinder beanNameFinder)
   {
      this.beanNameFinder = beanNameFinder;
   }

   /**
    * This method scans the supplied class for PrettyFaces annotations. The
    * method must be called for every class that should be scanner before
    * finally calling {@link #build(PrettyConfigBuilder)}.
    * 
    * @param clazz The class to scan
    */
   public void processClass(Class clazz)
   {

      // log class name on trace level
      if (log.isTraceEnabled())
      {
         log.trace("Analyzing class: " + clazz.getName());
      }

      try
      {

         // scan for PrettyAnnotation class
         // returns the mapping ID, if an annotation was found
         String classMappingId = processPrettyMappingAnnotation(clazz);

         // scan for PrettyBean annotation
         processPrettyBeanAnnotation(clazz);

         // process annotations on public methods
         for (Method method : clazz.getMethods())
         {
            processMethodAnnotations(method, classMappingId);
         }

         // loop over fields to find URLQueryParameter annotations
         for (Field field : clazz.getDeclaredFields())
         {
            processFieldAnnotations(field, classMappingId);
         }

      }
      catch (NoClassDefFoundError e)
      {
         // reference to another class unknown to the classloader
         log.debug("Unable to process class '" + clazz.getName() + "': " + e.toString());
      }

   }

   /**
    * Checks the class for a {@link URLMapping} annotation.
    * 
    * @param clazz Class to scan
    * @return The mapping ID or <code>null</code> if no mapping was found
    */
   private String processPrettyMappingAnnotation(Class clazz)
   {

      // get reference to @URLMapping annotation
      URLMapping mappingAnnotation = (URLMapping) clazz.getAnnotation(URLMapping.class);

      // process annotation if it exists
      if (mappingAnnotation != null)
      {

         // log class name
         if (log.isTraceEnabled())
         {
            log.trace("Found @URLMapping annotation on class: " + clazz.getName());
         }

         // create UrlMapping from annotation
         UrlMapping mapping = new UrlMapping();
         mapping.setId(mappingAnnotation.id());
         mapping.setParentId(mappingAnnotation.parentId());
         mapping.setPattern(mappingAnnotation.pattern());
         mapping.setViewId(mappingAnnotation.viewId());
         mapping.setOutbound(mappingAnnotation.outbound());
         mapping.setOnPostback(mappingAnnotation.onPostback());

         // register mapping
         Object existingMapping = urlMappings.put(mapping.getId(), mapping);

         // fail if a mapping with this ID already existed
         if (existingMapping != null)
         {
            throw new IllegalArgumentException("Duplicated mapping id: " + mapping.getId());
         }

         // At bean name to lookup map if it has been specified
         if ((mappingAnnotation.beanName() != null) && (mappingAnnotation.beanName().length() > 0))
         {
            beanNameMap.put(clazz, mappingAnnotation.beanName());
         }

         // process validations
         for (URLValidator validationAnnotation : mappingAnnotation.validation())
         {

            // index attribute is required in this case
            if (validationAnnotation.index() < 0)
            {
               throw new IllegalArgumentException(
                     "Please set the index of the path parameter you want to validate with the @URLValidator specified on mapping: " + mapping.getId());
            }

            // prepare PathValidator
            PathValidator pathValidator = new PathValidator();
            pathValidator.setIndex(validationAnnotation.index());
            pathValidator.setOnError(validationAnnotation.onError());
            pathValidator.setValidatorIds(join(validationAnnotation.validatorIds(), " "));

            // optional validator method
            if (!isBlank(validationAnnotation.validator()))
            {
               pathValidator.setValidatorExpression(new ConstantExpression(validationAnnotation.validator()));
            }

            // add PathValidator to the mapping
            mapping.getPathValidators().add(pathValidator);

         }

         // return mapping id
         return mapping.getId();

      }

      // we have not found a mapping
      return null;
   }

   /**
    * Checks the class for a {@link URLBeanName} annotation.
    * 
    * @param clazz Class to scan
    */
   private void processPrettyBeanAnnotation(Class clazz)
   {

      // get reference to @URLMapping annotation
      URLBeanName prettyBean = (URLBeanName) clazz.getAnnotation(URLBeanName.class);

      // process annotation if it exists
      if (prettyBean != null)
      {

         // log class name
         if (log.isTraceEnabled())
         {
            log.trace("Found @URLBeanName annotation on class: " + clazz.getName());
         }

         // add bean to map
         beanNameMap.put(clazz, prettyBean.value());

      }

   }

   /**
    * Searches for {@link URLAction} or {@link URLActions} annotations on a
    * method.
    * 
    * @param method Method to scan
    * @param classMappingId The mapping ID of the class this method belongs to
    */
   private void processMethodAnnotations(Method method, String classMappingId)
   {

      // is there a @URLAction annotation on the class?
      URLAction actionAnnotation = method.getAnnotation(URLAction.class);
      if (actionAnnotation != null)
      {
         processPrettyActionAnnotation(actionAnnotation, method, classMappingId);
      }

      // is there a @URLAction container annotation on the class?
      URLActions actionsAnnotation = method.getAnnotation(URLActions.class);
      if (actionsAnnotation != null)
      {
         // process all @URLAction annotations
         for (URLAction child : actionsAnnotation.actions())
         {
            processPrettyActionAnnotation(child, method, classMappingId);
         }
      }
   }

   /**
    * Searches for {@link URLQueryParameter} annotations on a single field.
    * 
    * @param field Field to scan
    * @param classMappingId The mapping ID of the class this method belongs to
    */
   private void processFieldAnnotations(Field field, String classMappingId)
   {
      // Is there a @URLQueryParameter annotation?
      URLQueryParameter queryParamAnnotation = field.getAnnotation(URLQueryParameter.class);

      if (queryParamAnnotation != null)
      {

         // create a QueryParamSpec from the annotation
         QueryParamSpec queryParam = new QueryParamSpec();
         queryParam.setFieldName(field.getName());
         queryParam.setOwnerClass(field.getDeclaringClass());
         queryParam.setName(queryParamAnnotation.value());
         queryParam.setOnPostback(queryParamAnnotation.onPostback());

         // check which mapping the action belongs to
         if (!isBlank(queryParamAnnotation.mappingId()))
         {
            // action belongs to the mapping mentioned with mappingId
            // attribute
            queryParam.setMappingId(queryParamAnnotation.mappingId().trim());
         }
         else if (!isBlank(classMappingId))
         {
            // use the mapping found on the class
            queryParam.setMappingId(classMappingId.trim());
         }
         else
         {
            throw new IllegalArgumentException("Unable to find a suitable mapping "
                  + "for the query-parameter definied on field '" + field.getName() + "' in class '"
                  + field.getDeclaringClass().getName() + "'. Either place a @URLMapping annotation on the "
                  + "class or reference a foreign mapping using the 'mappingId' attribute.");
         }

         // check if there is also a validation annotation placed on the field
         URLValidator validationAnnotation = field.getAnnotation(URLValidator.class);

         // check if annotation has been found
         if (validationAnnotation != null)
         {

            // set validation options on the QueryParamSpec object
            queryParam.setValidatorIds(validationAnnotation.validatorIds());
            queryParam.setOnError(validationAnnotation.onError());
            queryParam.setValidator(validationAnnotation.validator());

         }

         // add the new spec object to the list of specs
         queryParamList.add(queryParam);

      }

   }

   /**
    * Creates a {@link UrlAction} object from the supplied {@link URLAction}
    * annotation
    * 
    * @param actionAnnotation The annotation
    * @param method The method that was annotated
    * @param classMappingId the mapping ID of the current class
    */
   private void processPrettyActionAnnotation(URLAction actionAnnotation, Method method, String classMappingId)
   {

      // Create ActionSpec
      ActionSpec actionSpec = new ActionSpec();
      actionSpec.setMethod(method);
      actionSpec.setOnPostback(actionAnnotation.onPostback());
      actionSpec.setPhaseId(actionAnnotation.phaseId());

      // check which mapping the action belongs to
      if (!isBlank(actionAnnotation.mappingId()))
      {
         // action belongs to the mapping mentioned with mappingId attribute
         actionSpec.setMappingId(actionAnnotation.mappingId().trim());
      }
      else if (!isBlank(classMappingId))
      {
         // use the mapping found on the class
         actionSpec.setMappingId(classMappingId.trim());
      }
      else
      {
         // No mapping found... throw an exception..
         throw new IllegalArgumentException("Unable to find a suitable mapping "
               + "for the action definied on method '" + method.getName() + "' in class '"
               + method.getDeclaringClass().getName() + "'. Either place a @URLMapping annotation on the "
               + "class or reference a foreign mapping using the 'mappingId' attribute.");
      }

      // add action to list of actions
      urlActions.add(actionSpec);

   }

   /**
    * Returns <code>true</code> for "blank" strings.
    * 
    * @param str Input string
    * @return <code>true</code> if string is <code>null</code> or trimmed value
    *         is empty
    */
   private static boolean isBlank(String str)
   {
      return (str == null) || (str.trim().length() == 0);
   }

   /**
    * This methods adds all mappings found to the supplied
    * {@link PrettyConfigBuilder}. It should be called after all classes has
    * been scanned via {@link #processClass(Class)}.
    * 
    * @param builder The builder to add the mappings to
    */
   public void build(PrettyConfigBuilder builder)
   {

      // process all actions found
      for (ActionSpec actionSpec : urlActions)
      {

         // Get the mapping references by the action
         UrlMapping mapping = urlMappings.get(actionSpec.getMappingId());

         /*
          * Fail for unresolved mappings. This may happen when the user places
          * invalid mapping IDs in the mappingId attribute of
          * 
          * @URLAction or @URLQueryParameter
          */
         if (mapping == null)
         {
            throw new IllegalArgumentException("Unable to find the mapping '" + actionSpec.getMappingId()
                  + "' referenced at method '" + actionSpec.getMethod().getName() + "' in class '"
                  + actionSpec.getMethod().getDeclaringClass().getName() + "'.");
         }

         // build UrlMapping
         UrlAction urlAction = new UrlAction();
         urlAction.setPhaseId(actionSpec.getPhaseId());
         urlAction.setOnPostback(actionSpec.isOnPostback());

         // try to get bean name
         Class clazz = actionSpec.getMethod().getDeclaringClass();

         // build expression
         PrettyExpression expression = buildPrettyExpression(clazz, actionSpec.getMethod().getName());
         urlAction.setAction(expression);

         // trace
         if (log.isTraceEnabled())
         {
            log.trace("Adding action expression '" + urlAction.getAction() + "' to mapping: " + mapping.getId());
         }

         // register this action
         mapping.addAction(urlAction);

      }

      for (QueryParamSpec queryParamSpec : queryParamList)
      {

         // Get the mapping references by the query param
         UrlMapping mapping = urlMappings.get(queryParamSpec.getMappingId());

         // fail for unresolved mappings
         if (mapping == null)
         {
            throw new IllegalArgumentException("Unable to find the mapping '" + queryParamSpec.getMappingId()
                  + "' referenced at field '" + queryParamSpec.getFieldName() + "' in class '"
                  + queryParamSpec.getOwnerClass().getName() + "'.");
         }

         // build UrlMapping
         QueryParameter queryParam = new QueryParameter();
         queryParam.setName(queryParamSpec.getName());
         queryParam.setOnError(queryParamSpec.getOnError());
         queryParam.setValidatorIds(join(queryParamSpec.getValidatorIds(), " "));
         queryParam.setOnPostback(queryParamSpec.isOnPostback());

         // optional validator method
         if (!isBlank(queryParamSpec.getValidator()))
         {
            queryParam.setValidatorExpression(new ConstantExpression(queryParamSpec.getValidator()));
         }

         // try to get bean name
         Class<?> clazz = queryParamSpec.getOwnerClass();

         // build expression
         PrettyExpression expression = buildPrettyExpression(clazz, queryParamSpec.getFieldName());
         queryParam.setExpression(expression);

         // trace
         if (log.isTraceEnabled())
         {
            log.trace("Registered query-param '" + queryParam.getName() + "' to '" + expression + "' in mapping: "
                  + mapping.getId());
         }

         // register this action
         mapping.addQueryParam(queryParam);

      }

      // finally register all mappings
      for (UrlMapping mapping : urlMappings.values())
      {
         builder.addMapping(mapping);
      }
   }

   /**
    * Creates a {@link PrettyExpression} for a class and component. This method
    * may return a {@link ConstantExpression} or a {@link LazyExpression}.
    * 
    * @param clazz The class of the bean
    * @param component the component (property or method name)
    * @return The expression
    */
   private PrettyExpression buildPrettyExpression(Class<?> clazz, String component)
   {

      if (log.isTraceEnabled())
      {
         log.trace("Searching name of bean: " + clazz.getName());
      }

      // get name from internal map build from @URLBeanName annotations and
      // previously resolved names
      String beanName = beanNameMap.get(clazz);

      // return a constant expression
      if (beanName != null)
      {
         if (log.isTraceEnabled())
         {
            log.trace("Got bean name from @URLBeanName annotation: " + beanName);
         }

         return new ConstantExpression("#{" + beanName + "." + component + "}");
      }

      // build a lazy expression
      else
      {

         if (log.isTraceEnabled())
         {
            log.trace("Name of bean not found. Building lazy expression for: " + clazz.getName());
         }

         return new LazyExpression(beanNameFinder, clazz, component);

      }

   }

   /**
    * Joins the list of values.
    * 
    * @param values values to join
    * @param separator the separator to use
    * @return joined list of values
    */
   private static String join(String[] values, String separator)
   {
      StringBuilder result = new StringBuilder();
      if (values != null)
      {
         for (int i = 0; i < values.length; i++)
         {
            if (i > 0)
            {
               result.append(separator);
            }
            result.append(values[i]);
         }
      }
      return result.toString();
   }

   /**
    * Internal class to hold parameters of a {@link URLAction} annotation.
    * 
    * @author Christian Kaltepoth
    */
   private static class ActionSpec
   {

      private Method method;
      private boolean onPostback;
      private PhaseId phaseId;
      private String mappingId;

      public boolean isOnPostback()
      {
         return onPostback;
      }

      public void setOnPostback(boolean onPostback)
      {
         this.onPostback = onPostback;
      }

      public PhaseId getPhaseId()
      {
         return phaseId;
      }

      public void setPhaseId(PhaseId phaseId)
      {
         this.phaseId = phaseId;
      }

      public String getMappingId()
      {
         return mappingId;
      }

      public void setMappingId(String mappingId)
      {
         this.mappingId = mappingId;
      }

      public Method getMethod()
      {
         return method;
      }

      public void setMethod(Method method)
      {
         this.method = method;
      }

   }

   /**
    * Internal class to hold parameters of a Pretty annotation.
    * 
    * @author Christian Kaltepoth
    */
   private static class QueryParamSpec
   {
      private String fieldName;
      private Class<?> ownerClass;
      private String mappingId;
      private String name;
      private String onError;
      private String[] validatorIds = {};
      private String validator;
      private boolean onPostback;

      public String getValidator()
      {
         return validator;
      }

      public void setValidator(String validator)
      {
         this.validator = validator;
      }

      public String getMappingId()
      {
         return mappingId;
      }

      public void setMappingId(String mappingId)
      {
         this.mappingId = mappingId;
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getFieldName()
      {
         return fieldName;
      }

      public void setFieldName(String fieldName)
      {
         this.fieldName = fieldName;
      }

      public Class<?> getOwnerClass()
      {
         return ownerClass;
      }

      public void setOwnerClass(Class<?> ownerClass)
      {
         this.ownerClass = ownerClass;
      }

      public String getOnError()
      {
         return onError;
      }

      public void setOnError(String onError)
      {
         this.onError = onError;
      }

      public String[] getValidatorIds()
      {
         return validatorIds;
      }

      public void setValidatorIds(String[] validatorIds)
      {
         this.validatorIds = validatorIds;
      }

      public boolean isOnPostback()
      {
         return onPostback;
      }

      public void setOnPostback(boolean onPostback)
      {
         this.onPostback = onPostback;
      }
   }

}