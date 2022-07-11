package org.basex.query;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.list.*;

/**
 * This class compiles all components of the query that are needed in an order that
 * maximizes the amount of inlining possible.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Leo Woerteler
 */
final class QueryCompiler {
  /** Node stack. */
  private final IntList stack = new IntList();
  /** Index and lowlink list. */
  private final IntList list = new IntList();
  /** Counter for the next free index. */
  private int next;

  /** Adjacency list. */
  private final ArrayList<int[]> adjacent = new ArrayList<>();
  /** Declaration list. */
  private final ArrayList<Scope> scopes = new ArrayList<>();
  /** Declaration list. */
  private final IdentityHashMap<Scope, Integer> ids = new IdentityHashMap<>();

  /**
   * Gathers all declarations (functions and static variables) used by the given main module.
   * @param main the main module to start from
   * @return list of all declarations that the main module uses
   */
  static List<StaticDecl> usedDecls(final MainModule main) {
    final List<StaticDecl> scopes = new ArrayList<>();
    final IdentityHashMap<Scope, Object> map = new IdentityHashMap<>();
    main.visit(new ASTVisitor() {
      @Override
      public boolean staticVar(final StaticVar var) {
        if(map.put(var, var) == null) {
          var.visit(this);
          scopes.add(var);
        }
        return true;
      }

      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        final StaticFunc f = call.func();
        if(map.put(f, f) == null) {
          f.visit(this);
          scopes.add(f);
        }
        return true;
      }

      @Override
      public boolean inlineFunc(final Scope scope) {
        if(map.put(scope, scope) == null) scope.visit(this);
        return true;
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        if(map.put(func, func) == null) func.visit(this);
        return true;
      }
    });
    return scopes;
  }

  /**
   * Compiles the main module.
   * @param cc compilation context
   * @throws QueryException compilation errors
   */
  void compile(final CompileContext cc) throws QueryException {
    add(cc.qc.main);

    // compile the used scopes only, collect static functions
    final ArrayList<StaticFunc> funcs = new ArrayList<>();
    final ArrayList<Scope> entries = new ArrayList<>();
    final ArrayList<ArrayList<Scope>> iter = scopes(0);

    for(final ArrayList<Scope> scps : iter) {
      entries.add(circCheck(scps));
    }
    for(final ArrayList<Scope> scps : iter) {
      for(final Scope scope : scps) scope.reset();
    }
    for(final Scope scope : entries) {
      scope.compile(cc);
      if(scope instanceof StaticFunc) funcs.add((StaticFunc) scope);
    }

    // check for circular variable declarations without compiling the unused scopes
    for(final StaticVar var : cc.qc.vars) {
      if(!ids.containsKey(var)) {
        for(final ArrayList<Scope> scope : scopes(add(var))) circCheck(scope);
      }
    }

    // optimize static functions
    for(final StaticFunc func : funcs) func.optimize(cc);
  }

  /**
   * Checks if the given component contains a static variable that depends on itself.
   * @param scopes scopes to check
   * @return scope to be compiled, the others are compiled recursively
   * @throws QueryException query exception
   */
  private static Scope circCheck(final ArrayList<Scope> scopes) throws QueryException {
    if(scopes.size() > 1) {
      for(final Scope scope : scopes) {
        if(scope instanceof StaticVar) {
          final StaticVar var = (StaticVar) scope;
          throw CIRCVAR_X.get(var.info, var.id());
        }
      }
    }
    return scopes.get(0);
  }

  /**
   * Returns the strongly connected scopes of the dependency graph.
   * @param id id of starting node
   * @return scopes
   * @throws QueryException if a variable directly calls itself
   */
  private ArrayList<ArrayList<Scope>> scopes(final int id) throws QueryException {
    final ArrayList<ArrayList<Scope>> result = new ArrayList<>();
    tarjan(id, result);
    return result;
  }

  /**
   * Algorithm of Tarjan for computing the strongly connected components of a graph.
   * @param id id of current node
   * @param result scopes
   * @throws QueryException if a variable directly calls itself
   */
  private void tarjan(final int id, final ArrayList<ArrayList<Scope>> result)
      throws QueryException {
    final int ixv = 2 * id, llv = ixv + 1, idx = next++;
    while(list.size() <= llv) list.add(-1);
    list.set(ixv, idx);
    list.set(llv, idx);
    stack.push(id);

    for(final int w : adjacentTo(id)) {
      final int ixw = 2 * w, llw = ixw + 1;
      if(list.size() <= ixw || list.get(ixw) < 0) {
        // Successor w has not yet been visited; recurse on it
        tarjan(w, result);
        list.set(llv, Math.min(list.get(llv), list.get(llw)));
      } else if(stack.contains(w)) {
        // Successor w is in stack S and hence in the current SCC
        list.set(llv, Math.min(list.get(llv), list.get(ixw)));
      }
    }

    // if v is a root node, pop the stack and generate an SCC
    if(list.get(llv) == list.get(ixv)) {
      final ArrayList<Scope> out = new ArrayList<>();
      int w;
      do {
        w = stack.pop();
        out.add(scopes.get(w));
      } while(w != id);
      result.add(out);
    }
  }

  /**
   * Adds a new scope and returns its ID.
   * @param scope scope to add
   * @return the scope's ID
   */
  private int add(final Scope scope) {
    final int id = scopes.size();
    scopes.add(scope);
    adjacent.add(null);
    ids.put(scope, id);
    return id;
  }

  /**
   * Returns the indices of all scopes called by the given one.
   * @param node source node index
   * @return destination node indices
   * @throws QueryException if a variable directly calls itself
   */
  private int[] adjacentTo(final int node) throws QueryException {
    int[] adj = adjacent.get(node);
    if(adj == null) {
      adj = neighbors(scopes.get(node));
      adjacent.set(node, adj);
    }
    return adj;
  }

  /**
   * Fills in all used scopes of the given one.
   * @param curr current scope
   * @return IDs of all directly reachable scopes
   * @throws QueryException if a variable directly calls itself
   */
  private int[] neighbors(final Scope curr) throws QueryException {
    final IntList neighbors = new IntList(0);
    final boolean ok = curr.visit(new ASTVisitor() {
      @Override
      public boolean staticVar(final StaticVar var) {
        return var != curr && neighbor(var);
      }

      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        return neighbor(call.func());
      }

      @Override
      public boolean inlineFunc(final Scope scope) {
        return scope.visit(this);
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        return neighbor(func);
      }

      /**
       * Adds a neighbor of the currently inspected scope.
       * @param scope the neighbor
       * @return {@code true} for convenience
       */
      private boolean neighbor(final Scope scope) {
        final Integer old = ids.get(scope);
        if(old == null) {
          neighbors.add(add(scope));
        } else if(!neighbors.contains(old)) {
          neighbors.add(old);
        }
        return true;
      }
    });

    if(!ok) {
      final StaticVar var = (StaticVar) curr;
      throw CIRCREF_X.get(var.info, "$" + var.name);
    }
    return neighbors.finish();
  }
}
