package org.basex.tests.w3c;

import org.basex.query.QueryException;
import org.basex.tests.w3c.qt3api.XQValue;

/**
 * Structure for storing XQuery results.
 */
class QT3Result {
  /** Query result. */
  XQValue value;
  /** Query exception. */
  QueryException exc;
  /** Query error. */
  Throwable error;
}
