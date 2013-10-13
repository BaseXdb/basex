package org.basex.query.util.json;

import org.basex.build.*;
import org.basex.build.JsonOptions.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter {
  /** JSON options. */
  protected final JsonOptions jopts;

  /**
   * Constructor.
   * @param opts json options
   */
  protected JsonConverter(final JsonOptions opts) {
    jopts = opts;
  }

  /**
   * Converts the given JSON string into an XQuery value.
   * @param in the JSON string
   * @return the result
   * @throws QueryIOException query I/O exception
   */
  public abstract Item convert(final String in) throws QueryIOException;

  /**
   * Returns a {@link JsonConverter} for the given configuration.
   * @param jopts json options
   * @return a JSON converter
   * @throws QueryIOException query I/O exception
   */
  public static JsonConverter get(final JsonOptions jopts) throws QueryIOException {
    final JsonFormat format = jopts.format();
    if(format == JsonFormat.JSONML)     return new JsonMLConverter(jopts);
    if(format == JsonFormat.ATTRIBUTES) return new JsonAttsConverter(jopts);
    if(format == JsonFormat.MAP)        return new JsonMapConverter(jopts);
    return new JsonDirectConverter(jopts);
  }
}
