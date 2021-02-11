package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.data.*;
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
 * @author BaseX Team 2005-21, BSD License
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
   * Creates a new, optimized list expression, or the first expression if only one was specified.
   * @param cc compilation context
   * @param ii input info
   * @param exprs one or more expressions
   * @return filter root, path or filter expression
   * @throws QueryException query exception
   */
  public static Expr get(final CompileContext cc, final InputInfo ii, final Expr... exprs)
      throws QueryException {
    return exprs.length == 1 ? exprs[0] : new List(ii, exprs).optimize(cc);
  }

  @Override
  public void checkUp() throws QueryException {
    checkAllUp(exprs);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) exprs[e] = exprs[e].compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    flatten(cc);

    // remove empty sequences
    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr == Empty.VALUE) {
        cc.info(OPTREMOVE_X_X, Empty.VALUE, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    exprs = list.finish();

    final int el = exprs.length;
    if(el == 0) return Empty.VALUE;

    // rewrite identical expressions to util:replicate
    int e = 0;
    while(++e < el && exprs[e].equals(exprs[0]));
    if(e == el) return el == 1 ? exprs[0] : cc.replicate(exprs[0], Int.get(el), info);

    // determine result type, compute number of results, set expression type
    SeqType st = null;
    Occ occ = Occ.ZERO;
    long size = 0;
    for(final Expr expr : exprs) {
      final SeqType st2 = expr.seqType();
      if(!st2.zero()) st = st == null ? st2 : st.union(st2);
      final long sz = expr.size();
      if(size != -1) size = sz == -1 ? -1 : size + sz;
      occ = occ.add(st2.occ);
    }
    exprType.assign(st != null ? st : SeqType.EMPTY_SEQUENCE_Z, occ, size);

    // pre-evaluate list; skip expressions with large result sizes
    if(allAreValues(true)) {
      // rewrite to range sequence: 1, 2, 3  ->  1 to 3
      final Expr range = toRange();
      if(range != null) return cc.replaceWith(this, range);

      Type tp = null;
      final Value[] values = new Value[el];
      int vl = 0;
      for(final Expr expr : exprs) {
        cc.qc.checkStop();
        final Value value = expr.value(cc.qc);
        if(vl == 0) tp = value.type;
        else if(tp != null && !tp.eq(value.type)) tp = null;
        values[vl++] = value;
      }

      // result size will be small enough to be cast to an integer
      Value value = Seq.get((int) size, tp, values);
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
   * Tries to rewrite the list to a range sequence.
   * @return rewritten expression or {@code null}
   */
  private Expr toRange() {
    Long start = null, end = null;
    for(final Expr expr : exprs) {
      long s, e;
      if(expr instanceof Int && expr.seqType().type == AtomType.INTEGER) {
        s = ((Int) expr).itr();
        e = s + 1;
      } else if(expr instanceof RangeSeq) {
        final long[] range = ((RangeSeq) expr).range(true);
        s = range[0];
        e = range[1] + 1;
        if(e <= s) return null;
      } else {
        return null;
      }
      if(start == null) start = s;
      else if(end != s) return null;
      end = e;
    }
    return RangeSeq.get(start, end - start, true);
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
    if(exprs.length == 2) {
      return ValueBuilder.concat(exprs[0].value(qc), exprs[1].value(qc), qc);
    }
    // general case: concatenate all sequences
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Expr expr : exprs) vb.add(expr.value(qc));
    return vb.value(this);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode == Simplify.EBV || mode == Simplify.PREDICATE) {
      // otherwise, rewrite list to union
      expr = toUnion(cc);
    } else if(mode == Simplify.DISTINCT) {
      final int el = exprs.length;
      final ExprList list = new ExprList(el);
      for(final Expr ex : exprs) list.addUnique(ex.simplifyFor(mode, cc));
      exprs = list.finish();
      if(exprs.length != el) {
        // remove duplicate list expressions
        expr = cc.simplify(this, List.get(cc, info, exprs));
      } else if(seqType().type == AtomType.INTEGER) {
        // merge numbers and ranges
        expr = toDistinctRange();
      } else {
        // otherwise, rewrite list to union
        expr = toUnion(cc);
      }
    } else if(simplifyAll(mode, cc)) {
      expr = optimize(cc);
    }
    return expr == this ? super.simplifyFor(mode, cc) : expr.simplifyFor(mode, cc);
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
  public Expr toDistinctRange() {
    long start = 0, end = 0;
    final LongList list = new LongList(2);
    for(final Expr ex : exprs) {
      if(ex instanceof Int) {
        list.add(((Int) ex).itr());
      } else if(ex instanceof RangeSeq) {
        list.add(((RangeSeq) ex).range(false));
      } else {
        return this;
      }
      final long mn = list.get(0), mx = list.peek() + 1;
      if(start == end) {
        start = mn;
        end = mx;
      } else {
        if(mn < start - 1 || mx > end + 1) return this;
        if(mn == start - 1) start = mn;
        if(mx == end + 1) end = mx;
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
  public Data data() {
    return data(exprs);
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
  public void plan(final QueryString qs) {
    qs.params(exprs);
  }
}
