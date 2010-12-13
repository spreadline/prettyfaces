package com.ocpsoft.pretty.faces.url;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ocpsoft.pretty.PrettyException;

public class URL
{
   private Metadata metadata = new Metadata();
   private String originalURL = "";
   private List<String> segments;

   private final Map<String, URL> encodedURLs = new HashMap<String, URL>();
   private final Map<String, URL> decodedURLs = new HashMap<String, URL>();
   private final Map<String, List<String>> decodedSegments = new HashMap<String, List<String>>();

   /**
    * Create a URL object for the given url String. The input string must not
    * yet have been decoded.
    * 
    * @param url The raw, un-decoded url String
    */
   public URL(final String url)
   {
      if (url != null)
      {
         originalURL = url.trim();
         if (originalURL.endsWith("/"))
         {
            metadata.setTrailingSlash(true);
         }
         if (originalURL.startsWith("/"))
         {
            metadata.setLeadingSlash(true);
         }

         String trimmedUrl = trimSurroundingSlashes(url);
         String[] segments = trimmedUrl.split("/");

         this.segments = Arrays.asList(segments);
      }
      else
      {
         throw new IllegalArgumentException("URL cannot be null.");
      }
   }

   /**
    * Create a URL object for the given url segments (separated by '/' from the
    * original url string), using the given metadata object to represent the
    * encoding and leading/trailing slash information about this URL.
    */
   public URL(final List<String> segments, final Metadata metadata)
   {
      this.metadata = metadata;
      this.segments = segments;
      this.originalURL = metadata.buildURLFromSegments(segments);
   }

   /**
    * Get a list of all decoded segments (separated by '/') in this URL.
    */
   public List<String> getDecodedSegments()
   {
      String encoding = metadata.getEncoding();
      if (!decodedSegments.containsKey(encoding))
      {
         List<String> result = new ArrayList<String>();
         for (String segment : segments)
         {
            try
            {
               String decoded = URLDecoder.decode(segment, encoding);
               result.add(decoded);
            }
            catch (UnsupportedEncodingException e)
            {
               throw new PrettyException("Could not decode URL with specified format: " + encoding, e);
            }
         }
         decodedSegments.put(encoding, Collections.unmodifiableList(result));
      }
      return decodedSegments.get(encoding);
   }

   /**
    * Get a list of all encoded segments (separated by '/') in this URL.
    */
   public List<String> getEncodedSegments()
   {
      String encoding = metadata.getEncoding();
      List<String> resultSegments = new ArrayList<String>();
      for (String segment : segments)
      {
         try
         {
            String encoded = URLEncoder.encode(segment, encoding);
            resultSegments.add(encoded);
         }
         catch (UnsupportedEncodingException e)
         {
            throw new PrettyException("Could not encode URL with specified format: " + encoding, e);
         }
      }
      return resultSegments;
   }

   /**
    * Return a decoded form of this URL.
    */
   public URL decode()
   {
      return new URL(getDecodedSegments(), metadata);
   }

   /**
    * Return an encoded form of this URL.
    */
   public URL encode()
   {
      return new URL(getEncodedSegments(), metadata);
   }

   /**
    * Get the number of segments (separated by '/') in this URL
    */
   public int numSegments()
   {
      return segments.size();
   }

   /**
    * Return a String representation of this URL
    */
   @Override
   public String toString()
   {
      return toURL();
   }

   /*
    * Getters & Setters
    */

   /**
    * Return a String representation of this URL
    */
   public String toURL()
   {
      return originalURL;
   }

   /**
    * Return all segments (separated by '/') in this URL
    * 
    * @return
    */
   public List<String> getSegments()
   {
      return Collections.unmodifiableList(segments);
   }

   /**
    * Return true if this URL begins with '/'
    */
   public boolean hasLeadingSlash()
   {
      return metadata.hasLeadingSlash();
   }

   /**
    * Return true if this URL ends with '/'
    */
   public boolean hasTrailingSlash()
   {
      return metadata.hasTrailingSlash();
   }

   /**
    * Get the character encoding of this URL (default UTF-8)
    */
   public String getEncoding()
   {
      return metadata.getEncoding();
   }

   /**
    * Set the character encoding of this URL (default UTF-8)
    */
   public void setEncoding(final String encoding)
   {
      metadata.setEncoding(encoding);
   }

   /**
    * Get the {@link Metadata} object for this URL
    */
   public Metadata getMetadata()
   {
      return metadata;
   }

   /**
    * Set the {@link Metadata} object for this URL
    */
   public void setMetadata(final Metadata metadata)
   {
      this.metadata = metadata;
   }

   /*
    * Helpers
    */
   private String trimSurroundingSlashes(final String url)
   {
      String result = null;
      if (url != null)
      {
         result = url.trim();
         if (result.startsWith("/"))
         {
            result = result.substring(1);
         }
         if (result.endsWith("/"))
         {
            result = result.substring(0, result.length() - 1);
         }
      }
      return result;
   }
}