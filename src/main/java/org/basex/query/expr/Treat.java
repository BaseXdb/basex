package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Treat as expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Treat extends Single {
  /**
   * Constructor.
   * @param ii input info
   * @param e expression
   * @param s sequence type
   */
  public Treat(final InputInfo ii, final Expr e, final SeqType s) {
    super(ii, e);
    type = s;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return expr.isValue() ? optPre(value(ctx), ctx) : this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr);
    final Item it = iter.next();
    // input is empty
    if(it == null) {
      if(type.mayBeZero()) return Empty.ITER;
      throw XPEMPTY.thrw(info, description());
    }
    // treat as empty sequence
    if(type.occ == Occ.ZERO) NOTREAT.thrw(info, description(), it.type, type);

    if(type.zeroOrOne()) {
      if(iter.next() != null) NOTREATS.thrw(info, description(), type);
      if(!it.type.instanceOf(type.type)) NOTREAT.thrw(info, description(), it.type, type);
      return it.iter();
    }

    return new Iter() {
      Item i = it;

      @Override
      public Item next() throws QueryException {
        if(i == null) return null;
        if(!i.type.instanceOf(type.type)) NOTREAT.thrw(info, description(), i.type, type);
        final Item ii = i;
        i = iter.next();
        return ii;
      }
    };
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    final Value val = ctx.value(expr);

    final long len = val.size();
    // input is empty
    if(len == 0) {
      if(type.mayBeZero()) return val;
      throw XPEMPTY.thrw(info, description());
    }
    // treat as empty sequence
    if(type.occ == Occ.ZERO) NOTREAT.thrw(info, description(), val.type, type);

    if(type.zeroOrOne()) {
      if(len > 1) NOTREATS.thrw(info, description(), type);
      final Item it = val.itemAt(0);
      if(!it.type.instanceOf(type.type)) NOTREAT.thrw(info, description(), it.type, type);
      return it;
    }

    for(long i = 0; i < len; i++) {
      final Item it = val.itemAt(i);
      if(!it.type.instanceOf(type.type))
        NOTREAT.thrw(info, description(), it.type, type);
    }
    return val;
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new Treat(info, expr.copy(ctx, scp, vs), type);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, type), expr);
  }

  @Override
  public String toString() {
    return '(' + expr.toString() + ')' + TREAT + ' ' + AS + ' ' + type;
  }
}
