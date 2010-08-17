package org.basex.query.util;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.util.InputInfo;

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
   * @param ii input info
   * @param err error message
   * @param ext extended info
   * @throws QueryException query exception
   */
  public static void or(final InputInfo ii, final Object[] err,
      final Object... ext) throws QueryException {
    throw new QueryException(ii, err, ext);
  }

  /**
   * Throws an exception.
   * @param ii input info
   * @param err error message
   * @param ext extended info
   * @throws QueryException query exception
   */
  public static void or(final InputInfo ii, final Object err,
      final Object... ext) throws QueryException {
    throw new QueryException(ii, err, ext);
  }

  /**
   * Throws a comparison exception.
   * @param ii input info
   * @param it1 first item
   * @param it2 second item
   * @throws QueryException query exception
   */
  public static void diff(final InputInfo ii, final Item it1, final Item it2)
      throws QueryException {
    if(it1 == it2) or(ii, TYPECMP, it1.type);
    else or(ii, XPTYPECMP, it1.type, it2.type);
  }

  /**
   * Throws a numeric type exception.
   * @param ii input info
   * @param t expression cast type
   * @param v value
   * @throws QueryException query exception
   */
  public static void cast(final InputInfo ii, final Type t, final Value v)
      throws QueryException {
    or(ii, XPINVCAST, v.type, t, v);
  }

  /**
   * Throws a type exception.
   * @param ii input info
   * @param inf expression info
   * @param t expected type
   * @param it found item
   * @throws QueryException query exception
   */
  public static void type(final InputInfo ii, final String inf,
      final Type t, final Item it) throws QueryException {
    or(ii, XPTYPE, inf, t, it.type);
  }

  /**
   * Throws a type exception.
   * @param e parsing expression
   * @param t expected type
   * @param it found item
   * @throws QueryException query exception
   */
  public static void type(final ParseExpr e, final Type t, final Item it)
      throws QueryException {
    type(e.input, e.desc(), t, it);
  }

  /**
   * Throws a number exception.
   * @param e parsing expression
   * @param it found item
   * @throws QueryException query exception
   */
  public static void number(final ParseExpr e, final Item it)
      throws QueryException {
    or(e.input, XPTYPENUM, e.desc(), it.type);
  }

  /**
   * Throws an invalid value exception.
   * @param ii input info
   * @param t expected type
   * @param v value
   * @throws QueryException query exception
   */
  public static void value(final InputInfo ii, final Type t, final Object v)
      throws QueryException {
    or(ii, INVALUE, t, v);
  }
}
