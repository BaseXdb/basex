package org.basex.query.util.json;

import org.basex.query.*;

/**
 * A handler for events from a {@link JsonParser}.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public interface JsonHandler {
  /**
   * Called when a JSON object is opened.
   * @throws QueryIOException query exception
   */
  void openObject() throws QueryIOException;

  /**
   * Called when a pair of a JSON object is opened.
   * @param key the key of the entry
   * @throws QueryIOException query exception
   */
  void openPair(byte[] key) throws QueryIOException;

  /**
   * Called when a pair of a JSON object is closed.
   * @throws QueryIOException query exception
   */
  void closePair() throws QueryIOException;

  /**
   * Called when a JSON object is closed.
   * @throws QueryIOException query exception
   */
  void closeObject() throws QueryIOException;

  /**
   * Called when a JSON array is opened.
   * @throws QueryIOException query exception
   */
  void openArray() throws QueryIOException;

  /**
   * Called when an item of a JSON array is opened.
   * @throws QueryIOException query exception
   */
  void openItem() throws QueryIOException;

  /**
   * Called when an item of a JSON array is closed.
   * @throws QueryIOException query exception
   */
  void closeItem() throws QueryIOException;

  /**
   * Called when a JSON array is closed.
   * @throws QueryIOException query exception
   */
  void closeArray() throws QueryIOException;

  /**
   * Called when a constructor function is opened.
   * @param name name of the constructor
   * @throws QueryIOException query exception
   */
  void openConstr(byte[] name) throws QueryIOException;

  /**
   * Called when an argument of a constructor function is opened.
   * @throws QueryIOException query exception
   */
  void openArg() throws QueryIOException;

  /**
   * Called when an argument of a constructor function is closed.
   * @throws QueryIOException query exception
   */
  void closeArg() throws QueryIOException;

  /**
   * Called when a constructor function is closed.
   * @throws QueryIOException query exception
   */
  void closeConstr() throws QueryIOException;

  /**
   * Called when a number literal is encountered.
   * @param value string representation of the number literal
   * @throws QueryIOException query exception
   */
  void numberLit(byte[] value) throws QueryIOException;

  /**
   * Called when a string literal is encountered.
   * @param bs the string
   * @throws QueryIOException query exception
   */
  void stringLit(byte[] bs) throws QueryIOException;

  /**
   * Called when a {@code null} literal is encountered.
   * @throws QueryIOException query exception
   */
  void nullLit() throws QueryIOException;

  /**
   * Called when a boolean literal is encountered.
   * @param b the boolean
   * @throws QueryIOException query exception
   */
  void booleanLit(byte[] b) throws QueryIOException;
}
