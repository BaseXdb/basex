package org.basex.query.expr.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.GFLWOR.*;
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
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class Let extends ForLet {
  /**
   * Constructor.
   * @param var variable
   * @param expr expression
   * @param scoring scoring flag
   */
  public Let(final Var var, final Expr expr, final boolean scoring) {
    super(var.info, scoring ? SeqType.DBL : SeqType.ITEM_ZM, var, expr, scoring, var);
  }

  /**
   * Creates a let expression from a for loop over a single item.
   * @param fr for loop
   * @return let binding
   */
  static Let fromFor(final For fr) {
    final Let lt = new Let(fr.var, fr.expr, false);
    lt.adoptType(fr.expr);
    return lt;
  }

  /**
   * Creates a let binding for the score variable of a for clause.
   * @param fr for clause
   * @return let binding for the score variable
   */
  static Let fromForScore(final For fr) {
    final Expr varRef = new VarRef(fr.info, fr.var);
    return new Let(fr.score, varRef, true);
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
    double s = 0;
    int c = 0;
    for(Item it; (it = iter.next()) != null; s += it.score(), c++) {
      qc.checkStop();
    }
    return Dbl.get(Scoring.avg(s, c));
  }

  @Override
  public Let optimize(final CompileContext cc) throws QueryException {
    // skip redundant type check
    if(!scoring && expr instanceof TypeCheck) {
      final TypeCheck tc = (TypeCheck) expr;
      if(tc.isRedundant(var) || var.adoptCheck(tc.seqType(), tc.promote)) {
        cc.info(OPTTYPE_X, tc.seqType());
        expr = tc.expr;
      }
    }
    // promote at compile time
    if(expr.isValue() && var.checksType()) expr = var.checkType((Value) expr, cc.qc, true);

    // assign type to clause and variable
    if(!scoring) {
      adoptType(expr);
      var.data = expr.data();
    }
    var.refineType(seqType(), size(), cc);
    return this;
  }

  @Override
  public Let copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Let(cc.copy(var, vm), expr.copy(cc, vm), scoring);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return expr.accept(visitor) && visitor.declared(var);
  }

  @Override
  void calcSize(final long[] minMax) {
  }

  /**
   * Returns an expression that is appropriate for inlining.
   * @param cc compilation context
   * @return inlineable expression
   * @throws QueryException query exception
   */
  Expr inlineExpr(final CompileContext cc) throws QueryException {
    return scoring ? cc.function(Function._FT_SCORE, info, expr) : var.checked(expr, cc);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Let && super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    if(scoring) e.add(planAttr(Token.token(SCORE), Token.TRUE));
    var.plan(e);
    expr.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    return LET + ' ' + (scoring ? SCORE + ' ' : "") + var + ' ' + ASSIGN + ' ' + expr;
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
     * @param subEval sub-evaluator
     */
    LetEval(final Let let, final Eval subEval) {
      lets = new ArrayList<>();
      lets.add(let);
      sub = subEval;
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
            vl = score(qc.iter(let.expr), qc);
          } finally {
            qc.scoring = s;
          }
        } else {
          vl = qc.value(let.expr);
        }
        qc.set(let.var, vl);
      }
      return true;
    }
  }
}
