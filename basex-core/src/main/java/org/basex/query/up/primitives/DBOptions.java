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
  private static final Option<?>[] OPTIONS = { MainOptions.MAXCATS, MainOptions.MAXLEN,
    MainOptions.INDEXSPLITSIZE, MainOptions.FTINDEXSPLITSIZE, MainOptions.LANGUAGE,
    MainOptions.STOPWORDS, MainOptions.TEXTINDEX, MainOptions.ATTRINDEX, MainOptions.FTINDEX,
    MainOptions.STEMMING, MainOptions.CASESENS, MainOptions.DIACRITICS, MainOptions.UPDINDEX };

  /** Runtime options. */
  private final HashMap<Option<?>, Object> rOptions = new HashMap<>();
  /** Original options. */
  private final HashMap<Option<?>, Object> oOptions = new HashMap<>();

  /**
   * Constructor.
   * @param options options
   * @param info input info
   * @param exclude options to be excluded
   * @throws QueryException query exception
   */
  DBOptions(final HashMap<String, String> options, final InputInfo info,
      final Option<?>... exclude) throws QueryException {

    final HashMap<String, Option<?>> opts = new HashMap<>();
    final int n = OPTIONS.length;
    for(int o = 0; o < n; o++) opts.put(OPTIONS[o].name().toLowerCase(Locale.ENGLISH), OPTIONS[o]);

    for(final Entry<String, String> entry : options.entrySet()) {
      final String key = entry.getKey();
      final Option<?> opt = opts.get(key);
      boolean valid = opt != null;
      if(valid) {
        for(final Option<?> ex : exclude) {
          if(opt == ex) valid = false;
        }
      }
      if(!valid) throw BASX_OPTIONS.get(info, key);

      final String value = entry.getValue();
      if(opt instanceof NumberOption) {
        final int v = toInt(value);
        if(v < 0) throw BASX_VALUE.get(info, key, value);
        rOptions.put(opt, v);
      } else if(opt instanceof BooleanOption) {
        final boolean yes = Util.yes(value);
        if(!yes && !Util.no(value)) throw BASX_VALUE.get(info, key, value);
        rOptions.put(opt, yes);
      } else {
        rOptions.put(opt, value);
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
