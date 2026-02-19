package org.basex.query.expr.path;

import java.util.*;
import java.util.List;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract node test.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Test extends ExprInfo {
  /** Node kind. */
  public final Kind kind;

  /**
   * Constructor.
   * @param kind node kind
   */
  Test(final Kind kind) {
    this.kind = kind;
  }

  /**
   * Creates a single test.
   * @param tests tests to be merged (can contain {@code null} references)
   * @return test, or {@code null} due to missing tests or {@code null} references
   */
  public static Test get(final List<Test> tests) {
    final int ts = tests.size();
    if(ts == 0) return null;
    if(ts == 1) return tests.get(0);

    final List<Test> list = new ArrayList<>(ts);
    for(final Test test : tests) {
      if(test instanceof final UnionTest ut) {
        for(final Test t : ut.tests) merge(t, list);
      } else if(test != null) {
        merge(test, list);
      } else {
        return null;
      }
    }
    return list.size() == 1 ? list.get(0) : new UnionTest(list.toArray(Test[]::new));
  }

  /**
   * Merges a test into the union test list.
   * @param test test to be merged
   * @param list list
   */
  private static void merge(final Test test, final List<Test> list) {
    final int ls = list.size();
    for(int l = 0; l < ls; l++) {
      final Test t = list.get(l);
      // skip URI-based comparisons (may not be assigned yet at parse time)
      if(test instanceof NameTest ntest && t instanceof final NameTest nt && (
          ntest.scope == NameTest.Scope.URI || nt.scope == NameTest.Scope.URI)) continue;
      // * union A
      if(test.instanceOf(t)) return;
      // A union * → *
      if(t.instanceOf(test)) {
        list.set(l, test);
        return;
      }
    }
    // A union B → (A|B)
    list.add(test);
  }

  /**
   * Optimizes the test.
   * @param data data reference (can be {@code null})
   * @return resulting test, or {@code null} if the test yields no results
   */
  @SuppressWarnings("unused")
  public Test optimize(final Data data) {
    return this;
  }

  /**
   * Checks if the specified node matches the test.
   * @param node node to be checked
   * @return result of check
   */
  public abstract boolean matches(XNode node);

  /**
   * Checks if the current test will match items of the specified type.
   * @param tp type to be checked
   * @return {@link Boolean#TRUE}: always, {@link Boolean#FALSE}: never, {@code null}: unknown
   */
  public Boolean matches(@SuppressWarnings("unused") final Type tp) {
    return null;
  }

  /**
   * Copies this test.
   * @return deep copy
   */
  public abstract Test copy();

  /**
   * Checks if the current test is an instance of the specified test.
   * @param test test to be checked
   * @return result of check
   */
  public boolean instanceOf(final Test test) {
    return test instanceof final UnionTest ut ? ut.instance(this) :
      test.kind == kind || test.kind == Kind.NODE;
  }

  /**
   * Computes the intersection between two tests.
   * @param test other test
   * @return intersection if it exists, {@code null} otherwise
   */
  public abstract Test intersect(Test test);

  /**
   * Returns a string representation of this test.
   * @param full include node kind
   * @return string
   */
  public abstract String toString(boolean full);

  @Override
  public void toXml(final QueryPlan plan) {
    throw Util.notExpected();
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(toString(true));
  }
}
