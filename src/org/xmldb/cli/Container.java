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
 * $Id: Container.java,v 1.4 2000/11/16 20:46:14 kstaken Exp $
 */

/**
 * Container is the generic representation of a Document Set.
 */

public interface Container {  
   /**
    * getFeatures returns the feature set for this Container.
    *
    * @see org.xmldb.cli.Features
    *
    * @return a Feature set
    */      
   Features getFeatures();
   
   /**
    * getParentContainer returns the parent Container of this Container 
    * (or null if there is none).
    *
    * @return The parent Container (or null)
    * @throws NotSupportedException exception
    */
   Container getParentContainer() throws NotSupportedException;
   
   /**
    * getChildContainerCount returns the number of child Containers that
    * this Container maintains.
    *
    * @return The child count
    * @throws NotSupportedException exception
    */
   int getChildContainerCount() throws NotSupportedException;
   
   /**
    * getChildContainers returns the set of child Containers that this
    * Container maintains.  If this features is supported by the 
    * implementation and there or no child containers, this method
    * MUST return a 0-length array of Container.
    *
    * @return The child Containers
    * @throws NotSupportedException exception
    */
   Container[] getChildContainers() throws NotSupportedException;
   
   /**
    * getDocumentCount returns the number of Documents being maintained
    * by this Container.
    *
    * @return The Document count 
    * @throws NotSupportedException exception
    */
   int getDocumentCount() throws NotSupportedException;
   
   /**
    * createID attempts to create a unique ID in the context of this Container.
    *
    * @param id A hint for the ID creation
    * @return The newly created ID
    * @throws NotSupportedException exception
    */
   ID createID(Object id) throws NotSupportedException;  
   
   /**
    * addDocumentText attempts to add a Document to this Container.  The
    * Document is represented in a textual format and therefore the
    * implementation is responsible for parsing to a native format.
    *
    * @param text The Document
    * @return A newly created ID
    * @throws ReadOnlyException exception
    * @throws NotSupportedException exception
    */
   ID addDocumentText(String text) throws ReadOnlyException,
     NotSupportedException;
   
   /**
    * addDocumentText attempts to add a Document to this Container.  The
    * Document is represented by a DOM tree and therefore the implementation 
    * is responsible for converting to a native format.
    *
    * @param doc The Document
    * @return A newly created ID
    * @throws ReadOnlyException exception
    * @throws NotSupportedException exception
    */
   ID addDocumentDOM(org.w3c.dom.Document doc) throws ReadOnlyException,
     NotSupportedException;
   
   /**
    * setDocumentText attempts to add or overwrite a Document in this 
    * Container.  The Document is represented in a textual format and 
    * therefore the implementation is responsible for parsing to a native 
    * format.
    *
    * @param id The Document ID
    * @param text The Document
    * @throws ReadOnlyException exception
    * @throws NotSupportedException exception
    */
   void setDocumentText(ID id, String text) throws ReadOnlyException,
     NotSupportedException;
   
   /**
    * setDocumentText attempts to add or overwrite a Document in this 
    * Container.  The Document is represented by a DOM tree and 
    * therefore the implementation is responsible for converting to a native 
    * format.
    *
    * @param id The Document ID
    * @param doc The Document
    * @throws ReadOnlyException exception
    * @throws NotSupportedException exception
    */   
   void setDocumentDOM(ID id, org.w3c.dom.Document doc)
     throws ReadOnlyException, NotSupportedException;

   /**
    * getDocument retrieves a Document by ID.  The Document 
    * is returned as Text.
    *
    * @param id The Document ID
    * @return The Document
    * @throws NotFoundException exception
    */
   String getDocumentText(ID id) throws NotFoundException;
   
   /**
    * getDocumentDOM retrieves a Document by ID.  The Document 
    * is returned as a DOM tree.
    *
    * @param id The Document ID
    * @return The Document
    * @throws NotFoundException exception
    * @throws NotSupportedException exception
    */   
   org.w3c.dom.Document getDocumentDOM(ID id) throws NotFoundException,
     NotSupportedException;
   
   /**
    * getDocument retrieves a Document by streaming it to a SAX ContentHandler.
    *
    * @param id The Document ID
    * @param handler The SAX ContentHandler
    * @throws NotFoundException exception
    * @throws NotSupportedException exception
    */   
   void getDocumentSAX(ID id, org.xml.sax.ContentHandler handler)
     throws NotFoundException, NotSupportedException;

   /**
    * removeDocument attempts to remove a Document from the Container.
    * 
    * @param id The Document ID
    * @return true if everything went alright
    * @throws ReadOnlyException exception
    * @throws NotSupportedException exception
    */
   boolean removeDocument(ID id) throws ReadOnlyException,
     NotSupportedException;

   /**
    * close closes the current Container and releases any resources being
    * maintained by the Container.
    * @throws DBException exception
    */
   void close() throws DBException; 
}

