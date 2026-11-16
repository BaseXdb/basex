package org.basex.query.util.parse;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * Variable context for resolving local variables.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class VarContext {
  /** Non-local variable bindings for closures. */
  final HashMap<Var, Expr> bindings;
  /** Current scope containing all variables and the closure. */
  final VarScope vs;
  /** Stack of local variables. */
  private final VarStack stack = new VarStack();
  /** Variables shadowed by the stack entries (entries can be {@code null}). */
  private final VarStack shadowed = new VarStack();
  /** Innermost variables on the stack, indexed by name. */
  private final QNmMap<Var> names = new QNmMap<>();

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
    shadowed.push(names.put(var.name, var));
  }

  /**
   * Returns the innermost variable on the stack with the specified name.
   * @param name variable name
   * @return variable (can be {@code null})
   */
  Var get(final QNm name) {
    return names.get(name);
  }

  /**
   * Returns the variable at the specified stack position.
   * @param index stack position
   * @return variable
   */
  Var get(final int index) {
    return stack.get(index);
  }

  /**
   * Returns the stack size.
   * @return stack size
   */
  int size() {
    return stack.size();
  }

  /**
   * Removes all variables from the stack above the specified size.
   * @param size new stack size
   */
  void size(final int size) {
    while(stack.size() > size) names.put(stack.pop().name, shadowed.pop());
  }
}
