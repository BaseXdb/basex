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
 * Expression list.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class List extends Arr {
  /** Limit for the size of sequences that are materialized at compile time. */
  private static final int MAX_MAT_SIZE = 1 << 20;
  /**
   * Constructor.
   * @param ii input info
   * @param l expression list
   */
  public List(final InputInfo ii, final Expr... l) {
    super(ii, l);
  }

  @Override
  public void checkUp() throws QueryException {
    checkAllUp(expr);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    final int es = expr.length;
    for(int e = 0; e < es; e++) expr[e] = expr[e].compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    // compute number of results
    size = 0;
    boolean ne = false;
    for(final Expr e : expr) {
      final long c = e.size();
      ne |= c > 0 || e.type().occ.min == 1;
      if(c == -1) {
        size = -1;
        break;
      } else if(size >= 0) {
        size += c;
      }
    }

    if(size >= 0) {
      if(size == 0 && !has(Flag.NDT) && !has(Flag.UPD)) return optPre(null, ctx);
      if(allAreValues() && size <= MAX_MAT_SIZE) {
        Type all = null;
        final Value[] vs = new Value[expr.length];
        int c = 0;
        for(final Expr e : expr) {
          final Value v = e.value(ctx);
          if(c == 0) all = v.type;
          else if(all != v.type) all = null;
          vs[c++] = v;
        }

        Value val = null;
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
        return optPre(val, ctx);
      }
    }

    if(size == 0) {
      type = SeqType.EMP;
    } else {
      final Occ o = size == 1 ? Occ.ONE : size < 0 && !ne ? Occ.ZERO_MORE : Occ.ONE_MORE;
      SeqType t = null;
      for(final Expr e : expr) {
        final SeqType st = e.type();
        if(e.size() != 0 && st.occ != Occ.ZERO) t = t == null ? st : t.union(st);
      }
      type = SeqType.get(t == null ? AtomType.ITEM : t.type, o);
    }

    return this;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    return new Iter() {
      Iter ir;
      int e;

      @Override
      public Item next() throws QueryException {
        while(true) {
          if(ir == null) {
            if(e == expr.length) return null;
            ir = ctx.iter(expr[e++]);
          }
          final Item it = ir.next();
          if(it != null) return it;
          ir = null;
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final ValueBuilder vb = new ValueBuilder();
    for(final Expr e : expr) vb.add(ctx.value(e));
    return vb.value();
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return copyType(new List(info, copyAll(ctx, scp, vs, expr)));
  }

  @Override
  public boolean isVacuous() {
    for(final Expr e : expr) if(!e.isVacuous()) return false;
    return true;
  }

  @Override
  public String toString() {
    return toString(SEP);
  }
}
