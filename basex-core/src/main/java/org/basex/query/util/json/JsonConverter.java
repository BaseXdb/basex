package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import org.basex.build.file.*;
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
  /** Input info. */
  protected final InputInfo info;
  /** The {@code map} conversion format. */
  public static final byte[] MAP = token("map");
  /** The {@code jsonml} conversion format. */
  public static final byte[] JSONML = token("jsonml");
  /** The {@code plain} conversion format. */
  public static final byte[] PLAIN = token("plain");
  /** The {@code json} conversion format. */
  public static final byte[] JSON = token("json");

  /**
   * Constructor.
   * @param ii input info
   */
  protected JsonConverter(final InputInfo ii) {
    info = ii;
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
   */
  public static JsonConverter get(final JsonProp jprop, final InputInfo ii) {
    final String format = jprop.get(JsonProp.FORMAT);
    if(format.equals(M_JSONML)) return new JsonMLConverter(jprop, ii);
    if(format.equals(M_PLAIN))  return new JsonPlainConverter(jprop, ii);
    if(format.equals(M_MAP))    return new JsonMapConverter(jprop, ii);
    return new JsonBaseXConverter(jprop, ii);
  }
}
