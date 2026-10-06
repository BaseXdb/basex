package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code let} clause, binding an expression to a variable.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class Let extends ForLet {
  /**
   * Constructor.
   * @param var variable
   * @param expr expression
   */
  public Let(final Var var, final Expr expr) {
    this(var, expr, false);
  }

  /**
   * Constructor.
   * @param var variable
   * @param expr expression
   * @param scoring scoring flag
   */
  public Let(final Var var, final Expr expr, final boolean scoring) {
    super(var.info, scoring ? Types.DOUBLE_O : Types.ITEM_ZM, var, expr, scoring, var);
  }

  /**
   * Binds the value of the expression to the variable.
   * @param qc query context
   * @throws QueryException query exception
   */
  void bind(final QueryContext qc) throws QueryException {
    qc.set(var, scoring ? score(qc) : expr.value(qc));
  }

  /**
   * Calculates the score of the expression.
   * @param qc query context
   * @return score
   * @throws QueryException query exception
   */
  private Value score(final QueryContext qc) throws QueryException {
    final boolean scrng = qc.scoring;
    try {
      qc.scoring = true;
      double s = 0;
      int c = 0;
      final Iter iter = expr.iter(qc);
      for(Item item; (item = qc.next(iter)) != null; c++) {
        s += item.score();
      }
      return Dbl.get(Scoring.avg(s, c));
    } finally {
      qc.scoring = scrng;
    }
  }

  @Override
  LetEval eval(final Eval sub) {
    if(sub instanceof final LetEval eval) {
      eval.add(this);
      return eval;
    }
    return new LetEval(this, sub);
  }

  /**
   * Converts the let binding to a for loop.
   * @param cc compilation context
   * @return for expression
   * @throws QueryException query exception
   */
  For toFor(final CompileContext cc) throws QueryException {
    return new For(var, scoring ? cc.function(Function._FT_SCORE, info(), expr) : expr);
  }

  @Override
  public Let optimize(final CompileContext cc) throws QueryException {
    // skip redundant type check
    if(!scoring && expr instanceof final TypeCheck tc) {
      if(var.declType != null && var.declType.instanceOf(expr.seqType(), true) ||
          var.adoptCheck(expr.seqType())) {
        cc.info(OPTTYPE_X, this);
        expr = tc.expr;
      }
    }
    // coerce at compile time
    if(expr instanceof final Value value) {
      expr = var.checkType(value, cc.qc, cc);
    } else if(!scoring && var.declType != null &&
        !expr.seqType().instanceOf(var.declType, true)) {
      // push type check into the expression: { 'a': 1 } coerce to local:r → local:r(1)
      final Expr checked = var.checked(expr, cc);
      if(!(checked instanceof TypeCheck)) expr = checked;
    }

    // assign type to clause and variable
    if(scoring) {
      var.expr(Dbl.ZERO);
    } else {
      adoptType(expr);
      var.expr(expr);
    }
    var.refineType(seqType(), size(), cc);
    return this;
  }

  @Override
  public Let copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new Let(cc.copy(var, vm), expr.copy(cc, vm), scoring));
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && visitor.declared(var);
  }

  @Override
  Expr inlineExpr(final CompileContext cc) throws QueryException {
    return scoring ? null : var.checked(expr, cc);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Let && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    final FBuilder elem = plan.attachVariable(plan.create(this), var, false);
    if(scoring) plan.addAttribute(elem, SCORE, true);
    plan.add(elem, expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(LET);
    if(scoring) qs.token(SCORE);
    qs.token(var).token(":=").token(expr);
  }

  /** Evaluator for a block of {@code let} expressions. */
  private static final class LetEval extends Eval {
    /** Let expressions of the current block, in declaration order. */
    private Let[] lets;
    /** Number of let expressions. */
    private int size = 1;
    /** Sub-evaluator. */
    private final Eval sub;

    /**
     * Constructor for the first let binding in the block.
     * @param let first let binding
     * @param sub sub-evaluator
     */
    LetEval(final Let let, final Eval sub) {
      lets = new Let[] { let };
      this.sub = sub;
    }

    /**
     * Appends a let binding to the block.
     * @param let let binding
     */
    void add(final Let let) {
      if(size == lets.length) lets = Arrays.copyOf(lets, Array.newCapacity(size));
      lets[size++] = let;
    }

    @Override
    boolean next(final QueryContext qc) throws QueryException {
      if(!sub.next(qc)) return false;
      for(int l = 0; l < size; l++) lets[l].bind(qc);
      return true;
    }
  }
}
