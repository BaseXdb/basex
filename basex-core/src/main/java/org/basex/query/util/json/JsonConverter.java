package org.basex.query.util.json;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.util.json.JsonParser.Spec;
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
  public static final byte[] MAPS = token("maps");
  /** The {@code jsonml} conversion format. */
  public static final byte[] JSONML = token("jsonml");
  /** The {@code json} conversion format. */
  public static final byte[] JSON = token("json");

  /**
   * Constructor.
   * @param ii input info
   */
  public JsonConverter(final InputInfo ii) {
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
   * @param format conversion format
   * @param spec JSON spec
   * @param unesc unescape flag
   * @param ii input info
   * @return a JSON converter
   */
  public static JsonConverter newInstance(final byte[] format, final Spec spec,
    final boolean unesc, final InputInfo ii) {

    if(format != null) {
      if(eq(format, JSONML)) return new JsonMLConverter(ii);
      if(eq(format, MAPS)) return new JsonMapConverter(spec, unesc, ii);
    }
    return new JsonCGConverter(spec, unesc, ii);
  }
}
