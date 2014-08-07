package org.basex.query.gflwor;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.gflwor.GFLWOR.Clause;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * FLWOR {@code let} clause, binding an expression to a variable.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Let extends ForLet {
  /** Score flag. */
  private final boolean score;

  /**
   * Constructor.
   * @param var variable
   * @param expr expression
   * @param score score flag
   * @param info input info
   */
  public Let(final Var var, final Expr expr, final boolean score, final InputInfo info) {
    super(info, var, expr, var);
    this.score = score;
  }

  /**
   * Creates a let expression from a for loop over a single item.
   * @param fr for loop
   * @return let binding
   */
  static Let fromFor(final For fr) {
    final Let lt = new Let(fr.var, fr.expr, false, fr.info);
    lt.seqType = fr.expr.seqType();
    return lt;
  }

  /**
   * Creates a let binding for the score variable of a for clause.
   * @param fr for clause
   * @return let binding for the score variable
   */
  static Let fromForScore(final For fr) {
    final Expr varRef = new VarRef(fr.info, fr.var);
    return new Let(fr.score, varRef, true, fr.info);
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
   * @return score
   * @throws QueryException evaluation exception
   */
  private static Dbl score(final Iter iter) throws QueryException {
    double sum = 0;
    int sz = 0;
    for(Item it; (it = iter.next()) != null; sum += it.score(), sz++);
    return Dbl.get(sum / sz);
  }

  @Override
  public Clause compile(final QueryContext qc, final VarScope scp) throws QueryException {
    var.refineType(score ? SeqType.DBL : expr.seqType(), qc, info);
    return super.compile(qc, scp);
  }

  @Override
  public Let optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    if(!score && expr instanceof TypeCheck) {
      final TypeCheck tc = (TypeCheck) expr;
      if(tc.isRedundant(var) || var.adoptCheck(tc.seqType(), tc.promote)) {
        qc.compInfo(OPTCAST, tc.seqType());
        expr = tc.expr;
      }
    }

    seqType = score ? SeqType.DBL : expr.seqType();
    var.refineType(seqType, qc, info);
    if(var.checksType() && expr.isValue()) {
      expr = var.checkType((Value) expr, qc, info, true);
      var.refineType(expr.seqType(), qc, info);
    }
    size = score ? 1 : expr.size();
    var.size = size;
    var.data = expr.data();
    return this;
  }

  @Override
  public Let copy(final QueryContext qc, final VarScope scp, final IntObjMap<Var> vs) {
    final Var v = scp.newCopyOf(qc, var);
    vs.put(var.id, v);
    return new Let(v, expr.copy(qc, scp, vs), score, info);
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
   * @param qc query context
   * @param scp variable scope
   * @return inlineable expression
   * @throws QueryException query exception
   */
  public Expr inlineExpr(final QueryContext qc, final VarScope scp) throws QueryException {
    return score ? Function._FT_SCORE.get(null, info, expr).optimize(qc, scp)
                 : var.checked(expr, qc, scp, info);
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem();
    if(score) e.add(planAttr(Token.token(SCORE), Token.TRUE));
    var.plan(e);
    expr.plan(e);
    plan.add(e);
  }

  @Override
  public String toString() {
    return LET + ' ' + (score ? SCORE + ' ' : "") + var + ' ' + ASSIGN + ' ' + expr;
  }

  /** Evaluator for a block of {@code let} expressions. */
  private static class LetEval implements Eval {
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
    public boolean next(final QueryContext qc) throws QueryException {
      if(!sub.next(qc)) return false;
      for(final Let let : lets)
        qc.set(let.var, let.score ? score(let.expr.iter(qc)) : qc.value(let.expr), let.info);
      return true;
    }
  }
}
