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
    for(int e = 0; e < expr.length; e++) expr[e] = expr[e].compile(ctx, scp);

    // compute number of results
    size = 0;
    for(final Expr e : expr) {
      final long c = e.size();
      if(c == -1) {
        size = c;
        break;
      }
      size += c;
    }

    // evaluate sequence type
    type = SeqType.EMP;
    Value[] val = new Value[expr.length];
    for(int v = 0; v < expr.length; v++) {
      final Expr e = expr[v];
      // check if all expressions are values
      if(val != null) {
        if(e.isValue()) val[v] = (Value) e;
        else val = null;
      }
      // skip expression that will not add any results
      if(e.isEmpty()) continue;
      // evaluate sequence type
      final SeqType et = e.type();
      type = type == SeqType.EMP ? et :
        SeqType.get(et.type == type.type ? et.type : AtomType.ITEM,
            et.mayBeZero() && type.mayBeZero() ? Occ.ZERO_MORE : Occ.ONE_MORE);
    }

    // return cached integer sequence, cached values or self reference
    Expr e = this;
    final int s = (int) size;
    if(val != null && size <= Integer.MAX_VALUE) {
      if(type.type == AtomType.STR) e = StrSeq.get(val, s);
      else if(type.type == AtomType.BLN) e = BlnSeq.get(val, s);
      else if(type.type == AtomType.FLT) e = FltSeq.get(val, s);
      else if(type.type == AtomType.DBL) e = DblSeq.get(val, s);
      else if(type.type == AtomType.DEC) e = DecSeq.get(val, s);
      else if(type.type == AtomType.BYT) e = BytSeq.get(val, s);
      else if(type.type.instanceOf(AtomType.ITR)) e = IntSeq.get(val, s, type.type);
      else {
        final ValueBuilder vb = new ValueBuilder(s);
        for(final Value v : val) vb.add(v);
        e = vb.value();
      }
    }
    return optPre(e, ctx);
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
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
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
