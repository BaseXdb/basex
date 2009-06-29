/*
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */

package javax.xml.xquery;

import java.io.Serializable;
import javax.xml.namespace.QName;

/**
 * This class represents a frame in a stack trace, akin to the 
 * <code>java.lang.StackTraceElement</code> but for XQuery callstacks 
 * instead of Java.
 * 
 * @see XQQueryException#getQueryStackTrace <code>XQQueryException.getQueryStackTrace</code>
 */
public class XQStackTraceElement implements Serializable {

  private String module;
  private int line = -1;
  private int column = -1;
  private int position = -1;
  private QName function;
  private XQStackTraceVariable[] variables;

 /**
  * Construct an <code>XQStackTraceElement</code> object representing 
  * a frame in a stack trace. 
  * 
  * @param moduleURI          the module URI containing the execution point representing
  *                           the stack trace element. <code>null</code> when it is the main
  *                           module or when the module is unknown
  * @param line               the line number in the query string where the error occured.
  *                           Line numbering starts at <code>1</code>. <code>-1</code> if unknown
  * @param column             the column number in the query string where the error occured.
  *                           Column numbering starts at <code>1</code>. <code>-1</code> if unknown
  * @param position           the position in the query string where the error occured. This
  *                           is a <code>0</code> based position. <code>-1<code> if unknown
  * @param function           the <code>QName</code> of the function in which the exception occurred, 
  *                           or <code>null</code> if it occurred outside an enclosing function
  * @param variables          the variables in scope at this execution point,
  *                           or <code>null</code> if no variable value retrieval is possible
  */
  public XQStackTraceElement(String moduleURI, int line, int column, int position, 
                     QName function, XQStackTraceVariable[] variables) {
    this.module = moduleURI;
    this.line = line;
    this.column = column;
    this.position = position;
    this.function = function;
    this.variables = variables;
  }

 /**
  * Gets the module URI containing the execution point represented by this
  * stack trace element.
  * <code>null</code> when it is the main module or when the module is
  * unknown.
  * 
  * @return                   the module URI containing the excution point
  *                           represented by the stack trace element or <code>null</code>
  */
  public String getModuleURI() {
    return module;
  }

  /**
   * Gets the character position in the query string containing the execution
   * point represented by this stack trace element.
   * <p>
   * This is a <code>0</code> based position. <code>-1</code> if unknown.</li>
   *
   * @return                the character position in the query string containing the
   *                        execution point represented by the stack trace element
   */
   public int getPosition() {
     return position;
   }

   /**
    * Gets the line number in the query string containing the execution
   * point represented by this stack trace element.
    * <p>
    * Line numbering starts at <code>1</code>. <code>-1</code> is returned
    * if the line number is unknown. If the implementation does not support this method,
    * it must return <code>-1</code>
    *
    * @return                the line number in the query string containing the
    *                        execution point represented by the stack trace element
    */
    public int getLineNumber() {
      return line;
    }

    /**
     * Gets the column number in the query string containing the execution
    * point represented by this stack trace element.
     * <p>
     * Column numbering starts at <code>1</code>. <code>-1</code> is returned
     * if the column number is unknown. If the implementation does not support this method,
     * it must return <code>-1</code>
     *
     * @return                the column number in the query string containing the
     *                        execution point represented by the stack trace element
     */
    public int getColumnNumber() {
      return column;
    }

   
 /**
  * Gets the <code>QName</code> of the function in which the error occurred,
  * or <code>null</code> if it occurred outside an enclosing function (in a main module).
  *
  * @return                   the <code>QName</code> of the function in which the error
  *                           occurred for this stack element or <code>null</code> if it
  *                           occurred outside an enclosing function
  */ 
  public QName getFunctionQName() {
    return function;
  }

 /**
  * Gets the variables in scope at this execution point, or <code>null</code> if no
  * variable value retrieval is possible.
  *
  * @return                   the variables in scope at this execution point, or
  *                           <code>null</code> if no variable value retrieval is
  *                           possible.
  */
  public XQStackTraceVariable[] getVariables() {
    return variables;
  }
}
