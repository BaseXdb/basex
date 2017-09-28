package org.basex.query.expr.path;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract node test.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Test extends ExprInfo {
  /** Kind of name test. */
  public enum Kind {
    /** Accept all nodes (*).            */ WILDCARD,
    /** Test name (*:name).              */ NAME,
    /** Test uri (prefix:*).             */ URI,
    /** Test uri and name (prefix:name). */ URI_NAME
  }

  /** Node kind. */
  public final NodeType type;
  /** Kind of name test (can be {@code null}). */
  public Kind kind;
  /** Name test (can be {@code null}). */
  public QNm name;
  /** Indicates if test will match exactly one node (e.g.: @id). */
  public boolean one;

  /**
   * Constructor.
   * @param type node type
   */
  Test(final NodeType type) {
    this.type = type;
  }

  /**
   * Optimizes the expression.
   * @param cc compilation context
   * @return false if test always returns false
   */
  @SuppressWarnings("unused")
  public boolean optimize(final QueryContext cc) {
    return true;
  }

  /**
   * Tests if the test yields true.
   * @param node node to be checked
   * @return result of check
   */
  public abstract boolean eq(ANode node);

  /**
   * Tests if the test yields true.
   * @param item item to be checked
   * @return result of check
   */
  public boolean eq(final Item item) {
    return item instanceof ANode && eq((ANode) item);
  }

  /**
   * Copies this test.
   * @return deep copy
   */
  public abstract Test copy();

  @Override
  public void plan(final FElem e) {
    throw Util.notExpected();
  }

  /**
   * Checks if this test is namespace-sensitive.
   * @return result of check
   */
  boolean nsSensitive() {
    return name != null;
  }

  /**
   * Computes the intersection between two tests.
   * @param other other test
   * @return intersection if it exists, {@code null} otherwise
   */
  public abstract Test intersect(Test other);
}
