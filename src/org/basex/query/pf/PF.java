package org.basex.query.pf;

import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPathProcessor;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * This class evaluates XPath requests on the pathfinder plan.
 * Moreover, it indexes compiled queries to speed up repeated executions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class PF extends Set {
  /** Hash values. */
  private XPathProcessor[] qu = new XPathProcessor[CAP];
  /** Data reference. */
  private final Data data;

  /**
   * Constructor.
   * @param d data reference
   */
  PF(final Data d) { data = d; }
  
  /**
   * Evaluates the specified query and returns the resulting node set.
   * @param q query
   * @param p pre node to start from
   * @return node set
   * @throws QueryException query exception
   */
  int[] get(final byte[] q, final int p) throws QueryException {
    final int i = add(q);
    final Nodes n = new Nodes(p, data);
    if(i > 0) qu[i] = new XPathProcessor(Token.string(q));
    return ((NodeSet) qu[Math.abs(i)].eval(n)).nodes;
  }

  @Override
  protected void rehash() {
    super.rehash();
    final XPathProcessor[] v = new XPathProcessor[size << 1];
    System.arraycopy(qu, 0, v, 0, size);
    qu = v;
  }
}
