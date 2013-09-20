package org.basex.query.func;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.*;
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
        sc.baseURI().string())) throw Err.FUNCPRIV.thrw(info, name.string());

    // compile mutually recursive functions
    func.compile(ctx);

    if(func.inline(ctx)) {
      // inline the function
      ctx.compInfo(OPTINLINEFN, func.name);

      // create let bindings for all variables
      final LinkedList<GFLWOR.Clause> cls = expr.length == 0 ? null :
        new LinkedList<GFLWOR.Clause>();
      final IntObjMap<Var> vs = new IntObjMap<Var>();
      for(int i = 0; i < func.args.length; i++) {
        final Var old = func.args[i], v = scp.newCopyOf(ctx, old);
        vs.put(old.id, v);
        cls.add(new Let(v, old.checked(expr[i], ctx, scp, info),
            false, func.info).optimize(ctx, scp));
      }

      // copy the function body
      final Expr cpy = func.expr.copy(ctx, scp, vs),
          rt = !func.cast ? cpy : new TypeCheck(sc, func.info, cpy, func.declType,
              true).optimize(ctx, scp);

      return cls == null ? rt : new GFLWOR(func.info, cls, rt).optimize(ctx, scp);
    }
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
        f.sc.baseURI().string())) throw Err.FUNCPRIV.thrw(info, f.name.string());
    return this;
  }

  /**
   * Returns the called function if already known, {@code false} otherwise.
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
    addPlan(plan, planElem(NAM, this, TCL, tailCall), expr);
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
    return visitor.funcCall(this) && super.accept(visitor);
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
