package org.basex.query.xpath.path;

import org.basex.data.Data;
import org.basex.query.xpath.item.NodeBuilder;

/**
 * Abstract NodeTest. Can be a node name test, node type test, ...
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public abstract class Test {
  /**
   * Evaluates a node test.
   * @param data data reference
   * @param pre pre value
   * @param kind node kind
   * @return result of evaluation
   */
  public abstract boolean eval(Data data, int pre, int kind);

  /**
   * Evaluates a node test.
   * @param data data reference
   * @param pre pre value
   * @param kind node kind
   * @param result result nodes
   */
  public final void eval(final Data data, final int pre, final int kind,
      final NodeBuilder result) {
    if(eval(data, pre, kind)) result.add(pre);
  }

  /**
   * Optimizes the expression.
   * @param data data reference
   */
  @SuppressWarnings("unused")
  public void compile(final Data data) { }

  /**
   * Checks current and specified class for equivalence.
   * @param test node test to be compared
   * @return result of check
   */
  public abstract boolean sameAs(Test test);

  @Override
  public abstract String toString();
}
