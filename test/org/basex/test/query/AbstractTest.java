package org.basex.test.query;

import org.basex.core.AProp;
import org.basex.data.Nodes;
import org.basex.query.item.Bln;
import org.basex.query.item.Dec;
import org.basex.query.item.Item;
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
  /** Document. */
  String doc;
  /** Queries. */
  Object[][] queries;

  /**
   * This method can be overwritten to return test details, which are
   * shown in case of an error.
   * @param prop database properties
   * @return details string
   */
  abstract String details(final AProp prop);

  /**
   * Creates a container for the specified node values.
   * @param nodes node values
   * @return node array
   */
  static Nodes nodes(final int... nodes) {
    return new Nodes(nodes);
  }

  /**
   * Creates an iterator for the specified string.
   * @param str string
   * @return iterator
   */
  static SeqIter string(final String str) {
    return item(Str.get(Token.token(str)));
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return iterator
   */
  static SeqIter itr(final long d) {
    return item(Itr.get(d));
  }

  /**
   * Creates an iterator for the specified double.
   * @param d double value
   * @return iterator
   */
  static SeqIter dec(final double d) {
    return item(Dec.get(d));
  }

  /**
   * Creates an iterator for the specified boolean.
   * @param b boolean value
   * @return iterator
   */
  static SeqIter bool(final boolean b) {
    return item(Bln.get(b));
  }

  /**
   * Creates an iterator for the specified item.
   * @param i item
   * @return iterator
   */
  private static SeqIter item(final Item i) {
    return new SeqIter(new Item[] { i }, 1);
  }
}
