package org.basex.query;

import java.util.*;

import org.basex.query.var.*;
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
   */
  public void removeScope() {
    scopes.remove(scopes.size() - 1);
  }

  /**
   * Returns the current variable scope.
   * @return variable scope
   */
  public VarScope scope() {
    return scopes.get(scopes.size() - 1);
  }

  /**
   * Returns the current static context.
   * @return static context
   */
  public StaticContext sc() {
    return scope().sc;
  }

  /**
   * Creates a new copy of the given variable in this scope.
   * @param var variable to copy (can be {@code null})
   * @param vs variable mapping (can be {@code null})
   * @return new variable
   */
  public Var copy(final Var var, final IntObjMap<Var> vs) {
    if(var == null) return null;
    final Var v = scope().addCopy(var, this);
    if(vs != null) vs.put(var.id, v);
    return v;
  }
}
