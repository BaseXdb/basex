package org.basex.util;

import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * This class contains methods for performance measurements.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Performance {
  /** Method for retrieving thread allocation statistics (can be {@code null}). */
  private static final Method ALLOCATED = allocatedMethod();
  /** Size units. */
  private static final String[] SIZE_UNITS = { "B", "kB", "MB", "GB", "TB", "PB", "EB" };
  /** Factors between size units. */
  private static final int[] SIZE_FACTORS = { 1024, 1024, 1024, 1024, 1024, 1024 };
  /** Time units. */
  private static final String[] TIME_UNITS = { "ns", "µs", "ms", "s", "min", "h", "d" };
  /** Factors between time units. */
  private static final int[] TIME_FACTORS = { 1000, 1000, 1000, 60, 60, 24 };

  /** Performance timer, using nanoseconds. */
  private long time = System.nanoTime();

  /**
   * Returns the measured runtime in nanoseconds and resets the timer.
   * @return runtime
   */
  public long nanoRuntime() {
    return nanoRuntime(true);
  }

  /**
   * Returns the measured runtime in nanoseconds.
   * @param reset reset timer
   * @return runtime
   */
  public long nanoRuntime(final boolean reset) {
    final long time2 = System.nanoTime(), diff = time2 - time;
    if(reset) time = time2;
    return diff;
  }

  /**
   * Returns the measured runtime in milliseconds and resets the timer.
   * @return runtime
   */
  public String formatRuntime() {
    return formatRuntime(1);
  }

  /**
   * Returns the measured runtime in milliseconds, divided by the number of runs,
   * and resets the timer.
   * @param runs number of runs
   * @return runtime
   */
  public String formatRuntime(final int runs) {
    final long time2 = System.nanoTime();
    final String t = formatNano(time2 - time, runs);
    time = time2;
    return t;
  }

  /**
   * Returns a string with the specified time in milliseconds.
   * @param nano time in nanoseconds
   * @return time in milliseconds with 2 decimal places
   */
  public static double nanoToMilli(final long nano) {
    return nanoToMilli(nano, 1);
  }

  /**
   * Returns a string with the specified time in milliseconds.
   * @param nano time in nanoseconds
   * @param runs number of runs
   * @return time in milliseconds with 2 decimal places
   */
  public static double nanoToMilli(final long nano, final int runs) {
    return Math.round(nano / 10000.0d / runs) / 100.0d;
  }

  /**
   * Returns a string with the specified time in milliseconds.
   * @param nano time in nanoseconds
   * @return time
   */
  public static String formatNano(final long nano) {
    return formatNano(nano, 1);
  }

  /**
   * Returns a string with the specified time in milliseconds.
   * @param nano measured time in nanoseconds
   * @param runs number of runs
   * @return formatted time in milliseconds with 2 decimal places
   */
  public static String formatNano(final long nano, final int runs) {
    final String ms = String.format(Locale.ENGLISH, "%.2f", nanoToMilli(nano, runs));
    return ms + " ms" + (runs > 1 ? " (avg)" : "");
  }

  /**
   * Returns a formatted representation of the current memory consumption.
   * @return formatted memory consumption
   */
  public static String formatMemory() {
    return formatHuman(memory());
  }

  /**
   * Returns a human-readable representation for the specified size value (B, kB, MB, ...).
   * @param size size in bytes
   * @return formatted size value
   */
  public static String formatHuman(final double size) {
    return formatUnits(size, SIZE_UNITS, SIZE_FACTORS);
  }

  /**
   * Returns a human-readable representation for the specified time (ns, µs, ms, s, ...).
   * @param seconds time in seconds
   * @return formatted time
   */
  public static String formatTime(final double seconds) {
    return seconds == 0 ? "0 s" : formatUnits(seconds * 1e9, TIME_UNITS, TIME_FACTORS);
  }

  /**
   * Returns a value with the largest fitting unit and at most one decimal place.
   * @param value value in the smallest unit
   * @param units units
   * @param factors factors between units
   * @return formatted value
   */
  private static String formatUnits(final double value, final String[] units, final int[] factors) {
    double v = Math.abs(value);
    int u = 0;
    while(u < factors.length && round(v) >= factors[u]) v /= factors[u++];
    final double r = round(v);
    final String string = String.format(Locale.ENGLISH, "%.1f", value < 0 && r != 0 ? -r : r);
    return (string.endsWith(".0") ? string.substring(0, string.length() - 2) : string) + ' ' +
      units[u];
  }

  /**
   * Rounds a non-negative value to one decimal place, with halves rounded up.
   * @param value value
   * @return rounded value
   */
  private static double round(final double value) {
    return Math.floor(value * 10 + 0.5) / 10;
  }

  /**
   * Sleeps the specified number of milliseconds.
   * @param ms time in milliseconds to wait
   */
  public static void sleep(final long ms) {
    try {
      Thread.sleep(Math.max(0, ms));
    } catch(final InterruptedException ex) {
      Util.debug(ex);
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Performs some garbage collection.
   * GC behavior in Java is a pretty complex task. Still, garbage collection
   * gets more probable when being called several times.
   * @param count number of times to execute garbage collection
   */
  public static void gc(final int count) {
    for(int c = 0; c < count; c++) System.gc();
  }

  /**
   * Returns the current memory consumption in bytes.
   * @return memory consumption
   */
  public static long memory() {
    final Runtime rt = Runtime.getRuntime();
    return rt.totalMemory() - rt.freeMemory();
  }

  /**
   * Returns the number of bytes that may still be allocated before the heap limit is reached.
   * @return available memory
   */
  public static long available() {
    final Runtime rt = Runtime.getRuntime();
    return rt.maxMemory() - rt.totalMemory() + rt.freeMemory();
  }

  /**
   * Returns the total number of bytes that have been allocated by the specified thread.
   * @param thread thread
   * @return allocated bytes, or {@code -1} if allocation statistics are unavailable
   */
  public static long allocated(final Thread thread) {
    try {
      if(ALLOCATED != null) {
        return (Long) ALLOCATED.invoke(ManagementFactory.getThreadMXBean(), thread.threadId());
      }
    } catch(final ReflectiveOperationException ex) {
      Util.debug(ex);
    }
    return -1;
  }

  /**
   * Returns the method for retrieving thread allocation statistics. The method is looked up by
   * name, as it belongs to a JVM-specific extension that is not supplied by every runtime.
   * @return method, or {@code null} if allocation statistics are unavailable
   */
  private static Method allocatedMethod() {
    try {
      final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
      final Class<?> clazz = Class.forName("com.sun.management.ThreadMXBean");
      if(clazz.isInstance(bean) &&
         clazz.getMethod("isThreadAllocatedMemoryEnabled").invoke(bean) == Boolean.TRUE) {
        return clazz.getMethod("getThreadAllocatedBytes", long.class);
      }
    } catch(final ReflectiveOperationException ex) {
      Util.debug(ex);
    }
    return null;
  }

  @Override
  public String toString() {
    return formatRuntime();
  }
}
