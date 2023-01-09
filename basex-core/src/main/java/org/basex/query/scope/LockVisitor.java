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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class LockVisitor extends ASTVisitor {
  /** Already visited scopes. */
  private final IdentityHashMap<Scope, Object> funcs = new IdentityHashMap<>();
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
    if(funcs.containsKey(var)) return true;
    funcs.put(var, null);
    return var.visit(this);
  }

  @Override
  public boolean staticFuncCall(final StaticFuncCall call) {
    return func(call.func());
  }

  @Override
  public boolean inlineFunc(final Scope scope) {
    enterFocus();
    final boolean ac = scope.visit(this);
    exitFocus();
    return ac;
  }

  @Override
  public boolean funcItem(final FuncItem func) {
    return func(func);
  }

  /**
   * Visits a scope.
   * @param scope scope
   * @return if more expressions should be visited
   */
  private boolean func(final Scope scope) {
    if(funcs.containsKey(scope)) return true;
    funcs.put(scope, null);
    enterFocus();
    final boolean ac = scope.visit(this);
    exitFocus();
    return ac;
  }
}