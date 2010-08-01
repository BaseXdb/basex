package org.basex.query.util;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Type;

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
    throw new QueryException(null, err, x);
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
   * Throws a numeric type exception.
   * @param t expression cast type
   * @param it item
   * @throws QueryException query exception
   */
  public static void cast(final Type t, final Item it) throws QueryException {
    or(XPINVCAST, it.type, t, it);
  }
}
