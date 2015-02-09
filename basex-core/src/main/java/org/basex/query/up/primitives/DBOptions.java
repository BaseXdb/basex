package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Contains various helper variables and methods for database operations.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DBOptions {
  /** Parsing options. */
  public static final Option<?>[] PARSING = { MainOptions.CREATEFILTER, MainOptions.ADDARCHIVES,
    MainOptions.SKIPCORRUPT, MainOptions.ADDRAW, MainOptions.ADDCACHE, MainOptions.CSVPARSER,
    MainOptions.TEXTPARSER, MainOptions.JSONPARSER, MainOptions.HTMLPARSER, MainOptions.PARSER,
    MainOptions.CHOP, MainOptions.INTPARSE, MainOptions.STRIPNS, MainOptions.DTD,
    MainOptions.CATFILE };
  /** Indexing options. */
  public static final Option<?>[] INDEXING = { MainOptions.MAXCATS, MainOptions.MAXLEN,
    MainOptions.INDEXSPLITSIZE, MainOptions.FTINDEXSPLITSIZE, MainOptions.LANGUAGE,
    MainOptions.STOPWORDS, MainOptions.TEXTINDEX, MainOptions.ATTRINDEX, MainOptions.FTINDEX,
    MainOptions.STEMMING, MainOptions.CASESENS, MainOptions.DIACRITICS, MainOptions.UPDINDEX,
    MainOptions.AUTOOPTIMIZE };

  /** Runtime options. */
  private final HashMap<Option<?>, Object> map = new HashMap<>();

  /**
   * Constructor.
   * @param options query options
   * @param supported supported options
   * @param info input info
   * @throws QueryException query exception
   */
  public DBOptions(final Options options, final List<Option<?>> supported, final InputInfo info)
      throws QueryException {
    this(options, supported.toArray(new Option<?>[supported.size()]), info);
  }

  /**
   * Constructor.
   * @param options query options
   * @param supported supported options
   * @param info input info
   * @throws QueryException query exception
   */
  public DBOptions(final Options options, final Option<?>[] supported, final InputInfo info)
      throws QueryException {

    final HashMap<String, Option<?>> opts = new HashMap<>();
    for(final Option<?> option : supported) {
      opts.put(option.name().toLowerCase(Locale.ENGLISH), option);
    }

    for(final Entry<String, String> entry : options.free().entrySet()) {
      final String key = entry.getKey();
      final Option<?> option = opts.get(key);
      if(option == null) throw BASX_OPTIONS_X.get(info, key);

      final String value = entry.getValue();
      if(option instanceof NumberOption) {
        final int v = Strings.toInt(value);
        if(v < 0) throw BASX_VALUE_X_X.get(info, key, value);
        map.put(option, v);
      } else if(option instanceof BooleanOption) {
        final boolean yes = Strings.yes(value);
        if(!yes && !Strings.no(value)) throw BASX_VALUE_X_X.get(info, key, value);
        map.put(option, yes);
      } else if(option instanceof EnumOption) {
        final EnumOption<?> eo = (EnumOption<?>) option;
        final Object ev = eo.get(value);
        if(ev == null) throw BASX_VALUE_X_X.get(info, key, value);
        map.put(option, ev);
      } else {
        map.put(option, value);
      }
    }
  }

  /**
   * Assigns the specified option if it has not been assigned before.
   * @param option option
   * @param value value
   */
  public void assign(final Option<?> option, final Object value) {
    if(!map.containsKey(option)) map.put(option, value);
  }

  /**
   * Assigns runtime options to the specified main options.
   * @param opts main options
   */
  public void assignTo(final MainOptions opts) {
    for(final Entry<Option<?>, Object> entry : map.entrySet()) {
      opts.put(entry.getKey(), entry.getValue());
    }
  }
}
