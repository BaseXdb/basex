package org.basex.query;

import java.util.*;

import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class compiles all components of the query that are needed in an order that
 * maximizes the amount of inlining possible.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class QueryCompiler {
  /** Number of scopes from which on linear search is replaced by a hash map. */
  private static final int MAP_THRESHOLD = 16;

  /** Query context. */
  private final QueryContext ctx;

  /** Result list. */
  private final ArrayList<Scope[]> result = new ArrayList<Scope[]>();
  /** Node stack. */
  private final IntList stack = new IntList();
  /** Index and lowlink list. */
  private final IntList list = new IntList();
  /** Counter for the next free index. */
  private int next;

  /** Adjacency list. */
  final ArrayList<int[]> adjacent = new ArrayList<int[]>();
  /** Declaration list. */
  final ArrayList<Scope> scopes = new ArrayList<Scope>();
  /** Declaration list. */
  private IdentityHashMap<Scope, Integer> ids;

  /**
   * Constructor.
   * @param cx query context
   * @param root root expression
   */
  private QueryCompiler(final QueryContext cx, final Scope root) {
    ctx = cx;
    add(root);
  }

  /**
   * Compiles all necessary parts of this query.
   * @param ctx query context
   * @param root root expression
   * @throws QueryException compilation errors
   */
  public static void compile(final QueryContext ctx, final MainModule root)
      throws QueryException {
    if(!root.compiled()) new QueryCompiler(ctx, root).compile();
  }

  /**
   * Compiles all necessary parts of this query.
   * @throws QueryException compilation errors
   */
  private void compile() throws QueryException {
    // compile the used scopes only
    for(final Scope[] comp : components(0)) circCheck(comp).compile(ctx);

    // check for circular variable declarations without compiling the unused scopes
    for(final StaticVar v : ctx.vars)
      if(id(v) == -1)
        for(final Scope[] comp : components(add(v))) circCheck(comp);
  }

  /**
   * Checks if the given component contains a static variable that depends on itself.
   * @param comp component to check
   * @return scope to be compiled, the others are compiled recursively
   * @throws QueryException query exception
   */
  private Scope circCheck(final Scope[] comp) throws QueryException {
    if(comp.length > 1)
      for(final Scope scp : comp)
        if(scp instanceof StaticVar) throw Err.circVar(ctx, (StaticVar) scp);
    return comp[0];
  }

  /**
   * Returns the strongly connected components of the dependency graph.
   * @param p ID of the starting point
   * @return the components
   * @throws QueryException if a variable directly calls itself
   */
  private Iterable<Scope[]> components(final int p) throws QueryException {
    result.clear();
    tarjan(p);
    return result;
  }

  /**
   * Algorithm of Tarjan for computing the strongly connected components of a graph.
   * @param v current node
   * @throws QueryException if a variable directly calls itself
   */
  private void tarjan(final int v) throws QueryException {
    final int ixv = 2 * v, llv = ixv + 1, idx = next++;
    while(list.size() <= llv) list.add(-1);
    list.set(ixv, idx);
    list.set(llv, idx);

    stack.push(v);

    for(int w : adjacentTo(v)) {
      final int ixw = 2 * w, llw = ixw + 1;
      if(list.size() <= ixw || list.get(ixw) < 0) {
        // Successor w has not yet been visited; recurse on it
        tarjan(w);
        list.set(llv, Math.min(list.get(llv), list.get(llw)));
      } else if(stack.contains(w)) {
        // Successor w is in stack S and hence in the current SCC
        list.set(llv, Math.min(list.get(llv), list.get(ixw)));
      }
    }

    // If v is a root node, pop the stack and generate an SCC
    if(list.get(llv) == list.get(ixv)) {
      int w;
      Scope[] out = null;
      do {
        w = stack.pop();
        final Scope scp = scopes.get(w);
        out = out == null ? new Scope[] { scp } : Array.add(out, scp);
      } while(w != v);
      result.add(out);
    }
  }

  /**
   * Gets the ID of the given scope.
   * @param scp scope
   * @return id if existing, {@code null} otherwise
   */
  int id(final Scope scp) {
    if(ids != null) {
      final Integer id = ids.get(scp);
      return id == null ? -1 : id;
    }

    for(int i = 0; i < scopes.size(); i++) if(scopes.get(i) == scp) return i;
    return -1;
  }

  /**
   * Adds a new scope and returns its ID.
   * @param scp scope to add
   * @return the scope's ID
   */
  int add(final Scope scp) {
    final int id = scopes.size();
    if(id == MAP_THRESHOLD) {
      ids = new IdentityHashMap<Scope, Integer>();
      for(final Scope s : scopes) ids.put(s, ids.size());
    }

    scopes.add(scp);
    adjacent.add(null);
    if(ids != null) ids.put(scp,  id);
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
    final IntList adj = new IntList();
    final boolean ok = curr.visit(new ASTVisitor() {
      @Override
      public boolean staticVar(final StaticVar var) {
        return var != curr && neighbor(var);
      }

      @Override
      public boolean funcCall(final UserFuncCall call) {
        return neighbor(call.func());
      }

      @Override
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }

      /**
       * Adds a neighbor of the currently inspected scope.
       * @param scp the neighbor
       * @return {@code true} for convenience
       */
      private boolean neighbor(final Scope scp) {
        final int old = id(scp), id = old == -1 ? add(scp) : old;
        if(old == -1 || !adj.contains(id)) adj.add(id);
        return true;
      }
    });
    if(!ok) throw Err.VARUNDEF.thrw(((StaticVar) curr).info, curr);
    return adj.toArray();
  }
}
