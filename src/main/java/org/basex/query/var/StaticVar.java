package org.basex.query.var;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Static variable which can be assigned an expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class StaticVar extends ParseExpr implements Scope {
  /** Annotation for lazy evaluation. */
  private static final QNm LAZY = new QNm(QueryText.LAZY, QueryText.BASEXURI);
  /** Static context. */
  private final StaticContext sc;
  /** Variable scope. */
  private final VarScope scope;
  /** Variable name. */
  private final QNm name;
  /** Annotations. */
  public final Ann ann;
  /** Declaration flag. */
  private boolean declared;
  /** If this variable can still be bound. */
  private boolean bindable = true;
  /** Type to be checked, {@code null} for no check. */
  private SeqType check;
  /** Bound value. */
  private Value value;
  /** Bound expression. */
  private Expr expr;
  /** Flag for lazy evaluation. */
  private boolean lazy;

  /** Variables should only be compiled once. */
  private boolean compiled;
  /** Flag that is set during compilation and execution and prevents infinite loops. */
  private boolean dontEnter;

  /**
   * Constructor for a variable declared in a query.
   * @param sctx static context
   * @param scp variable scope
   * @param ii input info
   * @param a annotations
   * @param n variable name
   * @param t variable type
   * @param e expression to be bound
   * @param ext external flag
   */
  StaticVar(final StaticContext sctx, final VarScope scp, final InputInfo ii, final Ann a,
      final QNm n, final SeqType t, final Expr e, final boolean ext) {

    super(ii);
    sc = sctx;
    scope = scp;
    name = n;
    ann = a == null ? new Ann() : a;
    check = t;
    type = t == null ? SeqType.ITEM_ZM : t;
    expr = e;
    declared = true;
    bindable = ext || e == null;
    lazy = ann.contains(LAZY);
  }

  /**
   * Constructor for an externally bound variable.
   * @param sctx static context
   * @param nm name
   * @param e bound expression
   * @param ii input info
   */
  StaticVar(final StaticContext sctx, final QNm nm, final Expr e, final InputInfo ii) {
    super(ii);
    sc = sctx;
    scope = new VarScope();
    name = nm;
    ann = new Ann();
    type = SeqType.ITEM_ZM;
    expr = e;
  }

  @Override
  public void checkUp() throws QueryException {
    if(expr != null && expr.uses(Use.UPD)) UPNOT.thrw(info, description());
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    compile(ctx, null);
  }

  @Override
  public Expr compile(final QueryContext ctx, final VarScope o) throws QueryException {
    if(expr == null) throw (declared ? VAREMPTY : VARUNDEF).thrw(info, this);
    if(dontEnter) throw Err.circVar(ctx, this);

    if(!compiled) {
      final StaticContext cs = ctx.sc;
      ctx.sc = sc;

      dontEnter = true;
      final int fp = scope.enter(ctx);
      try {
        expr = expr.compile(ctx, scope);
      } catch(final QueryException qe) {
        compiled = true;
        if(lazy) {
          expr = FNInfo.error(qe, info);
          return this;
        }
        throw qe.notCatchable();
      } finally {
        scope.cleanUp(this);
        scope.exit(ctx, fp);
        ctx.sc = cs;
        dontEnter = false;
      }

      compiled = true;
      if(!lazy || expr.isValue()) {
        final Value val = bind(value(ctx));
        return val;
      }
    }

    return value != null ? value : this;
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    if(dontEnter) throw Err.circVar(ctx, this);
    if(lazy) {
      if(!compiled) throw Util.notexpected(this + " was not compiled.");
      if(value != null) return value;
      final StaticContext cs = ctx.sc;
      ctx.sc = sc;
      dontEnter = true;
      final int fp = scope.enter(ctx);
      try {
        return bind(expr.value(ctx));
      } catch(final QueryException qe) {
        throw qe.notCatchable();
      } finally {
        scope.exit(ctx, fp);
        ctx.sc = cs;
        dontEnter = false;
      }
    }

    if(value != null) return value;
    if(expr == null) throw VAREMPTY.thrw(info, this);
    dontEnter = true;
    final int fp = scope.enter(ctx);
    final StaticContext cs = ctx.sc;
    ctx.sc = sc;
    try {
      return bind(expr.value(ctx));
    } finally {
      scope.exit(ctx, fp);
      ctx.sc = cs;
      dontEnter = false;
    }
  }

  /**
   * Binds a value to this variable from outside the query.
   * @param e expression to bind
   * @param ctx query context
   * @param ii input info
   * @return the variable if it could be bound, {@code null} otherwise
   * @throws QueryException query exception
   */
  public StaticVar bind(final Expr e, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return bind(e, true, ctx, info != null ? info : ii);
  }

  /**
   * Binds the specified expression to the variable.
   * @param e expression to be set
   * @param ext if the value is bound from outside the query
   * @param ctx query context
   * @param ii input info
   * @return the variable if it could be bound, {@code null} otherwise
   * @throws QueryException query exception
   */
  private StaticVar bind(final Expr e, final boolean ext, final QueryContext ctx,
      final InputInfo ii) throws QueryException {
    if(!bindable || compiled) return null;

    if(e instanceof Value) {
      Value v = (Value) e;
      if(ext && check != null && !check.instance(v)) v = check.cast(v, ctx, ii, this);
      bind(v);
    } else {
      expr = checkType(e, ii);
      value = null;
    }
    return this;
  }

  /**
   * Declares an already bound variable.
   * @param t declared type
   * @param a annotations, possibly {@code null}
   * @param e bound expression, possibly {@code null}
   * @param ext external flag
   * @param ctx query context
   * @param ii input info
   * @throws QueryException query exception
   */
  public void declare(final SeqType t, final Ann a, final Expr e, final boolean ext,
      final QueryContext ctx, final InputInfo ii) throws QueryException {
    if(declared) throw Err.VARDEFINE.thrw(ii, this);
    declared = true;
    check = t;
    info = ii;
    if(a != null) for(int i = 0; i < a.size(); i++) ann.add(a.names[i], a.values[i]);
    lazy = ann.contains(LAZY);
    if(ext && expr != null) {
      bind(expr, true, ctx, ii);
      if(e != null) checkType(e, ii);
    } else if(e != null) {
      bind(e, false, ctx, ii);
    }
    bindable = ext;
  }

  /**
   * Checks if the given expression can be bound to this variable.
   * @param e expression
   * @param ii input info
   * @return the expression
   * @throws QueryException query exception
   */
  private Expr checkType(final Expr e, final InputInfo ii) throws QueryException {
    if(check != null) {
      if(e instanceof Value) check.treat((Value) e, ii);
      else if(e.type().intersect(check) == null) throw Err.treat(ii, check, e);
    }
    return e;
  }

  /**
   * Binds the specified value to the variable.
   * @param v value to be set
   * @return self reference
   * @throws QueryException query exception
   */
  private Value bind(final Value v) throws QueryException {
    expr = v;
    value = check != null ? check.treat(v, info) : v;
    return value;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(NAM, name.string());
    if(expr != null) expr.plan(e);
    plan.add(e);
  }

  @Override
  public SeqType type() {
    return type != null ? type : SeqType.ITEM_ZM;
  }

  @Override
  public boolean uses(final Use u) {
    return expr != null && expr.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    return true;
  }

  @Override
  public VarUsage count(final Var v) {
    return VarUsage.NEVER;
  }

  @Override
  public Expr inline(final QueryContext ctx, final VarScope scp,
      final Var v, final Expr e) throws QueryException {
    // global variables cannot contain free references to local ones
    return null;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.staticVar(this);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return expr == null || expr.accept(visitor);
  }

  /**
   * Adds the description of this variable to the given string builder.
   * @param sb string builder
   * @return the string builder for convenience
   */
  public StringBuilder fullDesc(final StringBuilder sb) {
    sb.append(DECLARE).append(' ');
    if(!ann.isEmpty()) sb.append(ann);
    sb.append(VARIABLE).append(' ').append(DOLLAR).append(
        Token.string(name.string())).append(' ');
    if(check != null) sb.append(AS).append(' ').append(check).append(' ');
    if(expr != null) sb.append(ASSIGN).append(' ').append(expr);
    else sb.append(EXTERNAL);
    return sb.append(';');
  }

  @Override
  public boolean databases(final StringList db) {
    return expr != null && expr.databases(db);
  }

  @Override
  public StaticVar copy(final QueryContext ctx, final VarScope scp,
      final IntMap<Var> vs) {
    return this;
  }

  @Override
  public String toString() {
    return new TokenBuilder(DOLLAR).add(name.string()).toString();
  }

  @Override
  public int exprSize() {
    return 0;
  }

  @Override
  public boolean compiled() {
    return compiled;
  }

  /**
   * Checks if this variable has already been declared.
   * @return result of check
   */
  public boolean declared() {
    return declared;
  }
}
