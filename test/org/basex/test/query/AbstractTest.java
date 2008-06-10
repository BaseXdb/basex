package org.basex.test.query;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.index.Names;
import org.basex.index.Namespaces;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Literal;
import org.basex.query.xpath.values.Num;
import org.basex.util.Token;

/**
 * Test interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
abstract class AbstractTest {
  /** Dummy data reference. */
  static final Data DATA = new MemData(1, new Names(true), new Names(false),
      new Namespaces());
  /** Document. */
  String doc;
  /** Queries. */
  Object[][] queries;

  /**
   * Constructor.
   */
  protected AbstractTest() { }
  
  /**
   * Create an {@link org.basex.data.Nodes} instance
   * for the specified node values.
   * @param nodes node values
   * @return node array
   */
  static Nodes nodes(final int... nodes) {
    return new Nodes(nodes, DATA);
  }
  
  /**
   * Create a {@link org.basex.query.xpath.values.Literal} instance for the 
   * specified string.
   * @param str string
   * @return literal
   */
  static Literal string(final String str) {
    return new Literal(Token.token(str));
  }
  
  /**
   * Create a {@link org.basex.query.xpath.values.Num} instance for the
   * specified double.
   * @param d double value
   * @return literal
   */
  static Num num(final double d) {
    return new Num(d);
  }
  
  /**
   * Create a {@link org.basex.query.xpath.values.Bool} instance for the
   * specified boolean.
   * @param b boolean value
   * @return literal
   */
  static Bool bool(final boolean b) {
    return Bool.get(b);
  }
}
