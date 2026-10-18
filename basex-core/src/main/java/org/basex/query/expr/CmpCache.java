package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Hash cache for {@link CmpHashG} general comparisons. Items of the right-hand operand are added
 * to a hash set on demand and reused across consecutive evaluations of the same expression.
 * Caching is dismissed if the operand changes and the previous cache turned out to be unused —
 * neither a hit was found nor was the right-hand operand fully consumed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpCache {
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Right-hand operand is deterministic and closed: cache is content-stable across calls. */
  final boolean stable;
  /** Hash set with items of the right-hand operand; {@code null} if caching has been dismissed. */
  HashItemSet set;
  /** Right-hand operand currently being cached (compared by identity). */
  Value value;
  /** Lazy iterator over the right-hand operand; {@code null} once exhausted. */
  Iter iter;
  /** Family of the cached items ({@code null} if none). */
  Type family;
  /** Hash set with untyped items, cast to numbers (built on demand). */
  HashItemSet numbers;
  /** Indicates if the cached items cannot be cast to numbers. */
  boolean noNumbers;
  /** Indicates if at least one cache hit occurred since the last (re-)initialization. */
  boolean hit;
  /** Indicates if the cache is being probed. */
  boolean busy;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr right-hand operand
   */
  public CmpCache(final InputInfo info, final Expr expr) {
    this.info = info;
    stable = expr.isSimple() && !expr.hasFreeVars();
    init();
  }

  /**
   * Activates the cache for the given right-hand operand. Re-initializes the hash set if the
   * operand has changed since the previous call; dismisses the cache permanently if the previous
   * cache had no hits and the right-hand operand was not fully consumed (no reuse benefit).
   * @param val right-hand operand value
   * @param ir right-hand operand iterator
   * @return {@code true} if the cache is active, {@code false} if the caller should fall back to
   *   a non-cached comparison
   */
  boolean active(final Value val, final Iter ir) {
    // check if caching was dismissed
    if(set == null) return false;

    // operand changed: dismiss only if cache was unused (no hits AND not fully built)
    if(value != val) {
      if(!hit && iter != null) {
        dismiss();
        return false;
      }
      init();
      value = val;
      iter = ir;
      hit = false;
    }
    return true;
  }

  /**
   * Returns a hash set with all items of the right-hand operand, cast to numbers
   * (see {@link ANum#decimal(Item, InputInfo)}).
   * @param qc query context
   * @return hash set, or {@code null} if an item is no untyped value or cannot be cast
   * @throws QueryException query exception
   */
  HashItemSet numbers(final QueryContext qc) throws QueryException {
    if(numbers == null && !noNumbers) {
      if(iter != null) {
        for(Item item; (item = qc.next(iter)) != null;) {
          if(!add(item)) return null;
        }
        iter = null;
      }
      numbers = cast();
      noNumbers = numbers == null;
    }
    return numbers;
  }

  /**
   * Casts the cached items to numbers.
   * @return hash set, or {@code null} if an item is no untyped value or cannot be cast
   * @throws QueryException query exception
   */
  private HashItemSet cast() throws QueryException {
    final HashItemSet nums = new HashItemSet(ItemSet.Mode.EQUAL, info, set.size());
    for(final Item item : set) {
      final ANum num = CmpHashG.number(item, info);
      if(num == null) return null;
      nums.add(num);
    }
    return nums;
  }

  /**
   * Looks up an item in the specified hash set and records a hit.
   * @param hs hash set
   * @param item item to look up
   * @return result of check
   * @throws QueryException query exception
   */
  boolean contains(final HashItemSet hs, final Item item) throws QueryException {
    final boolean found = hs.contains(item);
    hit |= found;
    return found;
  }

  /**
   * Adds an item of the right-hand operand to the hash set.
   * @param item item to add
   * @return {@code false} if the cache was dismissed (items of different or unsupported families)
   * @throws QueryException query exception
   */
  boolean add(final Item item) throws QueryException {
    family = CmpHashG.family(family, item);
    if(family == BasicType.ITEM) {
      dismiss();
      return false;
    }
    set.add(item);
    return true;
  }

  /**
   * Dismisses the cache permanently.
   */
  void dismiss() {
    set = null;
    value = null;
    iter = null;
    numbers = null;
  }

  /**
   * Creates a fresh empty hash set.
   */
  private void init() {
    set = new HashItemSet(ItemSet.Mode.EQUAL, info);
    family = null;
    numbers = null;
    noNumbers = false;
  }
}