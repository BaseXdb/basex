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
 * @author BaseX Team 2005-17, BSD License
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
      Type t = null;
      final Value[] values = new Value[exprs.length];
      int vl = 0;
      for(final Expr expr : exprs) {
        final Value val = cc.qc.value(expr);
        if(vl == 0) t = val.type;
        else if(t != null && !t.eq(val.type)) t = null;
        values[vl++] = val;
      }

      final Value value;
      final int s = (int) size;
      if(t == AtomType.STR)      value = StrSeq.get(values, s);
      else if(t == AtomType.BLN) value = BlnSeq.get(values, s);
      else if(t == AtomType.FLT) value = FltSeq.get(values, s);
      else if(t == AtomType.DBL) value = DblSeq.get(values, s);
      else if(t == AtomType.DEC) value = DecSeq.get(values, s);
      else if(t == AtomType.BYT) value = BytSeq.get(values, s);
      else if(t != null && t.instanceOf(AtomType.ITR)) {
        value = IntSeq.get(values, s, t);
      } else {
        final ValueBuilder vb = new ValueBuilder();
        for(int v = 0; v < vl; v++) vb.add(values[v]);
        value = vb.value();
      }
      return cc.replaceWith(this, value);
    }

    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final int el = exprs.length;
      long[] off = new long[el];
      Iter[] iter = new Iter[el];
      long size = Long.MIN_VALUE;
      int e;

      @Override
      public Item next() throws QueryException {
        while(e < el) {
          final Item it = iter(e).next();
          if(it != null) return it;
          iter[e++] = null;
        }
        return null;
      }

      @Override
      public Item get(final long i) throws QueryException {
        int o = 0;
        while(o < el - 1 && off[o + 1] <= i) o++;
        return iter(o).get(i - off[o]);
      }

      @Override
      public long size() throws QueryException {
        long s1 = 0;
        if(size == Long.MIN_VALUE) {
          for(int o = 0; o < el && s1 != -1; o++) {
            final long s2 = iter(o).size();
            off[o] = s1;
            s1 = s2 == -1 ? -1 : s1 + s2;
          }
          size = s1;
        }
        return s1;
      }

      private Iter iter(final int i) throws QueryException {
        Iter ir = iter[i];
        if(ir == null) {
          ir = qc.iter(exprs[i]);
          iter[i] = ir;
        }
        return ir;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // special case: concatenate two sequences
    if(exprs.length == 2) return ValueBuilder.concat(qc.value(exprs[0]), qc.value(exprs[1]));
    // general case: concatenate all sequences
    final ValueBuilder vb = new ValueBuilder();
    for(final Expr expr : exprs) vb.add(qc.value(expr));
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
