package org.basex.query.var;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Static variable to which an expression can be assigned.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class StaticVar extends StaticDecl {
  /** Indicates if this variable can be bound from outside the query. */
  public final boolean external;
  /** Value assigned at compile time (can be {@code null}). */
  public volatile Value value;
  /** Flag for lazy evaluation. */
  private final boolean lazy;

  /**
   * Constructor for a variable declared in a query.
   * @param var variable
   * @param expr expression to be bound
   * @param anns annotations
   * @param external external flag
   * @param vs variable scope
   * @param doc xqdoc string
   */
  StaticVar(final Var var, final Expr expr, final AnnList anns, final boolean external,
      final VarScope vs, final String doc) {
    super(var.name, var.declType, anns, vs, var.info, doc);
    this.expr = expr;
    this.external = external;
    lazy = anns.contains(Annotation._BASEX_LAZY);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    if(expr == null) throw VAREMPTY_X.get(info, name());
    if(!compiled) {
      compiled = true;

      final QueryFocus focus = cc.qc.focus;
      pushFocus(cc.qc);
      cc.pushScope(vs);
      try {
        expr = expr.compile(cc);
      } finally {
        cc.removeScope(this);
        cc.qc.focus = focus;
      }

      // dynamic compilation, eager evaluation: pre-evaluate deterministic expressions
      if(expr instanceof Value || cc.dynamic && !lazy && !expr.has(Flag.NDT)) {
        try {
          if(value == null) value = compute(cc.qc);
          cc.replaceWith(expr, value);
        } catch(final QueryException ex) {
          if(ex.error() != NOCTX_X) throw ex;
        }
      }
    }
    return null;
  }

  /**
   * Returns the value of this variable.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  public Value value(final QueryContext qc) throws QueryException {
    if(!lazy && expr == null) throw VAREMPTY_X.get(info, name());

    final Value cached = value;
    return cached != null ? cached : qc.globals.value(this, qc);
  }

  /**
   * Evaluates the expression of this variable.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  Value compute(final QueryContext qc) throws QueryException {
    final QueryFocus focus = qc.focus;
    pushFocus(qc);
    final int fp = vs.enter(qc);
    try {
      return coerce(expr.value(qc), qc);
    } catch(final QueryException ex) {
      // errors of lazy variables can surface anywhere: make them non-catchable
      throw lazy ? ex.notCatchable() : ex;
    } finally {
      vs.exit(fp, qc);
      qc.focus = focus;
    }
  }

  /**
   * Returns a copy of an error that was raised in another context.
   * @param error original error
   * @return query exception
   */
  QueryException error(final QueryException error) {
    final QueryException ex = new QueryException(error.info(), error.qname(),
        error.getLocalizedMessage()).value(error.value());
    return lazy ? ex.notCatchable() : ex;
  }

  /**
   * Ensures that the variable expression is not updating.
   * @throws QueryException query exception
   */
  void checkUp() throws QueryException {
    if(expr != null && expr.has(Flag.UPD)) throw UPNOT_X.get(info, description());
  }

  /**
   * Binds an external value and casts it to the declared type (if specified).
   * @param val value to bind
   * @param qc query context
   * @param cast cast flag, value will be coerced if false
   * @throws QueryException query exception
   */
  void bind(final Value val, final QueryContext qc, final boolean cast) throws QueryException {
    if(external && !compiled) {
      value = declType == null || declType.instance(val) ? val :
        cast ? declType.cast(val, true, qc, info) : declType.coerce(val, qc, info, name, null);
      expr = value;
    }
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    return expr == null || expr.accept(visitor);
  }

  /**
   * Returns the name of the variable.
   * @return name
   */
  public String name() {
    return Strings.concat(Token.cpToken('$'), name.string());
  }

  /**
   * Indicates if the expression bound to this variable has one of the specified compiler
   * properties.
   * @param flags flags
   * @return result of check
   * @see Expr#has(Flag...)
   */
  boolean has(final Flag... flags) {
    return check(flags);
  }

  /**
   * Assigns a new query focus with the global context value.
   * @param qc query context
   */
  private static void pushFocus(final QueryContext qc) {
    final QueryFocus qf = new QueryFocus();
    qf.value = qc.finalContext ? qc.contextValue.value : null;
    qc.focus = qf;
  }

  @Override
  public String description() {
    return "variable declaration";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string()), expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(DECLARE).token(anns).token(VARIABLE).token(name());
    if(declType != null) qs.token(AS).token(declType);
    if(external) qs.token(EXTERNAL);
    if(expr != null) qs.token(":=").token(expr);
    qs.token(';');
  }
}
