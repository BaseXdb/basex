package org.basex.index.stats;

/**
 * Value types, used for index statistics and query optimizations.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class StatsType {
  /** Hidden constructor. */
  private StatsType() { }

  /** Values are arbitrary strings. */
  public static final byte STRING = 0;
  /** A limited number of distinct strings exists. */
  public static final byte STRING_CATEGORY = 1;
  /** All values are of type integer.  */
  public static final byte INTEGER = 2;
  /** All values are of type double. */
  public static final byte DOUBLE = 3;
  /** No values exist. */
  public static final byte NONE = 4;
  /** All distinct values are of type integer.  */
  public static final byte INTEGER_CATEGORY = 5;
  /** All distinct values are of type double. */
  public static final byte DOUBLE_CATEGORY = 6;

  /**
   * Indicates if no data exists.
   * @param type type
   * @return result of check
   */
  public static boolean isNone(final int type) {
    return type == NONE;
  }

  /**
   * Indicates if the specified type is numeric.
   * @param type type
   * @return result of check
   */
  public static boolean isNumeric(final int type) {
    return isInteger(type) || isDouble(type);
  }

  /**
   * Indicates if the specified type is an integer.
   * @param type type
   * @return result of check
   */
  public static boolean isInteger(final int type) {
    return type == INTEGER || type == INTEGER_CATEGORY;
  }

  /**
   * Indicates if the specified type is a double.
   * @param type type
   * @return result of check
   */
  public static boolean isDouble(final int type) {
    return type == DOUBLE || type == DOUBLE_CATEGORY;
  }

  /**
   * Indicates if the specified type is a string.
   * @param type type
   * @return result of check
   */
  public static boolean isString(final int type) {
    return type == STRING || type == STRING_CATEGORY;
  }

  /**
   * Indicates if the specified type is a category.
   * @param type type
   * @return result of check
   */
  public static boolean isCategory(final int type) {
    return type == INTEGER_CATEGORY || type == DOUBLE_CATEGORY || type == STRING_CATEGORY;
  }

  /**
   * Returns a string representation of the specified type.
   * @param type type
   * @return string
   */
  public static String toString(final int type) {
    return isInteger(type) ? "integer" : isDouble(type) ? "double" : isString(type) ? "string" :
      "none";
  }
}
