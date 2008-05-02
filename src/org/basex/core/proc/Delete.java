package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;

/**
 * Evaluates the 'delete' command. Deletes nodes from the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Delete extends Proc {
  @Override
  protected boolean exec() {
    final Data data = context.data();

    // gui mode: use currently marked nodes
    final boolean gui = cmd.nrArgs() == 0;
    Nodes nodes;
    if(gui) {
      // ...from marked node set
      nodes = context.marked();
      context.marked(new Nodes(data));
    } else {
      // ...from query
      nodes = query(cmd.arg(0), null);
    }
    if(nodes == null) return false;
    if(nodes.size == 0) return true;
    
    // reset indexes
    data.noIndex();

    // delete all nodes
    final int size = nodes.size;
    for(int i = size - 1; i >= 0; i--) {
      if(nodes.pre[i] == 0) return error(DELETEROOT);
      data.delete(nodes.pre[i]);
    }

    // refresh current context
    final Nodes curr = context.current();
    if(gui && curr.size > 1 || curr.pre[0] == nodes.pre[0]) {
      context.current(new Nodes(0, data));
    }
    data.flush();

    return Prop.info ? timer(BaseX.info(DELETEINFO, nodes.size)) : true;
  }
}
