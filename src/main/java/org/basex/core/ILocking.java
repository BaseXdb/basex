package org.basex.core;

/**
 * Lock interface; will get obsolete in a later version.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface ILocking {

  /**
   * Puts read locks on an array of objects.
   *
   * Store and return the {@code token} for unlocking these objects again.
   * @param pr progress
   * @param objects Objects to put read locks on
   * @param <T> Allow all Object arrays with mutual comparable objects
   */
  <T extends Object & Comparable<? super T>> void acquire(
      final Progress pr, final Comparable<? extends T>[] objects);

  /**
   * Unlock all objects a transaction locked.
   * @param pr progress
   */
  void release(final Progress pr);
}
