package org.xmldb.api.base;

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

/**
 * A <code>Collection</code> represents a collection of <code>Resource</code>s 
 * stored within an XML
 * database. An XML
 * database MAY expose collections as a hierarchical set of parent and child
 * collections.<p />
 *
 * A <code>Collection</code> provides access to the <code>Resource</code>s
 * stored by the <code>Collection</code> and to <code>Service</code> instances
 * that can operate against the <code>Collection</code> and the 
 * <code>Resource</code>s stored within it. The <code>Service</code> mechanism
 * provides the ability to extend the functionality of a <code>Collection</code>
 * in ways that allows optional functionality to be enabled for the <code>Collection</code>. 
 */
public interface Collection extends Configurable {
   /**
    * Returns the name associated with the Collection instance.
    *
    * @return the name of the object.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   String getName() throws XMLDBException;
   
   /**
    * Provides a list of all services known to the collection. If no services
    * are known an empty list is returned.
    *
    * @return An array of registered <code>Service</code> implementations.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   Service[] getServices() throws XMLDBException;

   /**
    * Returns a <code>Service</code> instance for the requested service name and version. If
    * no <code>Service</code> exists for those parameters a null value is returned.
    *
    * @param name Description of Parameter
    * @param version Description of Parameter
    * @return the Service instance or null if no Service could be found.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   Service getService(String name, String version) throws XMLDBException;

   /**
    * Returns the parent collection for this collection or null if no parent
    * collection exists.
    *
    * @return the parent <code>Collection</code> instance.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   Collection getParentCollection() throws XMLDBException;

   /**
    * Returns the number of child collections under this 
    * <code>Collection</code> or 0 if no child collections exist.
    *
    * @return the number of child collections.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   int getChildCollectionCount() throws XMLDBException;

   /**
    * Returns a list of collection names naming all child collections
    * of the current collection. If no child collections exist an empty list is
    * returned.
    *
    * @return an array containing collection names for all child
    *      collections.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   String[] listChildCollections() throws XMLDBException;

   /**
    * Returns a <code>Collection</code> instance for the requested child collection 
    * if it exists.
    *
    * @param name the name of the child collection to retrieve.
    * @return the requested child collection or null if it couldn't be found.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   Collection getChildCollection(String name) throws XMLDBException;
   
   /**
    * Returns the number of resources currently stored in this collection or 0
    * if the collection is empty.
    *
    * @return the number of resource in the collection.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   int getResourceCount() throws XMLDBException;

   /**
    * Returns a list of the ids for all resources stored in the collection.
    *
    * @return a string array containing the names for all 
    *  <code>Resource</code>s in the collection.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   String[] listResources() throws XMLDBException;

   /**
    * Creates a new empty <code>Resource</code> with the provided id. 
    * The type of <code>Resource</code>
    * returned is determined by the <code>type</code> parameter. The XML:DB API currently 
    * defines "XMLResource" and "BinaryResource" as valid resource types.
    * The <code>id</code> provided must be unique within the scope of the 
    * collection. If 
    * <code>id</code> is null or its value is empty then an id is generated by   
    * calling <code>createId()</code>. The
    * <code>Resource</code> created is not stored to the database until 
    * <code>storeResource()</code> is called.
    *
    * @param id the unique id to associate with the created <code>Resource</code>.
    * @param type the <code>Resource</code> type to create.
    * @return an empty <code>Resource</code> instance.    
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.UNKNOWN_RESOURCE_TYPE</code> if the <code>type</code>
    *   parameter is not a known <code>Resource</code> type.
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   Resource createResource(String id, String type) throws XMLDBException;

   /**
    * Removes the <code>Resource</code> from the database.
    *
    * @param res the resource to remove.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.INVALID_RESOURCE</code> if the <code>Resource</code> is
    *   not valid.<br />
    *  <code>ErrorCodes.NO_SUCH_RESOURCE</code> if the <code>Resource</code> is
    *   not known to this <code>Collection</code>.
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   void removeResource(Resource res) throws XMLDBException;

   /**
    * Stores the provided resource into the database. If the resource does not
    * already exist it will be created. If it does already exist it will be
    * updated.
    *
    * @param res the resource to store in the database.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.INVALID_RESOURCE</code> if the <code>Resource</code> is
    *   not valid.
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   void storeResource(Resource res) throws XMLDBException;

   /**
    * Retrieves a <code>Resource</code> from the database. If the 
    * <code>Resource</code> could not be
    * located a null value will be returned.
    *
    * @param id the unique id for the requested resource.
    * @return The retrieved <code>Resource</code> instance.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />    
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   Resource getResource(String id) throws XMLDBException;

   /**
    * Creates a new unique ID within the context of the <code>Collection</code>
    *
    * @return the created id as a string.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.COLLECTION_CLOSED</code> if the <code>close</code> 
    *  method has been called on the <code>Collection</code><br />
    */
   String createId() throws XMLDBException;

   /**
    * Returns true if the  <code>Collection</code> is open false otherwise.
    * Calling the <code>close</code> method on 
    * <code>Collection</code> will result in <code>isOpen</code>
    * returning false. It is not safe to use <code>Collection</code> instances
    * that have been closed.
    *
    * @return true if the <code>Collection</code> is open, false otherwise.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   boolean isOpen() throws XMLDBException;

   /**
    * Releases all resources consumed by the <code>Collection</code>. 
    * The <code>close</code> method must
    * always be called when use of a <code>Collection</code> is complete. It is
    * not safe to use a  <code>Collection</code> after the <code>close</code>
    * method has been called.
    *
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   void close() throws XMLDBException;
}

