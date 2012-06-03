package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * Node comparison.
 *
 * @author BaseX Team 2005-12, BSD License
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
    super(ii, e1, e2);
    op = o;
    type = SeqType.BLN_ZO;
  }

  @Override
  public Expr compile(final QueryContext ctx) throws QueryException {
    super.compile(ctx);
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
    throw Util.notexpected();
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
