package org.basex.query.expr.path;

import java.util.*;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Union node test.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class UnionTest extends Test {
  /** Tests. */
  final Test[] tests;

  /**
   * Constructor.
   * @param type node type
   * @param tests tests
   */
  private UnionTest(final NodeType type, final Test[] tests) {
    super(type);
    this.tests = tests;
  }

  /**
   * Returns a combined node test.
   * @param tests tests
   * @return test, or {@code null} if test are empty or node types are different
   */
  public static Test get(final Test[] tests) {
    final int tl = tests.length;
    if(tl == 0) return null;
    if(tl == 1) return tests[0];

    final NodeType type = tests[0].type;
    for(int t = 1; t < tl; t++) {
      if(tests[t].type != type) return null;
    }
    return new UnionTest(type, tests);
  }

  @Override
  public boolean matches(final ANode node) {
    for(final Test test : tests) {
      if(test.matches(node)) return true;
    }
    return false;
  }

  @Override
  public Test copy() {
    return get(tests);
  }

  @Override
  public Test intersect(final Test test) {
    final ArrayList<Test> list = new ArrayList<>(1);
    for(final Test t : tests) {
      final Test t2 = t.intersect(test);
      if(t2 != null) list.add(t2);
    }
    return get(list.toArray(new Test[0]));
  }

  @Override
  public String toString() {
    return new TokenBuilder().addSep(tests, "|").toString();
  }
}
