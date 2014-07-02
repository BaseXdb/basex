package org.basex.query.util.unit;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Thrown to indicate an XQuery unit exception.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class UnitException extends QueryException {
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
  public UnitException(final InputInfo info, final Err err, final Item returned,
      final Item expected, final int count) {
    super(info, err, count, expected, returned);
    this.expected = expected;
    this.returned = returned;
    this.count = count;
  }
}
