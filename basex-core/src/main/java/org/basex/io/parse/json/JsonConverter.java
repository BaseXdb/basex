package org.basex.io.parse.json;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter {
  /** JSON options. */
  final JsonParserOptions jopts;

  /**
   * Constructor.
   * @param opts json options
   */
  JsonConverter(final JsonParserOptions opts) {
    jopts = opts;
  }

  /**
   * Converts the specified input to XML.
   * @param input input stream
   * @throws IOException I/O exception
   * @return result
   */
  public Item convert(final IO input) throws IOException {
    final String encoding = jopts.get(JsonParserOptions.ENCODING);
    return convert(new NewlineInput(input).encoding(encoding).content(), input.path());
  }

  /**
   * Converts the specified input to an XQuery item.
   * @param input input
   * @param path input path (can be {@code null)}
   * @throws QueryIOException query I/O exception
   * @return result
   */
  public Item convert(final byte[] input, final String path) throws QueryIOException {
    JsonParser.parse(Token.string(input), path, jopts, this);
    return finish();
  }

  /**
   * Returns a JSON converter for the given configuration.
   * @param jopts options
   * @return JSON converter
   */
  public static JsonConverter get(final JsonParserOptions jopts) {
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
   * @param add add pair
   * @throws QueryIOException query exception
   */
  abstract void closePair(boolean add) throws QueryIOException;

  /**
   * Called when a JSON object is closed.
   */
  abstract void closeObject();

  /**
   * Called when a JSON array is opened.
   * @throws QueryIOException query exception
   */
  abstract void openArray() throws QueryIOException;

  /**
   * Called when an item of a JSON array is opened.
   */
  abstract void openItem();

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
   */
  abstract void openArg();

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
