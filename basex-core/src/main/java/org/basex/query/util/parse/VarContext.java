package org.basex.query.util.parse;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.var.*;

/**
 * Variable context for resolving local variables.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class VarContext {
  /** Stack of local variables (can be {@code null}). */
  final VarStack stack = new VarStack();
  /** Non-local variable bindings for closures. */
  final HashMap<Var, Expr> bindings;
  /** Current scope containing all variables and the closure. */
  final VarScope vs;

  /**
   * Constructor.
   * @param bindings non-local variable bindings for closures (can be {@code null})
   */
  VarContext(final HashMap<Var, Expr> bindings) {
    this.bindings = bindings;
    vs = new VarScope();
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
