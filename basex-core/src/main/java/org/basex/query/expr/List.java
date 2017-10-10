package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
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
  /** Limit for the size of sequences that are materialized at compile time. */
  private static final int MAX_MAT_SIZE = 1 << 16;

  /**
   * Constructor.
   * @param info input info
   * @param exprs expressions
   */
  public List(final InputInfo info, final Expr... exprs) {
    super(info, exprs);
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
      if(!expr.isEmpty()) list.add(expr);
    }

    final int ls = list.size();
    if(ls != exprs.length) {
      if(ls < 2) return cc.replaceWith(this, ls == 0 ? Empty.SEQ : list.get(0));
      cc.info(OPTREMOVE_X_X, description(), Empty.SEQ);
      exprs = list.finish();
    }

    // compute number of results
    size = 0;
    boolean ne = false;
    for(final Expr expr : exprs) {
      final long c = expr.size();
      ne |= c > 0 || expr.seqType().occ.min == 1;
      if(c == -1) {
        size = -1;
        break;
      } else if(size >= 0) {
        size += c;
      }
    }

    if(size == 0) {
      seqType = SeqType.EMP;
    } else {
      final Occ o = size == 1 ? Occ.ONE : size < 0 && !ne ? Occ.ZERO_MORE : Occ.ONE_MORE;
      SeqType st = null;
      for(final Expr expr : exprs) {
        final SeqType et = expr.seqType();
        if(!et.zero()) st = st == null ? et : st.union(et);
      }
      seqType = st != null ? st.withOcc(o) : SeqType.get(AtomType.ITEM, o);
    }

    if(allAreValues() && size >= 0 && size <= MAX_MAT_SIZE) {
      Type type = null;
      final Value[] values = new Value[exprs.length];
      int vl = 0;
      for(final Expr expr : exprs) {
        final Value val = cc.qc.value(expr);
        if(vl == 0) type = val.type;
        else if(type != val.type) type = null;
        values[vl++] = val;
      }

      final Value value;
      final int s = (int) size;
      if(type == AtomType.STR)      value = StrSeq.get(values, s);
      else if(type == AtomType.BLN) value = BlnSeq.get(values, s);
      else if(type == AtomType.FLT) value = FltSeq.get(values, s);
      else if(type == AtomType.DBL) value = DblSeq.get(values, s);
      else if(type == AtomType.DEC) value = DecSeq.get(values, s);
      else if(type == AtomType.BYT) value = BytSeq.get(values, s);
      else if(type != null && type.instanceOf(AtomType.ITR)) {
        value = IntSeq.get(values, s, type);
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
  public Iter iter(final QueryContext qc) {
    return new Iter() {
      Iter ir;
      int e;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir == null) {
            if(e == exprs.length) return null;
            ir = qc.iter(exprs[e++]);
          }
          final Item it = ir.next();
          if(it != null) return it;
          ir = null;
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // most common case
    if(exprs.length == 2) return ValueBuilder.concat(qc.value(exprs[0]), qc.value(exprs[1]));
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
