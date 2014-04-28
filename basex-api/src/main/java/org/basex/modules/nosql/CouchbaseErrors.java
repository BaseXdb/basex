package org.basex.modules.nosql;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This module contains static error functions for the Couchbase module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Prakash Thapa
 */
public final class CouchbaseErrors {
  /** Error namespace. */
  private static final byte[] NS = QueryText.EXPERROR;
  /** Namespace and error code prefix. */
  private static final String PREFIX =
      new TokenBuilder(QueryText.EXPERR).add(":COUCHBASE").toString();

  /** Private constructor, preventing instantiation. */
  private CouchbaseErrors() { }

  /**
   * CB0001: General Exceptions.
   * @param e error object
   * @return query exception
   */
  public static QueryException generalExceptionError(final Object e) {
      return thrw(1, "%s", e);
  }
  /**
   * CB0001: JSON format error.
   * @param e error object
   * @return query exception
   */
  static QueryException jsonFormatError(final Object e) {
      return thrw(2, "Invalid JSON syntax: '%s'", e);
  }
  /**
   * CB0003: Incorrect username or password or server is is not working.
   * @return query exception
   */
  public static QueryException unAuthorised() {
    return thrw(3, "Invalid Authentication parameters");
  }
  /**
   * CB0004: Couchbase's database handler handler don't exists.
   * @param cbClient couchbase client.
   * @return query exception
   */
  public static QueryException couchbaseClientError(final Object cbClient) {
    return thrw(4, "Unknown CouchbaseClient handler: '%s'", cbClient);
  }
  /**
   * CB0005: Couchbase Operation failed like "add", "update".
   * @param type which operation: add, replace ...
   * @param msg return message from couchbase server
   * @return query exception
   */
  public static QueryException couchbaseOperationFail(final Object type,
          final Object msg) {
    return thrw(5, "operation '%s' failed: '%s'", type, msg);
  }
  /**
   * CB0006: Message with one parameter.
   * @param msg return message from couchbase server
   * @param key key object
   * @return query exception
   */
  public static QueryException couchbaseMessageOneKey(final String msg,
          final Object key) {
    return thrw(6, msg, key);
  }
  /**
   * CB0007: for Bulk get, keys supplied are empty.
   * @return query exception
   */
  public static QueryException keysetEmpty() {
    return thrw(7, "Key set cannot be empty");
  }
  /**
   * CB0008: Shutdown error.
   * @return query exception
   */
  public static QueryException shutdownError() {
    return thrw(8, "Cannot be shutdown right now");
  }
  /**
   * CB0009: supplied time for shutdown in second.
   * @return query exception
   */
  public static QueryException timeInvalid() {
    return thrw(9, "Given time is not valid. It should be Integer value, second");
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
    return new QNm(String.format("%s:CB%04d", PREFIX, code), NS);
  }
}
