package org.basex.query;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class compiles all components of the query that are needed in an order that
 * maximizes the amount of inlining possible.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
final class QueryCompiler {
  /** Number of scopes from which on linear search is replaced by a hash map. */
  private static final int MAP_THRESHOLD = 16;

  /** Compilation context. */
  private final CompileContext cc;

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
   * @param cc compilation context
   * @param root root expression
   */
  private QueryCompiler(final CompileContext cc, final Scope root) {
    this.cc = cc;
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
   * Compiles all necessary parts of this query.
   * @param cc compilation context
   * @param root root expression
   * @throws QueryException compilation errors
   */
  public static void compile(final CompileContext cc, final MainModule root) throws QueryException {
    if(!root.compiled()) {
      new QueryCompiler(cc, root).compile();
    }
  }

  /**
   * Compiles all necessary parts of this query.
   * @throws QueryException compilation errors
   */
  private void compile() throws QueryException {
    // compile the used scopes only, collect static functions
    final ArrayList<StaticFunc> funcs = new ArrayList<>();
    for(final Scope[] scps : scopes(0)) {
      final Scope scope = circCheck(scps);
      scope.comp(cc);
      if(scope instanceof StaticFunc) funcs.add((StaticFunc) scope);
    }

    // check for circular variable declarations without compiling the unused scopes
    for(final StaticVar var : cc.qc.vars) {
      if(id(var) == -1) {
        for(final Scope[] scope : scopes(add(var))) circCheck(scope);
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
  private static Scope circCheck(final Scope[] scopes) throws QueryException {
    if(scopes.length > 1) {
      for(final Scope scope : scopes) {
        if(scope instanceof StaticVar) {
          final StaticVar var = (StaticVar) scope;
          throw CIRCVAR_X.get(var.info, var.id());
        }
      }
    }
    return scopes[0];
  }

  /**
   * Returns the strongly connected scopes of the dependency graph.
   * @param p ID of the starting point
   * @return scopes
   * @throws QueryException if a variable directly calls itself
   */
  private Iterable<Scope[]> scopes(final int p) throws QueryException {
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

    final int ss = scopes.size();
    for(int s = 0; s < ss; s++) {
      if(scopes.get(s) == scp) return s;
    }
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
      public boolean staticVar(final StaticVar var) { return var != curr && neighbor(var); }
      @Override
      public boolean staticFuncCall(final StaticFuncCall call) { return neighbor(call.func()); }
      @Override
      public boolean inlineFunc(final Scope scope) { return scope.visit(this); }
      @Override
      public boolean funcItem(final FuncItem func) { return neighbor(func); }

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
      throw CIRCREF_X.get(var.info, "$" + var.name);
    }
    return adj.finish();
  }
}
