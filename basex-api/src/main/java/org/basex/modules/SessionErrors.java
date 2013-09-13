package org.basex.modules;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This module contains static error functions for the Session module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class SessionErrors {
  /** Error namespace. */
  private static final byte[] NS = QueryText.BXERRORS;
  /** Namespace and error code prefix. */
  private static final String PREFIX = "bxerr:BXSE";

  /** Private constructor, preventing instantiation. */
  private SessionErrors() { }

  /**
   * BXSE0001: function items cannot be stored in sessions.
   * @return query exception
   */
  static QueryException functionItem() {
    return thrw(1, "Function items cannot be stored in sessions.");
  }

  /**
   * BXSE0002: stored attribute cannot be retrieved.
   * @param name name of attribute
   * @return query exception
   */
  static QueryException noAttribute(final Object name) {
    return thrw(2, "Stored attribute cannot be retrieved: %.", name);
  }

  /**
   * BXSE0003: servlet context required.
   * @return query exception
   */
  static QueryException noContext() {
    return thrw(3, "Servlet context required.");
  }

  /**
   * BXSE0004: session not found.
   * @param id session id
   * @return query exception
   */
  static QueryException whichSession(final Object id) {
    return thrw(4, "Session not found: %.", id);
  }

  /**
   * Creates a new exception.
   * @param code error code
   * @param msg error message
   * @param ext optional error extension
   * @return query exception
   */
  private static QueryException thrw(final int code, final String msg,
      final Object... ext) {

    final QNm name = new QNm(String.format("%s%04d", PREFIX, code), NS);
    return new QueryException(null, name, msg, ext);
  }
}
