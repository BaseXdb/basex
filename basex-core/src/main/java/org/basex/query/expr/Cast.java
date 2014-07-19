package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
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
   * @param seqType target type
   */
  public Cast(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType) {
    super(info, expr);
    this.sc = sc;
    this.seqType = seqType;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    if(expr.seqType().one()) seqType = SeqType.get(seqType.type, Occ.ONE);

    // pre-evaluate value
    if(expr.isValue()) return optPre(value(qc), qc);

    // skip cast if specified and return types are equal
    // (the following types will always be correct)
    final Type t = seqType.type;
    if((t == AtomType.BLN || t == AtomType.FLT || t == AtomType.DBL ||
        t == AtomType.QNM || t == AtomType.URI) && seqType.eq(expr.seqType())) {
      optPre(expr, qc);
      return expr;
    }
    size = seqType.occ();
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value v = expr.value(qc);
    if(!seqType.occ.check(v.size())) throw INVCASTEX.get(info, v.seqType(), seqType, v);
    return v instanceof Item ? seqType.cast((Item) v, qc, sc, info, true) : v;
  }

  @Override
  public Cast copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new Cast(sc, info, expr.copy(qc, scp, vs), seqType);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, seqType), expr);
  }

  @Override
  public String toString() {
    return expr + " " + CAST + ' ' + AS + ' ' + seqType;
  }
}
