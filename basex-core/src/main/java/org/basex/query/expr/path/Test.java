package org.basex.query.expr.path;

import java.util.*;
import java.util.List;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
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
  /** Node type. */
  public final NodeType type;

  /**
   * Constructor.
   * @param type node type
   */
  Test(final NodeType type) {
    this.type = type;
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
    final boolean skip = test instanceof final NameTest nt && nt.scope != NameTest.Scope.FULL;
    final int ls = list.size();
    for(int l = 0; l < ls; l++) {
      final Test t = list.get(l);
      // skip partial name tests (*:A, A:*)
      if(skip || t instanceof final NameTest nt && nt.scope != NameTest.Scope.FULL) continue;
      // * union A
      if(test.instanceOf(t)) return;
      // A union *
      if(t.instanceOf(test)) {
        list.set(l, test);
        return;
      }
    }
    // A union B
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
  public abstract boolean matches(ANode node);

  /**
   * Checks if the specified item matches the test.
   * @param item item to be checked
   * @return result of check
   */
  public final boolean matches(final Item item) {
    return item instanceof final ANode node && matches(node);
  }

  /**
   * Checks if the current test will match items of the specified type.
   * @param seqType type to be checked
   * @return result of check (matches never, always, or {@code null} if unknown)
   */
  public Boolean matches(@SuppressWarnings("unused") final SeqType seqType) {
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
    return test instanceof final UnionTest ut ? ut.instance(this) : type.instanceOf(test.type);
  }

  /**
   * Computes the intersection between two tests.
   * @param test other test
   * @return intersection if it exists, {@code null} otherwise
   */
  public abstract Test intersect(Test test);

  /**
   * Returns a string representation of this test.
   * @param full include node type
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
