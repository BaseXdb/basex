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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class DBOptions {
  /** Runtime options. */
  private final HashMap<Option<?>, Object> map = new HashMap<>();

  /**
   * Constructor.
   * @param qopts query options
   * @param supported supported options
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DBOptions(final HashMap<String, String> qopts, final Option<?>[] supported,
      final InputInfo info) throws QueryException {

    final HashMap<String, Option<?>> options = new HashMap<>();
    for(final Option<?> option : supported) {
      options.put(option.name().toLowerCase(Locale.ENGLISH), option);
    }

    for(final Entry<String, String> entry : qopts.entrySet()) {
      final String name = entry.getKey();
      final Option<?> option = options.get(name);
      if(option == null) throw BASEX_OPTIONS_X.get(info, Options.similar(name, options));
      final String error = Options.assign(option, entry.getValue(), -1,
          v -> map.put(option, v), null);
      if(error != null) throw BASEX_OPTIONS_X.get(info, error);
    }
  }

  /**
   * Returns the value of the specified option.
   * @param option option
   * @return main options
   */
  public Object get(final Option<?> option) {
    return map.get(option);
  }

  /**
   * Assigns the specified option if it has not been assigned before.
   * @param option option
   * @param value value
   */
  public void assignIfAbsent(final Option<?> option, final Object value) {
    map.putIfAbsent(option, value);
  }

  /**
   * Assigns runtime options to the specified main options.
   * @param opts main options
   * @return main options
   */
  public MainOptions assignTo(final MainOptions opts) {
    map.forEach(opts::put);
    return opts;
  }
}
