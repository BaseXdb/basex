package org.basex.http.restxq;

import java.util.*;

import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;

/**
 * This class catches RESTXQ errors with the same priority.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class RestXqError implements Comparable<RestXqError> {
  /** Error tests. */
  private final ArrayList<NameTest> tests = new ArrayList<>(1);

  /**
   * Adds a test if it has not been specified before.
   * @param test test to be added
   * @return success flag
   */
  boolean add(final NameTest test) {
    for(final NameTest t : tests) if(t.eq(test)) return false;
    tests.add(test);
    return true;
  }

  /**
   * Returns the test at the specified position, or {@code null}.
   * @param index test index
   * @return test
   */
  NameTest get(final int index) {
    return index < tests.size() ? tests.get(index) : null;
  }

  /**
   * Checks if the specified name matches the test.
   * @param name name
   * @return result of check
   */
  boolean matches(final QNm name) {
    for(final NameTest test : tests) if(test.eq(name)) return true;
    return false;
  }

  @Override
  public int compareTo(final RestXqError error) {
    final NameTest nt1 = tests.get(0), nt2 = error.tests.get(0);
    return nt1 == null || nt2 == null ? 0 : nt2.kind.ordinal() - nt1.kind.ordinal();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final NameTest test : tests) {
      if(sb.length() != 0) sb.append(", ");
      sb.append(test);
    }
    return sb.toString();
  }
}
