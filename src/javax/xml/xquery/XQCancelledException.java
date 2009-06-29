/*
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import javax.xml.namespace.QName;

/**
 * <code>XQCancelledException</code> is an exception to indicate that the
 * current XQuery processing is cancelled by the application through a
 * <code>cancel()</code> request. This exception allows an application to
 * easily differentiate between a user's cancellation of the query from a
 * general execution failure.
 */
public class XQCancelledException extends XQQueryException {

 /**
   * Constructs an <code>XQCancelledException</code> object with a given message,
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
   * @param errorObject    an <code>XQSequence</code> representing the error object passed to
   *                       <code>fn:error()</code>. <code>null</code> if this error was not
   *                       triggered by <code>fn:error()</code> or when the error object is
   *                       not available
   * @param stackTrace     the XQuery stack trace where the error occurred. <code>null</code>
   *                       if not available
   */
  public XQCancelledException(String message, String vendorCode, QName errorCode,
                          int line, int column, int position,
                          String moduleURI, XQSequence errorObject, XQStackTraceElement[] stackTrace)
  {
    super(message, vendorCode, errorCode, line, column, position, moduleURI, errorObject, stackTrace);
  }
}
