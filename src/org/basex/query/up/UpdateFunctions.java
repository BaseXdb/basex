package org.basex.query.up;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FElem;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * XQuery Update update functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public final class UpdateFunctions {
  
  /**
   * Constructor.
   */
  private UpdateFunctions() { }
  
  /**
   * Delete nodes from database. Nodes are deleted backwards to preserve pre
   * values. All given nodes are part of the same Data instance.
   * @param nodes nodes to delete
   */
  public static void deleteDBNodes(final Nodes nodes) {
    final Data data = nodes.data;
    final int size = nodes.size();
    for(int i = size - 1; i >= 0; i--) {
      final int pre = nodes.nodes[i];
      if(data.fs != null) data.fs.delete(pre);
      data.delete(pre);
    }
  }
  
  /**
   * Renames the specified node.
   * @param id node identity
   * @param name new name
   * @param data data reference
   */
  public static void rename(final int id, final byte[] name, final Data data) {
    final int p = data.pre(id);
    final int k = data.kind(p);
    if(k == Data.ELEM || k == Data.PI) data.update(data.pre(id), name);
    else if(k == Data.ATTR) {
      final byte[] v = data.attValue(p);
      data.update(p, name, v);
    }
  }
  
  /**
   * Builds new MemData instance from iterator.
   * @param n node
   * @param item iterator
   * @return new MemData instance
   * @throws QueryException query exception
   */
  public static MemData buildDB(final DBNode n, final Item item) 
      throws QueryException {
    final Data d = n.data;
    final MemData m = new MemData(20, d.tags, d.atts, d.ns, d.path, 
        d.meta.prop);
    final Iter it = item.iter();
    Item i = it.next();
    while(i != null) {
      if(i instanceof FElem) {
//        final FElem e = (FElem) i;
//        final int ti = m.tags.add(e.str());
//        m.addElem(ti, n, d, a, s, ne);
      }
      i = it.next();
    }
    return m;
  }
}
