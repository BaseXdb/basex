package org.basex.query.expr.gflwor;

import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * A group of tuples of post-grouping variables.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
final class Group {
  /** Grouping key, may contain {@code null} values. */
  final Item[] key;
  /** Non-grouping variables. */
  final ValueBuilder[] ngv;
  /** Overflow list. */
  Group next;

  /**
   * Constructor.
   * @param k grouping key
   * @param ng non-grouping variables
   */
  Group(final Item[] k, final ValueBuilder[] ng) {
    key = k;
    ngv = ng;
  }
}
