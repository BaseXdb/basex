package org.basex.io.parse.json;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter {
  /** JSON options. */
  final JsonParserOptions jopts;
  /** Fallback function. */
  JsonFallback fallback;

  /**
   * Constructor.
   * @param jopts json options
   */
  JsonConverter(final JsonParserOptions jopts) {
    this.jopts = jopts;
  }

  /**
   * Assigns a fallback function for invalid characters.
   * @param func fallback function
   * @return self reference
   */
  public final JsonConverter fallback(final JsonFallback func) {
    fallback = func;
    return this;
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @throws IOException I/O exception
   * @return result
   */
  public final Item convert(final IO input) throws IOException {
    final String encoding = jopts.get(JsonParserOptions.ENCODING);
    try(NewlineInput ni = new NewlineInput(input)) {
      return convert(ni.encoding(encoding).cache().toString(), input.url());
    }
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @param path input path (can be empty string}
   * @throws QueryIOException query I/O exception
   * @return result
   */
  public final Item convert(final String input, final String path) throws QueryIOException {
    init(path.isEmpty() ? "" : IO.get(path).url());

    final JsonParser parser = new JsonParser(input, jopts, this);
    parser.parse();
    return finish();
  }

  /**
   * Returns a JSON converter for the given configuration.
   * @param jopts options
   * @return JSON converter
   * @throws QueryIOException query I/O exception
   */
  public static JsonConverter get(final JsonParserOptions jopts) throws QueryIOException {
    switch(jopts.get(JsonOptions.FORMAT)) {
      case JSONML:     return new JsonMLConverter(jopts);
      case ATTRIBUTES: return new JsonAttsConverter(jopts);
      case XQUERY:     return new JsonXQueryConverter(jopts);
      case BASIC:      return new JsonBasicConverter(jopts);
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
   * @param add add pair
   * @throws QueryIOException query exception
   */
  abstract void openPair(byte[] key, boolean add) throws QueryIOException;

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
   */
  abstract void closeItem();

  /**
   * Called when a JSON array is closed.
   * @throws QueryIOException query exception
   */
  abstract void closeArray() throws QueryIOException;

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
   * Initializes the conversion.
   * @param uri base URI
   */
  abstract void init(String uri);

  /**
   * Returns the resulting XQuery value.
   * @return result
   */
  abstract Item finish();
}
