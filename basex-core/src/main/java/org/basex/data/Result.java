package org.basex.data;

import java.io.*;

import org.basex.io.out.*;
import org.basex.io.serial.*;

/**
 * This is an interface for query results.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface Result {
  /**
   * Number of values, stored in the result instance.
   * @return number of values
   */
  long size();

  /**
   * Compares results for equality.
   * @param r result to be compared
   * @return true if results are equal
   */
  boolean sameAs(Result r);

  /**
   * Serializes the result, using the standard serializer,
   * and returns the cached result.
   * @return serialized value
   * @throws IOException I/O exception
   */
  ArrayOutput serialize() throws IOException;

  /**
   * Serializes the complete result.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  void serialize(Serializer ser) throws IOException;

  /**
   * Serializes the specified result.
   * @param ser serializer
   * @param n offset of result to serialize
   * @throws IOException I/O exception
   */
  void serialize(Serializer ser, int n) throws IOException;
}
