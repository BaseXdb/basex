package org.basex.query.up;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.basex.data.Data;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes them.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class PendingUpdates {
  /** Delete primitives. */
  ArrayList<DeletePrimitive> deletes;
  /** Rename primitives. */
  ArrayList<RenamePrimitive> renames;
  /** Replace primitives. */
  ArrayList<ReplacePrimitive> replaces;
  
  /**
   * Constructor.
   */
  public PendingUpdates() {
    deletes = new ArrayList<DeletePrimitive>();
    renames = new ArrayList<RenamePrimitive>();
    replaces = new ArrayList<ReplacePrimitive>();
  }
  
  /**
   * Adds an update primitive to the corresponding primitive list.
   * @param p primitive to add
   */
  public void addPrimitive(final UpdatePrimitive p) {
    if(p instanceof DeletePrimitive) deletes.add((DeletePrimitive) p);
    if(p instanceof RenamePrimitive) renames.add((RenamePrimitive) p);
    if(p instanceof ReplacePrimitive) replaces.add((ReplacePrimitive) p);
  }
  
  /**
   * Applies all update primitives to the database if no constraints are hurt.
   * XQueryUP specification 3.2.2
   */
  public void applyUpdates()  {
    // [LK] check constraints
    
    // [LK] apply updates
    final Map<Data, DBNode> delData = new HashMap<Data, DBNode>();
    ArrayList<Nod> delFrag = new ArrayList<Nod>();
    for(final DeletePrimitive p : deletes) {
      final Nod n = p.node;
      if(n instanceof DBNode) delData.put(((DBNode) n).data, (DBNode) n);
      else delFrag.add(n);
    }
  }
}
