package org.basex.query.util;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.java.*;
import org.basex.query.scope.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Checks if a value can be passed on to another query context.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class TransferVisitor extends ASTVisitor {
  /** Test for nodes that can be shared with another query context (databases can be updated). */
  public static final Predicate<Data> SHAREABLE = data -> data == null;

  /** Visited scopes. */
  private final Set<Scope> scopes = Collections.newSetFromMap(new IdentityHashMap<>());
  /** Test for nodes that can be shared. */
  private final Predicate<Data> test;
  /** Values that have already been materialized. */
  private final Set<Value> copies;
  /** Dependency on the creating query (can be {@code null}). */
  private String dependency;

  /**
   * Constructor.
   * @param test test for nodes that can be shared
   * @param copies values that have already been materialized
   */
  private TransferVisitor(final Predicate<Data> test, final Set<Value> copies) {
    this.test = test;
    this.copies = copies;
  }

  /**
   * Returns a test for nodes that are neither persistent nor shared with other queries.
   * @param ctx database context
   * @return test
   */
  public static Predicate<Data> unshared(final Context ctx) {
    final Map<Data, Boolean> pinned = new IdentityHashMap<>();
    return data -> data == null || data.inMemory() &&
        !pinned.computeIfAbsent(data, ctx.datas::pinned);
  }

  /**
   * Raises an error if a function item cannot be passed on to another query context.
   * @param function function item
   * @param test test for nodes that can be shared
   * @param copies values that have already been materialized
   * @param ii input info (can be {@code null})
   * @throws QueryException query exception
   */
  public static void check(final FItem function, final Predicate<Data> test,
      final Set<Value> copies, final InputInfo ii) throws QueryException {
    final TransferVisitor visitor = new TransferVisitor(test, copies);
    visitor.value(function);
    if(visitor.dependency != null) throw BASEX_EVAL_X_X.get(ii, visitor.dependency, function);
  }

  /**
   * Returns the dependency that keeps a value from being passed on to another query context.
   * @param value value
   * @param test test for nodes that can be shared
   * @return dependency, or {@code null} if the value can be passed on
   */
  public static String dependency(final Value value, final Predicate<Data> test) {
    final TransferVisitor visitor = new TransferVisitor(test, Set.of());
    visitor.value(value);
    return visitor.dependency;
  }

  @Override
  public boolean value(final Value value) {
    if(copies.contains(value)) return true;
    // values without function items are checked by a single call
    try {
      if(value.materialized(test, false, null)) return true;
    } catch(final QueryException ex) {
      Util.debug(ex);
    }
    for(final Item item : value) {
      if(item instanceof final FuncItem func) {
        if(!funcItem(func)) return false;
      } else if(item instanceof final XQArray array) {
        for(final Value member : array.members()) {
          if(!value(member)) return false;
        }
      } else if(item instanceof final XQMap map) {
        try {
          if(!map.test((key, val) -> value(val))) return false;
        } catch(final QueryException ex) {
          Util.debug(ex);
          return reject("map entry");
        }
      } else if(item instanceof XQJava) {
        return reject("Java code");
      } else if(!materialized(item)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean funcItem(final FuncItem func) {
    if(!func.simple()) {
      // the function accesses the query focus, which is invisible to its signature
      final QueryFocus focus = func.focus();
      if(focus == null) return reject("context value");
      if(focus.value != null && !value(focus.value)) return false;
    }
    return cached(func) || func.visit(this);
  }

  @Override
  public boolean staticFuncCall(final StaticFuncCall call) {
    final StaticFunc func = call.func();
    return func == null ? reject("unresolved function call") : cached(func) || func.visit(this);
  }

  @Override
  public boolean staticVar(final StaticVar var) {
    // an evaluated variable is checked like any other value; otherwise, its declaration is visited
    final Value val = var.value;
    if(val != null ? value(val) : cached(var) || var.visit(this)) return true;
    return reject(dependency + " in static variable $" + Token.string(var.name.prefixString()));
  }

  @Override
  public boolean database(final Data data) {
    return test.test(data) || reject("database '" + data.meta.name + '\'');
  }

  @Override
  public boolean javaCall(final JavaCall call) {
    return reject("Java code");
  }

  /**
   * Checks if an item has no dependency on a database node.
   * @param item item
   * @return result of check
   */
  private boolean materialized(final Item item) {
    try {
      if(item.materialized(test, false, null)) return true;
    } catch(final QueryException ex) {
      Util.debug(ex);
    }
    return reject("database node");
  }

  /**
   * Caches a scope.
   * @param scope scope
   * @return if the scope has already been cached
   */
  private boolean cached(final Scope scope) {
    return !scopes.add(scope);
  }

  /**
   * Registers a dependency and stops the visit.
   * @param dep dependency
   * @return {@code false}
   */
  private boolean reject(final String dep) {
    dependency = dep;
    return false;
  }
}
