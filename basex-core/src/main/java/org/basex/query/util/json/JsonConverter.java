package org.basex.query.util.json;

import org.basex.build.*;
import org.basex.build.JsonOptions.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter {
  /** JSON options. */
  protected final JsonOptions jopts;
  /** Input info. */
  protected final InputInfo info;

  /**
   * Constructor.
   * @param opts json options
   * @param ii input info
   */
  protected JsonConverter(final JsonOptions opts, final InputInfo ii) {
    info = ii;
    jopts = opts;
  }

  /**
   * Converts the given JSON string into an XQuery value.
   * @param in the JSON string
   * @return the result
   * @throws QueryException parse exception
   */
  public abstract Item convert(final String in) throws QueryException;

  /**
   * Returns a {@link JsonConverter} for the given configuration.
   * @param jopts json options
   * @param ii input info
   * @return a JSON converter
   * @throws QueryIOException query I/O exception
   */
  public static JsonConverter get(final JsonOptions jopts, final InputInfo ii)
      throws QueryIOException {

    final JsonFormat format = jopts.format();
    if(format == JsonFormat.JSONML) return new JsonMLConverter(jopts, ii);
    if(format == JsonFormat.PLAIN)  return new JsonPlainConverter(jopts, ii);
    if(format == JsonFormat.MAP)    return new JsonMapConverter(jopts, ii);
    return new JsonDefaultConverter(jopts, ii);
  }
}
