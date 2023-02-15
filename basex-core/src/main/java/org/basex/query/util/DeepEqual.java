package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Utility class for comparing XQuery values.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DeepEqual {
  /** Input info (can be {@code null}). */
  public final InputInfo info;
  /** Query context (to interrupt process, can be {@code null}). */
  public final QueryContext qc;
  /** Collation. */
  public final Collation coll;
  /** Options. */
  public final DeepEqualOptions options;
  /** Flag for nested node comparisons. */
  public boolean nested;

  /**
   * Constructor.
   */
  public DeepEqual() {
    this(null, null, null, null);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param qc query context (to interrupt process, can be {@code null})
   * @param coll collation (can be {@code null})
   * @param options options (can be {@code null})
   */
  public DeepEqual(final InputInfo info, final  QueryContext qc, final Collation coll,
      final DeepEqualOptions options) {
    this.info = info;
    this.qc = qc;
    this.coll = coll;
    this.options = options != null ? options : new DeepEqualOptions();
  }

  /**
   * Checks values for deep equality.
   * @param value1 first value
   * @param value2 second value
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Value value1, final Value value2) throws QueryException {
    return value1.size() == value2.size() && equal(value1.iter(), value2.iter());
  }

  /**
   * Checks values for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Iter iter1, final Iter iter2) throws QueryException {
    final long size1 = iter1.size(), size2 = iter2.size();
    if(size1 != -1 && size2 != -1 && size1 != size2) return false;

    while(true) {
      if(qc != null) qc.checkStop();

      final Item item1 = iter1.next(), item2 = iter2.next();
      if(item1 == null || item2 == null) return item1 == null && item2 == null;
      nested = false;
      if(!item1.equal(item2, this)) return false;
    }
  }
}
