package org.basex.query.expr.gflwor;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.List;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code order by}-expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class OrderBy extends Clause {
  /** References to the variables to be sorted. */
  VarRef[] refs;
  /** Sort keys. */
  final OrderKey[] keys;

  /**
   * Constructor.
   * @param refs variables to sort
   * @param keys sort keys
   * @param info input info
   */
  public OrderBy(final VarRef[] refs, final OrderKey[] keys, final InputInfo info) {
    super(info, SeqType.ITEM_ZM);
    this.refs = refs;
    this.keys = keys;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      private Value[][] tpls;
      private Integer[] perm;
      int pos;

      @Override
      public boolean next(final QueryContext qc) throws QueryException {
        if(tpls == null) sort(qc);
        if(pos == tpls.length) return false;
        final int p = perm[pos++];
        final Value[] tuple = tpls[p];
        // free the space occupied by the tuple
        tpls[p] = null;
        final int rl = refs.length;
        for(int r = 0; r < rl; r++) qc.set(refs[r].var, tuple[r]);
        return true;
      }

      /**
       * Caches and sorts all incoming tuples.
       * @param qc query context
       * @throws QueryException evaluation exception
       */
      private void sort(final QueryContext qc) throws QueryException {
        // keys are stored at odd positions, values at even ones
        List<Value[]> tuples = new ArrayList<>();
        while(sub.next(qc)) {
          final int kl = keys.length;
          final Item[] key = new Item[kl];
          for(int k = 0; k < kl; k++) key[k] = keys[k].expr.atomItem(qc, keys[k].info);
          tuples.add(key);

          final int rl = refs.length;
          final Value[] vals = new Value[rl];
          for(int r = 0; r < rl; r++) vals[r] = refs[r].value(qc);
          tuples.add(vals);
        }

        final int len = tuples.size() >>> 1;
        final Item[][] ks = new Item[len][];
        perm = new Integer[len];
        tpls = new Value[len][];
        for(int i = 0; i < len; i++) {
          perm[i] = i;
          tpls[i] = tuples.get(i << 1 | 1);
          ks[i] = (Item[]) tuples.get(i << 1);
        }
        // be nice to the garbage collector
        tuples = null;
        try {
          Arrays.sort(perm, (x, y) -> {
            try {
              final Item[] a = ks[x], b = ks[y];
              final int kl = keys.length;
              for(int k = 0; k < kl; k++) {
                final OrderKey key = keys[k];
                Item m = a[k], n = b[k];
                if(m == Dbl.NAN || m == Flt.NAN) m = Empty.VALUE;
                if(n == Dbl.NAN || n == Flt.NAN) n = Empty.VALUE;
                if(m != Empty.VALUE && n != Empty.VALUE && !m.comparable(n))
                  throw typeError(n, m.type, key.info);

                final int c = m == Empty.VALUE
                    ? n == Empty.VALUE ? 0                 : key.least ? -1 : 1
                    : n == Empty.VALUE ? key.least ? 1 : -1 : m.diff(n, key.coll, key.info);
                if(c != 0) return key.desc ? -c : c;
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
   * Merges the order by clause with the supplied for clause.
   * @param fr for clause
   * @param cc compilation context
   * @return success flag
   * @throws QueryException query exception
   */
  boolean merge(final For fr, final CompileContext cc) throws QueryException {
    if(keys.length == 1 && keys[0].coll == null && keys[0].least) {
      final Expr expr = keys[0].expr;
      // do not rewrite array keys (as they may contain sequences)
      if(expr.seqType().mayBeArray()) return false;
      // for $i in 1 to 2 order by 1 return $i  ->  for $i in 1 to 2 return $i
      if(expr instanceof Item) return true;
      // for $i in 1 to 2 order by $i return $i  ->  for $i in sort(1 to 2) return $i
      if(fr != null && expr instanceof VarRef && ((VarRef) expr).var.is(fr.var)) {
        fr.expr = cc.function(SORT, info, fr.expr);
        if(keys[0].desc) fr.expr = cc.function(REVERSE, info, fr.expr);
        return true;
      }
    }
    return false;
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
        if(refs[r].var.is(ic.var)) refs = Array.remove(refs, r);
      }
    }
    return ic.inline(keys) ? optimize(ic.cc) : null;
  }

  @Override
  public OrderBy copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new OrderBy(Arr.copyAll(cc, vm, refs), Arr.copyAll(cc, vm, keys), info));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, keys);
  }

  @Override
  boolean clean(final IntObjMap<Var> decl, final BitArray used) {
    // delete unused variables
    final int len = refs.length;
    for(int r = refs.length; --r >= 0;) {
      if(!used.get(refs[r].var.id)) refs = Array.remove(refs, r);
    }
    if(refs.length == used.cardinality()) return refs.length != len;

    // add new variables, possible when an expression is inlined below this clause
    OUTER: for(int id = used.nextSet(0); id >= 0; id = used.nextSet(id + 1)) {
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
    if(this == obj) return true;
    if(!(obj instanceof OrderBy)) return false;
    final OrderBy o = (OrderBy) obj;
    return Array.equals(refs, o.refs) && Array.equals(keys, o.keys);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), keys);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(ORDER).token(BY).tokens(keys, SEP);
  }
}
