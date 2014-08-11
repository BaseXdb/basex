package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Contains various helper variables and methods for database operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class DBOptions {
  /** Index options. */
  static final Option<?>[] INDEXING = { MainOptions.MAXCATS, MainOptions.MAXLEN,
    MainOptions.INDEXSPLITSIZE, MainOptions.FTINDEXSPLITSIZE, MainOptions.LANGUAGE,
    MainOptions.STOPWORDS, MainOptions.TEXTINDEX, MainOptions.ATTRINDEX, MainOptions.FTINDEX,
    MainOptions.STEMMING, MainOptions.CASESENS, MainOptions.DIACRITICS, MainOptions.UPDINDEX };
  /** Parsing options. */
  static final Option<?>[] PARSING = { MainOptions.CREATEFILTER, MainOptions.ADDARCHIVES,
    MainOptions.SKIPCORRUPT, MainOptions.ADDRAW, MainOptions.ADDCACHE, MainOptions.CSVPARSER,
    MainOptions.TEXTPARSER, MainOptions.JSONPARSER, MainOptions.HTMLPARSER, MainOptions.PARSER,
    MainOptions.CHOP, MainOptions.INTPARSE, MainOptions.STRIPNS, MainOptions.DTD,
    MainOptions.CATFILE };

  /** Runtime options. */
  private final HashMap<Option<?>, Object> rOptions = new HashMap<>();
  /** Original options. */
  private final HashMap<Option<?>, Object> oOptions = new HashMap<>();

  /**
   * Constructor.
   * @param options query options
   * @param supported supported options
   * @param info input info
   * @throws QueryException query exception
   */
  DBOptions(final HashMap<String, String> options, final List<Option<?>> supported,
      final InputInfo info) throws QueryException {

    final HashMap<String, Option<?>> opts = new HashMap<>();
    for(final Option<?> option : supported) {
      opts.put(option.name().toLowerCase(Locale.ENGLISH), option);
    }

    for(final Entry<String, String> entry : options.entrySet()) {
      final String key = entry.getKey();
      final Option<?> option = opts.get(key);
      if(option == null) throw BASX_OPTIONS_X.get(info, key);

      final String value = entry.getValue();
      if(option instanceof NumberOption) {
        final int v = toInt(value);
        if(v < 0) throw BASX_VALUE_X_X.get(info, key, value);
        rOptions.put(option, v);
      } else if(option instanceof BooleanOption) {
        final boolean yes = Util.yes(value);
        if(!yes && !Util.no(value)) throw BASX_VALUE_X_X.get(info, key, value);
        rOptions.put(option, yes);
      } else {
        rOptions.put(option, value);
      }
    }
  }

  /**
   * Assigns the specified option if it has not been assigned before.
   * @param option option
   * @param value value
   */
  void assign(final Option<?> option, final Object value) {
    if(!rOptions.containsKey(option)) rOptions.put(option, value);
  }

  /**
   * Caches original options and assigns runtime options.
   * @param opts main options
   */
  void assign(final MainOptions opts) {
    for(final Map.Entry<Option<?>, Object> entry : rOptions.entrySet()) {
      final Option<?> option = entry.getKey();
      oOptions.put(option, opts.get(option));
      opts.put(option, entry.getValue());
    }
  }

  /**
   * Restores original options.
   * @param opts main options
   */
  void reset(final MainOptions opts) {
    for(final Entry<Option<?>, Object> e : oOptions.entrySet()) {
      opts.put(e.getKey(), e.getValue());
    }
  }
}
