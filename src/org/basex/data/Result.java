package org.basex.data;

/**
 * This is an interface for query results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public interface Result {
  /**
   * Number of values, stored in the result instance.
   * @return number of values
   */
  int size();

  /**
   * Compares values for equality.
   * @param v value to be compared
   * @return true if values are equal
   */
  boolean same(Result v);

  /**
   * Serializes the result.
   * @param ser serializer
   * @throws Exception exception
   */
  void serialize(Serializer ser) throws Exception;
}
