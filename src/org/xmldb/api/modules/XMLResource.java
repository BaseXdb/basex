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
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Provides access to XML resources stored in the database. An XMLResource can
 * be accessed either as text XML or via the DOM or SAX APIs.<p />
 *
 * The default behavior for getContent and setContent is to work with XML data
 * as text so these methods work on <code>String</code> content.
 */
public interface XMLResource extends Resource {
  /**
   * RESOURCE_TYPE
   */
   public static final String RESOURCE_TYPE = "XMLResource";
   
   /**
    * Returns the unique id for the parent document to this <code>Resource</code>
    * or null if the <code>Resource</code> does not have a parent document. 
    * <code>getDocumentId()</code> is typically used with <code>Resource</code>
    * instances retrieved using a query. It enables accessing the parent
    * document of the <code>Resource</code> even if the <code>Resource</code> is
    * a child node of the document. If the <code>Resource</code> was not
    * obtained through a query then <code>getId()</code> and 
    * <code>getDocumentId()</code> will return the same id.
    *
    * @return the id for the parent document of this <code>Resource</code> or 
    *  null if there is no parent document for this <code>Resource</code>.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br /> 
    */
   String getDocumentId() throws XMLDBException;
   
   /**
    * Returns the content of the <code>Resource</code> as a DOM Node.
    *
    * @return The XML content as a DOM <code>Node</code>
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   Node getContentAsDOM() throws XMLDBException;

   /**
    * Sets the content of the <code>Resource</code> using a DOM Node as the
    * source.
    *
    * @param content The new content value
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.INVALID_RESOURCE</code> if the content value provided is
    *  null.<br />
    *  <code>ErrorCodes.WRONG_CONTENT_TYPE</code> if the content provided in not
    *  a valid DOM <code>Node</code>.
    */
   void setContentAsDOM(Node content) throws XMLDBException;

   /**
    * Allows you to use a <code>ContentHandler</code> to parse the XML data from
    * the database for use in an application.
    *
    * @param handler the SAX <code>ContentHandler</code> to use to handle the
    *  <code>Resource</code> content.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    *  <code>ErrorCodes.INVALID_RESOURCE</code> if the 
    *  <code>ContentHandler</code> provided is null.<br />
    */
   void getContentAsSAX(ContentHandler handler) throws XMLDBException;

   /**
    * Sets the content of the <code>Resource</code> using a SAX 
    * <code>ContentHandler</code>.
    *
    * @return a SAX <code>ContentHandler</code> that can be used to add content
    *  into the <code>Resource</code>.
    * @exception XMLDBException with expected error codes.<br />
    *  <code>ErrorCodes.VENDOR_ERROR</code> for any vendor
    *  specific errors that occur.<br />
    */
   ContentHandler setContentAsSAX() throws XMLDBException;
}

