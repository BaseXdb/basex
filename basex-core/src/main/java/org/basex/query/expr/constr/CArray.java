package org.basex.query.expr.constr;

import static org.basex.query.QueryText.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.value.array.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Array constructor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CArray extends Arr {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param exprs array expressions
   */
  public CArray(final InputInfo info, final Expr... exprs) {
    super(info, Types.ARRAY_O, exprs);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // [ E ]  ->  util:array-member(E)
    final int el = exprs.length;
    if(el == 0) return XQArray.empty();
    if(el == 1) return cc.replaceWith(this, cc.function(_UTIL_ARRAY_MEMBER, info, exprs));

    // if possible, rewrite to curly array constructor
    // [ 1, 2, 3 ]  ->  array { 1, 2, 3 }
    if(((Checks<Expr>) expr -> expr.seqType().one()).all(exprs)) {
      return cc.replaceWith(this, new CItemArray(info, List.get(cc, info, exprs)).optimize(cc));
    }

    exprType.assign(ArrayType.get(SeqType.union(exprs, true)));
    return values(false, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ArrayBuilder ab = new ArrayBuilder(qc);
    for(final Expr expr : exprs) ab.add(expr.value(qc));
    return ab.array(this);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    // data([ (1, 2) ])  ->  data((1, 2))
    if(mode.oneOf(Simplify.NUMBER, Simplify.DATA)) expr = List.get(cc, info, simplifyAll(mode, cc));
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CArray(info, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CArray && super.equals(obj);
  }

  @Override
  public String description() {
    return "squared " + ARRAY + " constructor";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token("[ ").tokens(exprs, SEP).token(" ]");
  }
}
