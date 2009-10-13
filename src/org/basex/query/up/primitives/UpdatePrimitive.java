package org.basex.query.up.primitives;

import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.query.item.Nod;

/**
 * Abstract XQuery Update Primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class UpdatePrimitive {
  /** Target node of update expression. */
  public final Nod node;
  /** Multiple updates are applied on this target node. */
  boolean mult;
  
  /**
   * Update primitive type enumeration.
   *
   * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
   * @author Lukas Kircher
   *
   */
  public enum Type { 
    /** Type Insert into. */ INSERTINTO,
    /** Type Insert attribute. */ INSERTATTR,
    /** Type Replace value. */ REPLACEVALUE,
    /** Type Rename. */ RENAME,
    /** Type Insert before. */ INSERTBEFORE,
    /** Type Insert after. */ INSERTAFTER,
    /** Type Insert into as first. */ INSERTINTOFI,
    /** Type Insert into as last. */ INSERTINTOLA,
    /** Type Replace node. */ REPLACENODE,
    /** Type Replace element content. */ REPLACEELEMCONT,
    /** Type Delete. */ DELETE;
  };
  
  /**
   * Constructor.
   * @param n DBNode reference
   */
  protected UpdatePrimitive(final Nod n) {
    node = n;
    mult = false;
  }
  
  /**
   * Returns the type of the update primitive.
   * @return type
   */
  public abstract Type type();
  
  /**
   * Returns the actual pre location an update takes place in a database. For
   * the greater part of update primitives this function returns the pre 
   * value of the target node. For an 'insert into' command i.e. the position of
   * the new nodes differs from the position of the target node.
   * @return pre value
   */
  public int ac() {
    if(node instanceof DBNode) return ((DBNode) node).pre;
    return node.id();
  }
  
  /**
   * Checks for constraints, etc.
   * @throws QueryException query exception 
   */
  public abstract void check() throws QueryException;
  
  /**
   * Applies the update operation represented by this primitive to the 
   * database. If an 'insert before' primitive is applied to a target node t,
   * the pre value of t changes. Thus the number of inserted nodes is added to
   * the pre value of t for the following update operations.
   * @param add size to add
   * @throws QueryException query exception 
   */
  public abstract void apply(final int add) throws QueryException;
  
  /**
   * Merges if possible two update primitives of the same type if they are
   * applied on the same target.
   * @param p primitive to be merged with 
   * @throws QueryException query exception
   */
  public abstract void merge(final UpdatePrimitive p) throws QueryException;
}
