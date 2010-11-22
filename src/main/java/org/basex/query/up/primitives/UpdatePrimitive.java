package org.basex.query.up.primitives;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.basex.query.QueryException;
import org.basex.query.item.FTxt;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.NodIter;
import org.basex.query.up.NamePool;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Abstract XQuery Update Primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Kircher
 */
public abstract class UpdatePrimitive {
  /** Target node of update expression. */
  public final Nod node;
  /** Input information. */
  public final InputInfo input;

  /**
   * Constructor.
   * @param ii input info
   * @param n DBNode reference
   */
  protected UpdatePrimitive(final InputInfo ii, final Nod n) {
    input = ii;
    node = n;
  }

  /**
   * Returns the type of the update primitive.
   * @return type
   */
  public abstract PrimitiveType type();

  /**
   * Applies the update operation represented by this primitive to the
   * database. If an 'insert before' primitive is applied to a target node t,
   * the pre value of t changes. Thus the number of inserted nodes is added to
   * the pre value of t for all following update operations.
   * @param add size to add
   * @throws QueryException query exception
   */
  public abstract void apply(final int add) throws QueryException;

  /**
   * Prepares the update.
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public void prepare() throws QueryException { }

  /**
   * Merges if possible two update primitives of the same type if they have the
   * same target node.
   * @param p primitive to be merged with
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public void merge(final UpdatePrimitive p) throws QueryException { }

  /**
   * Updates the name pool, which is used for finding duplicate attributes
   * and namespace conflicts.
   * @param pool name pool
   */
  @SuppressWarnings("unused")
  public void update(final NamePool pool) { }

  /**
   * Merges all adjacent text nodes in the given sequence.
   * @param n iterator
   * @return iterator with merged text nodes
   */
  protected static NodIter mergeText(final NodIter n) {
    final NodIter s = new NodIter();
    Nod i = n.next();
    while(i != null) {
      if(i.type == Type.TXT) {
        final TokenBuilder tb = new TokenBuilder();
        while(i != null && i.type == Type.TXT) {
          tb.add(i.atom());
          i = n.next();
        }
        s.add(new FTxt(tb.finish(), null));
      } else {
        s.add(i);
        i = n.next();
      }
    }
    return s;
  }

  /**
   * Merges two adjacent text nodes in a database. The two node arguments must
   * be ordered ascending, otherwise the text of the two nodes is concatenated
   * in the wrong order.
   * @param d data reference
   * @param a node pre value
   * @param b node pre value
   * @return true if nodes have been merged
   */
  public static boolean mergeTexts(final Data d, final int a, final int b) {
    // some pre value checks to prevent database errors
    final int s = d.meta.size;
    if(a >= s || b >= s) return false;
    if(d.kind(a) != Data.TEXT || d.kind(b) != Data.TEXT) return false;
    if(d.parent(a, Data.TEXT) != d.parent(b, Data.TEXT)) return false;

    d.replace(a, Data.TEXT, concat(d.text(a, true), d.text(b, true)));
    d.delete(b);
    return true;
  }
}
