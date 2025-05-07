package org.basex.io.parse.json;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Interface for converters from JSON to XQuery values.
 *
 * @author BaseX Team, BSD License
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
  /** Null value. */
  protected Value nullValue = Empty.VALUE;
  /** Query context (can be {@code null}). */
  protected QueryContext qctx;

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
      case XQUERY: //deprecated
      case W3:         return new JsonW3Converter(jopts);
      case BASIC: //deprecated
      case W3_XML:     return new JsonW3XmlConverter(jopts);
      default:         return new JsonDirectConverter(jopts);
    }
  }

  /**
   * Constructor.
   * @param jopts JSON options
   */
  protected JsonConverter(final JsonParserOptions jopts) {
    this.jopts = jopts;
  }

  /**
   * Assigns a fallback function for invalid characters.
   * @param func fallback function
   */
  public final void fallback(final QueryFunction<byte[], byte[]> func) {
    fallback = func;
  }

  /**
   * Assigns a number parser function.
   * @param func number parser function
   */
  public final void numberParser(final QueryFunction<byte[], Item> func) {
    numberParser = func;
  }

  /**
   * Assigns a value for 'null' values.
   * @param item null value
   */
  public final void nullValue(final Value item) {
    nullValue = item;
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @return result
   */
  public final Value convert(final IO input) throws QueryException, IOException {
    final String encoding = jopts.get(JsonParserOptions.ENCODING);
    try(NewlineInput ni = new NewlineInput(input)) {
      return convert(ni.encoding(encoding), input.url(), null, null);
    }
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @param uri uri (can be empty)
   * @param ii input info (can be {@code null})
   * @param qc query context (if {@code null}, result may lack XQuery-specific contents)
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public final Value convert(final TextInput input, final String uri, final InputInfo ii,
      final QueryContext qc) throws QueryException, IOException {
    qctx = qc;
    init(uri);
    new JsonParser(input, jopts, this).parse(ii);
    return finish();
  }

  /**
   * Initializes the conversion.
   * @param uri base URI
   */
  protected abstract void init(String uri);

  /**
   * Returns the resulting XQuery value.
   * @return result
   */
  protected abstract Item finish();

  /**
   * Called when a JSON object is opened.
   * @throws QueryException query exception
   */
  protected abstract void openObject() throws QueryException;

  /**
   * Called when a JSON object is closed.
   */
  protected abstract void closeObject();

  /**
   * Called when a pair of a JSON object is opened.
   * @param key the key of the entry
   * @param add add pair
   * @throws QueryException query exception
   */
  protected abstract void openPair(byte[] key, boolean add) throws QueryException;

  /**
   * Called when a pair of a JSON object is closed.
   * @param add add pair
   * @throws QueryException query exception
   */
  protected abstract void closePair(boolean add) throws QueryException;

  /**
   * Called when a JSON array is opened.
   * @throws QueryException query exception
   */
  protected abstract void openArray() throws QueryException;

  /**
   * Called when a JSON array is closed.
   * @throws QueryException query exception
   */
  protected abstract void closeArray() throws QueryException;

  /**
   * Called when an item of a JSON array is opened.
   */
  protected abstract void openItem();

  /**
   * Called when an item of a JSON array is closed.
   */
  protected abstract void closeItem();

  /**
   * Called when a number literal is encountered.
   * @param value string representation
   * @throws QueryException query exception
   */
  protected abstract void numberLit(byte[] value) throws QueryException;

  /**
   * Called when a string literal is encountered.
   * @param value string representation
   * @throws QueryException query exception
   */
  protected abstract void stringLit(byte[] value) throws QueryException;

  /**
   * Called when a {@code null} literal is encountered.
   * @throws QueryException query exception
   */
  protected abstract void nullLit() throws QueryException;

  /**
   * Called when a boolean literal is encountered.
   * @param value string representation
   * @throws QueryException query exception
   */
  protected abstract void booleanLit(byte[] value) throws QueryException;
}
