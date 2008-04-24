package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;

/**
 * Evaluates the 'delete' command. Deletes a node from the table.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Delete extends Updates {
  @Override
  protected boolean exec() {
    final Data data = context.data();

    // retrieve nodes to be deleted...
    Nodes nodes;
    final boolean gui = cmd.nrArgs() == 0;
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
    data.meta.noIndex();

    // delete all nodes
    final int size = nodes.size;
    for(int i = size - 1; i >= 0; i--) {
      if(nodes.pre[i] == 0) return error(DELETEROOT);
      data.delete(nodes.pre[i]);
    }

    if(gui && context.current().size > 1 || 
        context.current().pre[0] == nodes.pre[0]) {
      context.current(new Nodes(0, data));
    }
    data.flush();

    return Prop.info ? timer(BaseX.info(DELETEINFO, nodes.size)) : true;
  }
}
