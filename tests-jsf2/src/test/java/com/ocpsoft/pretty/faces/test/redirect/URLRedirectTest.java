/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocpsoft.pretty.faces.test.redirect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URLEncoder;

import javax.faces.context.FacesContext;

import org.jboss.arquillian.MavenArtifactResolver;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jsfunit.jsfsession.JSFClientSession;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.jboss.jsfunit.jsfsession.JSFSession;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.ocpsoft.pretty.PrettyContext;

@RunWith(Arquillian.class)
public class URLRedirectTest
{
   @Deployment
   public static Archive<?> createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class, "test.war")
               .addClass(RedirectBean.class)
               .addResource("redirect/redirect.xhtml", "redirect.xhtml")
               .addWebResource("redirect/redirect-pretty-config.xml", "pretty-config.xml")
               .addWebResource("faces-config.xml")
               .addLibrary(MavenArtifactResolver.resolve(
                        "com.ocpsoft:prettyfaces-jsf2:3.1.1-SNAPSHOT"
                        ))
               .setWebXML("jsf-web.xml");
   }

   @Test
   public void testRefreshEncodesValuesPropertly() throws Exception
   {
      String expected = "/1 1/2 2";

      JSFSession jsfSession = new JSFSession(expected);
      JSFServerSession server = jsfSession.getJSFServerSession();

      JSFClientSession client = jsfSession.getJSFClientSession();

      FacesContext context = server.getFacesContext();
      PrettyContext prettyContext = PrettyContext.getCurrentInstance(context);

      client.click("refresh");

      String actual = prettyContext.getRequestURL().toString();
      assertEquals(expected, actual);
   }

   @Test
   public void testRedirectEncodesValuesPropertly() throws Exception
   {
      String requestURL = "/foo/" + URLEncoder.encode(RedirectBean.PATH_VALUE, "UTF-8") + "?que="
               + URLEncoder.encode(RedirectBean.QUERY_VALUE, "UTF-8");

      JSFSession jsfSession = new JSFSession(requestURL);
      JSFServerSession server = jsfSession.getJSFServerSession();

      JSFClientSession client = jsfSession.getJSFClientSession();

      FacesContext context = server.getFacesContext();
      PrettyContext prettyContext = PrettyContext.getCurrentInstance(context);

      client.click("redirect");

      String browserURL = client.getContentPage().getUrl().toString();
      assertTrue(browserURL.contains(requestURL));

      String expected = "/foo/" + RedirectBean.PATH_VALUE;
      String actual = prettyContext.getRequestURL().toString();
      assertEquals(expected, actual);

      // TODO QueryString should probably separate encoding from default behavior
      // String expectedQuery = "?que=" + RedirectBean.QUERY_VALUE;
      // String actualQuery = prettyContext.getRequestQueryString().toString();
      // assertEquals(expectedQuery, actualQuery);
   }
}
