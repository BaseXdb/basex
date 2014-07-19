package org.basex.util;

/**
 * This class contains methods for performance measurements.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Performance {
  /** Performance timer, using nano seconds. */
  private long time = System.nanoTime();

  /**
   * Returns the measured execution time in nanoseconds.
   * @return execution time
   */
  public long time() {
    final long time2 = System.nanoTime();
    final long diff = time2 - time;
    time = time2;
    return diff;
  }

  /**
   * Returns the measured execution time in milliseconds and reinitializes
   * the timer.
   * @return execution time
   */
  public String getTime() {
    return getTime(1);
  }

  /**
   * Returns the measured execution time in milliseconds, divided by the
   * number of runs, and reinitializes the timer.
   * @param runs number of runs
   * @return execution time
   */
  public String getTime(final int runs) {
    final long time2 = System.nanoTime();
    final String t = getTime(time2 - time, runs);
    time = time2;
    return t;
  }

  /**
   * Returns a string with the measured execution time in milliseconds.
   * @param time measured time in nanoseconds
   * @param runs number of runs
   * @return execution time
   */
  public static String getTime(final long time, final int runs) {
    return Math.round(time / 10000d / runs) / 100d + " ms" + (runs > 1 ? " (avg)" : "");
  }

  /**
   * Returns a formatted representation of the current memory consumption.
   * @return memory consumption
   */
  public static String getMemory() {
    return format(memory());
  }

  /**
   * Formats a number according to the binary size orders (KB, MB, ...).
   * @param size value to be formatted
   * @return formatted size value
   */
  public static String format(final long size) {
    return format(size, true, 5);
  }

  /**
   * Formats a file size according to the binary size orders (KB, MB, ...).
   * @param size file size
   * @param det detailed suffix
   * @return formatted size value
   */
  public static String format(final long size, final boolean det) {
    return format(size, det, 0);
  }

  /**
   * Formats a file size according to the binary size orders (KB, MB, ...),
   * adding the specified offset to the orders of magnitude.
   * @param size file size
   * @param det detailed suffix
   * @param off offset: higher values will result in more digits
   * @return formatted size value
   */
  private static String format(final long size, final boolean det, final int off) {
    if(size >= 1L << 50 + off) return (size + (1L << 49) >> 50) + " PB";
    if(size >= 1L << 40 + off) return (size + (1L << 39) >> 40) + " TB";
    if(size >= 1L << 30 + off) return (size + (1L << 29) >> 30) + " GB";
    if(size >= 1L << 20 + off) return (size + (1L << 19) >> 20) + " MB";
    if(size >= 1L << 10 + off) return (size + (1L <<  9) >> 10) + " KB";
    return size + (det ? " Byte"  + (size == 1 ? "" : "s") : " B");
  }

  /**
   * Sleeps the specified number of milliseconds.
   * @param ms time in milliseconds to wait
   */
  public static void sleep(final long ms) {
    try { Thread.sleep(Math.max(0, ms)); } catch(final InterruptedException ignored) { }
  }

  /**
   * Performs some garbage collection.
   * GC behavior in Java is a pretty complex task. Still, garbage collection
   * can be forced by calling it several times.
   * @param number number of times to execute garbage collection
   */
  public static void gc(final int number) {
    for(int i = 0; i < number; ++i) System.gc();
  }

  /**
   * Returns the current memory consumption in bytes.
   * @return memory consumption
   */
  public static long memory() {
    final Runtime rt = Runtime.getRuntime();
    return rt.totalMemory() - rt.freeMemory();
  }

  @Override
  public String toString() {
    return getTime();
  }
}
