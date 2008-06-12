package org.basex.query.xquery.util;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Type;

/**
 * This class is supposed to support a consistent error output.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Err {
  /** Private constructor. */
  private Err() { }

  /**
   * Throws an exception.
   * @param err error message
   * @param x extended info
   * @throws XQException evaluation exception
   */
  public static void or(final Object[] err, final Object... x)
      throws XQException {
    throw new XQException(err, x);
  }

  /**
   * Throws a type exception.
   * @param inf expression info
   * @param t expected type
   * @param it item
   * @throws XQException evaluation exception
   */
  public static void type(final String inf, final Type t, final Item it)
      throws XQException {
    or(XPTYPE, inf, t, it.type);
  }
  
  /**
   * Throws a date format exception.
   * @param t expected type
   * @param ex example format
   * @throws XQException evaluation exception
   */
  public static void date(final Type t, final String ex) throws XQException {
    or(DATEFORMAT, t, ex);
  }
  
  /**
   * Throws an invalid value exception.
   * @param t expected type
   * @param v value
   * @throws XQException evaluation exception
   */
  public static void value(final Type t, final Object v) throws XQException {
    or(INVALUE, t, v);
  }
  
  /**
   * Throws a date range exception.
   * @param t expected type
   * @param v value
   * @throws XQException evaluation exception
   */
  public static void range(final Type t, final byte[] v) throws XQException {
    or(DATERANGE, t, v);
  }
  
  /**
   * Throws a empty sequence exception.
   * @param ex expression
   * @throws XQException evaluation exception
   */
  public static void empty(final Expr ex) throws XQException {
    or(XPEMPTY, ex.info());
  }

  /**
   * Throws a comparison exception.
   * @param it1 first item
   * @param it2 second item
   * @throws XQException evaluation exception
   */
  public static void cmp(final Item it1, final Item it2) throws XQException {
    if(it1 == it2) or(TYPECMP, it1.type);
    else or(XPTYPECMP, it1.type, it2.type);
  }
  
  /**
   * Throws a numeric type exception.
   * @param inf expression info
   * @param it item
   * @throws XQException evaluation exception
   */
  public static void num(final String inf, final Item it) throws XQException {
    or(XPTYPENUM, inf, it.type);
  }
  
  /**
   * Throws a node exception.
   * @param ex expression
   * @throws XQException evaluation exception
   */
  public static void nodes(final Expr ex) throws XQException {
    or(EVALNODES, ex);
  }
  
  /**
   * Throws a numeric type exception.
   * @param t expression cast type
   * @param it item
   * @throws XQException evaluation exception
   */
  public static void cast(final Type t, final Item it) throws XQException {
    String str = it.toString();
    if(str.length() > 30) str = str.substring(0, 30) + "...";
    or(XPINVCAST, it.type, t, str);
  }
}
