package org.basex.query.func.unit;

import java.util.function.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Thrown to indicate an XQUnit exception.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class UnitException extends QueryException {
  /** Expected item (can be {@code null}). */
  Item expected;
  /** Returned item (can be {@code null}). */
  Item returned;
  /** Type of the expected item (can be {@code null}). */
  final Type expectedType;
  /** Type of the returned item (can be {@code null}). */
  final Type returnedType;
  /** Item count. */
  final int count;

  /**
   * Default constructor.
   * @param info input info (can be {@code null})
   * @param err error reference
   * @param returned returned result (can be {@code null})
   * @param expected expected result (can be {@code null})
   * @param count item count
   */
  UnitException(final InputInfo info, final QueryError err, final Item returned,
      final Item expected, final int count) {
    super(info, err, count, expected == null ? "()" : expected, returned == null ? "()" : returned);
    this.expected = expected;
    this.returned = returned;
    expectedType = expected != null ? expected.type : null;
    returnedType = returned != null ? returned.type : null;
    this.count = count;
  }

  /**
   * Replaces the expected and returned items with items that can be reported after the query.
   * @param test test for nodes that can be shared
   * @param qc query context
   * @throws QueryException query exception
   */
  void report(final Predicate<Data> test, final QueryContext qc) throws QueryException {
    if(expected != null) expected = Unit.report(expected, test, qc);
    if(returned != null) returned = Unit.report(returned, test, qc);
  }
}
