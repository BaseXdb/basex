package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Contains various helper variables and methods for database operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DBOptions {
  /** Runtime options. */
  private final HashMap<Option<?>, Object> map = new HashMap<>();

  /**
   * Constructor.
   * @param qopts query options
   * @param supported supported options
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DBOptions(final XQMap qopts, final Option<?>[] supported, final QueryContext qc,
      final InputInfo info) throws QueryException {

    final HashMap<String, Option<?>> options = new HashMap<>();
    for(final Option<?> option : supported) {
      options.put(option.name().toLowerCase(Locale.ENGLISH), option);
    }

    for(final Item key : qopts.keys()) {
      final String name = Options.name(key, info);
      final Option<?> option = options.get(name);
      if(option == null) throw BASEX_OPTIONS_X.get(info, Options.similar(name, options));

      // nested maps and functions cannot be serialized as strings and are assigned with types
      Value value = qopts.get(key);
      XQMap typed = XQMap.empty();
      if(option instanceof OptionsOption && value instanceof final XQMap nested) {
        final MapBuilder strings = new MapBuilder(), values = new MapBuilder();
        for(final Item k : nested.keys()) {
          final Value v = nested.get(k);
          (v instanceof FItem ? values : strings).put(k, v);
        }
        value = strings.map();
        typed = values.map();
      }
      final String error = Options.assign(option, Options.serialize(value, info), -1,
          v -> map.put(option, v), null);
      if(error != null) throw BASEX_OPTIONS_X.get(info, error);
      if(typed.structSize() != 0) ((Options) map.get(option)).assign(typed, qc, info);
    }
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
