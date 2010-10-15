package org.basex.util.locale;

import java.util.HashMap;

import org.basex.util.Util;

/**
 * Abstract class for formatting data in different languages.
 * Implementations are expected to return strings in a capitalized syntax.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Formatter {
  /** Language code: English. */
  private static final String EN = "en";

  /** Formatter instances. */
  private static final HashMap<String, Formatter> MAP =
    new HashMap<String, Formatter>();

  // initialize hash map with English formatter as default
  static { MAP.put(EN, new FormatterEN()); }

  /**
   * Returns a formatter for the specified language.
   * @param ln language
   * @return formatter instance
   */
  public static Formatter get(final String ln) {
    // check if formatter has already been created
    Formatter form = MAP.get(ln);
    if(form == null) {
      try {
        // create new instance (class name + language in upper case)
        final String clz = Util.name(Formatter.class) + ln.toUpperCase();
        form = (Formatter) Class.forName(clz).newInstance();
        MAP.put(ln, form);
      } catch(final Exception ex) {
        // instantiation not successful: return default formatter
        form = MAP.get(EN);
      }
    }
    return form;
  }

  /**
   * Returns a word representation for the specified number.
   * @param n number to be formatted
   * @param ord ordinal suffix
   * @return token
   */
  public abstract byte[] word(final long n, final String ord);

  /**
   * Returns an ordinal representation for the specified number.
   * @param n number to be formatted
   * @param ord ordinal suffix
   * @return ordinal
   */
  public abstract byte[] ordinal(final long n, final String ord);

  /**
   * Returns the specified month (0-11).
   * @param n number to be formatted
   * @param min minimum length
   * @param max maximum length
   * @return month
   */
  public abstract byte[] month(final int n, final int min, final int max);

  /**
   * Returns the specified day of the week (0-6, Sunday-Saturday).
   * @param n number to be formatted
   * @param min minimum length
   * @param max maximum length
   * @return day of week
   */
  public abstract byte[] day(final int n, final int min, final int max);

  /**
   * Returns the am/pm marker.
   * @param am am flag
   * @return am/pm marker
   */
  public abstract byte[] ampm(final boolean am);

  /**
   * Returns the calendar.
   * @return calendar
   */
  public abstract byte[] calendar();

  /**
   * Returns the era.
   * @param year year
   * @return era
   */
  public abstract byte[] era(final int year);
}
