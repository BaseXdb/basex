package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Node comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpN extends Cmp {
  /** Comparator. */
  private final CmpOp op;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op comparator
   */
  public CmpN(final InputInfo info, final Expr expr1, final Expr expr2, final CmpOp op) {
    super(info, expr1, expr2, Types.BOOLEAN_ZO);
    this.op = op;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.oneOrMore() && st2.oneOrMore()) exprType.assign(Occ.EXACTLY_ONE);

    final Expr expr = emptyExpr();
    return expr == this && values(false, cc) ? cc.preEval(this) : cc.replaceWith(this, expr);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ANode n1 = toNodeOrNull(exprs[0], qc);
    if(n1 == null) return Empty.VALUE;
    final ANode n2 = toNodeOrNull(exprs[1], qc);
    if(n2 == null) return Empty.VALUE;
    return Bln.get(op.eval(n1, n2));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final ANode n1 = toNodeOrNull(exprs[0], qc);
    if(n1 == null) return false;
    final ANode n2 = toNodeOrNull(exprs[1], qc);
    if(n2 == null) return false;
    return op.eval(n1, n2);
  }

  @Override
  public Expr invert() {
    return null;
  }

  @Override
  public CmpOp cmpOp() {
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CmpN(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final CmpN cmp && op == cmp.op && super.equals(obj);
  }

  @Override
  public String description() {
    return "'" + op.toNodeString() + "' comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, OP, op.toNodeString()), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, " " + op.toNodeString() + ' ', true);
  }
}
