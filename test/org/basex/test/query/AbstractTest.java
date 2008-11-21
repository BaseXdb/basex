package org.basex.test.query;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Namespaces;
import org.basex.data.Nodes;
import org.basex.data.Skeleton;
import org.basex.index.Names;
import org.basex.query.xpath.item.Bln;
import org.basex.query.xpath.item.Dbl;
import org.basex.query.xpath.item.Str;
import org.basex.util.Token;

/**
 * Test interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
abstract class AbstractTest {
  /** Dummy data reference. */
  static final Data DATA = new MemData(1, new Names(), new Names(),
      new Namespaces(), new Skeleton());
  /** Document. */
  String doc;
  /** Queries. */
  Object[][] queries;
  
  /**
   * This method can be overwritten to return test details, which are
   * shown in case of an error.
   * @return details string
   */
  String details() { return null; }
  
  /**
   * Create an {@link Nodes} instance
   * for the specified node values.
   * @param nodes node values
   * @return node array
   */
  static Nodes nodes(final int... nodes) {
    return new Nodes(nodes, DATA);
  }
  
  /**
   * Create a {@link Str} instance for the 
   * specified string.
   * @param str string
   * @return literal
   */
  static Str string(final String str) {
    return new Str(Token.token(str));
  }
  
  /**
   * Create a {@link Dbl} instance for the
   * specified double.
   * @param d double value
   * @return literal
   */
  static Dbl num(final double d) {
    return new Dbl(d);
  }
  
  /**
   * Create a {@link Bln} instance for the
   * specified boolean.
   * @param b boolean value
   * @return literal
   */
  static Bln bool(final boolean b) {
    return Bln.get(b);
  }
}
