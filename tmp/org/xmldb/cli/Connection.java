package org.xmldb.cli;

/*
 * Copyright (c) 2000 The XML:DB Initiative. All Rights Reserved.
 *
 * This XML:DB work (including software, documents, or other related 
 * items) is being provided by the copyright holders under the following
 * license. By obtaining, using and/or copying this work, you (the 
 * licensee) agree that you have read, understood, and will comply with 
 * the following terms and conditions: 
 *
 * Permission to use, copy, and modify this software and its 
 * documentation, with or without modification, for any purpose and 
 * without fee or royalty is hereby granted, provided that you include 
 * the following on ALL copies of the software and documentation or 
 * portions thereof, including modifications, that you make: 
 *
 *    1. The full text of this NOTICE in a location viewable to users 
 *       of the redistributed or derivative work. 
 *    2. Any pre-existing intellectual property disclaimers, notices, 
 *       or terms and conditions. 
 *    3. Notice of any changes or modifications to the XML:DB files, 
 *       including the date changes were made. (We recommend you provide 
 *       URIs to the location from which the code is derived.) 
 *
 * THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT 
 * HOLDERS MAKE NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO, WARRANTIES OF MERCHANTABILITY OR FITNESS
 * FOR ANY PARTICULAR PURPOSE OR THAT THE USE OF THE SOFTWARE OR 
 * DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, 
 * TRADEMARKS OR OTHER RIGHTS. 
 *
 * COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL 
 * OR CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR 
 * DOCUMENTATION. 
 *
 * The name and trademarks of copyright holders may NOT be used in 
 * advertising or publicity pertaining to the software without specific,
 * written prior permission. Title to copyright in this software and any 
 * associated documentation will at all times remain with copyright 
 * holders. 
 *
 * $Id: Connection.java,v 1.6 2000/11/16 20:46:14 kstaken Exp $
 */

/**
 * getConnection represents a physical (or logical) connection to 
 * an XML database.
 */

public interface Connection {
   /**
    * getFeatures returns the feature set for this Connection.
    *
    * @see org.xmldb.cli.Features
    *
    * @return a Feature set
    */   
   Features getFeatures();
   
   /**
    * getDriver returns the Driver that manages this Connection.
    *
    * @return The Driver
    */
   Driver getDriver();  

   /**
    * createTransaction creates a new Transaction object.
    *
    * @return A new Transaction
    */
   Transaction createTransaction() throws NotSupportedException;

   /**
    * resolveContainer resolves/opens a Container based on the Connection-
    * relative location provided.
    *
    * @param location The Container location
    * @return The resolved Container
    */
   Container resolveContainer(String location) throws NotFoundException;
   
   /**
    * createContainer attempts to create a new Container based on the
    * Connection-relative location provided.
    *
    * @param location The Container location
    * @return The newly created Container
    */
   Container createContainer(String location) throws NotFoundException, NotSupportedException;
   
   /**
    * destroyContainer attempts to destroy the phyiscal image of the
    * specific Container object.
    *
    * @param container The Container to destroy
    */
   boolean destroyContainer(Container container) throws NotSupportedException;
   
   /**
    * getLanguages returns a list of the query languages supported by this
    * Connection.  Languages are considered features, so a developer may
    * also check the Feature set for a specific version of a language.
    *
    * @return The language set
    */
   String[] getLanguages() throws NotSupportedException;
   
   /**
    * getDefaultLanguage returns the default language used by this
    * Connection.  Languages are considered features, so a developer may
    * also check the Feature set for a specific version of a language.
    *
    * @return The default language name
    */
   String getDefaultLanguage() throws NotSupportedException;
   

   /**
    * setDefaultLanguage setsthe default language to be used by this
    * Connection.  Languages are considered features, so a developer may
    * also check the Feature set for a specific version of a language.
    *
    * @return The default language name
    */   
   void setDefaultLanguage(String language) throws NotSupportedException;
   
   /**
    * query performs a query against this Connection using the Connection's
    * default query language.
    *
    * @param query The query to execute
    * @return The resulting Container
    */
   Container query(String query) throws DBException;     

   /**
    * queryWithLanguage performs a query against this Connection using the 
    * specified query language.
    *
    * @param query The query to execute
    * @param language The language to use
    * @return The resulting Container
    */   
   Container queryWithLanguage(String query, String language) throws DBException;
   
   /**
    * query performs a query against this Connection using the Connection's
    * default query language.  The query is represented as a DOM Document.
    *
    * @param query The query to execute
    * @return The resulting Container
    */   
   Container queryDOM(org.w3c.dom.Document query) throws DBException;
   
   /**
    * queryDOMWithLanguage performs a query against this Connection using 
    * the specified query language.  The query is represented as a DOM Document.
    *
    * @param query The query to execute
    * @param language The language to use
    * @return The resulting Container
    */      
   Container queryDOMWithLanguage(org.w3c.dom.Document query, String language) throws DBException;
   
   /**
    * getRootContainer returns the root-level Container relative to the
    * current Connection.  The contents of this Container will differ
    * depending upon the implementation.
    *
    * @return The root-level Container
    */
   Container getRootContainer() throws NotSupportedException;
   
   /**
    * close closes the current Connection and releases any resources being
    * maintained by the Connection.
    */
   void close() throws DBException;
}

