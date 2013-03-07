package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Cast expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Cast extends Single {
  /**
   * Function constructor.
   * @param ii input info
   * @param e expression
   * @param t data type
   */
  public Cast(final InputInfo ii, final Expr e, final SeqType t) {
    super(ii, e);
    type = t;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    if(expr.type().one()) type = SeqType.get(type.type, Occ.ONE);

    // pre-evaluate value
    if(expr.isValue()) return preEval(ctx);

    // skip cast if specified and return types are equal
    // (the following types will always be correct)
    final Type t = type.type;
    if((t == AtomType.BLN || t == AtomType.FLT || t == AtomType.DBL ||
        t == AtomType.QNM || t == AtomType.URI) && type.eq(expr.type())) {
      optPre(expr, ctx);
      return expr;
    }
    return this;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return type.cast(expr.item(ctx, ii), ctx, ii, this);
  }

  @Override
  public Cast copy(final QueryContext ctx, final VarScope scp, final IntMap<Var> vs) {
    return new Cast(info, expr.copy(ctx, scp, vs), type);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, type), expr);
  }

  @Override
  public String toString() {
    return expr + " " + CAST + ' ' + AS + ' ' + type;
  }
}
