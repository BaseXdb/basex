package org.basex.core.proc;

import org.basex.data.Nodes;

/**
 * Evaluates the 'cd' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Cd extends AQuery {
  /**
   * Constructor.
   * @param q query
   */
  public Cd(final String q) {
    super(DATAREF, q);
  }
  
  @Override
  protected boolean exec() {
    final Nodes nodes = query(args[0], null);
    if(nodes != null && nodes.size() != 0) context.current(nodes);
    return nodes != null;
  }
}
