package org.xmldb.api.modules;

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

/**
 * XPathQueryService is a <code>Service</code> that enables the execution of
 * XPath queries within the context of a <code>Collection</code> or against a 
 * single XML <code>Resource</code> stored in the <code>Collection</code>.
 */
public interface XPathQueryService extends Service {
   
   /**
    * Sets a namespace mapping in the internal namespace map used to evaluate
    * queries. If <code>prefix</code> is null or empty the default namespace is
    * associated with the provided URI. A null or empty <code>uri</code> results
    * in an exception being thrown.
    *
    * @param prefix The prefix to set in the map. If 
    *  <code>prefix</code> is empty or null the
    *  default namespace will be associated with the provided URI.
    * @param uri The URI for the namespace to be associated with prefix.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  TODO: probably need some special error here.
    */
   void setNamespace( String prefix, String uri ) throws XMLDBException; 

   /**   
    * Returns the URI string associated with <code>prefix</code> from
    * the internal namespace map. If <code>prefix</code> is null or empty the
    * URI for the default namespace will be returned. If a mapping for the 
    * <code>prefix</code> can not be found null is returned.
    *
    * @param prefix The prefix to retrieve from the namespace map. 
    * @return The URI associated with <code>prefix</code>
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   String getNamespace( String prefix ) throws XMLDBException;
   
   /**   
    * Removes the namespace mapping associated with <code>prefix</code> from
    * the internal namespace map. If <code>prefix</code> is null or empty the
    * mapping for the default namespace will be removed.
    *
    * @param prefix The prefix to remove from the namespace map. If 
    *  <code>prefix</code> is null or empty the mapping for the default
    *  namespace will be removed.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   void removeNamespace( String prefix ) throws XMLDBException;

   /**
    * Removes all namespace mappings stored in the internal namespace map.
    *
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   void clearNamespaces() throws XMLDBException;
   
   /**
    * Run an XPath query against the <code>Collection</code>. The XPath will be
    * applied to all XML resources stored in the <code>Collection</code>. 
    * The result is a 
    * <code>ResourceSet</code> containing the results of the query. Any
    * namespaces used in the <code>query</code> string will be evaluated using
    * the mappings setup using <code>setNamespace</code>.
    *
    * @param query The XPath query string to use.
    * @return A <code>ResourceSet</code> containing the results of the query.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   ResourceSet query( String query ) throws XMLDBException;

   /**
    * Run an XPath query against an XML resource stored in the 
    * <code>Collection</code> associated with this service. The result is a 
    * <code>ResourceSet</code> containing the results of the query. Any
    * namespaces used in the <code>query</code> string will be evaluated using
    * the mappings setup using <code>setNamespace</code>.
    *
    * @param query The XPath query string to use.
    * @param id The id of the document to run the query against.
    * @return A <code>ResourceSet</code> containing the results of the query.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */   
   ResourceSet queryResource( String id, String query ) throws XMLDBException;
}

