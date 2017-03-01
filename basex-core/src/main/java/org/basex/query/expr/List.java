package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
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
    int p = 0;
    for(final Expr expr : exprs) {
      if(!expr.isEmpty()) exprs[p++] = expr;
    }

    if(p != exprs.length) {
      cc.info(OPTREMOVE_X_X, this, Empty.SEQ);
      if(p < 2) return p == 0 ? Empty.SEQ : exprs[0];
      final Expr[] es = new Expr[p];
      System.arraycopy(exprs, 0, es, 0, p);
      exprs = es;
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

    if(size >= 0) {
      if(allAreValues() && size <= MAX_MAT_SIZE) {
        Type all = null;
        final Value[] vs = new Value[exprs.length];
        int c = 0;
        for(final Expr expr : exprs) {
          final Value v = expr.value(cc.qc);
          if(c == 0) all = v.type;
          else if(all != v.type) all = null;
          vs[c++] = v;
        }

        final Value val;
        final int s = (int) size;
        if(all == AtomType.STR)      val = StrSeq.get(vs, s);
        else if(all == AtomType.BLN) val = BlnSeq.get(vs, s);
        else if(all == AtomType.FLT) val = FltSeq.get(vs, s);
        else if(all == AtomType.DBL) val = DblSeq.get(vs, s);
        else if(all == AtomType.DEC) val = DecSeq.get(vs, s);
        else if(all == AtomType.BYT) val = BytSeq.get(vs, s);
        else if(all != null && all.instanceOf(AtomType.ITR)) {
          val = IntSeq.get(vs, s, all);
        } else {
          final ValueBuilder vb = new ValueBuilder();
          for(int i = 0; i < c; i++) vb.add(vs[i]);
          val = vb.value();
        }
        cc.info(OPTREWRITE_X, val);
        return val;
      }
    }

    if(size == 0) {
      seqType = SeqType.EMP;
    } else {
      final Occ o = size == 1 ? Occ.ONE : size < 0 && !ne ? Occ.ZERO_MORE : Occ.ONE_MORE;
      SeqType st = null;
      for(final Expr expr : exprs) {
        final SeqType et = expr.seqType();
        if(et.occ != Occ.ZERO) st = st == null ? et : st.union(et);
      }
      seqType = st != null ? st.withOcc(o) : SeqType.get(AtomType.ITEM, o);
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
  public boolean isVacuous() {
    for(final Expr expr : exprs) if(!expr.isVacuous()) return false;
    return true;
  }

  @Override
  public String toString() {
    return toString(SEP);
  }
}
