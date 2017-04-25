package org.basex.modules.nosql;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This module contains static error functions for the Common Nosql Errors.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Prakash Thapa
 */
public final class NosqlErrors {
  /** Error namespace. */
  private static final byte[] NS = QueryText.EXPERROR;
  /** Namespace and error code prefix. */
  private static final String PREFIX =
      new TokenBuilder(QueryText.EXPERR).add(":NOSQL").toString();

  /** Private constructor, preventing instantiation. */
  private NosqlErrors() { }

  /**
   * CB0001: General Exceptions.
   * @param e error Object
   * @return query exception
   */
  public static QueryException generalExceptionError(final Object e) {
      return thrw(1, "%s", e);
  }
  /**
   * Returns a query exception.
   * @param code code
   * @param msg message
   * @param ext extension
   * @return query exception
   */
  private static QueryException thrw(final int code, final String msg,
      final Object... ext) {
    return new QueryException(null, qname(code), msg, ext);
  }

  /**
   * Creates an error QName for the specified code.
   * @param code code
   * @return query exception
   */
  public static QNm qname(final int code) {
    return new QNm(String.format("%s:NS%04d", PREFIX, code), NS);
  }
}
