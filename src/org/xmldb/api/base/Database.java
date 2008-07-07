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
 * <code>Database</code> is an encapsulation of the database driver 
 * functionality that is
 * necessary to access an XML database. Each vendor must provide their own
 * implmentation of the <code>Database</code> interface. The implementation 
 * is registered with the
 * <code>DatabaseManager</code> to provide access to the resources of the XML database.
 * <p />
 * In general usage client applications should only access <code>Database</code>
 * implementations directly during initialization.
 */
public interface Database extends Configurable {
   /**
    * Returns the name associated with the Database instance.
    *
    * @return the name of the object.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   String getName() throws XMLDBException;
   
   /**
    * Retrieves a <code>Collection</code> instance based on the URI provided 
    * in the <code>uri</code> parameter. The format of the URI is defined in the
    * documentation for DatabaseManager.getCollection().<p/>
    *
    * Authentication is handled via username and password however it is not
    * required that the database support authentication. Databases that do not
    * support authentication MUST ignore the 
    * <code>username</code> and <code>password</code> if those provided are not 
    * null. 
    *
    * @param uri the URI to use to locate the collection.
    * @param username the Username to use to locate the collection.
    * @param password The password to use for authentication to the database or
    *    null if the database does not support authentication.
    * @return A <code>Collection</code> instance for the requested collection or
    *  null if the collection could not be found.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrroCodes.INVALID_URI</code> If the URI is not in a valid format. <br />
    *  <code>ErrroCodes.PERMISSION_DENIED</code> If the <code>username</code>
    *    and <code>password</code> were not accepted by the database.
    */
   Collection getCollection(String uri, String username, String password) 
      throws XMLDBException;

   /**
    * acceptsURI determines whether this <code>Database</code> implementation 
    * can handle the URI. It should return true
    * if the Database instance knows how to handle the URI and false otherwise.
    *
    * @param uri the URI to check for.
    * @return true if the URI can be handled, false otherwise.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrroCodes.INVALID_URI</code> If the URI is not in a valid format. <br />
    */
   boolean acceptsURI(String uri) throws XMLDBException;

   /**
    * Returns the XML:DB API Conformance level for the implementation. This can
    * be used by client programs to determine what functionality is available to
    * them.
    *
    * @return the XML:DB API conformance level for this implementation.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   String getConformanceLevel() throws XMLDBException;
}

