package org.basex.query.util;

import java.util.*;
import java.util.function.*;

/**
 * Notifications on the databases that need to be locked by a query.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@SuppressWarnings("unused")
public interface LockCollector {
  /**
   * Notifies the visitor of database locks.
   * @param list function supplying lock strings
   * @param write write access
   * @return if the locks could be resolved statically ({@code true} by default)
   */
  default boolean lock(final Supplier<ArrayList<String>> list, final boolean write) {
    return true;
  }

  /**
   * Notifies the visitor of a database lock.
   * @param lock lock string (can be {@code null})
   * @param write write access
   * @return if the lock could be resolved statically ({@code true} by default)
   */
  default boolean lock(final String lock, final boolean write) {
    return true;
  }

  /**
   * Notifies the visitor of custom query locks.
   * @param list function supplying lock strings
   */
  default void queryLock(final Supplier<ArrayList<String>> list) { }

  /**
   * Notifies the visitor of an update whose target database cannot be resolved statically.
   */
  default void unresolvedTarget() { }
}
