package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Some/Every satisfier clause.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class Quantifier extends Single {
  /** Every flag. */
  private final boolean every;

  /**
   * Constructor.
   * @param info input info
   * @param inputs variable inputs
   * @param satisfier satisfier
   * @param every every flag
   * @param sc static context
   */
  public Quantifier(final InputInfo info, final For[] inputs, final Expr satisfier,
      final boolean every, final StaticContext sc) {
    this(info, new GFLWOR(info, new LinkedList<>(Arrays.asList(inputs)),
        Function.BOOLEAN.get(sc, info, satisfier)), every);
  }

  /**
   * Copy constructor.
   * @param info input info
   * @param expr test expression
   * @param every every flag
   */
  private Quantifier(final InputInfo info, final Expr expr, final boolean every) {
    super(info, expr, SeqType.BLN_O);
    this.every = every;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // return pre-evaluated result
    if(expr instanceof Value) return cc.preEval(this);
    // non-deterministic expression: rewrite to list (ensure evaluation)
    if(expr.size() == 0) {
      return cc.replaceWith(this, new List(info, expr, Bln.get(every)).optimize(cc));
    }
    return this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = expr.iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      // resulting item will always be boolean (no EBV check required)
      if(every ^ item.bool(info)) return Bln.get(!every);
    }
    return Bln.get(every);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new Quantifier(info, expr.copy(cc, vm), every));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Quantifier && every == ((Quantifier) obj).every &&
        super.equals(obj);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, OPTION, every ? EVERY : SOME), expr);
  }

  @Override
  public void plan(final QueryString qs) {
    final String arg = new QueryString().token('(').token(expr).token(')').token("=").
        token(every ? Bln.FALSE : Bln.TRUE).toString();
    if(every) {
      qs.function(Function.NOT, ' ' + arg);
    } else {
      qs.token(arg);
    }
  }
}
