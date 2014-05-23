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
   * @param ii input info
   * @param er error reference
   * @param exp expected result
   * @param ret returned result
   * @param c item count
   */
  public UnitException(final InputInfo ii, final Err er, final Item ret, final Item exp,
      final int c) {
    super(ii, er, c, exp, ret);
    expected = exp;
    returned = ret;
    count = c;
  }
}
