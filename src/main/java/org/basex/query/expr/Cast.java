package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.item.SeqType.Occ;
import org.basex.util.*;

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
  public Expr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);

    Expr e = this;
    final SeqType t = expr.type();
    if(expr.isValue()) {
      // pre-evaluate value
      e = preEval(ctx);
    } else if(type.type == AtomType.BLN || type.type == AtomType.FLT ||
        type.type == AtomType.DBL || type.type == AtomType.QNM ||
        type.type == AtomType.URI) {
      // skip cast if specified and return types are equal
      if(t.eq(type) || t.type == type.type && t.one() && type.zeroOrOne())
        e = expr;
      if(e != this) optPre(e, ctx);
    }
    // adopt occurrence of argument
    if(e == this && t.one()) type = SeqType.get(type.type, Occ.ONE);
    return e;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return type.cast(expr.item(ctx, ii), true, ctx, ii, this);
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
