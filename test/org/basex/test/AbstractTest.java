package org.basex.test;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.index.Names;

/**
 * Test interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-07, ISC License
 * @author Christian Gruen
 */
abstract class AbstractTest {
  /** Dummy data reference. */
  static final Data DATA = new MemData(1, new Names(true), new Names(false));
  /** Title. */
  String title;
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
}
