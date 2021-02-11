package org.basex.query.expr.path;

import static org.basex.query.value.type.AtomType.*;
import static org.basex.query.value.type.NodeType.*;

import java.util.*;
import java.util.List;
import java.util.function.*;

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
 * @author BaseX Team 2005-21, BSD License
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
   * @param name node name
   * @return test or {@code null}
   */
  public static Test get(final NodeType type, final QNm name) {
    return get(type, name, null, null);
  }

  /**
   * Returns a node test, a name test or {@code null}.
   * @param type node type (element, attribute, processing instruction)
   * @param name node name (can be {@code null})
   * @param ann type annotation (can be {@code null})
   * @param ns default element namespace (can be {@code null})
   * @return test or {@code null}
   */
  public static Test get(final NodeType type, final QNm name, final Type ann, final byte[] ns) {
    if(ann == null || ann.oneOf(ANY_TYPE, UNTYPED) ||
        type == ATTRIBUTE && ann.oneOf(ANY_SIMPLE_TYPE, ANY_ATOMIC_TYPE, UNTYPED_ATOMIC)) {
      return name == null ? KindTest.get(type) : new NameTest(name,
        type == PROCESSING_INSTRUCTION ? NamePart.LOCAL : NamePart.FULL, type, ns);
    }
    return null;
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

    // check if tests can be merged or discarded
    final List<Test> list = new ArrayList<>(tl);
    final Consumer<Test> add = tst -> {
      if(tst instanceof KindTest) {
        list.removeIf(t -> t instanceof NameTest);
      } else if(tst instanceof NameTest) {
        if(((Checks<Test>) t -> t instanceof KindTest).any(list)) return;
      }
      if(!list.contains(tst)) list.add(tst);
    };

    NodeType type = null;
    for(final Test test : tests) {
      if(test == null || type != null && type != test.type) return null;
      type = test.type;
      if(test instanceof UnionTest) {
        for(final Test t : ((UnionTest) test).tests) add.accept(t);
      } else {
        add.accept(test);
      }
    }
    return list.size() == 1 ? list.get(0) : new UnionTest(type, list.toArray(new Test[0]));
  }

  /**
   * Checks if evaluation can be dropped.
   * @param data data reference (can be {@code null})
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean noMatches(final Data data) {
    return false;
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
  public void plan(final QueryPlan plan) {
    throw Util.notExpected();
  }

  @Override
  public final void plan(final QueryString qs) {
    qs.token(toString(true));
  }
}
