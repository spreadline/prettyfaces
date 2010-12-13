/*
 * PrettyFaces is an OpenSource JSF library to create bookmarkable URLs.
 * 
 * Copyright (C) 2009 - Lincoln Baxter, III <lincoln@ocpsoft.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see the file COPYING.LESSER or visit the GNU
 * website at <http://www.gnu.org/licenses/>.
 */
package com.ocpsoft.pretty.faces.component;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentELTag;

import com.ocpsoft.pretty.faces.el.Expressions;

/**
 * @author Lincoln Baxter, III <lincoln@ocpsoft.com>
 */
abstract public class PrettyTagBase extends UIComponentELTag
{
   protected void setAttributeProperites(final UIComponent component, final String attributeName, final String attribute)
   {
      if (attribute != null)
      {
         if (Expressions.isEL(attribute))
         {
            FacesContext context = FacesContext.getCurrentInstance();
            Application app = context.getApplication();

            ExpressionFactory expressionFactory = app.getExpressionFactory();
            ValueExpression ve = expressionFactory.createValueExpression(attribute, Object.class);
            component.setValueExpression(attributeName, ve);
         }
         else
         {
            component.getAttributes().put(attributeName, attribute);
         }
      }
   }
}