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
 * $Id: DocumentIterator.java,v 1.4 2000/11/16 20:46:14 kstaken Exp $
 */

/**
 * DocumentIterator allows a client to iterate over a set of Documents.
 */

public interface DocumentIterator {
   /**
    * getContainer returns the Container that this DocumentIterator was
    * generated for (if any).
    *
    * @return The Container
    */
   Container getContainer();
   
   /**
    * hasMoreDocuments returns whether or not the DocumentIterator has reached
    * the end of its set.
    *
    * @return More Documents or not
    */
   boolean hasMoreDocuments();
   
   /**
    * nextID returns the next Document ID in the set
    * 
    * @return The next ID
    */
   ID nextID();
   
   /**
    * nextDocumentText returns the next Document in the set as text.
    *
    * @return The next Document
    */
   String nextDocumentText(); 
   
   /**
    * nextDocumentDOM returns the next Document in the set as a DOM tree.
    *
    * @return The next Document  
    */
   org.w3c.dom.Document nextDocumentDOM() throws NotSupportedException;
   
   /**
    * nextDocumentSAX processes the next Document in the set using the
    * specified SAX ContentHandler.
    *
    * @param handler The SAX ContentHandler
    */
   void nextDocumentSAX(org.xml.sax.ContentHandler handler) throws NotSupportedException;
   
   /**
    * getID returns the current Document ID in the Document set.
    *
    * @return the current ID
    */
   ID getID();
   
   /**
    * getDocumentText returns the current Document in the set as text.
    *
    * @return The current Document
    */
   String getDocumentText();
   
   /**
    * getDocumentDOM returns the current Document in the set as a DOM tree.
    *
    * @return The current Document  
    */   
   org.w3c.dom.Document getDocumentDOM() throws NotSupportedException;
   
   /**
    * getDocumentSAX processes the current Document in the set using the
    * specified SAX ContentHandler.
    *
    * @param handler The SAX ContentHandler
    */   
   void getDocumentSAX(org.xml.sax.ContentHandler handler) throws NotSupportedException;
   
   /**
    * close closes the current DocumentIterator and releases any resources 
    * being maintained by the DocumentIterator.
    */
   void close() throws DBException;    
}
