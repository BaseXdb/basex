package org.basex.query.util;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.util.Token;

/**
 * This class is supposed to support a consistent error output.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Err {
  /** Private constructor. */
  private Err() { }

  /**
   * Throws an exception.
   * @param err error message
   * @param x extended info
   * @throws QueryException evaluation exception
   */
  public static void or(final Object[] err, final Object... x)
      throws QueryException {
    throw new QueryException(err, x);
  }

  /**
   * Throws a type exception.
   * @param inf expression info
   * @param t expected type
   * @param it item
   * @throws QueryException evaluation exception
   */
  public static void type(final String inf, final Type t, final Item it)
      throws QueryException {
    or(XPTYPE, inf, t, it.type);
  }
  
  /**
   * Throws a date format exception.
   * @param t expected type
   * @param ex example format
   * @throws QueryException evaluation exception
   */
  public static void date(final Type t, final String ex) throws QueryException {
    or(DATEFORMAT, t, ex);
  }
  
  /**
   * Throws an invalid value exception.
   * @param t expected type
   * @param v value
   * @throws QueryException evaluation exception
   */
  public static void value(final Type t, final Object v) throws QueryException {
    or(INVALUE, t, v);
  }
  
  /**
   * Throws a date range exception.
   * @param t expected type
   * @param v value
   * @throws QueryException evaluation exception
   */
  public static void range(final Type t, final byte[] v) throws QueryException {
    or(DATERANGE, t, v);
  }
  
  /**
   * Throws a empty sequence exception.
   * @param e calling expression
   * @throws QueryException evaluation exception
   */
  public static void empty(final Expr e) throws QueryException {
    or(XPEMPTY, e.info());
  }

  /**
   * Throws a comparison exception.
   * @param it1 first item
   * @param it2 second item
   * @throws QueryException evaluation exception
   */
  public static void cmp(final Item it1, final Item it2) throws QueryException {
    if(it1 == it2) or(TYPECMP, it1.type);
    else or(XPTYPECMP, it1.type, it2.type);
  }
  
  /**
   * Throws a numeric type exception.
   * @param inf expression info
   * @param it item
   * @throws QueryException evaluation exception
   */
  public static void num(final String inf, final Item it)
      throws QueryException {
    or(XPTYPENUM, inf, it.type);
  }
  
  /**
   * Throws a node exception.
   * @param ex expression
   * @throws QueryException evaluation exception
   */
  public static void nodes(final Expr ex) throws QueryException {
    or(EVALNODES, ex);
  }
  
  /**
   * Throws a numeric type exception.
   * @param t expression cast type
   * @param it item
   * @throws QueryException evaluation exception
   */
  public static void cast(final Type t, final Item it) throws QueryException {
    or(XPINVCAST, it.type, t, chop(it));
  }
  
  /**
   * Chops the specified input and returns the string.
   * @param val input value
   * @return chopped string
   */
  public static String chop(final Object val) {
    final String str = val instanceof byte[] ? Token.string((byte[]) val) :
      val.toString();
    return str.length() > 30 ? str.substring(0, 30) + "..." : str;
  }
}
