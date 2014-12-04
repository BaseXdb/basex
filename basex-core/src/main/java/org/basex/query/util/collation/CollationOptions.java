package org.basex.query.util.collation;

import java.util.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Collation options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class CollationOptions extends Options {
  /** Initialization of locales. */
  protected static class Locales {
    /** Available locales, indexed by language code. */
    static HashMap<String, Locale> map = new HashMap<>();
    static {
      for(final Locale l : Locale.getAvailableLocales()) map.put(l.toString().replace('_', '-'), l);
    }
  }

  /**
   * Parses the specified options and returns the faulty key.
   * @param args arguments
   * @return error message
   */
  abstract Collation get(final String args);

  /**
   * Parses the specified options and returns the faulty key.
   * @param args arguments
   * @return error message
   */
  String check(final String args) {
    String error = null;
    for(final String option : Strings.split(args, ';')) {
      final String[] kv = Strings.split(option, '=', 2);
      try {
        assign(kv[0], kv.length == 2 ? kv[1] : "");
      } catch(final BaseXException ex) {
        error = option;
      }
    }
    return error;
  }

  /**
   * Creates an error for an invalid option.
   * @param option option
   * @return error
   */
  protected IllegalArgumentException error(final Option<?> option) {
    return new IllegalArgumentException("Invalid \"" + option + "\" value \"" + get(option) + "\"");
  }
}
