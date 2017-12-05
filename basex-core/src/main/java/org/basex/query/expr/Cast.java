package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Cast expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
    super(info, expr, seqType);
    this.sc = sc;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final SeqType st = expr.seqType();
    if(st.oneNoArray()) exprType.assign(Occ.ONE);

    // pre-evaluate value
    if(expr instanceof Value) return cc.preEval(this);

    // skip cast if specified and return types are equal
    // (the following types will always be correct)
    final Type t = seqType().type;
    if((t == AtomType.BLN || t == AtomType.FLT || t == AtomType.DBL ||
        t == AtomType.QNM || t == AtomType.URI) && seqType().eq(expr.seqType())) {
      return cc.replaceWith(this, expr);
    }
    return this;
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.atomValue(qc, info);
    final SeqType st = seqType();
    if(!st.occ.check(value.size())) throw INVTYPE_X_X_X.get(info, value.seqType(), st, value);
    return value instanceof Item ? st.cast((Item) value, qc, sc, info, true) : value;
  }

  @Override
  public Cast copy(final CompileContext cc, final IntObjMap<Var> vs) {
    return copyType(new Cast(sc, info, expr.copy(cc, vs), seqType()));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Cast && seqType().eq(((Cast) obj).seqType()) &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(AS, seqType()), expr);
  }

  @Override
  public String toString() {
    return expr + " " + CAST + ' ' + AS + ' ' + seqType();
  }
}
