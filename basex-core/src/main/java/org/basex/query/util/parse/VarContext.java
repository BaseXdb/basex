package org.basex.query.util.parse;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * Variable context for resolving local variables.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class VarContext {
  /** Query parser. */
  private final QueryParser parser;
  /** Stack of local variables. */
  final VarStack stack = new VarStack();
  /** Non-local variable bindings for closures. */
  final HashMap<Var, Expr> bindings;
  /** Current scope containing all variables and the closure. */
  final VarScope scope;

  /**
   * Constructor.
   * @param bindings non-local variable bindings for closures
   * @param queryParser TODO
   */
  VarContext(final QueryParser queryParser, final HashMap<Var, Expr> bindings) {
    this.parser = queryParser;
    this.bindings = bindings;
    scope = new VarScope(parser.sc);
  }

  /**
   * Adds a new variable to this context.
   * @param name variable name
   * @param tp variable type
   * @param prm promotion flag
   * @return the variable
   */
  Var add(final QNm name, final SeqType tp, final boolean prm) {
    final Var var = scope.newLocal(parser.qc, name, tp, prm);
    stack.push(var);
    return var;
  }
}
