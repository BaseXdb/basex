package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.expr.CmpV.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CmpN extends Cmp {
  /** Comparators. */
  public enum OpN {
    /** Node comparison: same. */
    EQ("is") {
      @Override
      public boolean eval(final ANode node1, final ANode node2) {
        return node1.is(node2);
      }
    },

    /** Node comparison: before. */
    ET("<<") {
      @Override
      public boolean eval(final ANode node1, final ANode node2) {
        return node1.diff(node2) < 0;
      }
    },

    /** Node comparison: after. */
    GT(">>") {
      @Override
      public boolean eval(final ANode node1, final ANode node2) {
        return node1.diff(node2) > 0;
      }
    };

    /** Cached enums (faster). */
    public static final OpN[] VALUES = values();
    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param name string representation
     */
    OpN(final String name) {
      this.name = name;
    }

    /**
     * Evaluates the expression.
     * @param node1 first node
     * @param node2 second node
     * @return result
     */
    public abstract boolean eval(ANode node1, ANode node2);

    @Override
    public String toString() {
      return name;
    }
  }

  /** Comparator. */
  private final OpN op;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op comparator
   * @param info input info
   */
  public CmpN(final Expr expr1, final Expr expr2, final OpN op, final InputInfo info) {
    super(info, expr1, expr2, null, SeqType.BOOLEAN_ZO, null);
    this.op = op;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.oneOrMore() && st2.oneOrMore()) exprType.assign(Occ.EXACTLY_ONE);

    final Expr expr = emptyExpr();
    return expr == this && allAreValues(false) ? cc.preEval(this) : cc.replaceWith(this, expr);
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
  public Expr invert() {
    return null;
  }

  @Override
  public OpV opV() {
    return null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CmpN(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, info));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof CmpN && op == ((CmpN) obj).op && super.equals(obj);
  }

  @Override
  public String description() {
    return "'" + op + "' comparison";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, OP, op.name), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.tokens(exprs, " " + op + ' ', true);
  }
}
