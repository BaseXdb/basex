package org.basex.http.restxq;

import org.basex.query.path.*;
import org.basex.query.value.item.*;

/**
 * This class contains a RESTXQ error.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RestXqError implements Comparable<RestXqError> {
  /** Error code ({@code null}: wildcard). */
  private final NameTest test;

  /**
   * Constructor.
   * @param test name test ({@code null}: wildcard)
   */
  RestXqError(final NameTest test) {
    this.test = test;
  }

  /**
   * Checks if the specified name matches the test.
   * @param name name
   * @return result of check
   */
  boolean matches(final QNm name) {
    return test.eq(name);
  }

  @Override
  public int compareTo(final RestXqError error) {
    return test.kind.ordinal() - error.test.kind.ordinal();
  }

  @Override
  public String toString() {
    return test.toString();
  }
}
