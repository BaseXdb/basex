package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code let} clause, binding an expression to a variable.
 *
 * @author BaseX Team 2005-21, BSD License
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
    super(var.info, scoring ? SeqType.DOUBLE_O : SeqType.ITEM_ZM, var, expr, scoring, var);
  }

  /**
   * Creates a let expression from a for loop over a single item.
   * @param fr for loop
   * @param cc compilation context
   * @return let binding
   * @throws QueryException query exception
   */
  static Let fromFor(final For fr, final CompileContext cc) throws QueryException {
    return new Let(fr.var, fr.expr).optimize(cc);
  }

  /**
   * Creates a let binding for the score variable of a for clause.
   * @param fr for clause
   * @param cc compilation context
   * @return let binding for the score variable
   * @throws QueryException query exception
   */
  static Let fromForScore(final For fr, final CompileContext cc) throws QueryException {
    return new Let(fr.score, new VarRef(fr.info, fr.var).optimize(cc), true).optimize(cc);
  }

  @Override
  LetEval eval(final Eval sub) {
    if(!(sub instanceof LetEval)) return new LetEval(this, sub);
    final LetEval eval = (LetEval) sub;
    eval.lets.add(this);
    return eval;
  }

  /**
   * Calculates the score of the given iterator.
   * @param iter iterator
   * @param qc query context
   * @return score
   * @throws QueryException evaluation exception
   */
  private static Dbl score(final Iter iter, final QueryContext qc) throws QueryException {
    double score = 0;
    int c = 0;
    for(Item item; (item = qc.next(iter)) != null; score += item.score(), c++);
    return Dbl.get(Scoring.avg(score, c));
  }

  @Override
  public Let optimize(final CompileContext cc) throws QueryException {
    // skip redundant type check
    if(!scoring && expr instanceof TypeCheck) {
      final TypeCheck tc = (TypeCheck) expr;
      if(tc.isRedundant(var) || var.adoptCheck(tc.seqType(), tc.promote)) {
        cc.info(OPTTYPE_X, this);
        expr = tc.expr;
      }
    }
    // promote at compile time
    if(expr instanceof Value) {
      expr = var.checkType((Value) expr, cc.qc, true);
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
  public Let copy(final CompileContext cc, final IntObjMap<Var> vm) {
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
  public void plan(final QueryPlan plan) {
    final FElem elem = plan.attachVariable(plan.create(this), var, false);
    if(scoring) plan.addAttribute(elem, SCORE, true);
    plan.add(elem, expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(LET);
    if(scoring) qs.token(SCORE);
    qs.token(var).token(ASSIGN).token(expr);
  }

  /** Evaluator for a block of {@code let} expressions. */
  private static class LetEval extends Eval {
    /** Let expressions of the current block, in declaration order. */
    private final ArrayList<Let> lets;
    /** Sub-evaluator. */
    private final Eval sub;

    /**
     * Constructor for the first let binding in the block.
     * @param let first let binding
     * @param sub sub-evaluator
     */
    LetEval(final Let let, final Eval sub) {
      lets = new ArrayList<>();
      lets.add(let);
      this.sub = sub;
    }

    @Override
    boolean next(final QueryContext qc) throws QueryException {
      if(!sub.next(qc)) return false;

      for(final Let let : lets) {
        final Value vl;
        if(let.scoring) {
          final boolean s = qc.scoring;
          try {
            qc.scoring = true;
            vl = score(let.expr.iter(qc), qc);
          } finally {
            qc.scoring = s;
          }
        } else {
          vl = let.expr.value(qc);
        }
        qc.set(let.var, vl);
      }
      return true;
    }
  }
}
