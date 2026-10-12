package org.basex.query;

import java.util.*;

import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * This class compiles all components of the query that are needed in an order that
 * maximizes the amount of inlining possible.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
final class QueryCompiler {
  /** Visited scopes. */
  private final Set<Scope> visited = Collections.newSetFromMap(new IdentityHashMap<>());
  /** Scopes in compilation order (callees first). */
  private final ArrayList<Scope> order = new ArrayList<>();
  /** Static functions and variables of the query (others belong to bound function items). */
  private final Set<Scope> declared = Collections.newSetFromMap(new IdentityHashMap<>());
  /** Calls of static functions, grouped by function. */
  private final Map<StaticFunc, ArrayList<StaticFuncCall>> calls = new IdentityHashMap<>();
  /** Indicates if all function calls were found. */
  private boolean complete = true;

  /**
   * Constructor.
   * @param qc query context
   */
  private QueryCompiler(final QueryContext qc) {
    for(final StaticFunc func : qc.functions) declared.add(func);
    for(final StaticVar var : qc.vars) declared.add(var);
  }

  /**
   * Compiles the main module.
   * @param cc compilation context
   * @throws QueryException query exception
   */
  static void compile(final CompileContext cc) throws QueryException {
    final QueryCompiler qcomp = new QueryCompiler(cc.qc);
    qcomp.visit(cc.qc.main);
    if(qcomp.complete) cc.calls = qcomp.calls;
    for(final Scope scope : qcomp.order) scope.compile(cc);
  }

  /**
   * Adds the scopes reachable from the specified scope in post-order (callees first).
   * @param curr current scope
   * @return {@code true} for convenience
   */
  private boolean visit(final Scope curr) {
    if(!visited.add(curr)) return true;
    curr.reset();
    curr.visit(new ASTVisitor() {
      @Override
      public boolean staticVar(final StaticVar var) {
        return var != curr && (!declared.contains(var) || visit(var));
      }

      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        final StaticFunc func = call.func();
        if(func == null || !declared.contains(func)) return true;
        calls.computeIfAbsent(func, k -> new ArrayList<>(1)).add(call);
        return visit(func);
      }

      @Override
      public boolean subScope(final Scope scope) {
        // bodies of function items are not recompiled (and may belong to other queries)
        if(!(curr instanceof FuncItem)) scope.reset();
        return scope.visit(this);
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        return visit(func);
      }

      @Override
      public boolean value(final Value value) {
        // function items in sequences, maps and arrays may reference functions of the query
        if(!funcItems(value)) complete = false;
        return true;
      }
    });
    order.add(curr);
    return true;
  }
}
