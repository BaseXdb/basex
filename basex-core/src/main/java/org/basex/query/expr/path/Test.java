package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract node test.
 *
 * @author BaseX Team 2005-19, BSD License
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
   * @param ann type annotation
   * @param ns default element namespace (may be {@code null})
   * @return test or {@code null}
   */
  public static Test get(final NodeType type, final QNm name, final Type ann, final byte[] ns) {
    if(!(ann == null || ann == AtomType.ATY || ann == AtomType.UTY || type == NodeType.ATT &&
      (ann == AtomType.AST || ann == AtomType.AAT || ann == AtomType.ATM))) return null;

    return name == null ? KindTest.get(type) :
      new NameTest(type, name, type == NodeType.PI ? NamePart.LOCAL : NamePart.FULL, ns);
  }

  /**
   * Optimizes the expression.
   * @param value context value (can be {@code null})
   * @return false if test always returns false
   */
  @SuppressWarnings("unused")
  public boolean optimize(final Value value) {
    return true;
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
   * Returns the name part.
   * @return part of name to be tested (can be {@code null})
   */
  public NamePart part() {
    return null;
  }

  /**
   * Returns the name test.
   * @return name test (can be {@code null})
   */
  public QNm name() {
    return null;
  }

  /**
   * Copies this test.
   * @return deep copy
   */
  public abstract Test copy();

  /**
   * Computes the intersection between two tests.
   * @param test other test
   * @return intersection if it exists, {@code null} otherwise
   */
  public abstract Test intersect(Test test);

  @Override
  public void plan(final QueryPlan plan) {
    throw Util.notExpected();
  }
}
