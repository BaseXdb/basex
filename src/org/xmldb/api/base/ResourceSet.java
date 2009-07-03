package org.xmldb.api.base;

/*
 * The XML:DB Initiative Software License, Version 1.0
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
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * XML:DB Initiative (http://www.xmldb.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "XML:DB Initiative" must not be used to endorse or
 * promote products derived from this software without prior written
 * permission. For written permission, please contact info@xmldb.org.
 *
 * 5. Products derived from this software may not be called "XML:DB",
 * nor may "XML:DB" appear in their name, without prior written
 * permission of the XML:DB Initiative.
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
 * ResourceSet is a container for a set of resources. Generally a
 * <code>ResourceSet</code> is obtained as the result of a query.
 */
public interface ResourceSet {
   /**
    * Returns the <code>Resource</code> instance stored at the index specified
    * by index.
    * <p />
    * If the underlying implementation uses a paging or streaming optimization
    * for retrieving Resource instances. Calling this method MAY result in a
    * block until the requested Resource has been downloaded.
    *
    * @param index the index of the resource to retrieve.
    * @return The <code>Resource</code> instance
    * @exception XMLDBException with expected error codes.<br />
    * <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    * specific errors that occur.<br />
    * <code>ErrorCodes.NO_SUCH_RESOURCE if the index is out of range for the
    * set.
    */
   Resource getResource(long index) throws XMLDBException;

   /**
    * Adds a <code>Resource</code> instance to the set.
    *
    * @param res The <code>Resource</code> to add to the set.
    * @exception XMLDBException with expected error codes.<br />
    * <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    * specific errors that occur.<br />
    */
   void addResource(Resource res) throws XMLDBException;

   /**
    * Removes the Resource located at <code>index</code> from the set.
    *
    * @param index  The index of the <code>Resource</code> instance to remove.
    * @exception XMLDBException with expected error codes.<br />
    * <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    * specific errors that occur.<br />
    */
   void removeResource(long index) throws XMLDBException;

   /**
    * Returns an iterator over all <code>Resource</code> instances stored in the set.
    *
    * @return a <code>ResourceIterator</code> over all <code>Resource</code>
    *   instances in the set.
    * @exception XMLDBException with expected error codes.<br />
    * <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    * specific errors that occur.<br />
    */
   ResourceIterator getIterator() throws XMLDBException;

   /**
    * Returns a Resource containing an XML representation of all resources
    * stored in the set. <p />
    *
    * @return A <code>Resource</code> instance containing an XML representation
    *   of all set members.
    * @exception XMLDBException with expected error codes.<br />
    * <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    * specific errors that occur.<br />
    */
   Resource getMembersAsResource() throws XMLDBException;

   /**
    * Returns the number of resources contained in the set.
    * <p />
    * If the underlying implementation uses a paging or streaming optimization
    * for retrieving <code>Resource</code> instances. Calling this method MAY
    * force the downloading of all set members before the size can be determined.
    *
    * @return The number of <code>Resource</code> instances in the set.
    * @exception XMLDBException with expected error codes.<br />
    * <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    * specific errors that occur.<br />
    */
   long getSize() throws XMLDBException;

   /**
    * Removes all <code>Resource</code> instances from the set.
    *
    * @exception XMLDBException with expected error codes.<br />
    * <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    * specific errors that occur.<br />
    */
   void clear() throws XMLDBException;
}

