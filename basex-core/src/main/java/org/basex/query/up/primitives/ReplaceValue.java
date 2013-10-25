package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * <p>ReplaceValue primitive. Replaces the value of a node.</p>
 *
 * <p>If the target T is an element node this primitive represents a replaceElementContent
 * expression (see XQUF). The children of T are deleted and a single (optional) text node
 * is inserted as the only child of T. The primitive for replaceElementContent is
 * substituted by <delete children(T), insertInto(T)>.</p>
 *
 * <p>After the end of the snapshot, T has either no child node at all (if the given text
 * node has been empty), or the given (non-empty) text node as a single child. Attributes
 * of T are not affected by a replaceElementContent expression.</p>
 *
 * <p>If T is a text node and the new text value is empty, T is deleted.</p>
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceValue extends UpdatePrimitive {
  /** New value. */
  public final byte[] value;
  /** States if this primitive represents a replaceElementContent expression. */
  public final boolean rec;

  /**
   * Constructor.
   * @param p target node PRE value
   * @param d target data reference
   * @param i input info
   * @param v new value
   */
  public ReplaceValue(final int p, final Data d, final InputInfo i, final byte[] v) {
    super(PrimitiveType.REPLACEVALUE, p, d, i);
    value = v;
    rec = d.kind(targetPre) == Data.ELEM;
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREPV.thrw(info, getTargetNode());
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public String toString() {
    return Util.info("%[%, %]", Util.className(this), getTargetNode(), value);
  }

  /**
   * Checks if this primitive leads to an empty text node.
   * @return true if application of primitive results in empty text node
   */
  private boolean deleteText() {
    return value.length == 0 && data.kind(targetPre) == Data.TEXT;
  }

  /**
   * Checks if this primitive is substituted.
   *
   * If target T is an element node, the primitive is substituted by a sequence of
   * <delete children(T), insertInto (T)>.
   * If T is a text node and the new value is empty, the primitive is substituted by
   * <delete T>, as empty text nodes are not allowed, see XDM.
   *
   * @return true if primitive is substituted
   */
  private boolean substituted() {
    return rec || deleteText();
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    if(!substituted())
      l.addUpdateValue(targetPre, value);
  }

  @Override
  public UpdatePrimitive[] substitute(final MemData tmp) {
    final int k = data.kind(targetPre);
    // else substitute if target is an element
    if(rec) {
      final List<UpdatePrimitive> l = new LinkedList<UpdatePrimitive>();
      // add the primitive to catch forbidden primitive merges (same target node)
      l.add(this);
      // add the delete primitives for the child nodes of the target
      // ... child axis boundaries
      final int firstChild = targetPre + data.attSize(targetPre, k);
      final int followingNode = targetPre + data.size(targetPre, k);
      int runner = firstChild;
      // while runner is child of target
      while(runner < followingNode) {
        l.add(new DeleteNode(runner, data, info, true));
        // set runner to following node
        runner += data.size(runner, data.kind(runner));
      }
      // add the insert for the optional text node, this is done by using an
      // insertIntoAsFirst primitive on the same target
      if(value.length > 0) {
        // create Data instance for insertion sequence
        // copy all nodes into a single database instance
        final int pre = tmp.meta.size;
        tmp.text(pre, 1, value, Data.TEXT);
        tmp.insert(pre);
        // add the substituting insertInto statement to the list
        final ANodeList nl = new ANodeList(new DBNode(tmp, pre));
        l.add(new ReplaceContent(targetPre, data, info, nl));
      }
      return l.toArray(new UpdatePrimitive[l.size()]);
    }

    // or a text node has to be deleted
    if(deleteText()) {
      // don't forget to add this primitive to catch forbidden primitive merges
      return new UpdatePrimitive[] { this, new DeleteNode(targetPre, data, info, false) };
    }

    // no substitution
    return new UpdatePrimitive[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
