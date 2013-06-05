package org.basex.query.func;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.gflwor.*;
import org.basex.query.iter.*;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class StaticFuncCall extends Arr {
  /** Static context of this function call. */
  protected final StaticContext sc;
  /** Function name. */
  final QNm name;
  /** Function reference. */
  StaticFunc func;

  /**
   * Function constructor.
   * @param ii input info
   * @param nm function name
   * @param arg arguments
   * @param sctx static context
   */
  StaticFuncCall(final QNm nm, final Expr[] arg, final StaticContext sctx,
      final InputInfo ii) {
    super(ii, arg);
    sc = sctx;
    name = nm;
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    super.compile(ctx, scp);

    // disallow call of private functions from module with different uri
    if(func.ann.contains(Ann.Q_PRIVATE) && !Token.eq(func.sc.baseURI().string(),
        ctx.sc.baseURI().string())) throw Err.FUNCPRIV.thrw(info, name.string());

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
        cls.add(new Let(v, expr[i], false, func.info).optimize(ctx, scp));
      }

      // copy the function body
      final Expr cpy = func.expr.copy(ctx, scp, vs), rt = !func.cast ? cpy :
            new TypeCheck(func.info, cpy, func.declType, true).optimize(ctx, scp);

      return cls == null ? rt : new GFLWOR(func.info, cls, rt).optimize(ctx, scp);
    }
    type = func.retType();
    return this;
  }

  @Override
  public final Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public final BaseFuncCall copy(final QueryContext ctx, final VarScope scp,
      final IntObjMap<Var> vs) {
    final Expr[] arg = new Expr[expr.length];
    for(int i = 0; i < arg.length; i++) arg[i] = expr[i].copy(ctx, scp, vs);
    final BaseFuncCall call = new BaseFuncCall(name, arg, sc, info);
    call.func = func;
    call.type = type;
    call.size = size;
    return call;
  }

  /**
   * Evaluates all function arguments.
   * @param ctx query context
   * @return argument values
   * @throws QueryException query exception
   */
  Value[] args(final QueryContext ctx) throws QueryException {
    final int al = expr.length;
    final Value[] args = new Value[al];
    // evaluate arguments
    for(int a = 0; a < al; ++a) args[a] = expr[a].value(ctx);
    return args;
  }

  /**
   * Initializes the function and checks for visibility.
   * @param f function reference
   * @throws QueryException query exception
   */
  public void init(final StaticFunc f) throws QueryException {
    func = f;
    if(f.ann.contains(Ann.Q_PRIVATE) && !Token.eq(sc.baseURI().string(),
        f.sc.baseURI().string())) throw Err.FUNCPRIV.thrw(info, f.name.string());
  }

  /**
   * Getter for the called function.
   * @return user-defined function
   */
  public final StaticFunc func() {
    return func;
  }

  @Override
  public final boolean isVacuous() {
    return func != null && func.isVacuous();
  }

  @Override
  public boolean uses(final Use u) {
    // check arguments, which will be evaluated before running the function code
    if(super.uses(u)) return true;
    // function code: position or context references will have no effect on calling code
    if(u == Use.POS || u == Use.CTX) return false;
    // pass on check to function code
    return func == null || (u == Use.UPD ? func.updating : func.uses(u));
  }

  @Override
  public final void plan(final FElem plan) {
    addPlan(plan, planElem(NAM, this), expr);
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

  /**
   * A continuation that's thrown to free stack frames.
   * @author Leo Woerteler
   */
  final class Continuation extends RuntimeException {
    /** Arguments. */
    private final Value[] args;

    /**
     * Constructor.
     * @param arg arguments
     */
    Continuation(final Value[] arg) {
      args = arg;
    }

    /**
     * Getter for the continuation function.
     * @return the next function to call
     */
    StaticFunc getFunc() {
      return func;
    }

    /**
     * Getter for the function arguments.
     * @return the next function call's arguments
     */
    Value[] getArgs() {
      return args;
    }

    @Override
    public synchronized Continuation fillInStackTrace() {
      // ignore this for efficiency reasons
      return this;
    }
  }
}
