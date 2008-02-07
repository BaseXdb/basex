package org.basex.core.proc;

import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.io.PrintOutput;

/**
 * CD command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Cd extends XPath {
  @Override
  protected boolean exec() {
    final boolean ok = super.exec();

    if(ok && !(result instanceof Nodes))
      return error("Result must be a NodeSet.");

    if(result.size() != 0) context.current((Nodes) result);
    // remove query info
    if(!Prop.allInfo) error("");
    return ok;
  }

  @Override
  protected void out(final PrintOutput out) { }
}
