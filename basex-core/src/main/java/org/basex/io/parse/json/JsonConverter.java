package org.basex.io.parse.json;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonConverter {
  /** Shared data references. */
  protected final SharedData shared = new SharedData();
  /** JSON options. */
  protected final JsonParserOptions jopts;

  /** Fallback function. */
  protected QueryFunction<byte[], byte[]> fallback;
  /** Number parser function. */
  protected QueryFunction<byte[], Item> numberParser;

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
  public final JsonConverter fallback(final QueryFunction<byte[], byte[]> func) {
    fallback = func;
    return this;
  }

  /**
   * Assigns a number parser function.
   * @param func number parser function
   * @return self reference
   */
  public final JsonConverter numberParser(final QueryFunction<byte[], Item> func) {
    numberParser = func;
    return this;
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @throws QueryException query exception
   * @throws IOException exception
   * @return result
   */
  public final Item convert(final IO input) throws QueryException, IOException {
    final String encoding = jopts.get(JsonParserOptions.ENCODING);
    try(NewlineInput ni = new NewlineInput(input)) {
      return convert(ni.encoding(encoding).cache().toString(), input.url());
    }
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @param path input path (can be empty string)
   * @throws QueryException query exception
   * @return result
   */
  public final Item convert(final String input, final String path) throws QueryException {
    init(path.isEmpty() ? "" : IO.get(path).url());
    final JsonParser parser = new JsonParser(input, jopts, this);
    parser.parse();
    return finish();
  }

  /**
   * Returns a JSON converter for the given configuration.
   * @param jopts options
   * @return JSON converter
   * @throws QueryException query exception
   */
  public static JsonConverter get(final JsonParserOptions jopts) throws QueryException {
    switch(jopts.get(JsonOptions.FORMAT)) {
      case JSONML:     return new JsonMLConverter(jopts);
      case ATTRIBUTES: return new JsonAttsConverter(jopts);
      case XQUERY:     return new JsonXQueryConverter(jopts);
      case BASIC:      return new JsonBasicConverter(jopts);
      default:         return new JsonDirectConverter(jopts);
    }
  }

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

  /**
   * Called when a JSON object is opened.
   * @throws QueryException query exception
   */
  abstract void openObject() throws QueryException;

  /**
   * Called when a JSON object is closed.
   */
  abstract void closeObject();

  /**
   * Called when a pair of a JSON object is opened.
   * @param key the key of the entry
   * @param add add pair
   * @throws QueryException query exception
   */
  abstract void openPair(byte[] key, boolean add) throws QueryException;

  /**
   * Called when a pair of a JSON object is closed.
   * @param add add pair
   * @throws QueryException query exception
   */
  abstract void closePair(boolean add) throws QueryException;

  /**
   * Called when a JSON array is opened.
   * @throws QueryException query exception
   */
  abstract void openArray() throws QueryException;

  /**
   * Called when a JSON array is closed.
   * @throws QueryException query exception
   */
  abstract void closeArray() throws QueryException;

  /**
   * Called when an item of a JSON array is opened.
   */
  abstract void openItem();

  /**
   * Called when an item of a JSON array is closed.
   */
  abstract void closeItem();

  /**
   * Called when a number literal is encountered.
   * @param value string representation of the number literal
   * @throws QueryException query exception
   */
  abstract void numberLit(Item value) throws QueryException;

  /**
   * Called when a string literal is encountered.
   * @param bs the string
   * @throws QueryException query exception
   */
  abstract void stringLit(byte[] bs) throws QueryException;

  /**
   * Called when a {@code null} literal is encountered.
   * @throws QueryException query exception
   */
  abstract void nullLit() throws QueryException;

  /**
   * Called when a boolean literal is encountered.
   * @param b the boolean
   * @throws QueryException query exception
   */
  abstract void booleanLit(byte[] b) throws QueryException;
}
