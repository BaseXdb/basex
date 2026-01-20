package org.basex.query.expr;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Value comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpV extends Cmp {
  /** Comparator. */
  private CmpOp op;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   */
  public CmpV(final InputInfo info, final Expr expr1, final Expr expr2, final CmpOp op) {
    super(info, expr1, expr2, Types.BOOLEAN_ZO);
    this.op = op;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    exprs = simplifyAll(Simplify.STRING, cc);
    if(values(false, cc)) return cc.preEval(this);

    // swap operands
    if(swap()) {
      cc.info(OPTSWAP_X, this);
      Collections.reverse(Arrays.asList(exprs));
      op = op.swap();
    }

    Expr expr = emptyExpr();
    if(expr == this) expr = toGeneral(cc, true);
    if(expr == this) expr = opt(cc);
    if(expr == this) {
      // restrict type
      final SeqType st1 = exprs[0].seqType(), st2 = exprs[1].seqType();
      if(st1.oneOrMore() && !st1.mayBeArray() && st2.oneOrMore() && !st2.mayBeArray()) {
        exprType.assign(Occ.EXACTLY_ONE);
      }
    }
    return cc.replaceWith(this, expr);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.EBV, Simplify.PREDICATE)) {
      // E[@x eq 'x'] → E[@x = 'x']  (enables further optimizations)
      expr = toGeneral(cc, false);
    }
    return cc.simplify(this, expr, mode);
  }

  /**
   * Tries to rewrite this expression to a general comparison.
   * @param cc compilation context
   * @param single operands must yield single values
   * @return general comparison or original expression
   * @throws QueryException query exception
   */
  private Expr toGeneral(final CompileContext cc, final boolean single) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    final Predicate<SeqType> p = st -> single ? st.one() : st.zeroOrOne();
    if(p.test(st1) && p.test(st2) && CmpG.compatible(st1, st2, op)) {
      return new CmpG(info, expr1, expr2, op).optimize(cc);
    }
    return this;
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return Empty.VALUE;
    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2 == Empty.VALUE) return Empty.VALUE;
    return Bln.get(test(item1, item2));
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info);
    if(item1 == Empty.VALUE) return false;
    final Item item2 = exprs[1].atomItem(qc, info);
    if(item2 == Empty.VALUE) return false;
    return test(item1, item2);
  }

  /**
   * Performs the test.
   * @param item1 first item
   * @param item2 second item
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean test(final Item item1, final Item item2) throws QueryException {
    if(item1.comparable(item2)) return op.eval(item1.compare(item2, null, false, info));
    throw compareError(item1, item2, info);
  }

  @Override
  public Expr invert() {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    return st1.one() && !st1.mayBeArray() && st2.one() && !st2.mayBeArray() ?
      new CmpV(info, expr1, expr2, op.invert()) : null;
  }

  @Override
  public CmpOp cmpOp() {
    return op;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CmpV(info, exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final CmpV cmp && op == cmp.op && super.equals(obj);
  }

  @Override
  public String description() {
    return "'" + op.toValueString() + "' comparison";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, OP, op.toValueString()), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.tokens(exprs, " " + op.toValueString() + ' ', true);
  }
}
