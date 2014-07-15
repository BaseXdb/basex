package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Cast expression.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Cast extends Single {
  /** Static context. */
  private final StaticContext sc;

  /**
   * Function constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression
   * @param type data type
   */
  public Cast(final StaticContext sc, final InputInfo info, final Expr expr, final SeqType type) {
    super(info, expr);
    this.sc = sc;
    this.type = type;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    if(expr.type().one()) type = SeqType.get(type.type, Occ.ONE);

    // pre-evaluate value
    if(expr.isValue()) return optPre(value(qc), qc);

    // skip cast if specified and return types are equal
    // (the following types will always be correct)
    final Type t = type.type;
    if((t == AtomType.BLN || t == AtomType.FLT || t == AtomType.DBL ||
        t == AtomType.QNM || t == AtomType.URI) && type.eq(expr.type())) {
      optPre(expr, qc);
      return expr;
    }

    size = type.occ();

    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return type.cast(expr.item(qc, info), qc, sc, info, this);
  }

  @Override
  public Cast copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Cast(sc, info, expr.copy(qc, scp, vs), type);
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
