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
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op comparator
   * @param info input info
   */
  public CmpN(final Expr expr1, final Expr expr2, final OpN op, final InputInfo info) {
    super(info, expr1, expr2, null);
    this.op = op;
    type = SeqType.BLN_ZO;
  }

  @Override
  public Expr compile(final QueryContext qc, final VarScope scp) throws QueryException {
    super.compile(qc, scp);
    return optimize(qc, scp);
  }

  @Override
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    type = SeqType.get(AtomType.BLN, exprs[0].size() == 1 && exprs[1].size() == 1 ?
        Occ.ONE : Occ.ZERO_ONE);

    return optPre(oneIsEmpty() ? null : allAreValues() ? item(qc, info) : this, qc);
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item a = exprs[0].item(qc, info);
    if(a == null) return null;
    final Item b = exprs[1].item(qc, info);
    if(b == null) return null;
    return Bln.get(op.eval(checkNode(a), checkNode(b)));
  }

  @Override
  public CmpN invert() {
    throw Util.notExpected();
  }

  @Override
  public Expr copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    return new CmpN(exprs[0].copy(qc, scp, vs), exprs[1].copy(qc, scp, vs), op, info);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(OP, op.name), exprs);
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
