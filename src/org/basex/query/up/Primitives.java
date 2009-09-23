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
    for(final RenamePrimitive p : renames) {
      final DBNode n = (DBNode) p.node;
      rename(n.pre, p.newName, n.data);
    }
    
    for(final ReplacePrimitive p : replaces) {
      if(!(p.node instanceof DBNode)) continue;
      final DBNode n = (DBNode) p.node;
      int pre = n.pre;
      final int par = data.parent(pre, data.kind(pre));
      data.delete(pre);
      Nod i = (Nod) p.replaceNodes.next();
      if(Nod.kind(i.type) == Data.ATTR) {
        while(i != null) {
          final FAttr attr = (FAttr) i;
          data.insert(pre++, par, attr.qname().str(), attr.str());
          i = (Nod) p.replaceNodes.next();
        }
      } else {
        while(i != null) {
          // [LK] correct? PI and COM child().size()==0 ?? test ...
          if(i.child().size() == -1) {
            DBNode dbn = null;
            if(i instanceof DBNode) dbn = (DBNode) i;
            data.insert(pre++, par, dbn == null ? i.nname() : 
              dbn.data.tag(dbn.pre), Nod.kind(i.type));
          } else {
            final MemData mem = buildDB(i);
            // [LK] remove next line - testing
            if(mem == null) break;
            data.insert(pre++, par, mem);
          }
          i = (Nod) p.replaceNodes.next();
        }
      }
    }
    
    final IntList pres = new IntList();
    for(final DeletePrimitive p : deletes) pres.add(((DBNode) p.node).pre);
    deleteDBNodes(new Nodes(pres.finish(), data));
  }
}
