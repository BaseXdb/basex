package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
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
  /** Default options. */
  private static final DeepEqualOptions DEFAULTS = new DeepEqualOptions();

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
    this(null);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   */
  public DeepEqual(final InputInfo info) {
    this(info, null, null);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param coll collation (can be {@code null})
   * @param qc query context (to interrupt process, can be {@code null})
   */
  public DeepEqual(final InputInfo info, final Collation coll, final  QueryContext qc) {
    this(info, coll, qc, null);
  }

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param coll collation (can be {@code null})
   * @param qc query context (to interrupt process, can be {@code null})
   * @param options options (can be {@code null})
   */
  public DeepEqual(final InputInfo info, final Collation coll, final  QueryContext qc,
      final DeepEqualOptions options) {
    this.info = info;
    this.coll = coll;
    this.qc = qc;
    this.options = options != null ? options : DEFAULTS;
  }

  /**
   * Checks values for deep equality.
   * @param value1 first value
   * @param value2 second value
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Value value1, final Value value2) throws QueryException {
    if(value1 == value2) return true;
    final long size1 = value1.size(), size2 = value2.size();
    return size1 == size2 && (size1 == 1 ? equal((Item) value1, (Item) value2) :
      equal(value1.iter(), value2.iter()));
  }

  /**
   * Checks iterated values for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Iter iter1, final Iter iter2) throws QueryException {
    if(options.get(DeepEqualOptions.UNORDERED)) {
      final ItemList items2 = new ItemList();
      int size1 = 0, size2 = 0;
      OUTER:
      for(Item item1; (item1 = iter1.next()) != null;) {
        size1++;
        for(int i = items2.size(); --i >= 0;) {
          if(equal(item1, items2.get(i))) {
            items2.remove(i);
            continue OUTER;
          }
        }
        for(Item item2; (item2 = iter2.next()) != null;) {
          size2++;
          if(equal(item1, item2)) continue OUTER;
          items2.add(item2);
        }
        return false;
      }
      return size1 == size2;
    }

    final long size1 = iter1.size(), size2 = iter2.size();
    if(size1 != -1 && size2 != -1 && size1 != size2) return false;
    while(true) {
      if(qc != null) qc.checkStop();
      final Item item1 = iter1.next(), item2 = iter2.next();
      if(item1 == null || item2 == null) return item1 == null && item2 == null;
      if(!equal(item1, item2)) return false;
    }
  }

  /**
   * Checks items for deep equality.
   * @param item1 first item
   * @param item2 second item
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Item item1, final Item item2) throws QueryException {
    nested = false;
    return item1 == item2 || item1.deepEqual(item2, this);
  }
}
