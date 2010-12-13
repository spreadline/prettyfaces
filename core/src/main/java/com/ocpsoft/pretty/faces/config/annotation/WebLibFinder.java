package com.ocpsoft.pretty.faces.config.annotation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.servlet.ServletContext;

/**
 * Implementation of {@link ClassFinder} that searches for classes in the JAR
 * archives found in the <code>/WEB-INF/lib/</code> directory of a web
 * application.
 * 
 * @author Christian Kaltepoth
 */
public class WebLibFinder extends AbstractClassFinder
{

   /**
    * The web application directory containing the JAR files
    */
   private final static String LIB_FOLDER = "/WEB-INF/lib/";

   /**
    * Initialization
    */
   public WebLibFinder(ServletContext servletContext, ClassLoader classLoader, PackageFilter packageFilter)
   {
      super(servletContext, classLoader, packageFilter);
   }

   /*
    * @see
    * com.ocpsoft.pretty.faces.config.annotation.ClassFinder#findClasses(com
    * .ocpsoft.pretty.faces.config.annotation.PrettyAnnotationHandler)
    */
   @SuppressWarnings("unchecked")
   public void findClasses(PrettyAnnotationHandler classHandler)
   {

      // catch MalformedURLException
      try
      {

         // get URL of the lib folder
         URL libFolderUrl = servletContext.getResource(LIB_FOLDER);

         // abort on missing lib folder
         if (libFolderUrl == null)
         {
            log.warn("Cannot find " + LIB_FOLDER + " folder!");
            return;
         }

         // build directory name relative to webapp root
         String relativeName = getWebappRelativeName(libFolderUrl, LIB_FOLDER);

         // call getResourcePaths to get directory entries
         Set paths = servletContext.getResourcePaths(relativeName);

         // loop over all entries of the directory
         for (Object relativePath : paths)
         {

            // get full URL of the current directory entry
            URL entryUrl = servletContext.getResource(relativePath.toString());

            // we are only interested in JAR files
            if (entryUrl.getPath().endsWith(".jar"))
            {
               processJarFile(entryUrl, classHandler);
            }

         }

      }
      catch (MalformedURLException e)
      {
         throw new IllegalStateException("Invalid URL: " + e.getMessage(), e);
      }
   }

   /**
    * Process a single JAR file in the <code>/WEB-INF/lib/</code> directory.
    * 
    * @param jarUrl
    *           The URL of the JAR file
    * @param classHandler
    *           The handler instance to notify
    */
   private void processJarFile(URL jarUrl, PrettyAnnotationHandler classHandler)
   {

      // log file name on debug lvel
      if (log.isDebugEnabled())
      {
         log.debug("Processing JAR file: " + jarUrl.toString());
      }

      // Use a JarInputStream to read the archive
      JarInputStream jarStream = null;

      // catch any type of IOException
      try
      {

         // open the JAR stream
         jarStream = new JarInputStream(jarUrl.openStream());

         // Loop over all entries of the archive
         JarEntry jarEntry = null;
         while ((jarEntry = jarStream.getNextJarEntry()) != null)
         {

            // We are only interested in java class files
            if (jarEntry.getName().endsWith(".class"))
            {

               // generate FQCN from entry
               String className = getClassName(jarEntry.getName(), null);

               // check name against PackageFilter
               if (mustProcessClass(className))
               {

                  // analyze this class
                  processClass(className, jarStream, classHandler);

               }

            }

         }

      }
      catch (IOException e)
      {
         log.error("Failed to read JAR file: " + jarUrl.toString(), e);
      }
      finally
      {
         // Close the stream if it has been opened
         if (jarStream != null)
         {
            try
            {
               jarStream.close();
            }
            catch (IOException e)
            {
               // ignore IO failures on close
            }
         }
      }

   }

}