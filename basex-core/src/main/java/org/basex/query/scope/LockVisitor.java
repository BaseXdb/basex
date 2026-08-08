package org.basex.query.scope;

import java.util.*;
import java.util.function.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * Lock visitor.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LockVisitor extends ASTVisitor {
  /** Visited scopes. */
  private final IdentityHashMap<Scope, Object> scopes = new IdentityHashMap<>();
  /** Reference to the read and write locks. */
  private final Locks locks;
  /** Updating query. */
  private final boolean updating;
  /** Focus level. */
  private int level;
  /** Nesting level of modify clauses. */
  private int modify;
  /** Indicates if the target of an update could not be resolved statically. */
  private boolean unresolved;

  /**
   * Constructor.
   * @param locks read and write locks
   * @param updating updating query
   * @param root root flag
   */
  public LockVisitor(final Locks locks, final boolean updating, final boolean root) {
    this.locks = locks;
    this.updating = updating;
    level = root ? 0 : 1;
  }

  @Override
  public boolean lock(final String lock, final boolean write) {
    final boolean local = lock != null;
    if(local) {
      // if context value is found on top level, it will refer to currently opened database
      if(lock != Locking.CONTEXT || level == 0) (write ? locks.writes : locks.reads).add(lock);
    }
    return local;
  }

  @Override
  public boolean lock(final Supplier<ArrayList<String>> list, final boolean write) {
    for(final String lock : list.get()) {
      if(!lock(lock, write)) return false;
    }
    return true;
  }

  @Override
  public void queryLock(final Supplier<ArrayList<String>> list) {
    final LockList ll = updating ? locks.writes : locks.reads;
    for(final String lock : list.get()) ll.add(lock);
  }

  @Override
  public void unresolvedTarget() {
    // updates in modify clauses are restricted to copied nodes
    if(modify == 0) unresolved = true;
  }

  @Override
  public void enterModify() {
    modify++;
  }

  @Override
  public void exitModify() {
    modify--;
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
    return cached(var) || visit(var, false);
  }

  @Override
  public boolean staticFuncCall(final StaticFuncCall call) {
    final StaticFunc func = call.func();
    return func == null || cached(func) || visit(func, true);
  }

  @Override
  public boolean subScope(final Scope scope) {
    return visit(scope, true);
  }

  @Override
  public boolean funcItem(final FuncItem func) {
    return cached(func) || visit(func, true);
  }

  @Override
  public boolean value(final Value value) {
    // function items in a sequence, map or array are invisible to the tree walk
    if(value.seqType().mayBeFunction()) {
      // large structures are not traversed; all databases are locked instead
      if(value.size() > CompileContext.MAX_PREEVAL) return false;
      for(final Item item : value) {
        if(item instanceof final FuncItem func) {
          if(!funcItem(func)) return false;
        } else if(item instanceof final XQStruct struct &&
            struct.funcType().declType.mayBeFunction()) {
          final long size = struct.structSize();
          if(size > CompileContext.MAX_PREEVAL) return false;
          for(long s = 0; s < size; s++) {
            if(!value(struct.valueAt(s))) return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean transferred(final Expr expr) {
    // a function item is invoked by another query, which registers its own locks
    return expr instanceof FuncItem || expr.accept(this);
  }

  /**
   * Resets the focus level before the context expression is visited.
   */
  public void resetFocus() {
    level = 0;
  }

  /**
   * Promotes all read locks to write locks if an update target could not be resolved statically.
   */
  public void finish() {
    // sound as all databases that can be reached by the query have been read-locked
    if(unresolved) {
      locks.writes.add(locks.reads);
      locks.reads.reset();
    }
  }

  /**
   * Caches a scope.
   * @param scope scope (ignored if {@code null})
   * @return if scope has already been cached
   */
  private boolean cached(final Scope scope) {
    if(scopes.containsKey(scope)) return true;
    scopes.put(scope, null);
    return false;
  }

  /**
   * Visits a scope outside the modify clauses of the current expression.
   * @param scope scope
   * @param focus enter a new focus
   * @return if more expressions should be visited
   */
  private boolean visit(final Scope scope, final boolean focus) {
    if(focus) enterFocus();
    final int tmp = modify;
    modify = 0;
    final boolean more = scope.visit(this);
    modify = tmp;
    if(focus) exitFocus();
    return more;
  }
}
