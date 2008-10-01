package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.core.Process;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;

/**
 * Evaluates the 'delete' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Delete extends Process {
  /**
   * Constructor.
   * @param a arguments
   */
  public Delete(final String... a) {
    super(DATAREF | UPDATING, a);
  }
  
  @Override
  protected boolean exec() {
    final Data data = context.data();

    // gui mode: use currently marked nodes
    final boolean gui = args.length == 0;
    Nodes nodes;
    if(gui) {
      // ...from marked node set
      nodes = context.marked();
      context.marked(new Nodes(data));
    } else {
      // ...from query
      nodes = query(args[0], null);
    }
    if(nodes == null) return false;
    if(nodes.size == 0) return true;
    
    // reset indexes
    data.noIndex();

    // delete all nodes backwards to preserve pre values of earlier nodes
    final int size = nodes.size;
    for(int i = size - 1; i >= 0; i--) data.delete(nodes.pre[i]);
    
    // recreate root node
    //if(data.size == 0) data.insert(0, 1, Token.EMPTY, Data.DOC);

    // refresh current context
    final Nodes curr = context.current();
    if(gui && curr.size > 1 || curr.pre[0] == nodes.pre[0]) {
      context.current(new Nodes(0, data));
    }
    data.flush();
    context.current(new Nodes(data.doc(), data));

    return Prop.info ? info(DELETEINFO, nodes.size, perf.getTimer()) : true;
  }
}
