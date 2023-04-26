package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * List of expressions that have been separated by commas.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public List(final InputInfo info, final Expr... exprs) {
    super(info, SeqType.ITEM_ZM, exprs);
  }

  /**
   * Creates a new, optimized list expression.
   * @param cc compilation context
   * @param ii input info
   * @param exprs expressions
   * @return list, single expression or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo ii, final Expr... exprs)
      throws QueryException {
    final int el = exprs.length;
    return el > 1 ? new List(ii, exprs).optimize(cc) : el > 0 ? exprs[0] : Empty.VALUE;
  }

  @Override
  public void checkUp() throws QueryException {
    checkAllUp(exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    flatten(cc);
    removeEmpty(cc);
    toRange(cc);
    toReplicate(cc);

    final int el = exprs.length;
    if(el == 0) return cc.emptySeq(this);
    if(el == 1) return exprs[0];

    // determine result type, compute number of results, set expression type
    final SeqType st = SeqType.union(exprs, false);
    Occ occ = Occ.ZERO;
    long size = 0;
    for(final Expr expr : exprs) {
      final long sz = expr.size();
      if(size != -1) size = sz == -1 ? -1 : size + sz;
      occ = occ.add(expr.seqType().occ);
    }
    exprType.assign(st != null ? st : SeqType.EMPTY_SEQUENCE_Z, occ, size).data(exprs);

    // pre-evaluate list; skip expressions with large result sizes
    if(allAreValues(true)) {
      Type type = null;
      final Value[] values = new Value[el];
      int vl = 0;
      for(final Expr expr : exprs) {
        cc.qc.checkStop();
        final Value value = expr.value(cc.qc);
        final Type tp = value.type;
        if(vl == 0) type = tp;
        else if(type != null && !type.eq(tp)) type = null;
        values[vl++] = value;
      }

      // result size will be small enough to be cast to an integer
      Value value = Seq.get((int) size, type, values);
      if(value == null) {
        final ValueBuilder vb = new ValueBuilder(cc.qc);
        for(int v = 0; v < vl; v++) vb.add(values[v]);
        value = vb.value(this);
      }
      return cc.replaceWith(this, value);
    }

    return this;
  }

  /**
   * Tries to rewrite identical expressions to replicate.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  private void toReplicate(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    final ExprList list = new ExprList(el);
    int s = 0, e = 0;
    while(++e <= el) {
      if(e == el || !exprs[e].equals(exprs[s])) {
        if(e - s > 1) {
          list.add(cc.replicate(exprs[s], Int.get(e - s), info));
          cc.info(OPTMERGE_X, list.peek());
        } else {
          while(s < e) list.add(exprs[s++]);
        }
        s = e;
      }
    }
    exprs = list.finish();
  }

  /**
   * Tries to rewrite consecutive integers to range sequences.
   * @param cc compilation context
   */
  private void toRange(final CompileContext cc) {
    if(!((Checks<Expr>) expr -> expr instanceof Int || expr instanceof RangeSeq).any(exprs)) return;

    long[] range = null;
    final int el = exprs.length;
    final ExprList list = new ExprList(el);
    for(int e = 0; e <= el; e++) {
      final Expr expr = e < el ? exprs[e] : null;
      long[] rng = null;
      if(expr instanceof Int && expr.seqType().type == AtomType.INTEGER) {
        final long l = ((Int) expr).itr();
        rng = new long[] { l, l };
      } else if(expr instanceof RangeSeq) {
        rng = ((RangeSeq) expr).range(true);
        if(rng[1] < rng[0]) rng = null;
      }
      boolean add = rng == null;
      if(!add) {
        if(range == null) {
          // start new range: 1 - 2
          range = rng;
        } else if(rng[0] == range[1] + 1) {
          // extend range: 1 - 2, 3 - 4  ->  1 - 4
          range[1] = rng[1];
        } else {
          // finalize existing range
          add = true;
        }
      }
      if(add) {
        if(range != null) {
          final long s = range[1] - range[0] + 1;
          list.add(RangeSeq.get(range[0], s, true));
          if(s > 1) cc.info(OPTMERGE_X, list.peek());
          range = rng;
        }
        if(range == null && expr != null) list.add(expr);
      }
    }
    exprs = list.finish();
  }

  @Override
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      private final int el = exprs.length;
      private final Iter[] iters = new Iter[el];
      private long[] offsets;
      private long size;
      private int e;

      @Override
      public Item next() throws QueryException {
        while(e < el) {
          final Item item = qc.next(iter(e));
          if(item != null) return item;
          e++;
        }
        return null;
      }

      @Override
      public Item get(final long i) throws QueryException {
        int o = 0;
        while(o < el - 1 && offsets[o + 1] <= i) o++;
        return iter(o).get(i - offsets[o]);
      }

      @Override
      public long size() throws QueryException {
        if(offsets == null) {
          // first call: add up sizes
          offsets = new long[el];
          for(int o = 0; o < el && size != -1; o++) {
            // cache offsets for direct access
            offsets[o] = size;
            final long s = iter(o).size();
            size = s == -1 || size + s < 0 ? -1 : size + s;
          }
        }
        return size;
      }

      private Iter iter(final int i) throws QueryException {
        Iter iter = iters[i];
        if(iter == null) {
          iter = exprs[i].iter(qc);
          iters[i] = iter;
        }
        return iter;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // special case: concatenate two sequences
    if(exprs.length == 2) return ValueBuilder.concat(exprs[0].value(qc), exprs[1].value(qc), qc);

    // general case: concatenate all sequences
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Expr expr : exprs) vb.add(expr.value(qc));
    return vb.value(this);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // E[A, B]  ->  E[A | B]
      expr = toUnion(cc);
    } else if(mode == Simplify.DISTINCT) {
      final int el = exprs.length;
      final ExprList list = new ExprList(el);
      for(final Expr ex : exprs) list.addUnique(ex.simplifyFor(mode, cc));
      exprs = list.finish();
      if(exprs.length != el) {
        // remove duplicate list expressions
        expr = List.get(cc, info, exprs);
      } else if(seqType().type == AtomType.INTEGER) {
        // merge numbers and ranges
        expr = toDistinctRange();
      } else {
        // otherwise, rewrite list to union
        expr = toUnion(cc);
      }
    } else {
      final Expr[] ex = simplifyAll(mode, cc);
      if(ex != exprs) expr = List.get(cc, info, ex);
    }
    return cc.simplify(this, expr, mode);
  }

  /**
   * If possible, rewrites the list to a union expression.
   * @param cc compilation context
   * @return union or original expression
   * @throws QueryException query exception
   */
  public Expr toUnion(final CompileContext cc) throws QueryException {
    return seqType().type instanceof NodeType ?
      cc.replaceWith(this, new Union(info, exprs)).optimize(cc) : this;
  }

  /**
   * If possible, rewrites the list to a distinct range expression.
   * @return range or original expression
   */
  private Expr toDistinctRange() {
    long start = 0, end = 0;
    final LongList list = new LongList(2);
    for(final Expr expr : exprs) {
      if(expr instanceof Int) {
        list.add(((Int) expr).itr());
      } else if(expr instanceof RangeSeq) {
        list.add(((RangeSeq) expr).range(false));
      } else {
        return this;
      }
      final long mn = list.get(0), mx = list.peek() + 1;
      if(start == end) {
        start = mn;
        end = mx;
      } else {
        if(mx < start - 1 || mn > end) return this;
        if(mn < start) start = mn;
        if(mx > end) end = mx;
      }
      list.reset();
    }
    return RangeSeq.get(start, end - start, true);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new List(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean vacuous() {
    return ((Checks<Expr>) Expr::vacuous).all(exprs);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof List && super.equals(obj);
  }

  @Override
  public String description() {
    return "list";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.params(exprs);
  }
}
