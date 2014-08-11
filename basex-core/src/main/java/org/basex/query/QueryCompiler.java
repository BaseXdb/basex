package org.basex.query;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class compiles all components of the query that are needed in an order that
 * maximizes the amount of inlining possible.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
final class QueryCompiler {
  /** Number of scopes from which on linear search is replaced by a hash map. */
  private static final int MAP_THRESHOLD = 16;

  /** Query context. */
  private final QueryContext qc;

  /** Result list. */
  private final ArrayList<Scope[]> result = new ArrayList<>();
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
  private IdentityHashMap<Scope, Integer> ids;

  /**
   * Constructor.
   * @param qc query context
   * @param root root expression
   */
  private QueryCompiler(final QueryContext qc, final Scope root) {
    this.qc = qc;
    add(root);
  }

  /**
   * Gathers all declarations (functions and static variables) used by the given main module.
   * @param main the main module to start from
   * @return list of all declarations that the main module uses
   */
  public static List<StaticDecl> usedDecls(final MainModule main) {
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
      public boolean inlineFunc(final Scope sub) {
        if(map.put(sub, sub) == null) sub.visit(this);
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
   * Compiles all necessary parts of this query.
   * @param qc query context
   * @param root root expression
   * @throws QueryException compilation errors
   */
  public static void compile(final QueryContext qc, final MainModule root) throws QueryException {
    if(!root.compiled()) new QueryCompiler(qc, root).compile();
  }

  /**
   * Compiles all necessary parts of this query.
   * @throws QueryException compilation errors
   */
  private void compile() throws QueryException {
    // compile the used scopes only
    for(final Scope[] comp : components(0)) circCheck(comp).compile(qc);

    // check for circular variable declarations without compiling the unused scopes
    for(final StaticVar v : qc.vars) {
      if(id(v) == -1) for(final Scope[] comp : components(add(v))) circCheck(comp);
    }
  }

  /**
   * Checks if the given component contains a static variable that depends on itself.
   * @param comp component to check
   * @return scope to be compiled, the others are compiled recursively
   * @throws QueryException query exception
   */
  private static Scope circCheck(final Scope[] comp) throws QueryException {
    if(comp.length > 1) {
      for(final Scope scp : comp) {
        if(scp instanceof StaticVar) throw circVarError((StaticVar) scp);
      }
    }
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

    for(final int w : adjacentTo(v)) {
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
  private int id(final Scope scp) {
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
  private int add(final Scope scp) {
    final int id = scopes.size();
    if(id == MAP_THRESHOLD) {
      ids = new IdentityHashMap<>();
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
    final IntList adj = new IntList(0);
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
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }

      @Override
      public boolean funcItem(final FuncItem func) {
        return neighbor(func);
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
    if(!ok) {
      final StaticVar var = (StaticVar) curr;
      throw Err.CIRCREF_X.get(var.info, "$" + var.name);
    }
    return adj.finish();
  }
}
