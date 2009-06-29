/*
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * This class represents the list of variables and their values
 * in an error stack.
 *
 * @see XQStackTraceElement <code>XQStackTraceElement</code>
 * @see XQQueryException <code>XQQueryException</code>
 */
public class XQStackTraceVariable implements Serializable {
  private QName qname;
  private String value;

  /** 
   * Construct a stack trace variable object. 
   *
   * @param qname          the <code>QName of the variable in the error stack
   * @param value          a vendor specific short string representation
   *                       of the value of the variable in the error stack 
   */ 
  public XQStackTraceVariable(QName qname, String value) {
    this.qname = qname;
    this.value = value;
  }

 /**
  * Gets the <code>QName</code> of the variable.
  *
  * @return                the <code>QName</code> of the variable in the stack
  */
  public QName getQName() { return qname; }

 /**
  * Gets a short string representation of the value of the 
  * stack variable.  Representations of values are vendor specific
  * and for XML node types may be XPath descriptions such as
  * "doc("0596003870/book1.xml")/book/chapter[5]". Sequences may
  * print just some set of values from the sequence such as
  * '("5", "6", "7", ...)'.
  *
  * @return                the <code>String</code> representation of the
  *                        value of the variable. This representation is
  *                        vendor specific.
  */
  public String getValue() { return value; }
}
