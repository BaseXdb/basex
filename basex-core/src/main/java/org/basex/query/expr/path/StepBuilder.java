package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.util.*;

/**
 * Constructor for a new axis step.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class StepBuilder {
  /** Input info. */
  private final InputInfo info;
  /** Kind test. Default: {@link KindTest#NOD}. */
  private Test t;
  /** Axis. Default: {@link Axis#SELF}. */
  private Axis a;
  /** Predicates (can be {@code null}). */
  private Expr[] e;

  /**
   * Constructor.
   * @param info input info
   */
  public StepBuilder(final InputInfo info) {
    this.info = info;
    t = KindTest.NOD;
    a = Axis.SELF;
  }

  /**
   * Assigns a test.
   * @param test test
   * @return builder
   */
  public StepBuilder test(final Test test) {
    t = test;
    return this;
  }

  /**
   * Assigns an axis.
   * @param axis axis.
   * @return builder
   */
  public StepBuilder axis(final Axis axis) {
    a = axis;
    return this;
  }

  /**
   * Assigns predicates.
   * @param exprs predicates
   * @return builder
   */
  public StepBuilder preds(final Expr... exprs) {
    e = exprs;
    return this;
  }

  /**
   * Returns a new and optimized expression for this step.
   * @param cc compilation context
   * @param expr context expression
   * @return expression
   * @throws QueryException query exception
   */
  public Expr finish(final CompileContext cc, final Expr expr) throws QueryException {
    final Expr ex = expr != null ? expr : cc.qc.focus.value;
    return Step.get(info, a, t, e != null ? e : new Expr[0]).optimize(ex, cc);
  }
}
