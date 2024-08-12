package org.basex.query;

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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
final class QueryCompiler {
  /** IDs of scopes. */
  private final IdentityHashMap<Scope, Integer> ids = new IdentityHashMap<>();
  /** Adjacency list. */
  private final ArrayList<int[]> adjacent = new ArrayList<>();
  /** Scopes. */
  private final ArrayList<Scope> scopes = new ArrayList<>();

  /** Node stack. */
  private final IntList stack = new IntList();
  /** Index and lowlink list. */
  private final IntList list = new IntList();
  /** Counter for the next free index. */
  private int next;

  /**
   * Compiles the main module.
   * @param cc compilation context
   * @throws QueryException compilation errors
   */
  static void compile(final CompileContext cc) throws QueryException {
    for(final ArrayList<Scope> scps : new QueryCompiler().scopes(cc.qc.main)) {
      scps.get(0).compile(cc);
    }
  }

  /**
   * Computes the scopes.
   * @param main reference to main module
   * @return scoped
   */
  private ArrayList<ArrayList<Scope>> scopes(final MainModule main) {
    add(main);
    final ArrayList<ArrayList<Scope>> lists = new ArrayList<>();
    tarjan(0, lists);
    return lists;
  }

  /**
   * Algorithm of Tarjan for computing the strongly connected components of a graph.
   * @param id id of current node
   * @param result scopes
   */
  private void tarjan(final int id, final ArrayList<ArrayList<Scope>> result) {
    final int ixv = id << 1, llv = ixv + 1, idx = next++;
    while(list.size() <= llv) list.add(-1);
    list.set(ixv, idx);
    list.set(llv, idx);
    stack.push(id);

    for(final int w : adjacentTo(id)) {
      final int ixw = w << 1, llw = ixw + 1;
      if(list.size() <= ixw || list.get(ixw) < 0) {
        // successor w has not yet been visited; recurse on it
        tarjan(w, result);
        list.set(llv, Math.min(list.get(llv), list.get(llw)));
      } else if(stack.contains(w)) {
        // successor w is in stack S and hence in the current SCC
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
    scope.reset();
    return id;
  }

  /**
   * Returns the indices of all scopes called by the given one.
   * @param node source node index
   * @return destination node indices
   */
  private int[] adjacentTo(final int node) {
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
   */
  private int[] neighbors(final Scope curr) {
    final IntList neighbors = new IntList(0);
    curr.visit(new ASTVisitor() {
      @Override
      public boolean staticVar(final StaticVar var) {
        return var != curr && neighbor(var);
      }

      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        final StaticFunc func = call.func();
        return func == null || neighbor(func);
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
    return neighbors.finish();
  }
}
