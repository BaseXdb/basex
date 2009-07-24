package org.basex.test.query;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Namespaces;
import org.basex.data.Nodes;
import org.basex.data.PathSummary;
import org.basex.index.Names;
import org.basex.query.item.Bln;
import org.basex.query.item.Dec;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.iter.SeqIter;
import org.basex.util.Token;

/**
 * Test interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
abstract class AbstractTest {
  /** Dummy data reference. */
  static final Data DATA = new MemData(1, new Names(), new Names(),
      new Namespaces(), new PathSummary());
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
   * Creates a container for the specified node values.
   * @param nodes node values
   * @return node array
   */
  static Nodes nodes(final int... nodes) {
    return new Nodes(nodes, DATA);
  }

  /**
   * Creates an iterator for the specified string.
   * @param str string
   * @return literal
   */
  static SeqIter string(final String str) {
    return new SeqIter(Str.get(Token.token(str)));
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return literal
   */
  static SeqIter itr(final long d) {
    return new SeqIter(Itr.get(d));
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return literal
   */
  static SeqIter dec(final double d) {
    return new SeqIter(Dec.get(d));
  }

  /**
   * Creates an iterator for the specified boolean.
   * @param b boolean value
   * @return literal
   */
  static SeqIter bool(final boolean b) {
    return new SeqIter(Bln.get(b));
  }
}
