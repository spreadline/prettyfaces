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
package com.ocpsoft.pretty.faces.url;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class URLTest
{
   @Test
   public void testURLPreservesOriginalURL() throws Exception
   {
      String value = "/com/ocpsoft/pretty/";
      URL url = new URL(value);
      assertEquals(value, url.toURL());
   }

   @Test
   public void testPreservesTrailingSlash() throws Exception
   {
      String value = "/com/ocpsoft/pretty/";
      URL url = new URL(value);
      url.setEncoding("UTF-8");
      assertEquals(value, url.decode().toURL());
   }

   @Test
   public void testGetURLReturnsOneSlashWhenBuiltWithEmptyList() throws Exception
   {
      Metadata metadata = new Metadata();
      metadata.setTrailingSlash(true);
      URL url = new URL(new ArrayList<String>(), metadata);

      assertEquals("/", url.toURL());
      assertEquals("/", url.decode().toURL());
   }

   @Test
   public void testDecode() throws Exception
   {
      String value = "/č";
      URL url = new URL(value);
      URL encoded = url.encode();
      assertEquals("/%C4%8D", encoded.toURL());
   }

   @Test
   public void testEncode() throws Exception
   {
      String value = "/č";
      URL url = new URL(value);
      URL encoded = url.encode();
      assertEquals("/%C4%8D", encoded.toURL());
      URL original = encoded.decode();
      assertEquals("/č", original.toURL());
   }

   @Test
   public void testEncodeDecodePreservesSlashes() throws Exception
   {
      String value = "/foo/bar";
      URL url = new URL(value);
      URL encoded = url.encode();
      assertEquals("/foo/bar", encoded.toURL());
      URL original = encoded.decode();
      assertEquals("/foo/bar", original.toURL());
   }
}
