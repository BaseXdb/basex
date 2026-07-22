package org.basex.query.util;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Utility class for comparing XQuery values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DeepEqual {
  /** Default options. */
  private static final DeepEqualOptions DEFAULTS = new DeepEqualOptions();

  /** Input info (can be {@code null}). */
  public final InputInfo info;
  /** Query context (can be {@code null}). */
  public final QueryContext qc;
  /** Collation. */
  public final Collation coll;
  /** Options. */
  public final DeepEqualOptions options;

  /** Comparison function (requires {@link #qc} to be assigned). */
  public FItem itemsEqual;
  /** Flag for nested node comparisons. */
  public boolean nested;
  /** Description of the first difference that was found (diagnostics, can be {@code null}). */
  private String difference;

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
   * @param qc query context (can be {@code null})
   * @param options options (can be {@code null})
   */
  public DeepEqual(final InputInfo info, final Collation coll, final  QueryContext qc,
      final DeepEqualOptions options) {
    this.info = info;
    this.coll = Collation.get(coll, info);
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
    if(size1 != size2) return different(size1, size2);
    if(size1 != 1) return equal(value1.iter(), value2.iter());
    final Item item1 = (Item) value1, item2 = (Item) value2;
    return equal(item1, item2) || different(item1, item2);
  }

  /**
   * Checks iterated values for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Iter iter1, final Iter iter2) throws QueryException {
    if(options.get(DeepEqualOptions.ORDERED)) {
      final long size1 = iter1.size(), size2 = iter2.size();
      if(size1 != -1 && size2 != -1 && size1 != size2) return different(size1, size2);
      while(true) {
        if(qc != null) qc.checkStop();
        final Item item1 = iter1.next(), item2 = iter2.next();
        if(item1 == null || item2 == null) return item1 == item2 ||
          different(item1 != null ? item1 : item2, null);
        if(!equal(item1, item2)) return different(item1, item2);
      }
    }

    // unordered comparison
    final ItemList items2 = new ItemList();
    int size1 = 0, size2 = 0;
    OUTER: for(Item item1; (item1 = iter1.next()) != null;) {
      if(qc != null) qc.checkStop();
      size1++;
      for(int i = items2.size(); --i >= 0;) {
        if(equal(item1, items2.get(i))) {
          items2.remove(i);
          continue OUTER;
        }
      }
      for(Item item2; (item2 = iter2.next()) != null;) {
        if(qc != null) qc.checkStop();
        size2++;
        if(equal(item1, item2)) continue OUTER;
        items2.add(item2);
      }
      return different(item1, null);
    }
    final Item item2 = iter2.next();
    if(item2 != null) return different(item2, null);
    if(size1 != size2) return different(size1, size2);
    return items2.isEmpty() || different(items2.get(0), null);
  }

  /**
   * Checks items for deep equality.
   * @param item1 first item
   * @param item2 second item
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean equal(final Item item1, final Item item2) throws QueryException {
    final Bln eq = itemsEqual(item1, item2);
    if(eq != null) return eq.bool(info);
    nested = false;
    return item1 == item2 || item1.deepEqual(item2, this);
  }

  /**
   * Remembers the first items that were found to be different. As nested comparisons record
   * their result first, the most specific difference is retained.
   * @param item1 first item
   * @param item2 second item (can be {@code null} if no counterpart exists)
   * @return {@code false}
   */
  public boolean different(final Item item1, final Item item2) {
    return different(() -> item2 == null ? "No counterpart: " + item1.toErrorString() :
      "Items differ: " + item1.toErrorString() + " vs. " + item2.toErrorString());
  }

  /**
   * Remembers differing sequence lengths.
   * @param size1 length of the first sequence
   * @param size2 length of the second sequence
   * @return {@code false}
   */
  public boolean different(final long size1, final long size2) {
    return different(() -> "Different number of items: " + size1 + ", " + size2);
  }

  /**
   * Remembers the first difference that was found.
   * @param message description of the difference
   * @return {@code false}
   */
  private boolean different(final Supplier<String> message) {
    if(difference == null && options.get(DeepEqualOptions.DEBUG)) difference = message.get();
    return false;
  }

  /**
   * Outputs diagnostics on the items that were found to be different.
   * Called if the 'debug' option is enabled and if the comparison was not successful.
   */
  public void debug() {
    if(difference != null && qc != null) qc.trace(null, () -> difference);
  }

  /**
   * Checks items for deep equality.
   * @param item1 first item
   * @param item2 second item
   * @return result of check
   * @throws QueryException query exception
   */
  public Bln itemsEqual(final Item item1, final Item item2) throws QueryException {
    if(itemsEqual != null) {
      final Value value = itemsEqual.invoke(qc, info, item1, item2);
      final Value test = Types.BOOLEAN_ZO.coerce(value, qc, info);
      if(test != Empty.VALUE) return (Bln) test;
    }
    return null;
  }
}
