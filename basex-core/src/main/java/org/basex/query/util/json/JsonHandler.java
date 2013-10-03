package org.basex.query.util.json;

import org.basex.query.QueryException;

/**
 * A handler for events from a {@link JsonParser}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public interface JsonHandler {
  /**
   * Called when a JSON object is opened.
   * @throws QueryException query exception
   */
  void openObject() throws QueryException;

  /**
   * Called when a pair of a JSON object is opened.
   * @param key the key of the entry
   * @throws QueryException query exception
   */
  void openPair(byte[] key) throws QueryException;

  /**
   * Called when a pair of a JSON object is closed.
   * @throws QueryException query exception
   */
  void closePair() throws QueryException;

  /**
   * Called when a JSON object is closed.
   * @throws QueryException query exception
   */
  void closeObject() throws QueryException;

  /**
   * Called when a JSON array is opened.
   * @throws QueryException query exception
   */
  void openArray() throws QueryException;

  /**
   * Called when an item of a JSON array is opened.
   * @throws QueryException query exception
   */
  void openItem() throws QueryException;

  /**
   * Called when an item of a JSON array is closed.
   * @throws QueryException query exception
   */
  void closeItem() throws QueryException;

  /**
   * Called when a JSON array is closed.
   * @throws QueryException query exception
   */
  void closeArray() throws QueryException;

  /**
   * Called when a constructor function is opened.
   * @param name name of the constructor
   * @throws QueryException query exception
   */
  void openConstr(byte[] name) throws QueryException;

  /**
   * Called when an argument of a constructor function is opened.
   * @throws QueryException query exception
   */
  void openArg() throws QueryException;

  /**
   * Called when an argument of a constructor function is closed.
   * @throws QueryException query exception
   */
  void closeArg() throws QueryException;

  /**
   * Called when a constructor function is closed.
   * @throws QueryException query exception
   */
  void closeConstr() throws QueryException;

  /**
   * Called when a number literal is encountered.
   * @param value string representation of the number literal
   * @throws QueryException query exception
   */
  void numberLit(byte[] value) throws QueryException;

  /**
   * Called when a string literal is encountered.
   * @param bs the string
   * @throws QueryException query exception
   */
  void stringLit(byte[] bs) throws QueryException;

  /**
   * Called when a {@code null} literal is encountered.
   * @throws QueryException query exception
   */
  void nullLit() throws QueryException;

  /**
   * Called when a boolean literal is encountered.
   * @param b the boolean
   * @throws QueryException query exception
   */
  void booleanLit(byte[] b) throws QueryException;
}
