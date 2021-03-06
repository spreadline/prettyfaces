/*
 * Copyright 2010 Lincoln Baxter, III
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ocpsoft.pretty.faces.rewrite.processor;

import com.ocpsoft.pretty.faces.config.rewrite.RewriteRule;
import com.ocpsoft.pretty.faces.rewrite.Processor;

/**
 * @author Lincoln Baxter, III <lincoln@ocpsoft.com>
 */
public class TrailingSlashProcessor implements Processor
{

   public String process(final RewriteRule rule, final String url)
   {

      String result = url;
      switch (rule.getTrailingSlash())
      {
      case APPEND:
         if (!result.endsWith("/"))
         {
            result = result + "/";
         }
         break;
      case REMOVE:
         if (result.endsWith("/") && !"/".equals(result))
         {
            result = result.substring(0, result.length() - 1);
         }
         break;
      default:
         break;
      }
      return result;
   }

}
