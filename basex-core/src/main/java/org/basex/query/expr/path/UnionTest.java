package org.basex.query.expr.path;

import java.util.*;

import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Union node test.
 *
 * @author BaseX Team 2005-20, BSD License
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
  UnionTest(final NodeType type, final Test[] tests) {
    super(type);
    this.tests = tests;
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
    return this;
  }

  @Override
  public Test intersect(final Test test) {
    final ArrayList<Test> list = new ArrayList<>(tests.length);
    for(final Test t : tests) {
      final Test t2 = t.intersect(test);
      if(t2 != null) list.add(t2);
    }
    return get(list);
  }

  @Override
  public String toString() {
    return new TokenBuilder().addSeparated(tests, "|").toString();
  }
}
