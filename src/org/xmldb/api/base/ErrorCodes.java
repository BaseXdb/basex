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
 * ErrorCodes defines XML:DB error codes that can be used to set the
 * <code>errorCodes</code> attribute of an <code>XMLDBException</code>
 */
public class ErrorCodes
{
   /**
    * Set when a more detailed error can not be determined.
    */
   public static final int UNKNOWN_ERROR = 0;
   /**
    * Set when a vendor specific error has occured.
    */
   public static final int VENDOR_ERROR = 1;
   /**
    * Set if the API implementation does not support the operation being
    * invoked.
    */
   public static final int NOT_IMPLEMENTED = 2;
   /**
    * Set if the content of a <code>Resource</code> is set to a content type
    * different then that for which the <code>Resource</code> was intended to
    * support.
    */
   public static final int WRONG_CONTENT_TYPE = 3;
   /**
    * Set if access to the requested <code>Collection</code> can not be granted
    * due to the lack of proper credentials.
    */
   public static final int PERMISSION_DENIED = 4;
   /**
    * Set if the URI format is invalid.
    */
   public static final int INVALID_URI = 5;

   /**
    * Set if the requested <code>Service</code> could not be located.
    */
   public static final int NO_SUCH_SERVICE = 100;

   /**
    * Set if the requested <code>Collection</code> could not be located.
    */
   public static final int NO_SUCH_COLLECTION = 200;
   /**
    * Set if the Collection instance is in an invalid state.
    */
   public static final int INVALID_COLLECTION = 201;
   /**
    * Set when an operation is invoked against a <code>Collection</code>
    * instance that has been closed.
    */
   public static final int COLLECTION_CLOSED = 202;

   /**
    * Set if the requested <code>Resource</code> could not be located.
    */
   public static final int NO_SUCH_RESOURCE = 300;
   /**
    * Set if the <code>Resource</code> provided to an operation is invalid.
    */
   public static final int INVALID_RESOURCE = 301;
   /**
    * Set if the resource type requested is unknown to the API implementation.
    */
   public static final int UNKNOWN_RESOURCE_TYPE = 302;

   /**
    * Set if a <code>Database</code> instance can not be located for the
    * provided URI.
    */
   public static final int NO_SUCH_DATABASE = 400;
   /**
    * Set if the <code>Database</code> instance being registered is invalid.
    */
   public static final int INVALID_DATABASE = 401;
}
