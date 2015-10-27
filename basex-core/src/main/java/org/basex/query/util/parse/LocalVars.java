package org.basex.query.util.parse;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Organizes local variables.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class LocalVars {
  /** Stack of variable contexts. */
  private final ArrayList<VarContext> vars = new ArrayList<>();
  /** Query parser. */
  private final QueryParser qp;

  /**
   * Constructor.
   * @param qp query parser
   */
  public LocalVars(final QueryParser qp) {
    this.qp = qp;
  }

  /**
   * Creates and registers a new local variable in the current scope.
   * @param name variable name
   * @param tp variable type
   * @param prm if the variable is a function parameter
   * @return registered variable
   */
  public Var add(final QNm name, final SeqType tp, final boolean prm) {
    return vars.get(vars.size() - 1).add(name, tp, prm);
  }

  /**
   * Tries to resolve a local variable reference.
   * @param name variable name
   * @param ii input info
   * @return variable reference (may be {@code null})
   */
  public VarRef resolveLocal(final QNm name, final InputInfo ii) {
    int l = vars.size();
    Var var = null;

    // look up through the scopes until we find the declaring scope
    while(--l >= 0) {
      var = vars.get(l).stack.get(name);
      if(var != null) break;
    }

    // looked through all scopes, must be a static variable
    if(var == null) return null;

    // go down through the scopes and add bindings to their closures
    final int ls = vars.size();
    while(++l < ls) {
      final VarContext vctx = vars.get(l);
      final Var local = vctx.add(var.name, var.seqType(), false);
      vctx.bindings.put(local, new VarRef(ii, var));
      var = local;
    }

    // return the properly propagated variable reference
    return new VarRef(ii, var);
  }

  /**
   * Resolves the referenced variable as a local or static variable and returns a reference to it.
   * IF the variable is not declared, the specified error is thrown.
   * @param name variable name
   * @param ii input info
   * @return referenced variable
   * @throws QueryException if the variable isn't defined
   */
  public Expr resolve(final QNm name, final InputInfo ii) throws QueryException {
    // local variable
    final VarRef local = resolveLocal(name, ii);
    if(local != null) return local;

    // static variable
    final byte[] uri = name.uri();

    // accept variable reference...
    // - if a variable uses the module or an imported URI, or
    // - if it is specified in the main module
    if(qp.module == null || eq(qp.module.uri(), uri) || qp.modules.contains(uri))
      return qp.qc.vars.newRef(name, qp.sc, ii);

    throw qp.error(VARUNDEF_X, '$' + string(name.string()));
  }

  /**
   * Pushes a new variable context onto the stack.
   * @param nonLocal mapping for non-local variables
   */
  public void pushContext(final HashMap<Var, Expr> nonLocal) {
    vars.add(new VarContext(qp, nonLocal));
  }

  /**
   * Pops one variable context from the stack.
   * @return the removed context's variable scope
   */
  public VarScope popContext() {
    return vars.remove(vars.size() - 1).scope;
  }

  /**
   * Opens a new sub-scope inside the current one. The returned marker has to be supplied to the
   * corresponding call to {@link #closeScope(int)} in order to mark the variables as
   * inaccessible.
   * @return marker for the current bindings
   */
  public int openScope() {
    return vars.get(vars.size() - 1).stack.size();
  }

  /**
   * Closes the sub-scope and marks all contained variables as inaccessible.
   * @param marker marker for the start of the sub-scope
   */
  public void closeScope(final int marker) {
    vars.get(vars.size() - 1).stack.size(marker);
  }
}
