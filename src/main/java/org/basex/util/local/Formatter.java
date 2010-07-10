package org.basex.util.local;

import java.util.HashMap;

/**
 * Abstract class for formatting data in different languages.
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

  // initialize hash map with English default formatter
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
        final String clz = Formatter.class.getSimpleName() + ln.toUpperCase();
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
  public abstract byte[] word(final long n, final byte[] ord);

  /**
   * Returns an ordinal representation for the specified number.
   * @param n number to be formatted
   * @param ord ordinal suffix
   * @return ordinal
   */
  public abstract byte[] ordinal(final long n, final byte[] ord);
}
