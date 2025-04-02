package org.basex.util;

/**
 * This class contains methods for performance measurements.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Performance {
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
    return nanoToMilli(nano, runs) + " ms" + (runs > 1 ? " (avg)" : "");
  }

  /**
   * Returns a formatted representation of the current memory consumption.
   * @return formatted memory consumption
   */
  public static String formatMemory() {
    return formatHuman(memory());
  }

  /**
   * Returns a human-readable representation for the specified size value (b, kB, MB, ...).
   * @param size value to be formatted
   * @return formatted size value
   */
  public static String formatHuman(final long size) {
    final String value = Long.toString(size);
    final int vl = value.length();
    return vl > 16 ? units(size, 1L << 40) + " PB" :
           vl > 13 ? units(size, 1L << 40) + " TB" :
           vl > 10 ? units(size, 1L << 30) + " GB" :
           vl >  7 ? units(size, 1L << 20) + " MB" :
           vl >  4 ? units(size, 1L << 10) + " kB" :
           value + " b";
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
    }
  }

  /**
   * Performs some garbage collection.
   * GC behavior in Java is a pretty complex task. Still, garbage collection
   * gets more probable when being called several times.
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
    return formatRuntime();
  }
}
