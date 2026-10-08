package org.basex.query.func.unit;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
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
    this.count = count;
  }

  /**
   * Copies database nodes of the expected and returned items.
   * @param qc query context
   * @throws QueryException query exception
   */
  void materialize(final QueryContext qc) throws QueryException {
    expected = materialize(expected, qc);
    returned = materialize(returned, qc);
  }

  /**
   * Copies an item if it is a database node.
   * @param item item (can be {@code null})
   * @param qc query context
   * @return item (can be {@code null})
   * @throws QueryException query exception
   */
  private static Item materialize(final Item item, final QueryContext qc) throws QueryException {
    return item == null ? null : (Item) item.materialize(TransferVisitor.SHAREABLE, null, qc);
  }
}
