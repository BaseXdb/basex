package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function call for user-defined functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class StaticFuncCall extends FuncCall {
  /** Static context of this function call. */
  private final StaticContext sc;
  /** Function name. */
  final QNm name;
  /** Function reference. */
  StaticFunc func;

  /**
   * Function call constructor.
   * @param ii input info
   * @param nm function name
   * @param arg arguments
   * @param sctx static context
   */
  public StaticFuncCall(final QNm nm, final Expr[] arg, final StaticContext sctx,
      final InputInfo ii) {
    this(nm, arg, sctx, null, false, ii);
  }

  /**
   * Copy constructor.
   * @param ii input info
   * @param nm function name
   * @param arg arguments
   * @param sctx static context
   * @param fun referenced function
   * @param tail tail-call flag
   */
  private StaticFuncCall(final QNm nm, final Expr[] arg, final StaticContext sctx,
      final StaticFunc fun, final boolean tail, final InputInfo ii) {
    super(ii, arg);
    sc = sctx;
    name = nm;
    func = fun;
    tailCall = tail;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);

    // disallow call of private functions from module with different uri
    if(func.ann.contains(Ann.Q_PRIVATE) && !Token.eq(func.sc.baseURI().string(),
        sc.baseURI().string())) throw FUNCPRIV.get(info, name.string());

    // compile mutually recursive functions
    func.compile(ctx);

    // try to inline the function
    final Expr inl = func.inlineExpr(expr, ctx, scp, info);
    if(inl != null) return inl;
    type = func.type();
    return this;
  }

  @Override
  public StaticFuncCall copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Expr[] arg = new Expr[expr.length];
    for(int i = 0; i < arg.length; i++) arg[i] = expr[i].copy(ctx, scp, vs);
    final StaticFuncCall call = new StaticFuncCall(name, arg, sc, func, false, info);
    call.type = type;
    call.size = size;
    return call;
  }

  /**
   * Initializes the function and checks for visibility.
   * @param f function reference
   * @return self reference
   * @throws QueryException query exception
   */
  public StaticFuncCall init(final StaticFunc f) throws QueryException {
    func = f;
    if(f.ann.contains(Ann.Q_PRIVATE) && !Token.eq(sc.baseURI().string(),
        f.sc.baseURI().string())) throw FUNCPRIV.get(info, f.name.string());
    return this;
  }

  /**
   * Returns the called function if already known, {@code null} otherwise.
   * @return the function
   */
  public StaticFunc func() {
    return func;
  }

  @Override
  public boolean isVacuous() {
    return func != null && func.isVacuous();
  }

  @Override
  public boolean has(final Flag flag) {
    // check arguments, which will be evaluated before running the function code
    if(super.has(flag)) return true;
    // function code: position or context references will have no effect on calling code
    if(flag == Flag.FCS || flag == Flag.CTX) return false;
    // pass on check to function code
    return func == null || (flag == Flag.UPD ? func.updating : func.has(flag));
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, name, TCL, tailCall), expr);
  }

  @Override
  public String description() {
    return FUNC;
  }

  @Override
  public String toString() {
    return new TokenBuilder(name.string()).add(toString(SEP)).toString();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.staticFuncCall(this) && super.accept(visitor);
  }

  @Override
  public StaticFunc evalFunc(final QueryContext ctx) {
    return func;
  }

  @Override
  Value[] evalArgs(final QueryContext ctx) throws QueryException {
    final int al = expr.length;
    final Value[] args = new Value[al];
    for(int a = 0; a < al; ++a) args[a] = ctx.value(expr[a]);
    return args;
  }
}
