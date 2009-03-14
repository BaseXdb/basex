package org.basex.core.proc;

import org.basex.data.Nodes;

/**
 * Evaluates the 'cs' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Cs extends AQuery {
  /**
   * Constructor.
   * @param q query
   */
  public Cs(final String q) {
    super(DATAREF, q);
  }
  
  @Override
  protected boolean exec() {
    final Nodes nodes = query(args[0], null);
    if(nodes != null && nodes.size() != 0) {
      context.current(nodes);
      result = nodes;
    }
    return nodes != null;
  }
}
