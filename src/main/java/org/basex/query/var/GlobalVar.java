package org.basex.query.var;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import org.basex.query.*;
import org.basex.query.expr.*;
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
public final class GlobalVar extends ParseExpr {
  /** Variable name. */
  private final QNm name;
  /** Annotations. */
  public final Ann ann;
  /** Declaration flag. */
  private boolean declared;
  /** External flag. */
  private boolean external = true;
  /** Type to be checked, {@code null} for no check. */
  private SeqType check;
  /** Bound value. */
  private Value value;
  /** Bound expression. */
  private Expr expr;

  /** Variables should only be compiled once. */
  private boolean compiled;

  /**
   * Constructor for a variable declared in a query.
   * @param ii input info
   * @param a annotations
   * @param n variable name
   * @param t variable type
   * @param e expression to be bound
   * @param ext external flag
   */
  GlobalVar(final InputInfo ii, final Ann a, final QNm n, final SeqType t, final Expr e,
      final boolean ext) {
    super(ii);
    name = n;
    ann = a == null ? new Ann() : a;
    check = t;
    type = t == null ? SeqType.ITEM_ZM : t;
    expr = e;
    declared = true;
    external = ext;
  }

  /**
   * Constructor for an externally bound variable.
   * @param nm name
   * @param e bound expression
   */
  GlobalVar(final QNm nm, final Expr e) {
    super(null);
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
  public Value compile(final QueryContext ctx, final VarScope scp) throws QueryException {
    if(compiled) {
      if(value == null) throw Err.CIRCVAR.thrw(info, this);
      return value;
    }
    if(expr == null) throw VAREMPTY.thrw(info, this);

    final Value val = expr.compile(ctx, scp).value(ctx);
    compiled = true;
    return bind(val);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    return value(ctx).iter();
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    if(value != null) return value;
    if(expr == null) throw VAREMPTY.thrw(info, this);
    return bind(expr.value(ctx));
  }

  /**
   * Binds a value to this variable from outside the query.
   * @param e expression to bind
   * @param ctx query context
   * @return if the expression could be bound
   * @throws QueryException query exception
   */
  public boolean bind(final Expr e, final QueryContext ctx) throws QueryException {
    return bind(e, true, ctx, info);
  }

  /**
   * Binds the specified expression to the variable.
   * @param e expression to be set
   * @param ext if the value is bound from outside the query
   * @param ctx query context
   * @param ii input info
   * @return if the value could be bound
   * @throws QueryException query exception
   */
  private boolean bind(final Expr e, final boolean ext, final QueryContext ctx,
      final InputInfo ii) throws QueryException {
    if(!external || compiled) return false;

    if(e instanceof Value) {
      Value v = (Value) e;
      if(ext && check != null && !check.instance(v)) v = check.cast(v, ctx, ii, this);
      bind(v);
    } else {
      expr = checkType(e, ii);
    }
    return true;
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
    external = ext;
    if(ext && expr != null) {
      bind(expr, true, ctx, ii);
      if(e != null) checkType(e, ii);
    } else if(e != null) {
      bind(e, false, ctx, ii);
    }
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
  public Expr remove(final Var v) {
    return this;
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
    return expr.accept(visitor);
  }

  /**
   * Tries to refine the compile-time type of this variable through the type of the bound
   * expression.
   * @param t type of the bound expression
   * @return {@code true} if the type changed, {@code false} otherwise
   * @throws QueryException if the types are incompatible
   */
  public boolean refineType(final SeqType t) throws QueryException {
    if(t != null && type != t) {
      if(type == null || type.instanceOf(t)) {
        type = t;
        return true;
      }
      if(!t.instanceOf(type)) throw XPTYPE.thrw(info, this, type, t);
    }
    return false;
  }

  /**
   * Getter.
   * @return the bound expression
   */
  protected Expr expr() {
    return expr;
  }

  @Override
  public boolean databases(final StringList db) {
    // [LW] what if {@code expr == null}?
    return expr != null && expr.databases(db);
  }

  @Override
  public GlobalVar copy(final QueryContext ctx, final VarScope scp,
      final IntMap<Var> vs) {
    return this;
  }

  @Override
  public String toString() {
    return new TokenBuilder(DOLLAR).add(name.string()).toString();
  }
}
