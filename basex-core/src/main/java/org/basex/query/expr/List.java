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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /** Limit for the size of sequences that are materialized at compile time. */
  private static final int MAX_MAT_SIZE = 1 << 20;
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
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    final int es = exprs.length;
    for(int e = 0; e < es; e++) exprs[e] = exprs[e].compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    // compute number of results
    size = 0;
    boolean ne = false;
    for(final Expr e : exprs) {
      final long c = e.size();
      ne |= c > 0 || e.seqType().occ.min == 1;
      if(c == -1) {
        size = -1;
        break;
      } else if(size >= 0) {
        size += c;
      }
    }

    if(size >= 0) {
      if(size == 0 && !has(Flag.NDT) && !has(Flag.UPD)) return optPre(qc);
      if(allAreValues() && size <= MAX_MAT_SIZE) {
        Type all = null;
        final Value[] vs = new Value[exprs.length];
        int c = 0;
        for(final Expr e : exprs) {
          final Value v = e.value(qc);
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
          final ValueBuilder vb = new ValueBuilder(s);
          for(int i = 0; i < c; i++) vb.add(vs[i]);
          val = vb.value();
        }
        return optPre(val, qc);
      }
    }

    if(size == 0) {
      seqType = SeqType.EMP;
    } else {
      final Occ o = size == 1 ? Occ.ONE : size < 0 && !ne ? Occ.ZERO_MORE : Occ.ONE_MORE;
      SeqType st = null;
      for(final Expr e : exprs) {
        final SeqType et = e.seqType();
        if(!e.isEmpty() && et.occ != Occ.ZERO) st = st == null ? et : st.union(et);
      }
      seqType = SeqType.get(st == null ? AtomType.ITEM : st.type, o);
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

      @Override
      public boolean reset() {
        ir = null;
        e = 0;
        return true;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final Expr e : exprs) vb.add(qc.value(e));
    return vb.value();
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new List(info, copyAll(qc, scp, vs, exprs)));
  }

  @Override
  public boolean isVacuous() {
    for(final Expr e : exprs) if(!e.isVacuous()) return false;
    return true;
  }

  @Override
  public String toString() {
    return toString(SEP);
  }
}
