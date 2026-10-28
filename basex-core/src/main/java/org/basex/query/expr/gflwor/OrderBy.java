package org.basex.query.expr.gflwor;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * FLWOR {@code order by}-expression.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class OrderBy extends Clause {
  /** References to the variables to be sorted. */
  private VarRef[] refs;
  /** Sort keys. */
  private final OrderKey[] keys;

  /**
   * Constructor.
   * @param refs variables to sort
   * @param keys sort keys
   * @param info input info (can be {@code null})
   */
  public OrderBy(final VarRef[] refs, final OrderKey[] keys, final InputInfo info) {
    super(info, Types.ITEM_ZM);
    this.refs = refs;
    this.keys = keys;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      /** Values of all tuples (one entry per tuple and variable). */
      private final ValueList vals = new ValueList();
      /** Order of the tuples. */
      private int[] perm;
      /** Current position. */
      private int pos;

      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(perm == null) sort(qc);
        if(pos == perm.length) return false;
        final int rl = refs.length, v = perm[pos++] * rl;
        for(int r = 0; r < rl; r++) {
          qc.set(refs[r].var, vals.get(v + r));
          // free the space occupied by the tuple
          vals.set(v + r, null);
        }
        return true;
      }

      /**
       * Caches and sorts all incoming tuples.
       * @param qc query context
       * @throws QueryException query exception
       */
      private void sort(final QueryContext qc) throws QueryException {
        // collation keys of strings (only created if a collation is specified)
        boolean coll = false;
        for(final OrderKey key : keys) coll |= key.coll != null;

        final ItemList items = new ItemList();
        final TokenList colls = coll ? new TokenList() : null;
        while(sub.next(qc)) {
          for(final OrderKey ok : keys) {
            final Item key = ok.expr.atomItem(qc, ok.info());
            items.add(key);
            if(colls != null) colls.add(Collation.key(key, ok.coll, ok.info()));
          }
          for(final VarRef ref : refs) vals.add(ref.value(qc));
        }

        final int kl = keys.length, size = items.size() / kl;
        final KeyComparator[] comparators = new KeyComparator[kl];
        for(int k = 0; k < kl; k++) comparators[k] = comparator(k, items, colls, size, qc);
        try {
          perm = Array.createOrder(size, (x, y) -> {
            try {
              qc.checkStop();
              for(int k = 0; k < kl; k++) {
                final int c = comparators[k].compare(x, y);
                if(c != 0) return keys[k].desc ? -c : c;
              }
              return 0;
            } catch(final QueryException ex) {
              throw new QueryRTException(ex);
            }
          });
        } catch(final QueryRTException ex) {
          throw ex.getCause();
        }
      }
    };
  }

  /**
   * Returns an evaluator that only returns the smallest tuples.
   * @param sub wrapped evaluator
   * @param limit maximum number of tuples
   * @return evaluator
   */
  Eval eval(final Eval sub, final int limit) {
    return new Eval() {
      /** Smallest tuples. */
      private Iterator<Tuple> tuples;

      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(tuples == null) {
          final TopK<Tuple> top = new TopK<>(limit, (t1, t2) -> compare(t1, t2, qc));
          while(sub.next(qc)) top.add(tuple(qc));
          tuples = top.finish().iterator();
        }
        if(!tuples.hasNext()) return false;
        final Value[] values = tuples.next().values;
        final int rl = refs.length;
        for(int r = 0; r < rl; r++) qc.set(refs[r].var, values[r]);
        return true;
      }
    };
  }

  /**
   * Creates a tuple with the sort keys and values of the current variables.
   * @param qc query context
   * @return tuple
   * @throws QueryException query exception
   */
  private Tuple tuple(final QueryContext qc) throws QueryException {
    final int kl = keys.length, rl = refs.length;
    final Item[] items = new Item[kl];
    final byte[][] colls = new byte[kl][];
    for(int k = 0; k < kl; k++) {
      final OrderKey ok = keys[k];
      items[k] = ok.expr.atomItem(qc, ok.info());
      colls[k] = Collation.key(items[k], ok.coll, ok.info());
    }
    final Value[] values = new Value[rl];
    for(int r = 0; r < rl; r++) values[r] = refs[r].value(qc);
    return new Tuple(items, colls, values);
  }

  /**
   * Compares the sort keys of two tuples.
   * @param tuple1 first tuple
   * @param tuple2 second tuple
   * @param qc query context
   * @return result of comparison
   * @throws QueryException query exception
   */
  private int compare(final Tuple tuple1, final Tuple tuple2, final QueryContext qc)
      throws QueryException {
    qc.checkStop();
    final int kl = keys.length;
    for(int k = 0; k < kl; k++) {
      final int c = compare(keys[k], tuple1.items[k], tuple1.colls[k], tuple2.items[k],
          tuple2.colls[k], qc);
      if(c != 0) return keys[k].desc ? -c : c;
    }
    return 0;
  }

  /**
   * Compares two sort keys.
   * @param key order key
   * @param item1 first sort key ({@link Empty#VALUE}: no key)
   * @param coll1 collation key of the first sort key (can be {@code null})
   * @param item2 second sort key ({@link Empty#VALUE}: no key)
   * @param coll2 collation key of the second sort key (can be {@code null})
   * @param qc query context
   * @return result of comparison
   * @throws QueryException query exception
   */
  private static int compare(final OrderKey key, final Item item1, final byte[] coll1,
      final Item item2, final byte[] coll2, final QueryContext qc) throws QueryException {
    final InputInfo ii = key.info();
    if(item1 != Empty.VALUE && item2 != Empty.VALUE && !item1.comparable(item2))
      throw typeError(item2, item1.type, ii);

    if(item1 == Empty.VALUE) return item2 == Empty.VALUE ? 0 : key.least ? -1 : 1;
    if(item2 == Empty.VALUE) return key.least ? 1 : -1;
    // empty greatest: NaN follows all other numbers
    if(!key.least) {
      final boolean n1 = nan(item1, ii), n2 = nan(item2, ii);
      if(n1 != n2) return n1 ? 1 : -1;
    }
    return Collation.compare(item1, coll1, item2, coll2, key.coll, qc, ii);
  }

  /**
   * Checks if the specified item is a NaN value.
   * @param item item
   * @param info input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean nan(final Item item, final InputInfo info) throws QueryException {
    return (item instanceof Dbl || item instanceof Flt) && Double.isNaN(item.dbl(info));
  }

  /**
   * Tuple with sort keys and variable values.
   * @param items sort keys
   * @param colls collation keys (entries can be {@code null})
   * @param values values of the variables
   */
  private record Tuple(Item[] items, byte[][] colls, Value[] values) { }

  /**
   * Returns a comparator for the specified sort key.
   * @param k index of the sort key
   * @param items atomized sort keys of all tuples
   * @param colls collation keys (can be {@code null})
   * @param size number of tuples
   * @param qc query context
   * @return comparator
   * @throws QueryException query exception
   */
  private KeyComparator comparator(final int k, final ItemList items, final TokenList colls,
      final int size, final QueryContext qc) throws QueryException {

    final OrderKey key = keys[k];
    final InputInfo ii = key.info();
    final int kl = keys.length;
    final IntBinaryOperator typed = TypedKeys.comparator(size, t -> items.get(t * kl + k),
        key.coll, key.least, ii);
    if(typed != null) {
      // primitive values: release the items
      for(int t = 0; t < size; t++) items.set(t * kl + k, null);
      return typed::applyAsInt;
    }
    return (x, y) -> {
      final int a = x * kl + k, b = y * kl + k;
      return compare(key, items.get(a), colls != null ? colls.get(a) : null, items.get(b),
          colls != null ? colls.get(b) : null, qc);
    };
  }

  /** Comparator for the sort keys of two tuples. */
  @FunctionalInterface
  private interface KeyComparator {
    /**
     * Compares the sort keys of two tuples.
     * @param x first tuple
     * @param y second tuple
     * @return result of comparison
     * @throws QueryException query exception
     */
    int compare(int x, int y) throws QueryException;
  }

  /**
   * Merges the order by clause with the supplied for clause.
   * @param fr for clause (can be {@code null})
   * @param cc compilation context
   * @return success flag
   * @throws QueryException query exception
   */
  boolean merge(final For fr, final CompileContext cc) throws QueryException {
    final OrderKey key = keys[0];
    if(keys.length != 1 || key.coll != null || !key.least) return false;

    final Expr expr = key.expr;
    // do not rewrite array keys (as they may contain sequences)
    if(expr.seqType().mayBeWrapped()) return false;
    // for $i in E order by 1 → for $i in E
    if(expr instanceof Item) return true;

    // key must only depend on the for variable, which must not be coerced
    if(fr == null || fr.var.declType != null || expr.has(Flag.CTX)) return false;
    final Var[] vrs = expr.freeVars();
    if(vrs == null || vrs.length != 1 || vrs[0] != fr.var) return false;

    // for $i in E order by K descending → for $i in sort-by(E, { 'key': fn($i) { K }, ... })
    final Value func = expr instanceof VarRef ? Empty.VALUE : func(expr, fr.var, cc);
    final Value order = key.desc ? Str.get("descending") : Empty.VALUE;
    final XQMap map = XQMap.get(Records.SORT_KEY.get(), func, Empty.VALUE, order);
    fr.expr = cc.function(SORT_BY, info, fr.expr, map);
    return true;
  }

  /**
   * Creates a key function from an order expression.
   * @param expr order expression
   * @param var for variable
   * @param cc compilation context
   * @return function item
   * @throws QueryException query exception
   */
  private FuncItem func(final Expr expr, final Var var, final CompileContext cc)
      throws QueryException {
    final VarScope vs = new VarScope();
    cc.pushScope(vs);
    try {
      final Var param = vs.addNew(var.name, null, cc.qc, info);
      final IntObjectMap<Var> vm = new IntObjectMap<>();
      vm.put(var.id, param);
      // atomize the key, reject multiple items
      final SeqType st = Types.ANY_ATOMIC_TYPE_ZO;
      final Expr body = new TypeCheck(info, expr.copy(cc, vm), st).optimize(cc);
      final Var[] params = { param };
      final FuncType ft = FuncType.get(AnnList.EMPTY, st, params).withRefinedType(body.seqType());
      return new FuncItem(info, body, params, AnnList.EMPTY, ft, vs.stackSize(), null);
    } finally {
      cc.removeScope();
    }
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final OrderKey key : keys) {
      if(key.has(flags)) return true;
    }
    return false;
  }

  @Override
  public OrderBy compile(final CompileContext cc) throws QueryException {
    for(final OrderKey key : keys) key.compile(cc);
    return this;
  }

  @Override
  public OrderBy optimize(final CompileContext cc) {
    return this;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final OrderKey key : keys) {
      if(!key.inlineable(ic)) return false;
    }
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, keys);
  }

  @Override
  public Clause inline(final InlineContext ic) throws QueryException {
    if(ic.var != null) {
      for(int r = refs.length; --r >= 0;) {
        if(refs[r].var == ic.var) refs = Array.remove(refs, r);
      }
    }
    return ic.inline(keys) ? optimize(ic.cc) : null;
  }

  @Override
  public OrderBy copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new OrderBy(Arr.copyAll(cc, vm, refs), Arr.copyAll(cc, vm, keys), info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, keys);
  }

  @Override
  boolean clean(final IntObjectMap<Var> decl, final BitSet used) {
    // delete unused variables
    final int len = refs.length;
    for(int r = len; --r >= 0;) {
      if(!used.get(refs[r].var.id)) refs = Array.remove(refs, r);
    }
    if(refs.length == used.cardinality()) return refs.length != len;

    // add new variables, possible when an expression is inlined below this clause
    OUTER: for(int id = used.nextSetBit(0); id >= 0; id = used.nextSetBit(id + 1)) {
      for(final VarRef ref : refs) {
        if(ref.var.id == id) continue OUTER;
      }
      refs = Array.add(refs, new VarRef(info, decl.get(id)));
    }
    return true;
  }

  @Override
  boolean skippable(final Clause cl) {
    return cl instanceof Where;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(keys);
  }

  @Override
  public int exprSize() {
    int size = 0;
    for(final Expr ref : refs) size += ref.exprSize();
    for(final Expr key : keys) size += key.exprSize();
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final OrderBy o && Arrays.equals(refs, o.refs) &&
        Arrays.equals(keys, o.keys);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this), keys);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(ORDER).token(BY).tokens(keys, SEP);
  }
}
