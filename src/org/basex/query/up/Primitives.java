package org.basex.query.up;

import static org.basex.query.up.UpdateFunctions.*;

import java.util.LinkedList;
import java.util.List;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FNode;
import org.basex.query.item.Nod;
import org.basex.util.IntList;

/**
 * Holds all update primitives for a specific data reference. The distinct 
 * primitives are hold seperately to support fast checking of update 
 * constraints.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class Primitives {
  /** Update primitives. */
  List<DeletePrimitive> deletes;
  /** Update primitives. */
  List<RenamePrimitive> renames;
  /** Update primitives. */
  List<ReplacePrimitive> replaces;
  /** Database reference. */
  Data data;
  
  /**
   * Constructor.
   */
  public Primitives() {
    deletes = new LinkedList<DeletePrimitive>();
    renames = new LinkedList<RenamePrimitive>();
    replaces = new LinkedList<ReplacePrimitive>();
  }

  /**
   * Adds a primitive to a primitive list depending on its type.
   * @param p update primitive
   */
  public void addPrimitive(final UpdatePrimitive p) {
    if(p instanceof DeletePrimitive) deletes.add((DeletePrimitive) p);
    else if(p instanceof RenamePrimitive) renames.add((RenamePrimitive) p);
    else if(p instanceof ReplacePrimitive) replaces.add((ReplacePrimitive) p);
    if(p.node instanceof DBNode && data == null) data = ((DBNode) p.node).data;
  }
  
  /**
   * Applies all updates to the data reference.
   * @throws QueryException query exception 
   */
  public void apply() throws QueryException {
    // rename
    for(final RenamePrimitive p : renames) {
      final DBNode n = (DBNode) p.node;
      rename(n.pre, p.newName, n.data);
    }
    
    // replace
    for(final ReplacePrimitive p : replaces) {
      // [LK] trgt / rpl node must be different nodes
      if(!(p.node instanceof DBNode)) continue;
      final DBNode n = (DBNode) p.node;
      int pre = n.pre;
      // [LK] check parent of replaced node
      final int par = data.parent(pre, data.kind(pre));
      // [LK] move delete?
      data.delete(pre);
      Nod i = (Nod) p.replaceNodes.next();
      if(Nod.kind(i.type) == Data.ATTR) {
        while(i != null) {
          // [LK] check for duplicate attributes
          if(i instanceof FNode) {
            final FAttr attr = (FAttr) i;
            data.insert(pre++, par, attr.qname().str(), attr.str());
          } else if(i instanceof DBNode) {
            final DBNode attr = (DBNode) i;
            final Data d = attr.data;
            data.insert(pre++, par, d.attName(attr.pre), 
                d.attValue(attr.pre));
          }
          i = (Nod) p.replaceNodes.next();
        }
      } else {
        while(i != null) {
          final int k = Nod.kind(i.type);
          if(k == Data.TEXT || k == Data.COMM || k == Data.PI) {
            // [LK] merge text nodes
            DBNode dbn = null;
            if(i instanceof DBNode) dbn = (DBNode) i;
            data.insert(pre++, par, dbn == null ? i.nname() : 
            dbn.data.tag(dbn.pre), Nod.kind(i.type));
          } else {
            // element nodes are added via a new MemData instance 
            final MemData m = buildDB(i);
            data.insert(pre++, par, m);
          }
          i = (Nod) p.replaceNodes.next();
        }
      }
    }
    
    // delete
    final IntList pres = new IntList();
    for(final DeletePrimitive p : deletes) pres.add(((DBNode) p.node).pre);
    deleteDBNodes(new Nodes(pres.finish(), data));
  }
}
