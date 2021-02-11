package org.basex.util;

/**
 * This class contains methods for performance measurements.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Performance {
  /** Performance timer, using nano seconds. */
  private long time = System.nanoTime();

  /**
   * Returns the measured execution time in nanoseconds and resets the timer.
   * @return execution time
   */
  public long ns() {
    return ns(true);
  }

  /**
   * Returns the measured execution time in nanoseconds.
   * @param reset reset timer
   * @return execution time
   */
  public long ns(final boolean reset) {
    final long time2 = System.nanoTime();
    final long diff = time2 - time;
    if(reset) time = time2;
    return diff;
  }

  /**
   * Returns the measured execution time in milliseconds and resets the timer.
   * @return execution time
   */
  public String getTime() {
    return getTime(1);
  }

  /**
   * Returns the measured execution time in milliseconds, divided by the number of runs,
   * and resets the timer.
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
   * Returns the measured execution time in nanoseconds and resets the timer.
   * @param nano execution time in nanoseconds
   * @param runs number of runs
   * @return execution time
   */
  public static double ms(final long nano, final int runs) {
    return Math.round(nano / 10000.0d / runs) / 100.0d;
  }

  /**
   * Returns a string with the measured execution time in milliseconds.
   * @param nano measured time in nanoseconds
   * @param runs number of runs
   * @return execution time
   */
  public static String getTime(final long nano, final int runs) {
    return ms(nano, runs) + " ms" + (runs > 1 ? " (avg)" : "");
  }

  /**
   * Returns a formatted representation of the current memory consumption.
   * @return memory consumption
   */
  public static String getMemory() {
    return format(memory());
  }

  /**
   * Returns a human-readable representation for the specified size value (b, kB, MB, ...).
   * @param size value to be formatted
   * @return formatted size value
   */
  public static String format(final long size) {
    final String num = Long.toString(size);
    final int nl = num.length();
    if(nl > 16) return units(size, 1L << 40) + " PB";
    if(nl > 13) return units(size, 1L << 40) + " TB";
    if(nl > 10) return units(size, 1L << 30) + " GB";
    if(nl >  7) return units(size, 1L << 20) + " MB";
    if(nl >  4) return units(size, 1L << 10) + " kB";
    return num + " b";
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
   * @param count number of times to execute garbage collection
   */
  public static void gc(final int count) {
    for(int c = 0; c < count; ++c) System.gc();
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
   * Returns the rounded up number of units.
   * @param number number
   * @param size size of unit
   * @return units
   */
  private static long units(final long number, final long size) {
    return (number + size - 1) / size;
  }

  @Override
  public String toString() {
    return getTime();
  }
}
