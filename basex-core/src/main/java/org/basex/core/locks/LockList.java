package org.basex.core.locks;

import static org.basex.core.Text.*;

import java.util.*;

import org.basex.data.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * List with strings that serve as lock keys.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class LockList implements Iterable<String> {
  /** Local locks. */
  private final StringList list = new StringList(0);
  /** Global locking flag. */
  private boolean global;

  /**
   * Adds a local lock if no global lock has been added.
   * @param lock lock to be added
   * @return self reference
   */
  public LockList add(final String lock) {
    if(!global) list.add(lock);
    return this;
  }

  /**
   * Indicates if local locks exist.
   * @return result of check
   */
  public boolean local() {
    return !list.isEmpty();
  }

  /**
   * Sets a global lock. All existing and following local locks will be ignored.
   */
  public void addGlobal() {
    global = true;
    list.reset();
  }

  /**
   * Resets the lock list.
   */
  public void reset() {
    global = false;
    list.reset();
  }

  /**
   * Indicates if a global lock exists.
   * @return result of check
   */
  public boolean global() {
    return global;
  }

  /**
   * Indicates if any lock exists.
   * @return result of check
   */
  public boolean locking() {
    return global() || local();
  }

  /**
   * Adds another lock list. A global lock will be adopted.
   * @param locks lock list
   * @return self reference
   */
  public LockList add(final LockList locks) {
    if(!global) {
      if(locks.global) {
        addGlobal();
      } else {
        list.add(locks.list);
      }
    }
    return this;
  }

  /**
   * Removes all locks from the specified list.
   * @param locks lock list
   */
  public void remove(final LockList locks) {
    list.remove(locks.list);
  }

  /**
   * Returns the element at the specified position.
   * @param index element index
   * @return element
   */
  public String get(final int index) {
    return list.get(index);
  }

  /**
   * Checks if the specified lock is found in the list.
   * @param lock lock to be found
   * @return result of check
   */
  public boolean contains(final String lock) {
    return list.contains(lock);
  }

  /**
   * Returns the number of local locks.
   * @return number of local locks
   */
  public int size() {
    return list.size();
  }

  /**
   * Finishes the lock list.
   * Locks of type {@link Locking#COLLECTION} and {@link Locking#CONTEXT} will be replaced with the
   * name of the current database, if it exists, or deleted otherwise.
   * The resulting list will be sorted, and duplicates will be removed.
   * @param data data reference
   */
  public void finish(final Data data) {
    for(int s = 0; s < list.size(); s++) {
      final String lock = list.get(s);
      if(Strings.eq(lock, Locking.COLLECTION, Locking.CONTEXT)) {
        if(data == null) list.remove(s--);
        else list.set(s, data.meta.name);
      }
    }
    list.sort().unique();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(global) sb.append(GLOBAL);
    else if(list.isEmpty()) sb.append(NONE);
    else sb.append(LOCAL).append(' ').append(Arrays.toString(list.toArray()));
    return sb.toString();
  }

  @Override
  public Iterator<String> iterator() {
    return list.iterator();
  }
}
