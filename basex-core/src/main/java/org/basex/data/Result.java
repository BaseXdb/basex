package org.basex.data;

import java.io.*;

import org.basex.io.serial.*;

/**
 * This is an interface for query results.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public interface Result {
  /**
   * Number of items, stored in the result instance.
   * @return number of items
   */
  long size();

  /**
   * Serializes the result, using the standard serializer.
   * @return serialized value
   * @throws IOException I/O exception
   */
  String serialize() throws IOException;

  /**
   * Serializes the result.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  void serialize(Serializer ser) throws IOException;

  /**
   * Serializes the item with the specified index.
   * @param ser serializer
   * @param index offset of result to serialize
   * @throws IOException I/O exception
   */
  void serialize(Serializer ser, int index) throws IOException;
}
