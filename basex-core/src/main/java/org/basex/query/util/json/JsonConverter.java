package org.basex.query.util.json;

import org.basex.build.*;
import org.basex.build.JsonProp.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter {
  /** JSON properties. */
  protected final JsonProp jprop;
  /** Input info. */
  protected final InputInfo info;

  /**
   * Constructor.
   * @param jp json properties
   * @param ii input info
   */
  protected JsonConverter(final JsonProp jp, final InputInfo ii) {
    info = ii;
    jprop = jp;
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
   * @param jprop json properties
   * @param ii input info
   * @return a JSON converter
   * @throws SerializerException serializer exception
   */
  public static JsonConverter get(final JsonProp jprop, final InputInfo ii)
      throws SerializerException {

    final JsonFormat format = jprop.format();
    if(format == JsonFormat.JSONML) return new JsonMLConverter(jprop, ii);
    if(format == JsonFormat.PLAIN)  return new JsonPlainConverter(jprop, ii);
    if(format == JsonFormat.MAP)    return new JsonMapConverter(jprop, ii);
    return new JsonDefaultConverter(jprop, ii);
  }
}
