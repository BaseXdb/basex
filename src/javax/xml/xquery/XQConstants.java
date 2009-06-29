/*
 * Copyright © 2003, 2004, 2005, 2006, 2007, 2008 Oracle.  All rights reserved.
 */
package javax.xml.xquery;

import javax.xml.namespace.QName;

/** 
 * <code>XQConstants</code> class provides constants that can be 
 * used in the XQJ API.
 */
public final class XQConstants
{
  private XQConstants() {}

 // XQResultHoldability - one of the following constants:
 /** The constant indicating that the result sequences must be 
   * closed when the commit on the connection is called. 
   */
 public static final int HOLDTYPE_CLOSE_CURSORS_AT_COMMIT = 2;

 /** The constant indicating that the result sequences must be 
   * preserved when the commit on the connection is called. 
   */
 public static final int HOLDTYPE_HOLD_CURSORS_OVER_COMMIT = 1;

 // Sequence scrollability one of the following constants:
 /** The constant indicating that the result sequence can only be scrolled
   * forward. 
   */ 
 public static final int SCROLLTYPE_FORWARD_ONLY = 1;

 /** The constant indicating that the result sequence can be scrolled
   * forward or backward and is insensitive to any updates done on the 
   * underlying objects 
   */
 public static final int SCROLLTYPE_SCROLLABLE = 2;
  
 /** The constant indicating that the expression language used in 
   * <code>XQConnection.prepareExpression</code> and 
   * <code>XQExpression.execute</code> is XQuery (any version). 
   */ 
 public static final int LANGTYPE_XQUERY  = 1;

 /** The constant indicating that the expression language used in 
   * <code>XQConnection.prepareExpression</code> and 
   * <code>XQExpression.execute</code> is XQueryX.
   */ 
 public static final int LANGTYPE_XQUERYX = 2;

 /** The constant indicating the the boundary-space policy for expression 
   * evaluation is to preserve white spaces */
 public static final int BOUNDARY_SPACE_PRESERVE = 1;

 /** The constant indicating the the boundary-space policy for expression 
   * evaluation is to strip white spaces */
 public static final int BOUNDARY_SPACE_STRIP = 2;

 /** The constant indicating that the type of a constructed element node
   * is <code>xs:anyType</code>, and all attribute and element nodes copied during
   * node construction retain their original types. */
 public static final int CONSTRUCTION_MODE_PRESERVE = 1;

 /** The constant indicating that the type of a constructed element
   * node is <code>xs:untyped</code>; all element nodes copied during node construction
   * receive the type <code>xs:untyped</code>, and all attribute nodes copied during node
   * construction receive the type <code>xs:untypedAtomic</code>. */
  public static final int CONSTRUCTION_MODE_STRIP = 2;

 /** The constant indicating that ordered results are to be returned
  *  by certain path expressions, union, intersect, and except expressions,
  *  and FLWOR expressions that have no order by clause.  */
  public static final int ORDERING_MODE_ORDERED = 1;

 /** The constant indicating that unordered results are to be returned
  *  by certain path expressions, union, intersect, and except expressions,
  *  and FLWOR expressions that have no order by clause.  */
  public static final int ORDERING_MODE_UNORDERED = 2;

 /** The constant indicating that ordering of empty sequences and NaN
  *  values as keys in an order by clause in a FLWOR expression is "greatest".
  *  See <a href="http://www.w3.org/TR/xquery/#id-orderby-return">
  *  <i>3.8.3 Order By and Return Clauses, XQuery 1.0: An XML Query
  *  Language</i></a> for details. */
  public static final int DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_GREATEST = 1;

 /** The constant indicating that ordering of empty sequences and NaN
  *  values as keys in an order by clause in a FLWOR expression is "least".
  *  See <a href="http://www.w3.org/TR/xquery/#id-orderby-return">
  *  <i>3.8.3 Order By and Return Clauses, XQuery 1.0: An XML Query
  *  Language</i></a> for details. */
  public static final int DEFAULT_ORDER_FOR_EMPTY_SEQUENCES_LEAST = 2;

 /** The constant indicating that the preserve mode should be used in
  *  namespace binding assignement when an existing element node is
  *  copied by an element constructor, as described in
  *  <a href="http://www.w3.org/TR/xquery/#id-element-constructor">
  *  <i>3.7.1 Direct Element Constructors, XQuery 1.0: An XML Query
  *  Language</i></a> */
  public static final int COPY_NAMESPACES_MODE_PRESERVE = 1 ;

 /** The constant indicating that the no-preserve mode should be used in
  *  namespace binding assignement when an existing element node is
  *  copied by an element constructor, as described in
  *  <a href="http://www.w3.org/TR/xquery/#id-element-constructor">
  *  <i>3.7.1 Direct Element Constructors, XQuery 1.0: An XML Query
  *  Language</i></a> */
  public static final int COPY_NAMESPACES_MODE_NO_PRESERVE = 2;

 /** The constant indicating that the inherit mode should be used in
  *  namespace binding assignement when an existing element node is
  *  copied by an element constructor, as described in
  *  <a href="http://www.w3.org/TR/xquery/#id-element-constructor">
  *  <i>3.7.1 Direct Element Constructors, XQuery 1.0: An XML Query
  *  Language</i></a> */
  public static final int COPY_NAMESPACES_MODE_INHERIT = 1;

 /** The constant indicating that the no-inherit mode should be used in
  *  namespace binding assignement when an existing element node is
  *  copied by an element constructor, as described in
  *  <a href="http://www.w3.org/TR/xquery/#id-element-constructor">
  *  <i>3.7.1 Direct Element Constructors, XQuery 1.0: An XML Query
  *  Language</i></a> */
  public static final int COPY_NAMESPACES_MODE_NO_INHERIT = 2;

 /**
  *  Defines the <code>QName</code> for the context item. This is
  *  used to bind values to the context item via the bind methods
  *  of <code>XQDynamicContext</code>. 
  */
  public static final QName CONTEXT_ITEM = new QName("http://xqj.jcp.org", "context-item", "xqj");

 /**
  * The constant indicating the binding mode immediate, refer to
  * <code>XQDynamicContext</code> for more information.
  */
  public static final int BINDING_MODE_IMMEDIATE = 0;

 /**
  * The constant indicating the binding mode deferred, refer to
  *  <code>XQDynamicContext</code> for more information.
  */
 public static final int BINDING_MODE_DEFERRED = 1;

};
