package org.basex.query.util.parse;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.var.*;

/**
 * Variable context for resolving local variables.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class VarContext {
  /** Stack of local variables. */
  final VarStack stack = new VarStack();
  /** Non-local variable bindings for closures. */
  final HashMap<Var, Expr> bindings;
  /** Current scope containing all variables and the closure. */
  final VarScope vs;

  /**
   * Constructor.
   * @param bindings non-local variable bindings for closures
   * @param sc static context
   */
  VarContext(final HashMap<Var, Expr> bindings, final StaticContext sc) {
    this.bindings = bindings;
    vs = new VarScope(sc);
  }

  /**
   * Adds a new variable to this context.
   * @param var variable
   */
  void add(final Var var) {
    vs.add(var);
    stack.push(var);
  }
}
