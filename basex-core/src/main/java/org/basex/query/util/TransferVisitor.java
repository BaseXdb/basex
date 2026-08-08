package org.basex.query.util;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.function.*;

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
  /** Test for nodes that can be shared with another query context. */
  public static final Predicate<Data> SHAREABLE = data -> data == null || data.inMemory();

  /** Visited scopes. */
  private final Set<Scope> scopes = Collections.newSetFromMap(new IdentityHashMap<>());
  /** Dependency on the creating query (can be {@code null}). */
  private String dependency;

  /**
   * Returns the dependency that keeps a value from being passed on to another query context.
   * @param value value
   * @return dependency, or {@code null} if the value can be passed on
   */
  public static String dependency(final Value value) {
    final TransferVisitor visitor = new TransferVisitor();
    visitor.value(value);
    return visitor.dependency;
  }

  /**
   * Raises an error if a function item cannot be passed on to another query context.
   * @param function function item
   * @param ii input info (can be {@code null})
   * @throws QueryException query exception
   */
  public static void check(final FItem function, final InputInfo ii) throws QueryException {
    final String dep = dependency(function);
    if(dep != null) throw BASEX_TRANSFER_X_X.get(ii, dep, function);
  }

  @Override
  public boolean value(final Value value) {
    // values without function items are checked by a single call
    try {
      if(value.materialized(SHAREABLE, false, null)) return true;
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
    // a captured query focus is invisible to the function signature
    if(!func.simple()) return reject("context value");
    return cached(func) || func.visit(this);
  }

  @Override
  public boolean staticFuncCall(final StaticFuncCall call) {
    final StaticFunc func = call.func();
    return func == null ? reject("unresolved function call") : cached(func) || func.visit(this);
  }

  @Override
  public boolean staticVar(final StaticVar var) {
    // the value is cached in the shared declaration
    return reject("static variable $" + Token.string(var.name.prefixString()));
  }

  @Override
  public boolean database(final Data data) {
    return SHAREABLE.test(data) || reject("database '" + data.meta.name + '\'');
  }

  @Override
  public boolean javaCall(final JavaCall call) {
    return reject("Java code");
  }

  /**
   * Checks if an item has no dependency on persistent data.
   * @param item item
   * @return result of check
   */
  private boolean materialized(final Item item) {
    try {
      if(item.materialized(SHAREABLE, false, null)) return true;
    } catch(final QueryException ex) {
      Util.debug(ex);
    }
    return reject("persistent data");
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
