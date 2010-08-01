package org.basex.query.util;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.util.Token;

/**
 * This class assembles common error messages.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Err {
  /** Private constructor. */
  private Err() { }

  /**
   * Throws an exception.
   * @param err error message
   * @param x extended info
   * @throws QueryException query exception
   */
  public static void or(final Object[] err, final Object... x)
      throws QueryException {
    throw new QueryException(err, x);
  }

  /**
   * Throws a date format exception.
   * @param i input
   * @param t expected type
   * @param ex example format
   * @throws QueryException query exception
   */
  public static void date(final byte[] i, final Type t, final String ex)
      throws QueryException {
    or(DATEFORMAT, t, i, ex);
  }

  /**
   * Throws an invalid value exception.
   * @param t expected type
   * @param v value
   * @throws QueryException query exception
   */
  public static void value(final Type t, final Object v) throws QueryException {
    or(INVALUE, t, v);
  }

  /**
   * Throws a date range exception.
   * @param t expected type
   * @param v value
   * @throws QueryException query exception
   */
  public static void range(final Type t, final byte[] v) throws QueryException {
    or(DATERANGE, t, v);
  }

  /**
   * Throws a comparison exception.
   * @param it1 first item
   * @param it2 second item
   * @throws QueryException query exception
   */
  public static void cmp(final Item it1, final Item it2) throws QueryException {
    if(it1 == it2) or(TYPECMP, it1.type);
    else or(XPTYPECMP, it1.type, it2.type);
  }

  /**
   * Throws a numeric type exception.
   * @param t expression cast type
   * @param it item
   * @throws QueryException query exception
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
