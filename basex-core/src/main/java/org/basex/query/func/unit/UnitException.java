package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Thrown to indicate an XQUnit exception.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class UnitException extends QueryException {
  /** Expected item. */
  final Item expected;
  /** Returned item. */
  final Item returned;
  /** Item count. */
  final int count;

  /**
   * Default constructor.
   * @param info input info
   * @param err error reference
   * @param expected expected result
   * @param returned returned result
   * @param count item count
   */
  UnitException(final InputInfo info, final QueryError err, final Item returned,
      final Item expected, final int count) {
    super(info, err, count, expected == null ? "()" : expected, returned == null ? "()" : returned);
    this.expected = expected;
    this.returned = returned;
    this.count = count;
  }
}
