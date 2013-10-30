package org.basex.http.restxq;

import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class contains a RESTXQ error.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class RestXqError implements Comparable<RestXqError> {
  /** Error code ({@code null}: wildcard). */
  private final QNm code;

  /**
   * Constructor.
   * @param error error code ({@code null}: wildcard)
   */
  RestXqError(final QNm error) {
    code = error;
  }

  /**
   * Checks if the specified name matches the test.
   * @param nm name
   * @return result of check
   */
  boolean matches(final QNm nm) {
    return code == null || code.eq(nm);
  }

  @Override
  public int compareTo(final RestXqError rxe) {
    return code == null ? rxe.code == null ? 0 : -1 : 1;
  }

  @Override
  public String toString() {
    return code == null ? "*" : Token.string(code.string());
  }
}
