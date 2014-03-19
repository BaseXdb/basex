package org.basex.query.var;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Static variable to which an expression can be assigned.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class StaticVar extends StaticDecl {
  /** Annotation for lazy evaluation. */
  private static final QNm LAZY = new QNm(QueryText.LAZY, BASEXURI);

  /** If this variable can be bound from outside the query. */
  private final boolean external;
  /** Bound value. */
  Value value;
  /** Flag for lazy evaluation. */
  private final boolean lazy;

  /**
   * Constructor for a variable declared in a query.
   * @param sctx static context
   * @param scp variable scope
   * @param a annotations
   * @param n variable name
   * @param t variable type
   * @param e expression to be bound
   * @param ext external flag
   * @param xqdoc current xqdoc cache
   * @param ii input info
   */
  StaticVar(final StaticContext sctx, final VarScope scp, final Ann a, final QNm n,
      final SeqType t, final Expr e, final boolean ext, final String xqdoc, final InputInfo ii) {
    super(sctx, a, n, t, scp, xqdoc, ii);
    expr = e;
    external = ext;
    lazy = ann.contains(LAZY);
  }

  @Override
  public void compile(final QueryContext ctx) throws QueryException {
    if(expr == null) throw VAREMPTY.get(info, '$' + Token.string(name.string()));
    if(dontEnter) throw circVarError(this);

    if(!compiled) {
      dontEnter = true;
      final int fp = scope.enter(ctx);
      try {
        expr = expr.compile(ctx, scope);
      } catch(final QueryException qe) {
        compiled = true;
        if(lazy) {
          expr = FNInfo.error(qe, expr.type());
          return;
        }
        throw qe.notCatchable();
      } finally {
        scope.cleanUp(this);
        scope.exit(ctx, fp);
        dontEnter = false;
      }

      compiled = true;
      if(!lazy || expr.isValue()) bind(value(ctx));
    }
  }

  /**
   * Evaluates this variable lazily.
   * @param ctx query context
   * @return value of this variable
   * @throws QueryException query exception
   */
  public Value value(final QueryContext ctx) throws QueryException {
    if(dontEnter) throw circVarError(this);
    if(lazy) {
      if(!compiled) throw Util.notExpected(this + " was not compiled.");
      if(value != null) return value;
      dontEnter = true;
      final int fp = scope.enter(ctx);
      try {
        return bind(expr.value(ctx));
      } catch(final QueryException qe) {
        throw qe.notCatchable();
      } finally {
        scope.exit(ctx, fp);
        dontEnter = false;
      }
    }

    if(value != null) return value;
    if(expr == null) throw VAREMPTY.get(info, this);
    dontEnter = true;
    final int fp = scope.enter(ctx);
    try {
      return bind(expr.value(ctx));
    } finally {
      scope.exit(ctx, fp);
      dontEnter = false;
    }
  }

  /**
   * Checks for the correct placement of updating expressions in this variable.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    if(expr != null && expr.has(Flag.UPD)) throw UPNOT.get(info, description());
  }

  /**
   * Binds an expression to this variable from outside the query.
   * @param e value to bind
   * @param ctx query context
   * @return if the value could be bound
   * @throws QueryException query exception
   */
  public boolean bind(final Expr e, final QueryContext ctx) throws QueryException {
    if(!external || compiled) return false;

    if(e instanceof Value) {
      Value v = (Value) e;
      if(declType != null && !declType.instance(v)) v = declType.cast(v, ctx, sc, info, e);
      bind(v);
    } else {
      expr = checkType(e, info);
      value = null;
    }
    return true;
  }

  /**
   * Checks if the given expression can be bound to this variable.
   * @param e expression
   * @param ii input info
   * @return the expression
   * @throws QueryException query exception
   */
  private Expr checkType(final Expr e, final InputInfo ii) throws QueryException {
    if(declType != null) {
      if(e instanceof Value) declType.treat((Value) e, ii);
      else if(e.type().intersect(declType) == null) throw treatError(ii, declType, e);
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
    value = v;
    if(declType != null) declType.treat(v, info);
    return value;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem e = planElem(NAM, name.string());
    if(expr != null) expr.plan(e);
    plan.add(e);
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return expr == null || expr.accept(visitor);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(DECLARE).append(' ');
    if(!ann.isEmpty()) sb.append(ann);
    sb.append(VARIABLE).append(' ').append(DOLLAR).append(
        Token.string(name.string())).append(' ');
    if(declType != null) sb.append(AS).append(' ').append(declType).append(' ');
    if(expr != null) sb.append(ASSIGN).append(' ').append(expr);
    else sb.append(EXTERNAL);
    return sb.append(';').toString();
  }

  @Override
  public byte[] id() {
    return Token.concat(new byte[] { '$' }, name.id());
  }

  /**
   * Checks if the expression bound to this variable has the given flag.
   * @param flag flag to check for
   * @return {@code true} if the expression has the given flag, {@code false} otherwise
   */
  public boolean has(final Flag flag) {
    if(dontEnter || expr == null) return false;
    dontEnter = true;
    final boolean res = expr.has(flag);
    dontEnter = false;
    return res;
  }
}
