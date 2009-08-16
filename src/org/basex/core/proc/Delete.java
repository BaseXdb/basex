package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Nodes;

/**
 * Evaluates the 'delete' command and deletes nodes in the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Delete extends AUpdate {
  /**
   * Default constructor.
   * @param target target query
   */
  public Delete(final String target) {
    super(false, target);
  }

  /**
   * Constructor, used by the GUI.
   */
  public Delete() {
    super(true);
  }

  @Override
  protected boolean exec() {
    if(!checkDB()) return false;

    final Data data = context.data();
    Nodes nodes;
    if(gui) {
      // gui mode: use currently marked nodes as source
      nodes = context.marked();
      context.marked(new Nodes(data));
      context.copy(null);
    } else {
      nodes = query(args[0], null);
      if(nodes == null) return false;
    }

    // delete all nodes backwards to preserve pre values of earlier nodes
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int pre = nodes.nodes[i];
      if(data.fs != null) data.fs.delete(pre);
      data.delete(pre);
    }

    data.flush();
    context.update();
    return info(DELETEINFO, nodes.size(), perf.getTimer());
  }

  @Override
  public String toString() {
    return BaseX.name(this).toUpperCase() + " " + args[0];
  }
}
