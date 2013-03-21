package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Use;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * A static User-defined function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class StaticFunc extends ExprInfo implements Scope, XQFunction {
  /** Input info. */
  public InputInfo info;
  /** Function name. */
  public final QNm name;
  /** Arguments. */
  public final Var[] args;
  /** Return type. */
  public final SeqType ret;
  /** Annotations. */
  public final Ann ann;
  /** Updating flag. */
  public final boolean updating;
  /** Declaration flag. */
  public final boolean declared;

  /** Map with requested function properties. */
  protected final EnumMap<Use, Boolean> map = new EnumMap<Expr.Use, Boolean>(Use.class);
  /** Static context. */
  private final StaticContext sc;
  /** Cast flag. */
  boolean cast;
  /** Compilation flag. */
  private boolean compiled;
  /** Flag that is turned on during compilation and prevents premature inlining. */
  private boolean compiling;
  /** Flag for avoiding loops in {@link #databases(org.basex.util.list.StringList)}. */
  private boolean dontEnter;

  /** Local variables in the scope of this function. */
  protected final VarScope scope;
  /** Function body. */
  Expr expr;

  /**
   * Function constructor.
   * @param ii input info
   * @param n function name
   * @param v arguments
   * @param r return type
   * @param a annotations
   * @param d declaration flag
   * @param stc static context
   * @param scp variable scope
   */
  public StaticFunc(final InputInfo ii, final QNm n, final Var[] v, final SeqType r,
      final Ann a, final boolean d, final StaticContext stc, final VarScope scp) {
    info = ii;
    name = n;
    args = v;
    ret = r;
    cast = r != null;
    ann = a == null ? new Ann() : a;
    updating = ann.contains(Ann.Q_UPDATING);
    scope = scp;
    sc = stc;
    declared = d;
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    if(compiled) return;
    compiling = compiled = true;
    final StaticContext tmp = ctx.sc;
    ctx.sc = sc;

    final int fp = scope.enter(ctx);
    try {
      expr = expr.compile(ctx, scope);
    } catch(final QueryException qe) {
      expr = FNInfo.error(qe, info);
    } finally {
      scope.cleanUp(this);
      scope.exit(ctx, fp);
      ctx.sc = tmp;
    }

    // convert all function calls in tail position to proper tail calls
    ctx.compInfo(OPTTCE, name);
    expr = expr.markTailCalls();

    if(ret != null) {
      // remove redundant casts
      if((ret.type == AtomType.BLN || ret.type == AtomType.FLT ||
          ret.type == AtomType.DBL || ret.type == AtomType.QNM ||
          ret.type == AtomType.URI) && ret.eq(expr.type())) {
        ctx.compInfo(OPTCAST, ret);
        cast = false;
      }
    }
    compiling = false;
  }

  /**
   * Checks if this function can be inlined.
   * @param ctx query context
   * @return result of check
   */
  boolean inline(final QueryContext ctx) {
    return expr.isValue() || expr.exprSize() < ctx.context.prop.num(Prop.INLINELIMIT) &&
        !(compiling || uses(Use.NDT) || uses(Use.CTX) || selfRecursive());
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(NAM, name.string());
    addPlan(plan, el, expr);
    for(int i = 0; i < args.length; ++i) {
      el.add(planAttr(ARG + i, args[i].name.string()));
    }
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(DECLARE).add(' ').addExt(ann);
    if(updating) tb.add(UPDATING).add(' ');
    tb.add(FUNCTION).add(' ').add(name.string());
    tb.add(PAR1).addSep(args, SEP).add(PAR2);
    if(ret != null) tb.add(' ' + AS + ' ' + ret);
    if(expr != null) tb.add(" { " + expr + " }; ");
    return tb.toString();
  }

  /**
   * Checks if this function calls itself recursively.
   * @return result of check
   */
  public boolean selfRecursive() {
    return !expr.accept(new ASTVisitor() {
      @Override
      public boolean funcCall(final StaticFuncCall call) {
        return call.func != StaticFunc.this;
      }

      @Override
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }
    });
  }

  /**
   * Gathers all databases accessed by this function
   * (see {@link Expr#databases(StringList)}).
   * @param db database list
   * @return {@code false} if all databases should be locked, {@code true} otherwise
   */
  public boolean databases(final StringList db) {
    if(dontEnter) return true;
    dontEnter = true;
    final boolean res = expr.databases(db);
    dontEnter = false;
    return res;
  }

  @Override
  public int arity() {
    return args.length;
  }

  @Override
  public QNm fName() {
    return name;
  }

  @Override
  public Item invItem(final QueryContext ctx, final InputInfo ii, final Value... arg)
      throws QueryException {

    // reset context and evaluate function
    final Value cv = ctx.value;
    final StaticContext tmp = ctx.sc;
    ctx.sc = sc;
    ctx.value = null;
    final int fp = addArgs(ctx, ii, arg);
    try {
      final Item it = expr.item(ctx, ii);
      final Value v = it == null ? Empty.SEQ : it;
      // optionally promote return value to target type
      return cast ? ret.funcConvert(ctx, ii, v).item(ctx, ii) : it;
    } finally {
      scope.exit(ctx, fp);
      ctx.value = cv;
      ctx.sc = tmp;
    }
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii, final Value... arg)
      throws QueryException {
    // reset context and evaluate function
    final Value cv = ctx.value;
    final StaticContext tmp = ctx.sc;
    ctx.sc = sc;
    ctx.value = null;
    final int fp = addArgs(ctx, ii, arg);
    try {
      final Value v = ctx.value(expr);
      // optionally promote return value to target type
      return cast ? ret.funcConvert(ctx, info, v) : v;
    } finally {
      scope.exit(ctx, fp);
      ctx.value = cv;
      ctx.sc = tmp;
    }
  }

  /**
   * Adds the given arguments to the variable stack.
   * @param ctx query context
   * @param ii input info
   * @param vals values to add
   * @return old stack frame pointer
   * @throws QueryException if the arguments can't be bound
   */
  private int addArgs(final QueryContext ctx, final InputInfo ii, final Value[] vals)
      throws QueryException {
    // move variables to stack
    final int fp = scope.enter(ctx);
    for(int i = 0; i < args.length; i++) ctx.set(args[i], vals[i], ii);
    return fp;
  }

  /**
   * Checks if all updating expressions in the function are correctly declared and placed.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    final boolean u = expr.uses(Use.UPD);
    if(u) expr.checkUp();
    if(updating) {
      // updating function
      if(ret != null) UPFUNCTYPE.thrw(info);
      if(!u && !expr.isVacuous()) UPEXPECTF.thrw(info);
    } else if(u) {
      // uses updates, but is not declared as such
      UPNOT.thrw(info, description());
    }
  }

  /**
   * Checks if this function returns vacuous results (see {@link Expr#isVacuous()}).
   * @return result of check
   */
  public boolean isVacuous() {
    return !uses(Use.UPD) && ret != null && ret.eq(SeqType.EMP);
  }

  /**
   * Checks if the given feature is used in this function (see {@link Expr#uses(Use)}).
   * @param u feature
   * @return result of check
   */
  public boolean uses(final Use u) {
    // handle recursive calls: set dummy value, eventually replace it with final value
    Boolean b = map.get(u);
    if(b == null) {
      map.put(u, false);
      b = expr == null || expr.uses(u);
      map.put(u, b);
    }
    return b;
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var v : args) if(!visitor.declared(v)) return false;
    return expr.accept(visitor);
  }

  @Override
  public boolean compiled() {
    return compiled;
  }

  /**
   * Returns the static return type of this function.
   * @return return type
   */
  public SeqType retType() {
    return ret != null ? ret : expr.type();
  }

  /**
   * Sets the function body of this function.
   * @param e function body expression
   * @return the function for convenience
   */
  public StaticFunc setBody(final Expr e) {
    expr = e;
    return this;
  }
}
