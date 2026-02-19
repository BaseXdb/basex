package org.basex.query.expr.path;

import java.util.*;

import org.basex.data.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Union test for nodes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UnionTest extends Test {
  /** Tests. */
  public final Test[] tests;

  /**
   * Constructor.
   * @param tests tests
   */
  public UnionTest(final Test[] tests) {
    super(unionKind(tests));
    this.tests = tests;
  }

  /**
   * Calculate union kind of tests.
   * @param tests tests
   * @return union kind
   */
  private static Kind unionKind(final Test[] tests) {
    Kind kind = tests[0].kind;
    for(int i = 1; i < tests.length; ++i) {
      final Kind kn = tests[i].kind;
      if(kn != kind) kind = Kind.NODE;
    }
    return kind;
  }

  @Override
  public Test optimize(final Data data) {
    final ArrayList<Test> list = new ArrayList<>();
    for(final Test test : tests) {
      final Test t = test.optimize(data);
      if(t != null) list.add(t);
    }
    return tests.length != list.size() ? get(list) : this;
  }

  @Override
  public boolean matches(final XNode node) {
    for(final Test test : tests) {
      if(test.matches(node)) return true;
    }
    return false;
  }

  @Override
  public Boolean matches(final Type tp) {
    for(final Test test : tests) {
      final Boolean m = test.matches(tp);
      if(m != Boolean.FALSE) return m;
    }
    return Boolean.FALSE;
  }

  @Override
  public Test copy() {
    return this;
  }

  @Override
  public boolean instanceOf(final Test test) {
    for(final Test t : tests) {
      if(!t.instanceOf(test)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified test is an instance of this test.
   * @param test test to be checked
   * @return result of check
   */
  boolean instance(final Test test) {
    for(final Test t : tests) {
      if(test.instanceOf(t)) return true;
    }
    return false;
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
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final UnionTest ut && Arrays.equals(tests, ut.tests);
  }

  @Override
  public String toString(final boolean full) {
    final TokenBuilder tb = new TokenBuilder();
    char ch = '(';
    for(final Test test : tests) {
      tb.add(ch).add(test.toString(full || test.kind == Kind.ATTRIBUTE));
      ch = '|';
    }
    return tb.add(')').toString();
  }
}
