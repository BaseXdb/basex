/*
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import javax.xml.namespace.QName;
import java.lang.String;

/**
 * An exception that provides information on errors occurring during the
 * evaluation of an xquery. 
 * <br>
 * <br>
 * Each <code>XQQueryException</code> provides several kinds of optional
 * information, in addition to the properties inherited from
 * <code>XQException</code>:
 * <ul>
 * <li>an error code. This <code>QName</code> identifies the error
 * according to the standard as described in
 * <a href="http://www.w3.org/TR/xquery/#id-errors"><i>Appendix F, XQuery 1.0:
 * An XML Query language</i></a>, <a href="http://www.w3.org/TR/xpath-functions/#error-summary"><i>
 * Appendix C, XQuery 1.0 and XPath 2.0 Functions and Operators</i></a>, and
 * and its associated specifications; implementation-defined errors may be raised.</li>
 * <li>a position. This identifies the character position of the failing expression in
 * the query text. This is a <code>0</code> based position. <code>-1</code> if unknown.</li>
 * <li>the line number in the query string where the error occured. Line numbering
 * starts at <code>1</code>. <code>-1</code> if unknown</li>
 * <li>the column number in the query string where the error occured. Column numbering
 * starts at <code>1</code>. <code>-1</code> if unknown</li>
 * <li>the module uri. This identifies the module in which the error
 * occurred, <code>null</code> when the error is located in the main module.</li>
 * <li>the XQuery error object of this exception. This is the
 * <code>$error-object</code> argument specified through the <code>fn:error()</code>
 * function. May be <code>null</code> if not specified. </li>
 * <li>the XQuery stack trace. This provides additional dynamic
 * information where the exception occurred in the XQuery expression.</li>
 */

public class XQQueryException extends XQException {
  private QName errorCode;
  private XQSequence errorObject;
  private XQStackTraceElement[] stackTrace;
  private int line = -1;
  private int column = -1;
  private int position = -1;
  private String moduleURI;

  /**
   * Constructs an <code>XQQueryException</code> object with a given message.
   *
   * @param message        the description of the error. <code>null</code> indicates
   *                       that the message string is non existant
   */
  public XQQueryException(String message)
  {
    super(message);
  }

  /**
   * Constructs an <code>XQQueryException</code> object with a given message,
   * and error code.
   *
   * @param message        the description of the error. <code>null</code> indicates
   *                       that the message string is non existant
   * @param errorCode      <code>QName</code> which identifies the error
   *                       according to the standard as described in
   *                       <a href="http://www.w3.org/TR/xquery/#id-errors">
   *                       <i>Appendix F, XQuery 1.0: An XML Query language</i></a>,
   *                       <a href="http://www.w3.org/TR/xpath-functions/#error-summary">
   *                       <i>Appendix C, XQuery 1.0 and XPath 2.0 Functions and Operators</i></a>,
   *                       and its associated specifications; implementation-defined 
   *                       errors may be raised.</li>
   */
  public XQQueryException(String message, QName errorCode)
  {
    super(message);
    this.errorCode = errorCode;
  }

  /**
   * Constructs an <code>XQQueryException</code> object with a given message,
   * error code, line number, column number, and position.
   *
   * @param message        the description of the error. <code>null</code> indicates
   *                       that the message string is non existant
   * @param errorCode      <code>QName</code> which identifies the error
   *                       according to the standard as described in
   *                       <a href="http://www.w3.org/TR/xquery/#id-errors">
   *                       <i>Appendix F, XQuery 1.0: An XML Query language</i></a>,
   *                       <a href="http://www.w3.org/TR/xpath-functions/#error-summary">
   *                       <i>Appendix C, XQuery 1.0 and XPath 2.0 Functions and Operators</i></a>,
   *                       and its associated specifications; implementation-defined 
   *                       errors may be raised</li>
   * @param line           the line number in the query string where the error occured.
   *                       Line numbering starts at <code>1</code>. <code>-1</code> if unknown
   * @param column         the column number in the query string where the error occured.
   *                       Column numbering starts at <code>1</code>. <code>-1</code> if unknown
   * @param position       the position in the query string where the error occured. This
   *                       is a <code>0</code> based position. <code>-1</code> if unknown
   */
  public XQQueryException(String message, QName errorCode,
                          int line, int column, int position) 
  {
    super(message);
    this.errorCode = errorCode;
    this.line = line;
    this.column = column;
    this.position = position;
  }

  /**
   * Constructs an <code>XQQueryException</code> object with a given message,
   * vendor code, error code, line number, column number, and position.
   *
   * @param message        the description of the error. <code>null</code> indicates
   *                       that the message string is non existant
   * @param vendorCode     a vendor-specific string identifying the error.
   *                       <code>null</code> indicates there is no vendor
   *                       code or it is unknown
   * @param errorCode      <code>QName</code> which identifies the error
   *                       according to the standard as described in
   *                       <a href="http://www.w3.org/TR/xquery/#id-errors">
   *                       <i>Appendix F, XQuery 1.0: An XML Query language</i></a>,
   *                       <a href="http://www.w3.org/TR/xpath-functions/#error-summary">
   *                       <i>Appendix C, XQuery 1.0 and XPath 2.0 Functions and Operators</i></a>,
   *                       and its associated specifications; implementation-defined 
   *                       errors may be raised</li>
   * @param line           the line number in the query string where the error occured.
   *                       Line numbering starts at <code>1</code>. <code>-1</code> if unknown
   * @param column         the column number in the query string where the error occured.
   *                       Column numbering starts at <code>1</code>. <code>-1</code> if unknown
   * @param position       the position in the query string where the error occured. This
   *                       is a <code>0</code> based position. <code>-1</code> if unknown
   */
  public XQQueryException(String message, String vendorCode, QName errorCode,
                          int line, int column, int position) 
  {
    super(message, vendorCode);
    this.errorCode = errorCode;
    this.line = line;
    this.column = column;
    this.position = position;
  }

  /**
   * Constructs an <code>XQQueryException</code> object with a given message,
   * vendor code, error code, line number, column number, position, module URI,
   * error object, and stack trace.
   *
   * @param message        the description of the error. <code>null</code> indicates
   *                       that the message string is non existant
   * @param vendorCode     a vendor-specific string identifying the error.
   *                       <code>null</code> indicates there is no vendor
   *                       code or it is unknown
   * @param errorCode      <code>QName</code> which identifies the error
   *                       according to the standard as described in
   *                       <a href="http://www.w3.org/TR/xquery/#id-errors">
   *                       <i>Appendix F, XQuery 1.0: An XML Query language</i></a>,
   *                       <a href="http://www.w3.org/TR/xpath-functions/#error-summary">
   *                       <i>Appendix C, XQuery 1.0 and XPath 2.0 Functions and Operators</i></a>,
   *                       and its associated specifications; implementation-defined
   *                       errors may be raised</li>
   * @param line           the line number in the query string where the error occured.
   *                       Line numbering starts at <code>1</code>. <code>-1</code> if unknown
   * @param column         the column number in the query string where the error occured.
   *                       Column numbering starts at <code>1</code>. <code>-1</code> if unknown
   * @param position       the position in the query string where the error occured. This
   *                       is a <code>0</code> based position. <code>-1</code> if unknown
   * @param moduleURI      the module URI of the module in which the error occurred.
   *                       <code>null</code> when it is the main module or when the module is
   *                       unknown
   * @param errorObject   an <code>XQSequence</code> representing the error object passed to
   *                       <code>fn:error()</code>. <code>null</code> if this error was not
   *                       triggered by <code>fn:error()</code> or when the error object is
   *                       not available. 
   * @param stackTrace     the XQuery stack trace where the error occurred. <code>null</code>
   *                       if not available
   */
  public XQQueryException(String message, String vendorCode, QName errorCode, 
                          int line, int column, int position,
                          String moduleURI, XQSequence errorObject, XQStackTraceElement[] stackTrace) 
  {
    super(message, vendorCode);
    this.errorCode = errorCode;
    this.line = line;
    this.column = column;
    this.position = position;
    this.moduleURI = moduleURI;
    this.errorObject = errorObject;
    this.stackTrace = stackTrace;
  }

 /**
  * Gets the code identifying the error according to the standard as
  * described in <a href="http://www.w3.org/TR/xquery/#id-errors"><i>Appendix F, XQuery 1.0:
  * An XML Query language</i></a>, <a href="http://www.w3.org/TR/xpath-functions/#error-summary"><i>
  * Appendix C, XQuery 1.0 and XPath 2.0 Functions and Operators</i></a>, and
  * its associated specifications; imlementation-defined errors may also be raised;
  * finally the error code may also be specified in the query using <code>fn:error()</code>.
  * 
  * @return                the code identifying the error, or <code>null</code>
  *                        if not available 
  */
  public QName getErrorCode() {
    return errorCode;
  }

 /**
  * Gets an <code>XQSequence</code> representing the error object passed to
  * <code>fn:error()</code>. Returns <code>null</code> if this error was not triggered by
  * <code>fn:error()</code> or when the error object is not available. 
  *
  * @return                the sequence passed to <code>fn:error()</code>,
  *                        <code>null</code> if not available
  *                        
  */
  public XQSequence getErrorObject() {
    return errorObject;
  }


 /**
  * Gets the character position in the query string where this exception
  * occurred.
  * <p>
  * This is a <code>0</code> based position. <code>-1</code> if unknown.</li>
  *
  * @return                the character position in the query string where the 
  *                        exception occurred
  */
  public int getPosition() {
    return position;
  }


 /**
  * Returns the query stack stackTrace when the exception occurred, or null if
  * none. On some implementations only the top frame may be visible.
  * 
  * @return                the stackTrace where the exception occurred
  */
  public XQStackTraceElement[] getQueryStackTrace() {
    return stackTrace;
  }

 /**
  * Gets the module URI of the module in which the error occurred.
  * <code>null</code> when it is the main module or when the module is
  * unknown.
  *
  * @return                the module URI or <code>null</code>
  */
  public String getModuleURI() {
    return moduleURI;
  }

 /**
  * Gets the line number in the query string where the error occurred.
  * <p>
  * Line numbering starts at <code>1</code>. <code>-1</code> is returned
  * if the line number is unknown. If the implementation does not support this method,
  * it must return <code>-1</code>
  *
  * @return                the line number in the query string where
  *                        the error occurred
  */
  public int getLineNumber() {
    return line;
  }

 /**
  * Gets the column number in the query string where the error occurred.
  * <p>
  * Column numbering starts at <code>1</code>. <code>-1</code> is returned
  * if the column number is unknown. If the implementation does not support this method,
  * it must return <code>-1</code>
  *  
  * @return                the column number in the query string where
  *                        the error occurred
  */
  public int getColumnNumber() {
    return column;
  }
}
