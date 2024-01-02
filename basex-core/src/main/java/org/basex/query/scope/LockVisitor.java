package org.basex.query.scope;

import java.util.*;
import java.util.function.*;

import org.basex.core.locks.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * Lock visitor.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class LockVisitor extends ASTVisitor {
  /** Visited scopes. */
  private final IdentityHashMap<Scope, Object> scopes = new IdentityHashMap<>();
  /** Reference to lock list. */
  private final LockList ll;
  /** Focus level. */
  private int level;

  /**
   * Constructor.
   * @param ll lock list
   * @param root root flag
   */
  public LockVisitor(final LockList ll, final boolean root) {
    this.ll = ll;
    level = root ? 0 : 1;
  }

  @Override
  public boolean lock(final String lock) {
    final boolean local = lock != null;
    if(local) {
      // if context item is found on top level, it will refer to currently opened database
      if(lock != Locking.CONTEXT || level == 0) ll.add(lock);
    }
    return local;
  }

  @Override
  public boolean lock(final Supplier<ArrayList<String>> list) {
    for(final String lock : list.get()) {
      if(!lock(lock)) return false;
    }
    return true;
  }

  @Override
  public void enterFocus() {
    level++;
  }

  @Override
  public void exitFocus() {
    level--;
  }

  @Override
  public boolean staticVar(final StaticVar var) {
    return cached(var) || var.visit(this);
  }

  @Override
  public boolean staticFuncCall(final StaticFuncCall call) {
    final StaticFunc func = call.func();
    return cached(func) || focusAndVisit(func);
  }

  @Override
  public boolean inlineFunc(final Scope scope) {
    return focusAndVisit(scope);
  }

  @Override
  public boolean funcItem(final FuncItem func) {
    return cached(func) || focusAndVisit(func);
  }

  /**
   * Caches a scope.
   * @param scope scope (ignored if {@code null})
   * @return if scope has already been cached
   */
  private boolean cached(final Scope scope) {
    if(scope == null || scopes.containsKey(scope)) return true;
    scopes.put(scope, null);
    return false;
  }

  /**
   * Visits a scope.
   * @param scope scope
   * @return if more expressions should be visited
   */
  private boolean focusAndVisit(final Scope scope) {
    enterFocus();
    final boolean more = scope.visit(this);
    exitFocus();
    return more;
  }
}