package org.basex.query.gflwor;

import static org.basex.query.QueryText.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.GFLWOR.Eval;
import org.basex.query.iter.Iter;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.Scoring;
import org.basex.util.list.*;

/**
 * FLWOR {@code let} clause, binding an expression to a variable.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public class Let extends GFLWOR.Clause {
  /** Variable. */
  public final Var var;
  /** Bound expression. */
  public Expr expr;
  /** Score flag. */
  final boolean score;

  /**
   * Constructor.
   * @param v variable
   * @param e expression
   * @param scr score flag
   * @param ii input info
   */
  public Let(final Var v, final Expr e, final boolean scr, final InputInfo ii) {
    super(ii, v);
    var = v;
    expr = e;
    score = scr;
  }

  @Override
  Eval eval(final Eval sub) {
    return new Eval() {
      @Override
      public boolean next(final QueryContext ctx) throws QueryException {
        if(!sub.next(ctx)) return false;
        ctx.set(var, score ? score(expr.iter(ctx)) : ctx.value(expr), info);
        return true;
      }
    };
  }

  /**
   * Calculates the score of the given iterator.
   * @param iter iterator
   * @return score
   * @throws QueryException evaluation exception
   */
  static Dbl score(final Iter iter) throws QueryException {
    double sum = 0;
    int sz = 0;
    for(Item it; (it = iter.next()) != null; sum += it.score(), sz++);
    return Dbl.get(Scoring.let(sum, sz));
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

  @Override
  public boolean uses(final Use u) {
    return u == Use.VAR || expr.uses(u);
  }

  @Override
  public Let compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    expr = expr.compile(ctx, scp);
    type = score ? SeqType.DBL : expr.type();
    var.refineType(type, info);
    size = score ? 1 : expr.size();
    return this;
  }

  /**
   * Binds the the value of this let clause to the context if it is statically known.
   * @param ctx query context
   * @throws QueryException evaluation exception
   */
  void bindConst(final QueryContext ctx) throws QueryException {
    if(expr.isValue()) ctx.set(var, score ? score(expr.iter(ctx)) : (Value) expr, info);
  }

  @Override
  public boolean removable(final Var v) {
    return expr.removable(v);
  }

  @Override
  public Let remove(final Var v) {
    expr = expr.remove(v);
    return this;
  }

  @Override
  public boolean visitVars(final VarVisitor visitor) {
    return expr.visitVars(visitor) && visitor.declared(var);
  }

  @Override
  public void checkUp() throws QueryException {
    expr.checkUp();
  }

  @Override
  public boolean databases(final StringList db) {
    return expr.databases(db);
  }
}
