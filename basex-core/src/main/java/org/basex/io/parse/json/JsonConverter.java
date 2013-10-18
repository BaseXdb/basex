package org.basex.io.parse.json;

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
public abstract class JsonConverter {
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
    return JsonParser.parse(Token.string(input), jopts, get(jopts));
  }

  /**
   * Returns a {@link JsonConverter} for the given configuration.
   * @param jopts options
   * @return a JSON converter
   */
  private static JsonConverter get(final JsonParserOptions jopts) {
    switch(jopts.get(JsonOptions.FORMAT)) {
      case JSONML:     return new JsonMLConverter(jopts);
      case ATTRIBUTES: return new JsonAttsConverter(jopts);
      case MAP:        return new JsonMapConverter(jopts);
      default:         return new JsonDirectConverter(jopts);
    }
  }

  /**
   * Called when a JSON object is opened.
   * @throws QueryIOException query exception
   */
  abstract void openObject() throws QueryIOException;

  /**
   * Called when a pair of a JSON object is opened.
   * @param key the key of the entry
   * @throws QueryIOException query exception
   */
  abstract void openPair(byte[] key) throws QueryIOException;

  /**
   * Called when a pair of a JSON object is closed.
   * @throws QueryIOException query exception
   */
  abstract void closePair() throws QueryIOException;

  /**
   * Called when a JSON object is closed.
   * @throws QueryIOException query exception
   */
  abstract void closeObject() throws QueryIOException;

  /**
   * Called when a JSON array is opened.
   * @throws QueryIOException query exception
   */
  abstract void openArray() throws QueryIOException;

  /**
   * Called when an item of a JSON array is opened.
   * @throws QueryIOException query exception
   */
  abstract void openItem() throws QueryIOException;

  /**
   * Called when an item of a JSON array is closed.
   * @throws QueryIOException query exception
   */
  abstract void closeItem() throws QueryIOException;

  /**
   * Called when a JSON array is closed.
   * @throws QueryIOException query exception
   */
  abstract void closeArray() throws QueryIOException;

  /**
   * Called when a constructor function is opened.
   * @param name name of the constructor
   * @throws QueryIOException query exception
   */
  abstract void openConstr(byte[] name) throws QueryIOException;

  /**
   * Called when an argument of a constructor function is opened.
   * @throws QueryIOException query exception
   */
  abstract void openArg() throws QueryIOException;

  /**
   * Called when an argument of a constructor function is closed.
   * @throws QueryIOException query exception
   */
  abstract void closeArg() throws QueryIOException;

  /**
   * Called when a constructor function is closed.
   * @throws QueryIOException query exception
   */
  abstract void closeConstr() throws QueryIOException;

  /**
   * Called when a number literal is encountered.
   * @param value string representation of the number literal
   * @throws QueryIOException query exception
   */
  abstract void numberLit(byte[] value) throws QueryIOException;

  /**
   * Called when a string literal is encountered.
   * @param bs the string
   * @throws QueryIOException query exception
   */
  abstract void stringLit(byte[] bs) throws QueryIOException;

  /**
   * Called when a {@code null} literal is encountered.
   * @throws QueryIOException query exception
   */
  abstract void nullLit() throws QueryIOException;

  /**
   * Called when a boolean literal is encountered.
   * @param b the boolean
   * @throws QueryIOException query exception
   */
  abstract void booleanLit(byte[] b) throws QueryIOException;

  /**
   * Returns the resulting XQuery value.
   * @return result
   */
  abstract Item finish();
}
