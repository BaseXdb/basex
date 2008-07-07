package org.xmldb.api;

/*
 *  The XML:DB Initiative Software License, Version 1.0
 *
 *
 * Copyright (c) 2000-2001 The XML:DB Initiative.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        XML:DB Initiative (http://www.xmldb.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "XML:DB Initiative" must not be used to endorse or
 *    promote products derived from this software without prior written
 *    permission. For written permission, please contact info@xmldb.org.
 *
 * 5. Products derived from this software may not be called "XML:DB",
 *    nor may "XML:DB" appear in their name, without prior written
 *    permission of the XML:DB Initiative.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the XML:DB Initiative. For more information
 * on the XML:DB Initiative, please see <http://www.xmldb.org/>.
 */

import org.xmldb.api.base.*;

import java.util.*;

/**
 * <code>DatabaseManager</code> is the entry point for the API and enables you to get the 
 * initial <code>Collection</code> references necessary to do anything useful with the API.
 * <code>DatabaseManager</code> is intended to be
 * provided as a concrete implementation in a particular programming
 * language. Individual language mappings should define the exact syntax and
 * semantics of its use. 
 */
public class DatabaseManager    
{   
   /** URI_PREFIX */
   protected static final String URI_PREFIX = "xmldb:"; 
   /** properties */
   static Properties properties = new Properties();
   /** databases */
   static Hashtable<String, Database> databases = new Hashtable<String, Database>(); 
 
   /**
    * Returns a list of all available <code>Database</code> implementations 
    * that have been registered with this <code>DatabaseManager</code>.
    *
    * @return An array of <code>Database</code> instances. 
    *  One for each <code>Database</code> registered
    *  with the <code>DatabaseManager</code>. If no <code>Database</code>
    *  instances exist then an empty array is returned.
    */ 
   public static Database[] getDatabases () {
      Enumeration e = databases.elements();
      Database[] result = new Database[databases.size()];
      
      int i = 0;
      while (e.hasMoreElements()) {
         result[i] = (Database) e.nextElement();
         i++;
      }
      
      return result;
   }
   
   /**
    * Registers a new <code>Database</code> implementation with the
    * <code>DatabaseManager</code>. 
    *
    * @param database The database instance to register.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.INVALID_DATABASE</code> if the provided <code>Database
    *  </code> instance is invalid.
    */
   public static void registerDatabase (Database database) throws XMLDBException {
      if ((database.getName() == null) || (database.getName().equals(""))) {
         throw new XMLDBException(ErrorCodes.INVALID_DATABASE);
      }
      
      databases.put(database.getName(), database);
   }
   
   /**
    * Deregisters a <code>Database</code> implementation from the <code>DatabaseManager</code>. Once a
    * <code>Database</code> has been deregistered it can no longer be used to handle
    * requests.
    *
    * @param database The <code>Database</code> instance to deregister.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.
    */
   public static void deregisterDatabase (Database database) 
         throws XMLDBException {
      databases.remove(database.getName());
   }
   
   /**
    * Retrieves a <code>Collection</code> instance from the database for the 
    * given URI. The format of the majority of the URI is database 
    * implementation specific however the uri must begin with characters xmldb:
    * and be followed by the name of the database instance as returned by 
    * <code>Database.getName()</code> and a colon
    * character. An example would be for the database named "vendordb" the URI
    * handed to getCollection would look something like the following.
    * <code>xmldb:vendordb://host:port/path/to/collection</code>. The xmldb:
    * prefix will be removed from the URI prior to handing the URI to the
    * <code>Database</code> instance for handling. <p/>
    *
    * This method is called when no authentication is necessary for the
    * database.
    *
    * @param uri The database specific URI to use to locate the collection.
    * @return A <code>Collection</code> instance for the requested collection or
    *  null if the collection could not be found.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrroCodes.INVALID_URI</code> If the URI is not in a valid format. <br />
    *  <code>ErrroCodes.NO_SUCH_DATABASE</code> If a <code>Database</code>
    *    instance could not be found to handle the provided URI.
    */
   public static org.xmldb.api.base.Collection getCollection (String uri) 
         throws XMLDBException {
      return getCollection(uri, null, null);         
   }
   
      /**
    * Retrieves a <code>Collection</code> instance from the database for the 
    * given URI. The format of the majority of the URI is database 
    * implementation specific however the uri must begin with characters xmldb:
    * and be followed by the name of the database instance as returned by 
    * <code>Database.getName()</code> and a colon
    * character. An example would be for the database named "vendordb" the URI
    * handed to getCollection would look something like the following.
    * <code>xmldb:vendordb://host:port/path/to/collection</code>. The xmldb:
    * prefix will be removed from the URI prior to handing the URI to the
    * <code>Database</code> instance for handling.
    *
    * @param uri The database specific URI to use to locate the collection.
    * @param username The username to use for authentication to the database or
    *    null if the database does not support authentication.
    * @param password The password to use for authentication to the database or
    *    null if the database does not support authentication.
    * @return A <code>Collection</code> instance for the requested collection or
    *  null if the collection could not be found.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrroCodes.INVALID_URI</code> If the URI is not in a valid format. <br />
    *  <code>ErrroCodes.NO_SUCH_DATABASE</code> If a <code>Database</code>
    *    instance could not be found to handle the provided URI.
    *  <code>ErrroCodes.PERMISSION_DENIED</code> If the <code>username</code>
    *    and <code>password</code> were not accepted by the database.
    */
   public static org.xmldb.api.base.Collection getCollection (String uri,
         String username, String password) throws XMLDBException {
      Database db = getDatabase(uri);
     
      //uri = stripURIPrefix(uri);
      String tmp = stripURIPrefix(uri);
      
      return db.getCollection(tmp, username, password);   
   }
   
   /**
    * Returns the Core Level conformance value for the provided URI. The current
    * API defines valid resuls of "0" or "1" as defined in the XML:DB API
    * specification.
    *
    * @param uri The database specific URI to use to locate the collection.
    * @return The XML:DB Core Level conformance for the uri.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.
    *  <code>ErrroCodes.INVALID_URI</code> If the URI is not in a valid format. <br />
    *  <code>ErrroCodes.NO_SUCH_DATABASE</code> If a <code>Database</code>
    *    instance could not be found to handle the provided URI.
    */
   public static String getConformanceLevel (String uri) throws XMLDBException {
      Database database = getDatabase(uri);
      return database.getConformanceLevel();
   }
   
   /**
    * Retrieves a property that has been set for the <code>DatabaseManager</code>.
    *
    * @param name The property name
    * @return The property value
    */
   public static String getProperty (String name) {
      return properties.getProperty(name);
   }

   /**
    * Sets a property for the <code>DatabaseManager</code>.
    *
    * @param name The property name
    * @param value The value to set.
    */   
   public static void setProperty (String name, String value) {
      properties.put(name, value);
   }
   
   /**
    * Retrieves the registered <code>Database</code> instance associated with the provided 
    * URI.
    *
    * @param uri The uri containing the database reference.
    * @return the requested <code>Database</code> instance.
    * @throws XMLDBException
    */
   protected static Database getDatabase(String uri) throws XMLDBException {
      if (!uri.startsWith(URI_PREFIX)) {
         throw new XMLDBException(ErrorCodes.INVALID_URI);
      }
      
      int end = uri.indexOf(":", URI_PREFIX.length());      
      if (end == -1) {
         throw new XMLDBException(ErrorCodes.INVALID_URI);
      }
      
      String databaseName = uri.substring(URI_PREFIX.length(), end);
      
      Database db = databases.get(databaseName); 
      if (db == null) {
         throw new XMLDBException(ErrorCodes.NO_SUCH_DATABASE);         
      }
      
      return db;
   }
   
   /**
    * Removes the URI_PREFIX from the front of the URI. This is so the database
    * can focus on handling its own URIs.
    *
    * @param uri The full URI to strip.
    * @return The database specific portion of the URI.
    * @throws XMLDBException
    */
   protected static String stripURIPrefix(String uri) throws XMLDBException {
      if (!uri.startsWith(URI_PREFIX)) {
         throw new XMLDBException(ErrorCodes.INVALID_URI);
      }
                  
      String dbURI = uri.substring(URI_PREFIX.length(), uri.length());
      return dbURI;
   }
}
