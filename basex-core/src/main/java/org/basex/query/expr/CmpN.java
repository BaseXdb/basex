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
 * Node comparison.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CmpN extends Cmp {
  /** Comparators. */
  public enum OpN {
    /** Node comparison: same. */
    EQ("is") {
      @Override
      public boolean eval(final ANode a, final ANode b) {
        return a.is(b);
      }
    },

    /** Node comparison: before. */
    ET("<<") {
      @Override
      public boolean eval(final ANode a, final ANode b) {
        return a.diff(b) < 0;
      }
    },

    /** Node comparison: after. */
    GT(">>") {
      @Override
      public boolean eval(final ANode a, final ANode b) {
        return a.diff(b) > 0;
      }
    };

    /** Cached enums (faster). */
    public static final OpN[] VALUES = values();
    /** String representation. */
    public final String name;

    /**
     * Constructor.
     * @param n string representation
     */
    OpN(final String n) {
      name = n;
    }

    /**
     * Evaluates the expression.
     * @param a first node
     * @param b second node
     * @return result
     */
    public abstract boolean eval(ANode a, ANode b);

    @Override
    public String toString() {
      return name;
    }
  }

  /** Comparator. */
  private final OpN op;

  /**
   * Constructor.
   * @param e1 first expression
   * @param e2 second expression
   * @param o comparator
   * @param ii input info
   */
  public CmpN(final Expr e1, final Expr e2, final OpN o, final InputInfo ii) {
    super(ii, e1, e2, null);
    op = o;
    type = SeqType.BLN_ZO;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);
    return optimize(ctx, scp);
  }

  @Override
  public Expr optimize(final QueryContext ctx, final VarScope scp) throws QueryException {
    type = SeqType.get(AtomType.BLN, expr[0].size() == 1 && expr[1].size() == 1 ?
        Occ.ONE : Occ.ZERO_ONE);

    return optPre(oneIsEmpty() ? null : allAreValues() ? item(ctx, info) : this, ctx);
  }

  @Override
  public Bln item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final Item a = expr[0].item(ctx, info);
    if(a == null) return null;
    final Item b = expr[1].item(ctx, info);
    if(b == null) return null;
    return Bln.get(op.eval(checkNode(a), checkNode(b)));
  }

  @Override
  public CmpN invert() {
    throw Util.notExpected();
  }

  @Override
  public Expr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpN(expr[0].copy(ctx, scp, vs), expr[1].copy(ctx, scp, vs), op, info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, op.name), expr);
  }

  @Override
  public String description() {
    return "'" + op + "' operator";
  }

  @Override
  public String toString() {
    return toString(" " + op + ' ');
  }
}
