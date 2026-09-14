package org.basex.io.parse.json;

import org.basex.core.jobs.*;
import org.basex.query.*;

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
   * @param key key of the entry
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
