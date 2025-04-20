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
  /** Square array constructor. */
  private boolean square;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param square square array constructor
   * @param exprs array expressions
   */
  public CArray(final InputInfo info, final boolean square, final Expr... exprs) {
    super(info, SeqType.ARRAY_O, exprs);
    this.square = square;
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // if possible, rewrite to curly array constructor
    // [ 1, 2, 3 ]  ->  array { 1, 2, 3 }
    if(square && ((Checks<Expr>) expr -> expr.seqType().one()).all(exprs)) {
      square = false;
    }
    // curly array constructor: simplify to single expression
    // array { (1, 2), 3 ]  ->  array { (1, 2, 3) }
    if(!square && exprs.length > 1) {
      exprs = new Expr[] { List.get(cc, info, exprs) };
    }

    // [ $value ], array { $item }  ->  util:array-member(...)
    final int el = exprs.length;
    if(el == 0) return XQArray.empty();
    if(el == 1 && (square || exprs[0].size() == 1)) {
      return cc.replaceWith(this, cc.function(_UTIL_ARRAY_MEMBER, info, exprs));
    }

    SeqType mt = null;
    if(square) {
      mt = SeqType.union(exprs, true);
    } else {
      for(final Expr expr : exprs) {
        final SeqType st = expr.seqType().with(Occ.EXACTLY_ONE);
        mt = mt == null ? st : mt.union(st);
      }
    }
    if(mt != null) exprType.assign(ArrayType.get(mt));

    return values(false, cc) ? cc.preEval(this) : this;
  }

  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    if(square) {
      final ArrayBuilder ab = new ArrayBuilder(qc);
      for(final Expr expr : exprs) ab.add(expr.value(qc));
      return ab.array(this);
    }
    // curly array constructor (after optimization, members are contained in a single expression)
    return XQArray.items(exprs[0].value(qc));
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    Expr expr = this;
    if(mode.oneOf(Simplify.STRING, Simplify.NUMBER, Simplify.DATA)) {
      expr = List.get(cc, info, simplifyAll(mode, cc));
    }
    return cc.simplify(this, expr, mode);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CArray(info, square, copyAll(cc, vm, exprs)));
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof CArray && square == ((CArray) obj).square && super.equals(obj);
  }

  @Override
  public String description() {
    return (square ? "squared" : "curly") + ' ' + ARRAY + " constructor";
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(square ? "[ " : ARRAY + " { ").tokens(exprs, SEP).token(square ? " ]" : " }");
  }
}
