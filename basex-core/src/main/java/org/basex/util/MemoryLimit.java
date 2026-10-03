package org.basex.util;

import java.lang.ref.*;
import java.lang.ref.Cleaner.*;
import java.util.concurrent.atomic.*;

/**
 * Main-memory limit for the temporary structures of database and index builders.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MemoryLimit {
  /** Default memory limit. */
  public static final long MAX = Runtime.getRuntime().maxMemory() / 2;
  /** Memory held by temporary instances. */
  private static final AtomicLong HELD = new AtomicLong();
  /** Releases the memory of unreferenced instances. */
  private static final Cleaner CLEANER = Cleaner.create();
  /** Memory limit. */
  private static long max = MAX;

  /** Private constructor. */
  private MemoryLimit() { }

  /**
   * Assigns the memory limit.
   * @param bytes limit in bytes
   */
  public static void max(final long bytes) {
    max = bytes;
  }

  /**
   * Checks if a temporary structure must be moved to disk.
   * @param memory estimated memory consumption of the structure
   * @return result of check
   */
  public static boolean exceeded(final long memory) {
    return memory >= max;
  }

  /**
   * Returns the memory that is held by temporary instances.
   * @return memory in bytes
   */
  public static long held() {
    return HELD.get();
  }

  /**
   * Registers memory that is held by a temporary instance until it is cleaned or unreferenced.
   * @param instance temporary instance
   * @param memory estimated memory consumption
   * @return cleanable action that releases the memory
   */
  public static Cleanable hold(final Object instance, final long memory) {
    HELD.addAndGet(memory);
    return CLEANER.register(instance, () -> HELD.addAndGet(-memory));
  }
}
