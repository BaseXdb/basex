package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Filter expression with an equality predicate, evaluated via a hash index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class HashFilter extends KeyedFilter<HashFilter.Cache> {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param pred hashable predicate
   * @param key position of the context-dependent operand (see {@link #key(Expr, Expr)})
   */
  HashFilter(final InputInfo info, final Expr root, final Expr pred, final int key) {
    super(info, root, pred, key);
  }

  /**
   * Returns the position of the context-dependent operand of a hashable filter predicate.
   * @param root root expression
   * @param pred predicate
   * @return position of the operand, or {@code -1} if the predicate cannot be hashed
   */
  static int key(final Expr root, final Expr pred) {
    return key(root, pred, cmp -> cmp.op == CmpOp.EQ && cmp.sc().collation == null,
      CmpHashG::hashable);
  }

  @Override
  Cache cache() {
    return new Cache();
  }

  @Override
  Value indexed(final Value value, final Cache cache, final QueryContext qc)
      throws QueryException {
    // keys of different or unsupported families: evaluate sequentially
    if(cache.family == BasicType.ITEM) return null;
    final Value probes = exprs[0].arg(1 - key).atomValue(qc, info);
    if(probes.isEmpty()) return Empty.VALUE;
    if(cache.index == null) {
      cache.index = index(value, cache, qc);
      if(cache.index == null) return null;
    }
    if(cache.index.isEmpty()) return Empty.VALUE;
    final ItemObjectMap<IntList> index = select(probes, cache);
    if(index == null) return null;

    IntList positions;
    if(probes.size() == 1) {
      positions = index.get(probes.itemAt(0));
      if(positions == null) return Empty.VALUE;
    } else {
      positions = new IntList();
      for(final Item item : probes) {
        final IntList list = index.get(item);
        if(list != null) positions.add(list.toArray());
      }
      positions.ddo();
    }
    return items(value, positions, qc);
  }

  /**
   * Creates a hash index from the values of the key operand to the positions of the items.
   * @param value root value
   * @param cache cache, in which the family of the keys is stored
   * @param qc query context
   * @return index, or {@code null} if the keys belong to different or unsupported families
   * @throws QueryException query exception
   */
  private ItemObjectMap<IntList> index(final Value value, final Cache cache,
      final QueryContext qc) throws QueryException {
    final ItemObjectMap<IntList> index = new ItemObjectMap<>(ItemSet.Mode.EQUAL, info);
    return keys(value, qc, (p, iter) -> {
      for(Item item; (item = qc.next(iter)) != null;) {
        cache.family = CmpHashG.family(cache.family, item);
        if(cache.family == BasicType.ITEM) return false;
        final IntList list = index.computeIfAbsent(item, () -> new IntList(1));
        if(list.isEmpty() || list.peek() != p) list.add(p);
      }
      return true;
    }) ? index : null;
  }

  /**
   * Returns the index for the specified probe values.
   * @param probes probe values
   * @param cache cache
   * @return index, or {@code null} if the filter must be evaluated sequentially
   * @throws QueryException query exception
   */
  private ItemObjectMap<IntList> select(final Value probes, final Cache cache)
      throws QueryException {
    // probes of the family of the keys, or integer and decimal probes for untyped keys
    boolean family = true, numeric = !cache.noNumbers;
    for(final Item item : probes) {
      family &= CmpHashG.family(item.type) == cache.family;
      numeric &= CmpHashG.numeric(cache.family, item);
      if(!family && !numeric) return null;
    }
    if(family) return cache.index;
    if(cache.numbers == null) {
      cache.numbers = numbers(cache.index);
      if(cache.numbers == null) cache.noNumbers = true;
    }
    return cache.numbers;
  }

  /**
   * Creates a hash index from untyped keys, cast to numbers (see {@link ANum#decimal(Item,
   * InputInfo)}), to the positions of the items.
   * @param index index with string keys
   * @return index, or {@code null} if a key is no untyped value or cannot be cast
   * @throws QueryException query exception
   */
  private ItemObjectMap<IntList> numbers(final ItemObjectMap<IntList> index) throws QueryException {
    final ItemObjectMap<IntList> numbers = new ItemObjectMap<>(ItemSet.Mode.EQUAL, info);
    final int is = index.size();
    for(int i = 1; i <= is; i++) {
      final ANum num = CmpHashG.number(index.key(i), info);
      if(num == null) return null;
      // equal numbers ('1', '01'): merge positions
      final IntList positions = index.value(i), list = numbers.get(num);
      numbers.put(num, list == null ? positions :
        new IntList(list.toArray()).add(positions.toArray()).ddo());
    }
    return numbers;
  }

  @Override
  public HashFilter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new HashFilter(info, root.copy(cc, vm), exprs[0].copy(cc, vm), key));
  }

  /**
   * Hash index of the last filtered root value.
   */
  static final class Cache {
    /** Index from keys to item positions (built on the second evaluation). */
    ItemObjectMap<IntList> index;
    /** Index from untyped keys, cast to numbers, to item positions (built on demand). */
    ItemObjectMap<IntList> numbers;
    /** Family of the keys ({@link BasicType#ITEM}: keys cannot be indexed). */
    Type family;
    /** Indicates if the keys cannot be cast to numbers. */
    boolean noNumbers;
  }
}
