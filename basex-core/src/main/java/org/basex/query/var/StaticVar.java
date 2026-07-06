package org.basex.query.var;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
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
  /** Flag for lazy evaluation. */
  private final boolean lazy;
  /** Context currently evaluating this variable ({@code null} if none). */
  private QueryContext evalContext;
  /** Error raised while evaluating this variable. Assigned once, returned by all future jobs. */
  private Throwable evalError;

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
      compiled = dontEnter = true;

      final QueryFocus focus = pushFocus(cc.qc);
      cc.pushScope(vs);
      try {
        expr = expr.compile(cc);
      } finally {
        cc.removeScope(this);
        cc.qc.focus = focus;
        dontEnter = false;
      }

      // dynamic compilation, eager evaluation: pre-evaluate deterministic expressions
      if(expr instanceof Value || cc.dynamic && !lazy && !expr.has(Flag.NDT)) {
        try {
          cc.replaceWith(expr, value(cc.qc));
        } catch(final QueryException ex) {
          if(ex.error() != NOCTX_X) throw ex;
        }
      }
    }
    return null;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    if(!lazy && expr == null) throw VAREMPTY_X.get(info, name());

    final Value cached = value;
    if(cached != null) return cached;
    final Value acquired = acquire(qc);
    if(acquired != null) return acquired;

    final QueryFocus focus = pushFocus(qc);
    Throwable error = null;
    try {
      return super.value(qc);
    } catch(final QueryException ex) {
      ex.notCatchable();
      // eagerly compute line/column so waiting threads see consistent values
      if(ex.info() != null) ex.info().init();
      error = ex;
      throw ex;
    } catch(final RuntimeException | Error ex) {
      error = ex;
      throw ex;
    } finally {
      qc.focus = focus;
      release(qc, error);
    }
  }

  /**
   * Acquires the right to evaluate this variable, or returns a cached value.
   * @param qc query context
   * @return cached value, or {@code null} if the caller must evaluate it
   * @throws QueryException query exception
   */
  private Value acquire(final QueryContext qc) throws QueryException {
    final IdentityHashMap<QueryContext, StaticVar> waiting = qc.staticVarWaiting;
    synchronized(waiting) {
      while(true) {
        if(value != null) return value;
        if(evalError != null) rethrow();
        if(evalContext == null) {
          evalContext = qc;
          return null;
        }
        // someone else is evaluating this variable: check for circular dependencies, wait
        if(circular(qc)) throw CIRCVAR_X.get(info, name());
        waiting.put(qc, this);
        try {
          waiting.wait();
        } catch(final InterruptedException ex) {
          Util.debug(ex);
          Thread.currentThread().interrupt();
          throw new JobException(Text.INTERRUPTED);
        } finally {
          waiting.remove(qc);
        }
      }
    }
  }

  /**
   * Releases evaluation ownership and wakes waiting threads.
   * @param qc query context
   * @param error error raised while evaluating this variable
   */
  private void release(final QueryContext qc, final Throwable error) {
    final IdentityHashMap<QueryContext, StaticVar> waiting = qc.staticVarWaiting;
    synchronized(waiting) {
      evalError = error;
      evalContext = null;
      waiting.notifyAll();
    }
  }

  /**
   * Checks if waiting for this variable would introduce a circular dependency. This is the case if
   * the current query context {@code qc} is the same as or nested in the evaluating context, or if
   * the chain of waiting contexts, that is linked by the variables they are waiting for, contains
   * the current query context {@code qc}.
   * @param qc current query context
   * @return result of check
   */
  private boolean circular(final QueryContext qc) {
    for(QueryContext q = qc; q != null; q = q.parent) {
      if(q == evalContext) return true;
    }
    for(StaticVar sv = this; sv != null;) {
      final QueryContext q = sv.evalContext;
      if(q == qc) return true;
      if(q == null) return false;
      sv = qc.staticVarWaiting.get(q);
    }
    return false;
  }

  /**
   * Throws an error that was raised while evaluating this variable.
   * @throws QueryException query exception
   */
  private void rethrow() throws QueryException {
    final Throwable th = evalError;
    if(th instanceof final QueryException ex) throw new QueryException(
        ex.info(), ex.qname(), ex.getLocalizedMessage()).value(ex.value()).notCatchable();
    if(th instanceof final RuntimeException ex) throw ex;
    if(th instanceof final Error ex) throw ex;
    throw Util.notExpected(th);
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
    if(dontEnter || expr == null) return false;
    dontEnter = true;
    final boolean has = expr.has(flags);
    dontEnter = false;
    return has;
  }

  /**
   * Assigns a new query focus with the global context value.
   * @param qc query context
   * @return old focus
   */
  private static QueryFocus pushFocus(final QueryContext qc) {
    final QueryFocus focus = qc.focus, qf = new QueryFocus();
    qf.value = qc.finalContext ? qc.contextValue.value : null;
    qc.focus = qf;
    return focus;
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
