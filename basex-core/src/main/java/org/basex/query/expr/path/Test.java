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
 * @author BaseX Team 2005-23, BSD License
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
   * Returns a node test, a name test or {@code null}.
   * @param type node type (element, attribute, processing instruction)
   * @param name node name (can be {@code null})
   * @param defaultNs default element namespace (used for optimizations, can be {@code null})
   * @return test
   */
  public static Test get(final NodeType type, final QNm name, final byte[] defaultNs) {
    final NamePart part = type == NodeType.PROCESSING_INSTRUCTION ? NamePart.LOCAL : NamePart.FULL;
    return new NameTest(name, part, type, defaultNs);
  }

  /**
   * Creates a single test with the same node type.
   * @param tests tests to be merged (can contain {@code null} references)
   * @return single test, union test, or {@code null} if test cannot be created.
   */
  public static Test get(final Test... tests) {
    final int tl = tests.length;
    if(tl == 0) return null;
    if(tl == 1) return tests[0];

    // find common node type
    NodeType type = null;
    for(final Test test : tests) {
      if(test == null || type != null && type != test.type) return null;
      type = test.type;
    }

    // merge name tests
    final List<Test> list = new ArrayList<>(tl);
    for(final Test test : tests) {
      if(test instanceof KindTest) return test;
      if(test instanceof UnionTest) {
        for(final Test t : ((UnionTest) test).tests) {
          if(!list.contains(t)) list.add(t);
        }
      } else if(!list.contains(test)) {
        list.add(test);
      }
    }
    return list.size() == 1 ? list.get(0) : new UnionTest(type, list.toArray(Test[]::new));
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
    return item instanceof ANode && matches((ANode) item);
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
    return test instanceof UnionTest ? ((UnionTest) test).instance(this) :
      type.instanceOf(test.type);
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
