package org.basex.core.proc;

import org.basex.data.Nodes;

/**
 * CD command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Cd extends Proc {
  @Override
  protected boolean exec() {
    final Nodes nodes = query(cmd.args(), null);
    if(nodes == null) return false;

    if(nodes.size() != 0) context.current(nodes);
    return true;
  }
}
