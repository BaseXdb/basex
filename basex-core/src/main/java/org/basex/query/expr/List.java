package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
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
 * @author BaseX Team 2005-18, BSD License
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

  @Override
  public void checkUp() throws QueryException {
    checkAllUp(exprs);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final int es = exprs.length;
    for(int e = 0; e < es; e++) exprs[e] = exprs[e].compile(cc);
    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // remove empty sequences
    final ExprList list = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      if(expr != Empty.SEQ) list.add(expr);
    }
    final int ls = list.size();
    if(ls != exprs.length) {
      if(ls < 2) return cc.replaceWith(this, ls == 0 ? Empty.SEQ : list.get(0));
      cc.info(OPTREMOVE_X_X, Empty.SEQ, description());
      exprs = list.finish();
    }

    // determine result type, compute number of results, set expression type
    Type type = null;
    for(final Expr expr : exprs) {
      final SeqType et = expr.seqType();
      if(!et.zero()) type = type == null ? et.type : type.union(et.type);
    }
    long size = 0;
    boolean zero = true;
    for(final Expr expr : exprs) {
      final long sz = expr.size();
      if(sz > 0 || expr.seqType().oneOrMore()) zero = false;
      if(size != -1) size = sz == -1 ? -1 : size + sz;
    }
    exprType.assign(type, zero ? Occ.ZERO_MORE : Occ.ONE_MORE, size);

    // pre-evaluate list
    if(allAreValues(true)) {
      Type tp = null;
      final Value[] values = new Value[exprs.length];
      int vl = 0;
      for(final Expr expr : exprs) {
        cc.qc.checkStop();
        final Value value = expr.value(cc.qc);
        if(vl == 0) tp = value.type;
        else if(tp != null && !tp.eq(value.type)) tp = null;
        values[vl++] = value;
      }

      Value value = Seq.get((int) size, tp, values);
      if(value == null) {
        final ValueBuilder vb = new ValueBuilder(cc.qc);
        for(int v = 0; v < vl; v++) vb.add(values[v]);
        value = vb.value();
      }
      return cc.replaceWith(this, value);
    }

    return this;
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
    if(exprs.length == 2) return ValueBuilder.concat(exprs[0].value(qc), exprs[1].value(qc), qc);
    // general case: concatenate all sequences
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Expr expr : exprs) vb.add(expr.value(qc));
    return vb.value();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new List(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof List && super.equals(obj);
  }

  @Override
  public boolean isVacuous() {
    for(final Expr expr : exprs) if(!expr.isVacuous()) return false;
    return true;
  }

  @Override
  public String description() {
    return "expression list";
  }

  @Override
  public String toString() {
    return toString(SEP);
  }
}
