package org.basex.query.util.collation;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Collation options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class CollationOptions extends Options {
  /**
   * Parses the specified options and returns the faulty key.
   * @param args arguments
   * @return error message
   * @throws BaseXException database exception
   */
  abstract Collation get(HashMap<String, String> args) throws BaseXException;

  /**
   * Assigns the specified options.
   * @param args arguments
   * @throws BaseXException database exception
   */
  void assign(final HashMap<String, String> args) throws BaseXException {
    for(final Entry<String, String> entry : args.entrySet()) {
      assign(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Creates an error for an invalid option.
   * @param option option
   * @return error
   */
  protected BaseXException error(final Option<?> option) {
    return new BaseXException("Invalid \"%\" value: \"%\".", option, get(option));
  }

  /** Initialization of locales. */
  protected static final class Locales {
    /** Private constructor. */
    private Locales() { }

    /** Available locales, indexed by language code. */
    static final HashMap<String, Locale> MAP = new HashMap<>();
    static {
      for(final Locale l : Locale.getAvailableLocales()) MAP.put(l.toString().replace('_', '-'), l);
    }
  }
}
