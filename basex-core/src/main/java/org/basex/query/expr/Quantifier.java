package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Some/Every satisfier clause.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class Quantifier extends Single {
  /** Every flag. */
  private final boolean every;

  /**
   * Constructor.
   * @param info input info
   * @param inputs variable inputs
   * @param expr satisfier
   * @param every every flag
   * @param sc static context
   */
  public Quantifier(final InputInfo info, final For[] inputs, final Expr expr,
      final boolean every, final StaticContext sc) {
    this(info, new GFLWOR(info, new LinkedList<>(Arrays.asList(inputs)),
        compBln(expr, info, sc)), every);
  }

  /**
   * Copy constructor.
   * @param info input info
   * @param tests expression
   * @param every every flag
   */
  private Quantifier(final InputInfo info, final Expr tests, final boolean every) {
    super(info, tests);
    this.every = every;
    seqType = SeqType.BLN;
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    return super.compile(cc).optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // return pre-evaluated result
    if(expr.isValue()) return cc.preEval(this);

    // pre-evaluate satisfy clause if it is a value
    if(expr instanceof GFLWOR && !expr.has(Flag.NDT) && !expr.has(Flag.UPD)) {
      final GFLWOR gflwor = (GFLWOR) expr;
      if(gflwor.size() > 0 && gflwor.ret.isValue()) {
        final Value value = (Value) gflwor.ret;
        return cc.replaceWith(value, Bln.get(value.ebv(cc.qc, info).bool(info)));
      }
    }
    return this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = qc.iter(expr);
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      if(every ^ it.ebv(qc, info).bool(info)) return Bln.get(!every);
    }
    return Bln.get(every);
  }

  @Override
  public int exprSize() {
    return expr.exprSize();
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new Quantifier(info, expr.copy(cc, vm), every);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Quantifier && every == ((Quantifier) obj).every &&
        super.equals(obj);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(TYP, every ? EVERY : SOME), expr);
  }

  @Override
  public String toString() {
    return (every ? EVERY : SOME) + '(' + expr + ')';
  }
}
