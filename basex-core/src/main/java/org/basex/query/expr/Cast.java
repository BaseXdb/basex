package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Cast expression.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class Cast extends Single {
  /** Static context. */
  private final StaticContext sc;
  /** Sequence type to cast to. */
  final SeqType seqType;

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
    expr = expr.simplifyFor(Simplify.STRING, cc);

    // pre-evaluate (check value)
    if(expr instanceof Value) return cc.preEval(this);

    // assign target type
    final SeqType ast = expr.seqType();
    Type dt = seqType.type;
    if(dt instanceof ListType) {
      dt = dt.atomic();
      exprType.assign(dt);
    } else {
      Occ occ = seqType.occ;
      if(occ == Occ.ZERO_ONE && ast.oneOrMore() && !ast.mayBeArray()) occ = Occ.ONE;
      exprType.assign(dt, occ);
    }

    // skip cast if input and target types are equal
    // (123 cast as xs:integer)  ->  123
    // xs:string('x'[. != 'y'])  ->  'x'[. != 'y']
    // (1, 2.0) ! xs:numeric(.)  ->  (1, 2.0)
    if(ast.occ.instanceOf(seqType().occ)) {
      final Type at = ast.type;
      if(at.eq(dt) || dt == AtomType.NUM && at.instanceOf(dt)) {
        return cc.replaceWith(this, expr);
      }
    }
    return this;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = expr.atomValue(qc, info);
    final long size = value.size();
    if(!seqType.occ.check(size)) throw INVTYPE_X_X_X.get(info, value.seqType(), seqType, value);
    return size == 0 ? value : seqType.cast((Item) value, true, qc, sc, info);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return simplifyCast(mode, cc);
  }

  @Override
  public Cast copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Cast(sc, info, expr.copy(cc, vm), seqType));
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
  public void plan(final QueryString qs) {
    if(seqType.one()) {
      qs.token("(").token(expr).token(CAST).token(AS).token(seqType).token(')');
    } else {
      qs.token(seqType.type).paren(expr);
    }
  }
}
