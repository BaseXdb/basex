package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Cast expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class Cast extends Single {
  /** Static context. */
  private final StaticContext sc;
  /** Sequence type to cast to. */
  private final SeqType seqType;

  /**
   * Function constructor.
   * @param sc static context
   * @param info input info
   * @param expr expression
   * @param seqType target type
   */
  public Cast(final StaticContext sc, final InputInfo info, final Expr expr,
      final SeqType seqType) {
    super(info, expr, SeqType.ITEM_ZM);
    this.sc = sc;
    this.seqType = seqType;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    expr = cc.simplifyAtom(expr);

    // skip cast if input and target type is equal
    final SeqType st = expr.seqType();
    final Type type = seqType.type;
    if(st.one() && st.type.eq(type)) return cc.replaceWith(this, expr);

    // pre-evaluate value argument
    if(expr instanceof Value) return cc.preEval(this);

    // assign target type
    if(type instanceof ListType) {
      exprType.assign(type.atomic());
    } else {
      exprType.assign(type, st.oneOrMore() && !st.mayBeArray() ? Occ.ONE : Occ.ZERO_ONE);
    }
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.atomValue(qc, info);
    final long size = value.size();
    if(!seqType.occ.check(size)) throw INVTYPE_X_X_X.get(info, value.seqType(), seqType, value);
    return size == 1 ? seqType.cast((Item) value, true, qc, sc, info) : value;
  }

  @Override
  public Cast copy(final CompileContext cc, final IntObjMap<Var> vs) {
    return copyType(new Cast(sc, info, expr.copy(cc, vs), seqType));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Cast && seqType.eq(((Cast) obj).seqType) &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, AS, seqType), expr);
  }

  @Override
  public String toString() {
    return expr + " " + CAST + ' ' + AS + ' ' + seqType;
  }
}
