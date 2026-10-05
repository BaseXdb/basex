package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General hash-based comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpHashG extends CmpG {
  /** Right-hand operand is deterministic and closed: cache is content-stable across calls. */
  private final boolean stable;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param info input info (can be {@code null})
   */
  CmpHashG(final Expr expr1, final Expr expr2, final CmpOp op, final InputInfo info) {
    super(info, expr1, expr2, op);
    stable = expr2.isSimple() && !expr2.hasFreeVars();
  }

  /**
   * Returns the family of values of the specified type that can be compared via hashing.
   * @param type type
   * @return {@link BasicType#STRING}, {@link BasicType#NUMERIC}, {@link BasicType#BOOLEAN},
   *   {@link BasicType#ITEM} if the family is only known at runtime, or {@code null}
   */
  static Type family(final Type type) {
    return type.isStringOrUntyped() ? BasicType.STRING : type.isNumber() ? BasicType.NUMERIC :
      type == BasicType.BOOLEAN ? BasicType.BOOLEAN :
      type.oneOf(BasicType.ITEM, BasicType.ANY_ATOMIC_TYPE) ? BasicType.ITEM : null;
  }

  /**
   * Returns the family of a sequence of items after adding another item.
   * @param family family of the previous items ({@code null} if there were none)
   * @param item item to add
   * @return family, or {@link BasicType#ITEM} if the items cannot be compared via hashing
   */
  static Type family(final Type family, final Item item) {
    final Type fam = family(item.type);
    return fam == null || family != null && fam != family ? BasicType.ITEM : fam;
  }

  /**
   * Checks if values of the specified types may be compared via hashing.
   * @param type1 first type
   * @param type2 second type
   * @return result of check
   */
  static boolean hashable(final Type type1, final Type type2) {
    final Type f1 = family(type1), f2 = family(type2);
    return f1 != null && f2 != null && (f1 == f2 || f1 == BasicType.ITEM || f2 == BasicType.ITEM);
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    final Iter iter1 = exprs[0].atomIter(qc, info);
    final long size1 = iter1.size();
    if(size1 == 0) return false;

    // stable right-hand operand: probe against populated cache without re-evaluating expr2
    final CmpCache cache = qc.threads.get(this, info).get();
    if(stable && cache.value != null) return probe(iter1, cache, qc);

    // dynamic right-hand operand: evaluate, consult cache, fall back if not eligible
    final Expr expr2 = exprs[1];
    final Iter iter2 = expr2.atomIter(qc, info);
    final long size2 = iter2.size();
    if(size2 == 0) return false;
    // check if iterator is based on value with more than one item, check if caching is enabled
    Value value2 = iter2.eagerValue();
    if(value2 == null && expr2 instanceof VarRef) value2 = expr2.value(qc);
    if(value2 != null && value2.size() > 1 && !(value2 instanceof RangeSeq) &&
        cache.active(value2, iter2)) {
      return probe(iter1, cache, qc);
    }
    return super.compare(iter1, iter2, size1, size2, qc);
  }

  /**
   * Probes items of the left-hand operand against the cache or the right-hand operand.
   * @param iter1 left-hand iterator
   * @param cache cache
   * @param qc query context
   * @return {@code true} on first hit, {@code false} if no item matches
   * @throws QueryException query exception
   */
  private boolean probe(final Iter iter1, final CmpCache cache, final QueryContext qc)
      throws QueryException {
    final Value value2 = cache.value;
    for(Item item1; (item1 = qc.next(iter1)) != null;) {
      if(cache.set != null ? find(item1, value2, cache, qc) : compare(item1, value2, qc)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Looks up an item in the cached hash set, lazily extending the set on misses.
   * @param item1 item of the left-hand operand
   * @param value2 right-hand operand
   * @param cache active cache
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean find(final Item item1, final Value value2, final CmpCache cache,
      final QueryContext qc) throws QueryException {
    final HashItemSet set = cache.set;
    final Type family = family(item1.type);
    if(cache.family != null && family != cache.family) return compare(item1, value2, qc);
    if(set.contains(item1)) {
      cache.hit = true;
      return true;
    }

    // cache remaining items (stop after first hit)
    final Iter ir2 = cache.iter;
    if(ir2 != null) {
      for(Item item2; (item2 = qc.next(ir2)) != null;) {
        cache.family = family(cache.family, item2);
        // items of different or unsupported families: dismiss cache, compare sequentially
        if(cache.family == BasicType.ITEM) {
          cache.dismiss();
          return compare(item1, value2, qc);
        }
        set.add(item2);
        if(family != cache.family) return compare(item1, value2, qc);
        if(set.contains(item1)) {
          cache.hit = true;
          return true;
        }
      }
      // iterator exhausted, all items are cached
      cache.iter = null;
    }
    return false;
  }

  /**
   * Compares an item sequentially with the items of the right-hand operand.
   * @param item1 item of the left-hand operand
   * @param value2 right-hand operand
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean compare(final Item item1, final Value value2, final QueryContext qc)
      throws QueryException {
    return compare(item1.iter(), value2.atomIter(qc, info), 1, value2.size(), qc);
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CmpHashG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, info));
  }

  @Override
  public String description() {
    return "hashed " + super.description();
  }
}
