package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdUpdate;
import org.basex.data.Data;
import org.basex.data.Nodes;

/**
 * Updates an element in  database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Update extends AUpdate {
  /**
   * Constructor.
   * @param t update type
   * @param a arguments
   */
  public Update(final CmdUpdate t, final String... a) {
    this(false, t, a);
  }

  /**
   * Constructor for GUI updates.
   * @param g gui flag
   * @param t update type
   * @param a arguments
   */
  public Update(final boolean g, final CmdUpdate t, final String... a) {
    super(g, t, a);
  }

  @Override
  protected boolean exec() {
    final Data data = context.data();

    // get sources from the marked node set or the specified query
    final Nodes nodes = gui ? context.marked() : query(args[type ==
      CmdUpdate.PI || type == CmdUpdate.ATTRIBUTE ? 2 : 1], null);
    if(nodes == null) return false;

    boolean ok = false;
    switch(type) {
      case ATTRIBUTE: ok = attr(data, nodes); break;
      default:        ok = node(data, nodes); break;
    }
    if(!ok) return false;

    data.flush();
    return Prop.info ? info(UPDATEINFO, nodes.size, perf.getTimer()) : true;
  }
  
  /**
   * Updates attributes.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean attr(final Data data, final Nodes nodes) {
    final byte[] n = token(args[0]);
    final byte[] v = token(args[1]);
    if(!check(n)) return error(ATTINVALID, n);
    
    // check if errors can occur
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int pre = nodes.nodes[i];
      if(data.kind(pre) != Data.ATTR)
        return error(UPDATENODE, CmdUpdate.values()[Data.ATTR]);
      // check existence of attribute
      int p = data.parent(pre, Data.ATTR);
      final int last = p + data.attSize(p, Data.ELEM);
      final int att = data.attNameID(n);
      while(++p < last) {
        if(p != pre && att == data.attNameID(p)) return error(ATTDUPL, n);
      }
    }
    
    // perform updates
    data.update();
    for(int i = nodes.size - 1; i >= 0; i--) data.update(nodes.nodes[i], n, v);
    return true;
  }
  
  /**
   * Updates nodes.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean node(final Data data, final Nodes nodes) {
    byte[] v = token(args[0]);
    final int kind = type.ordinal();
    final boolean pi = kind == Data.PI;
    
    if(kind == Data.TEXT) {
      if(v.length == 0) return error(TXTINVALID, v);
    } else if(kind == Data.ELEM || pi) {
      if(!check(v)) return error(NAMEINVALID, v);
      if(pi) {
        final byte[] vv = token(args[1]);
        v = v.length == 0 ? vv : concat(v, SPACE, vv);
      }
    }
    
    // check if nodes to be updated have the same type
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int k = data.kind(nodes.nodes[i]);
      if(k != kind) return error(UPDATENODE, CmdUpdate.values()[kind]);
    }
    
    // perform updates
    data.update();
    for(int i = nodes.size - 1; i >= 0; i--) data.update(nodes.nodes[i], v);
    return true;
  }

  
  @Override
  public String toString() {
    return name() + " " + type + args();
  }
}
