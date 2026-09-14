package org.basex.io.parse.json;

import java.io.*;

import org.basex.core.jobs.*;
import org.basex.query.*;

/**
 * Sink for the parse events emitted by {@link JsonParser}, in the style of a SAX content
 * handler.
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
   * @throws IOException I/O exception
   */
  protected abstract void openObject() throws QueryException, IOException;

  /**
   * Called when a JSON object is closed.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void closeObject() throws QueryException, IOException;

  /**
   * Called when a pair of a JSON object is opened.
   * @param key key of the entry
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void openPair(byte[] key) throws QueryException, IOException;

  /**
   * Called when a pair of a JSON object is closed.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void closePair() throws QueryException, IOException;

  /**
   * Called when a JSON array is opened.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void openArray() throws QueryException, IOException;

  /**
   * Called when a JSON array is closed.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void closeArray() throws QueryException, IOException;

  /**
   * Called when an item of a JSON array is opened.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void openItem() throws QueryException, IOException;

  /**
   * Called when an item of a JSON array is closed.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void closeItem() throws QueryException, IOException;

  /**
   * Called when a number literal is encountered.
   * @param value string representation
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void numberLit(byte[] value) throws QueryException, IOException;

  /**
   * Called when a string literal is encountered.
   * @param value string representation
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void stringLit(byte[] value) throws QueryException, IOException;

  /**
   * Called when a {@code null} literal is encountered.
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void nullLit() throws QueryException, IOException;

  /**
   * Called when a boolean literal is encountered.
   * @param value string representation
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract void booleanLit(byte[] value) throws QueryException, IOException;
}
