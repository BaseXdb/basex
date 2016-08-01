package org.basex.query;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.scope.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Compilation context.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class CompileContext {
  /** Query context. */
  public final QueryContext qc;
  /** Variable scopes. */
  public ArrayList<VarScope> scopes = new ArrayList<>();

  /**
   * Constructor.
   * @param qc query context
   */
  public CompileContext(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Adds some compilation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void info(final String string, final Object... ext) {
    qc.info.compInfo(string,  ext);
  }

  /**
   * Pushes a new variable scope to the stack.
   * @param scp variable scope
   */
  public void pushScope(final VarScope scp) {
    scopes.add(scp);
  }

  /**
   * Removes a variable scope from the stack.
   * @return the removed element
   */
  public VarScope removeScope() {
    return scopes.remove(scopes.size() - 1);
  }

  /**
   * Prepares the variable scope for being compiled.
   * This method should be run after compiling a scope.
   * @param scope scope
   */
  public void removeScope(final Scope scope) {
    removeScope().cleanUp(scope);
  }

  /**
   * Returns the current variable scope.
   * @return variable scope
   */
  public VarScope vs() {
    return scopes.get(scopes.size() - 1);
  }

  /**
   * Returns the current static context.
   * @return static context
   */
  public StaticContext sc() {
    return vs().sc;
  }

  /**
   * Creates a new copy of the given variable in this scope.
   * @param var variable to copy (can be {@code null})
   * @param vm variable mapping (can be {@code null})
   * @return new variable
   */
  public Var copy(final Var var, final IntObjMap<Var> vm) {
    if(var == null) return null;
    final VarScope vs = vs();
    final Var v = vs.add(new Var(var, qc, vs.sc));
    if(vm != null) vm.put(var.id, v);
    return v;
  }

  /**
   * Creates an error function instance.
   * @param qe exception to be raised
   * @param expr expression
   * @return function
   */
  public StandardFunc error(final QueryException qe, final Expr expr) {
    return FnError.get(qe, expr.seqType(), sc());
  }

  /**
   * Creates and returns an optimized function instance.
   * @param func function
   * @param info input info
   * @param exprs expressions
   * @return function
   * @throws QueryException query exception
   */
  public Expr function(final Function func, final InputInfo info, final Expr... exprs)
      throws QueryException {
    return func.get(sc(), info, exprs).optimize(this);
  }
}
