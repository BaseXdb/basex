/*
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

/**
 * <code>XQMetaData</code> interface provides information about the data source, 
 * in various aspects, such as the product name and version identification,
 * supported features, specific behaviors, user information, product limits 
 * and so forth.
 * <br>
 * <br> 
 * An object implementing this interface is obtained from the connection object
 * by calling the <code>getMetaData()</code> method, for example:
 * <br>
 * <pre>
 *  XQMetaData metaData = connection.getMetaData();
 *  String productVersion = metaData.getProductVersion();
    ...
 * </pre>
 * Since the metadata object depends on the connection, all its methods would 
 * raise an exception if the connection it is created from is no longer valid.
 * 
 * @see XQConnection
 */
public interface XQMetaData
{
   // (1) Product identification
   /**
    * Gets the major version of this product.
    *
    * @return                    a integer indicating the major version of this product
    * @exception XQException     if the connection is no longer valid
    */
   public int getProductMajorVersion() throws XQException;

   /**
    * Gets the minor version of this product.
    *
    * @return                    a integer indicating the minor version of this product
    * @exception XQException     if the connection is no longer valid
    */
   public int getProductMinorVersion() throws XQException;

   /**
    * Gets the name of this product.
    * The value of string returned by this method is implementation-defined.
    *
    * @return                    a string indicating the product name
    * @exception XQException     if the connection is no longer valid
    */
   public String getProductName() throws XQException;

   /**
    * Gets the full version of this product.
    * The format and value of the string returned by this method is 
    * implementation-defined.
    *
    * @return                    a string indicating the product version
    * @exception XQException     if the connection is no longer valid
    */
   public String getProductVersion() throws XQException; 

   // (2) XQJ Specification identification
   /**
    * Gets the major version number of XQJ specification supported by 
    * this implementation. 
    *
    * @return                    an integer indicating the XQJ major version
    * @exception XQException     if the connection is no longer valid
    */
   public int getXQJMajorVersion() throws XQException;

   /**
    * Gets the minor version number of XQJ specification supported by 
    * this implementation. 
    *
    * @return                    an integer indicating the XQJ minor version
    * @exception XQException     if the connection is no longer valid
    */
   public int getXQJMinorVersion() throws XQException;

   /**
    * Gets the full version of XQJ specification supported by this implementation.
    *
    * @return                    a string indicating the version of XQJ specification
    * @exception XQException     if the connection is no longer valid
    */
   public String getXQJVersion() throws XQException;

   // (3) Connection Information
   /**
    * Query if the associated conection is restricted for read only use.
    *
    * @return                    <code>true</code> if the associated connection is
    *                            for read-only; <code>false</code> otherwise
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isReadOnly() throws XQException;

   // (5) Supported features (Capability)
   /**
    * Query if XQueryX format is supported in this data source.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isXQueryXSupported() throws XQException;

   /**
    * Query if transaction is supported in this data source.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isTransactionSupported() throws XQException;

   /**
    * Query if XQuery static typing feature is supported in this data source.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isStaticTypingFeatureSupported() throws XQException;

   /**
    * Query if XQuery schema import feature is supported in this connection.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isSchemaImportFeatureSupported() throws XQException;

   /**
    * Query if XQuery schema validation feature is supported in this connection.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isSchemaValidationFeatureSupported() throws XQException;

   /**
    * Query if XQuery full axis feature is supported in this connection.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isFullAxisFeatureSupported() throws XQException;

   /**
    * Query if XQuery module feature is supported in this connection.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isModuleFeatureSupported() throws XQException;

   /**
    * Query if XQuery serialization feature is supported in this connection.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isSerializationFeatureSupported() throws XQException;

   /**
    * Query if XQuery static typing extensions are supported in
    * this connection.
    *
    * @return                    <code>true</code> if so; otherwise <code>false</code>
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isStaticTypingExtensionsSupported() throws XQException;

   // (9) User Information
   /**
    * Gets the user name associated with this connection.
    *
    * @return                    the user's name
    * @exception XQException     if the connection is no longer valid
    */
   public String getUserName() throws XQException;

   // (10) Product limits
   // Define few, minimal number of methods;add more when requirements 
   // becomes clear

   /**
    * Gets the maximum number of characters allowed in an expression in this 
    * data source.
    *
    * @return                    the maximum length of expression as an integer.
    *                            A zero value means that there is no limit or the
    *                            limit is unknown 
    * @exception XQException     if the connection is no longer valid
    */
   public int getMaxExpressionLength() throws XQException;

   /**
    * Gets the maximum number of characters allowed in a user name.
    *
    * @return                    the maximum length of user name as an integer.
    *                            A zero value means that there is no limit or the
    *                            limit is unknown 
    * @exception XQException     if the connection is no longer valid
    */
   public int getMaxUserNameLength() throws XQException;
   
   /**
    * Query if this connection was created from a JDBC connection.
    *
    * @return                    <code>true</code>, if this connection was created
    *                            from a JDBC connection, <code>false</code> otherwise. 
    * @exception XQException     if the connection is no longer valid
    */
   public boolean wasCreatedFromJDBCConnection() throws XQException;

   /**
    * Query if the XQuery encoding declaration is supported by the XQJ implementation.
    * 
    * @return                    <code>true</code> if the XQuery encoding declaration
    *                            is supported; <code>false</code> otherwise
    * 
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isXQueryEncodingDeclSupported() throws XQException;

   /**
    * Returns a set of <code>java.lang.String</code>, each of which
    * specifies a character encoding method the XQJ implmentation supports to
    * parse the XQuery query text. <br>
    *
    * For an example, for an XQJ impmentation which is able to parse the
    * XQuery encoded in "UTF-8" or "UTF-16", it returns a
    * <code>java.util.Set</code> of  "UTF-8" and "UTF-16".
    *
    * If the implemetation is not able to generate a list of encodings supported,
    * an empty set is returned. If a non-empty set is returned, the encodings
    * returned in this set are guaranteed to be supported. Note that encodings not in the
    * returned set might also be supported. For example, if the set has two
    * encoding methods: 'UTF-8' and 'UTF-16', they are supported by the implementation.
    * However, this does not mean 'Shift-Js' is not supported. It might be supported.
    *
    * @return                    a <code>java.util.Set</code> of 
    *                            <code>java.lang.String</code>, each of which
    *                            is an XQuery query text encoding method
    *
    * @exception XQException     if the connection is no longer valid
    */
   public java.util.Set getSupportedXQueryEncodings() throws XQException;

   /**
    * Query if a character encoding method of the XQuery query text
    * is supported by the XQJ implmentation.
    *
    * @param encoding            <code>String</code> representing the character
    *                            encoding method of the XQuery query text.
    * @return                    <code>true</code> if an XQuery query character encoding
    *                            method is supported, <code>false</code> otherwise
    *
    * @exception XQException     if (1) the connection is no longer valid,
    *                            or (2) the specified <code>encoding</code> 
    *                            parameter is <code>null</code>
    */
   public boolean isXQueryEncodingSupported(String encoding) throws XQException;

  /**
    * Check if the user defined XML schema type is supported in this connection.
    * If this method returns <code>true</code>, then 
    * <code>XQItemAccessor.instanceOf(XQItemType)</code> must
    * be able to determine if the type of an <code>XQItemAccessor</code> is an
    * instance of the <code>XQItemType</code> even if either of them is a user
    * defined XML schema type which is defined by the non-predefined XML schema.
    * The pre-defined XML Schema refers to the XML schema whose schema URL is
    * <code>"http://www.w3.org/2001/XMLSchema"</code>
    *
    * @return                    <code>true</code> if the user defined XML schema
    *                            type is supported in this connection, <code>false</code>
    *                            otherwise.
    * @exception XQException     if the connection is no longer valid
    */
   public boolean isUserDefinedXMLSchemaTypeSupported() throws XQException;
}
