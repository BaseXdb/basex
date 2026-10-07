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
 * @author BaseX Team, BSD License
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
  /** Scopes on the node stack. */
  private final BitSet onStack = new BitSet();
  /** Indexes of scopes (-1: not visited yet). */
  private final IntList index = new IntList();
  /** Lowlinks of scopes. */
  private final IntList lowlink = new IntList();
  /** Counter for the next free index. */
  private int next;
  /** Static functions and variables of the query (others belong to bound function items). */
  private final Set<Scope> declared = Collections.newSetFromMap(new IdentityHashMap<>());

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
    for(final ArrayList<Scope> scps : new QueryCompiler(cc.qc).scopes(cc.qc.main)) {
      scps.getFirst().compile(cc);
    }
  }

  /**
   * Computes the scopes.
   * @param main reference to main module
   * @return scopes
   */
  private ArrayList<ArrayList<Scope>> scopes(final MainModule main) {
    addScope(main);
    final ArrayList<ArrayList<Scope>> lists = new ArrayList<>();
    tarjan(0, lists);
    return lists;
  }

  /**
   * Algorithm of Tarjan for computing the strongly connected components of a graph.
   * @param id ID of current node
   * @param result scopes
   */
  private void tarjan(final int id, final ArrayList<ArrayList<Scope>> result) {
    final int idx = next++;
    index.set(id, idx);
    lowlink.set(id, idx);
    stack.push(id);
    onStack.set(id);

    for(final int w : adjacentTo(id)) {
      if(index.get(w) < 0) {
        // successor w has not yet been visited; recurse on it
        tarjan(w, result);
        lowlink.set(id, Math.min(lowlink.get(id), lowlink.get(w)));
      } else if(onStack.get(w)) {
        // successor w is in stack S and hence in the current SCC
        lowlink.set(id, Math.min(lowlink.get(id), index.get(w)));
      }
    }

    // if v is a root node, pop the stack and generate an SCC
    if(lowlink.get(id) == idx) {
      final ArrayList<Scope> out = new ArrayList<>();
      int w;
      do {
        w = stack.pop();
        onStack.clear(w);
        out.add(scopes.get(w));
      } while(w != id);
      result.add(out);
    }
  }

  /**
   * Adds a new scope and returns its ID.
   * @param scope scope to add
   * @return scope ID
   */
  private int addScope(final Scope scope) {
    final int id = scopes.size();
    scopes.add(scope);
    adjacent.add(null);
    index.add(-1);
    lowlink.add(-1);
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
        return var != curr && (!declared.contains(var) || add(var));
      }

      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        final StaticFunc func = call.func();
        return func == null || !declared.contains(func) || add(func);
      }

      @Override
      public boolean subScope(final Scope scope) {
        // bodies of function items are not recompiled (and may belong to other queries)
        if(!(curr instanceof FuncItem)) scope.reset();
        return scope.visit(this);
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        return add(func);
      }

      /**
       * Adds a neighbor of the currently inspected scope.
       * @param scope neighbor
       * @return {@code true} for convenience
       */
      private boolean add(final Scope scope) {
        final Integer old = ids.get(scope);
        if(old == null) {
          neighbors.add(addScope(scope));
        } else {
          neighbors.addUnique(old);
        }
        return true;
      }
    });
    return neighbors.finish();
  }
}
