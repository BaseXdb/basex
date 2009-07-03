/*
 * Copyright � 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;
import java.lang.Exception;

/**
 * An exception that provides information on XQJ, XQuery or other errors
 * reported by an XQJ implementation.
 * <br>
 * <br>
 * Each <code>XQException</code> provides several kinds of information:
 * <ul>
 * <li>a string describing the error. This is used as the Java
 * Exception message, available via the method <code>getMessage</code>.</li>
 * <li>the cause of the error. This is used as the Java Exception
 * cause, available via the method <code>getCause</code>.</li>
 * <li>the vendor code identifying the error. Available via the
 * method <code>getVendorCode</code>. Refer to the vendor documentation
 * which specific codes can be returned.</li>
 * <li>a chain of <code>XQException</code> objects. If more than one
 * error occurred the exceptions are referenced via this chain.</li>
 * </ul>
 * <br>
 * <br>
 * Note that <code>XQException</code> has a subclass
 * {@link XQQueryException XQQueryException} providing more detailed
 * information about errors that occurred during the processing of a query.
 * An implementation throws a base <code>XQException</code> when an error
 * occurs in the XQJ implementation. Further, implementations are encouraged to
 * use the more detailed <code>XQQueryException</code> in case of an
 * error reported by the XQuery engine.
 * <br>
 * <br>
 * It is possible that during the processing of a query that one or more
 * errors could occur, each with their own potential causal relationship.
 * This means that when an XQJ application catches an
 * <code>XQException</code>, there is a possibility that there may be
 * additional <code>XQException</code> objects chained to the original
 * thrown <code>XQException</code>. To access the additional chained
 * <code>XQException</code> objects, an application would recursively
 * invoke <code>getNextException</code> until a <code>null</code> value is
 * returned.
 * <br>
 * <br>
 * An <code>XQException</code> may have a causal relationship, which
 * consists of one or more <code>Throwable</code> instances which caused
 * the <code>XQException</code> to be thrown. The application may
 * recursively call the method <code>getCause</code>, until a <code>null</code>
 * value is returned, to navigate the chain of causes.
 */
public  class XQException extends Exception
{
  /** Vendor code. */
  private String vendorCode;

  /** Query exception. */
  XQException nextException;

  /**
   * Constructs an <code>XQException</code> object with a given message.
   * An optional chain of additional <code>XQException</code> objects may be set
   * subsequently using <code>setNextException</code>.
   *
   * @param message     the description of the error. <code>null</code> indicates
   *                    that the message string is non existant
   */
  public XQException(String message)
  {
     super(message);
  }

  /**
   * Constructs an <code>XQException</code> object with a given message
   * and vendor code. An optional chain of additional
   * <code>XQException</code> objects may be set subsequently using
   * <code>setNextException</code>.
   *
   * @param message     the description of the error. <code>null</code>
   *                    indicates that the message string is non existant
   * @param vendorCode  a vendor-specific string identifying the error.
   *                    <code>null</code> indicates there is no vendor
   *                    code or it is unknown
   */
  public XQException(String message, String vendorCode)
  {
     super(message);
     this.vendorCode = vendorCode;
  }

  /**
   * Gets the vendor code associated with this exception or <code>null</code>
   * if none. A vendor code is a vendor-specific string identifying the failure in
   * a computer-comparable manner. For example, "NOCONNECT" if unable to
   * connect or "DIVBYZERO" if division by zero occurred within the XQuery.
   *
   * @return     the vendor code string, or <code>null</code> if none available
   */
  public String getVendorCode() {
     return vendorCode;
  }

 /**
  * Returns the next <code>XQException</code> in the chain or
  * <code>null</code> if none.
  *
  * @return     the next exception, or <code>null</code> if none
  */
  public XQException getNextException() {
    return nextException;
  }

 /**
  * Adds an <code>XQException</code> to the chain of exceptions.
  *
  * @param next     the next exception to be added to the chain
  *                 of exceptions
  */
  public void setNextException(XQException next) {
    nextException = next;
  }
}

