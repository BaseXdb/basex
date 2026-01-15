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

/**
 * List of expressions that have been separated by commas.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs expressions
   */
  public List(final InputInfo info, final Expr... exprs) {
    super(info, Types.ITEM_ZM, exprs);
  }

  /**
   * Creates a new, optimized list expression.
   * @param cc compilation context
   * @param info input info (can be {@code null})
   * @param exprs expressions
   * @return list, single expression or empty sequence
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo info, final Expr... exprs)
      throws QueryException {
    final int el = exprs.length;
    return el > 1 ? new List(info, exprs).optimize(cc) : el > 0 ? exprs[0] : Empty.VALUE;
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
    exprType.assign(st != null ? st : Types.EMPTY_SEQUENCE_Z, occ, size).data(exprs);

    // pre-evaluate list; skip expressions with large result sizes
    return values(true, cc) ? cc.preEval(this).shrink(cc.qc) : this;
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
          list.add(cc.replicate(exprs[s], Itr.get(e - s), info));
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
    if(!((Checks<Expr>) expr -> expr instanceof Itr || expr instanceof RangeSeq).any(exprs)) return;

    long min = Long.MIN_VALUE, max = 0;
    final int el = exprs.length;
    final ExprList list = new ExprList(el);
    for(int e = 0; e <= el; e++) {
      final Expr expr = e < el ? exprs[e] : null;
      long mn = Long.MIN_VALUE, mx = 0;
      if(expr instanceof final Itr itr && itr.type == AtomType.INTEGER) {
        final long l = itr.itr();
        mn = l;
        mx = l;
      } else if(expr instanceof final RangeSeq rs && rs.ascending()) {
        mn = rs.min();
        mx = rs.max();
      }
      boolean add = mn == Long.MIN_VALUE;
      if(!add) {
        if(min == Long.MIN_VALUE) {
          // start new range: 1 - 2
          min = mn;
          max = mx;
        } else if(mn == max + 1) {
          // extend range: 1 - 2, 3 - 4 → 1 - 4
          max = mx;
        } else {
          // finalize existing range
          add = true;
        }
      }
      if(add) {
        if(min != Long.MIN_VALUE) {
          final long s = max - min + 1;
          list.add(RangeSeq.get(min, s, true));
          if(s > 1) cc.info(OPTMERGE_X, list.peek());
          min = mn;
          max = mx;
        }
        if(min == Long.MIN_VALUE && expr != null) list.add(expr);
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
          // first call: sum up sizes
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
    if(exprs.length == 2) return exprs[0].value(qc).append(exprs[1].value(qc), qc);

    // general case: concatenate all sequences (unknown size: create tree)
    final long size = size();
    final ValueBuilder vb = new ValueBuilder(qc, size);
    if(size == -1) vb.tree(false);
    for(final Expr expr : exprs) vb.add(expr.value(qc));
    return vb.value(this);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // E[A, B] → E[A | B]
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
        expr = toRange();
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
   * If possible, rewrites the list to a range sequence.
   * @return range or original expression
   */
  private Expr toRange() {
    long start = Long.MAX_VALUE, end = Long.MIN_VALUE, s, e;
    for(final Expr expr : exprs) {
      if(expr instanceof final Itr itr) {
        s = itr.itr();
        e = s;
      } else if(expr instanceof final RangeSeq rs && rs.ascending()) {
        s = rs.min();
        e = rs.max();
      } else {
        return this;
      }
      if(start > end) {
        start = s;
        end = e;
      } else {
        // (1, 2, 0)  or  (1, 2, 4)
        if(s < start || s > end + 1) return this;
        if(e > end) end = e;
      }
    }
    // (1 to 3, 3, 4) → 1 to 4
    return RangeSeq.get(start, end - start + 1, true);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
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
