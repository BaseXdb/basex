package org.basex.http.restxq;

import java.util.*;
import java.util.function.*;

import org.basex.query.expr.path.*;
import org.basex.query.value.item.*;

/**
 * This class catches RESTXQ errors with the same priority.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RestXqError implements Comparable<RestXqError> {
  /** Error tests. */
  private final ArrayList<NameTest> tests = new ArrayList<>(1);
  /** Function for comparing tests. */
  private static final Function<NameTest, Integer> COMPARE =
      test -> test == null ? -1 : test.part().ordinal();

  /**
   * Adds a test if it has not been specified before.
   * @param test test to be added
   * @return success flag
   */
  boolean add(final NameTest test) {
    for(final NameTest nt : tests) {
      if(Objects.equals(nt, test)) return false;
    }
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
   * Tests whether the error has no tests.
   * @return result of check
   */
  public boolean isEmpty() {
    return tests.isEmpty();
  }

  /**
   * Checks if the specified name matches the test.
   * @param name name
   * @return result of check
   */
  boolean matches(final QNm name) {
    for(final NameTest nt : tests) {
      if(nt == null || nt.matches(name)) return true;
    }
    return false;
  }

  @Override
  public int compareTo(final RestXqError error) {
    return COMPARE.apply(error.tests.get(0)) - COMPARE.apply(tests.get(0));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final NameTest test : tests) {
      if(sb.length() != 0) sb.append(", ");
      sb.append(test != null ? test : "*");
    }
    return sb.toString();
  }
}
