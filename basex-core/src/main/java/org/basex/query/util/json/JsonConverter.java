package org.basex.query.util.json;

import java.io.*;

import org.basex.build.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter implements JsonHandler {
  /** JSON options. */
  protected final JsonParserOptions jopts;

  /**
   * Constructor.
   * @param opts json options
   */
  protected JsonConverter(final JsonParserOptions opts) {
    jopts = opts;
  }

  /**
   * Returns the resulting XQuery value.
   * @return result
   */
  protected abstract Item finish();

  /**
   * Converts the specified input to an XQuery item.
   * @param input input stream
   * @param jopts options
   * @return item
   * @throws IOException I/O exception
   */
  public static Item convert(final IO input, final JsonParserOptions jopts) throws IOException {
    final String encoding = jopts.get(JsonParserOptions.ENCODING);
    return convert(new NewlineInput(input).encoding(encoding).content(), jopts);
  }

  /**
   * Converts the specified input to an XQuery item.
   * @param input input
   * @param jopts json options
   * @return item
   * @throws QueryIOException query I/O exception
   */
  public static Item convert(final byte[] input, final JsonParserOptions jopts)
      throws QueryIOException {

    final JsonConverter conv = get(jopts);
    JsonParser.parse(Token.string(input), jopts, conv);
    return conv.finish();
  }

  /**
   * Returns a {@link JsonConverter} for the given configuration.
   * @param jopts options
   * @return a JSON converter
   * @throws QueryIOException query I/O exception
   */
  private static JsonConverter get(final JsonParserOptions jopts) throws QueryIOException {
    switch(jopts.format()) {
      case JSONML:     return new JsonMLConverter(jopts);
      case ATTRIBUTES: return new JsonAttsConverter(jopts);
      case MAP:        return new JsonMapConverter(jopts);
      default:         return new JsonDirectConverter(jopts);
    }
  }
}
