package org.basex.query.up;

import static org.basex.query.QueryText.*;
import static org.basex.query.up.UpdateFunctions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.IntList;

/**
 * Holds all update operations and primitives a snapshot contains, checks
 * constraints and finally executes them.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class PendingUpdates {
  /** Holds delete primitives. */
  ArrayList<UpdatePrimitive> deletes;
  /** Holds rename primitives. */
  ArrayList<UpdatePrimitive> renames;
  
  /**
   * Constructor.
   */
  public PendingUpdates() {
    deletes = new ArrayList<UpdatePrimitive>();
    renames = new ArrayList<UpdatePrimitive>();
  }
  
  /**
   * Adds an update primitive to the corresponding primitive list.
   * @param p primitive to add
   */
  public void addPrimitive(final UpdatePrimitive p) {
    if(p instanceof DeletePrimitive) deletes.add(p);
    if(p instanceof RenamePrimitive) renames.add(p);
  }
  
  /**
   * Checks all update operations for correctness. 
   * XQueryUP specification 3.2.2
   * @throws QueryException query exception
   */
  private void checkConstraints() throws QueryException {
    final Set<Integer> err = new HashSet<Integer>();
    for(final UpdatePrimitive p : renames)
      if(!err.add(p.id)) Err.or(INCOMPLETE, p);
    err.clear();
    for(final UpdatePrimitive p : deletes)
      if(!err.add(p.id)) Err.or(INCOMPLETE, p);
  }
  
  /**
   * Applies all update primitives to the database if no constraints are hurt.
   * XQueryUP specification 3.2.2
   * @param ctx query context
   * @throws QueryException query exception
   */
  public void applyUpdates(final QueryContext ctx) throws QueryException {
    checkConstraints();
    final Data data = ctx.data();
    for(final UpdatePrimitive p : renames) {
      final RenamePrimitive rp = (RenamePrimitive) p;
      rename(rp.id, rp.newName, data);
    }
    // apply deletes
    final IntList il = new IntList();
    for(final UpdatePrimitive p : deletes) il.add(p.pre);
    delete(new Nodes(il.finish(), data));
  }
}
