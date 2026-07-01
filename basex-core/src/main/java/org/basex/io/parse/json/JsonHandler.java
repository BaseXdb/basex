package org.basex.io.parse.json;

import org.basex.core.jobs.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Sink for the parse events emitted by {@link JsonParser}, in the style of a SAX content
 * handler. Implementations either build an in-memory XQuery value ({@link JsonConverter})
 * or stream the events onward without materializing a tree.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class JsonHandler extends Job {
  /** Fallback function for invalid characters. */
  protected QueryFunction<byte[], byte[]> fallback;

  /**
   * Assigns a fallback function for invalid characters.
   * @param func fallback function
   */
  public final void fallback(final QueryFunction<byte[], byte[]> func) {
    fallback = func;
  }

  /**
   * Raises an error for an option value that is not supported by the target format.
   * @param name option name
   * @param value option value
   * @return query exception
   */
  protected static QueryException optionError(final String name, final Object value) {
    return QueryError.OPTION_JSON_X.get(null,
        Util.info("'%':'%' is not supported by the target format.", name, value));
  }

  /**
   * Called when a JSON object is opened.
   * @throws QueryException query exception
   */
  protected abstract void openObject() throws QueryException;

  /**
   * Called when a JSON object is closed.
   * @throws QueryException query exception
   */
  protected abstract void closeObject() throws QueryException;

  /**
   * Called when a pair of a JSON object is opened.
   * @param key the key of the entry
   * @throws QueryException query exception
   */
  protected abstract void openPair(byte[] key) throws QueryException;

  /**
   * Called when a pair of a JSON object is closed.
   * @throws QueryException query exception
   */
  protected abstract void closePair() throws QueryException;

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
   * @throws QueryException query exception
   */
  protected abstract void openItem() throws QueryException;

  /**
   * Called when an item of a JSON array is closed.
   * @throws QueryException query exception
   */
  protected abstract void closeItem() throws QueryException;

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
