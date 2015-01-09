package org.basex.query.up.primitives.node;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * <p>ReplaceValue primitive. Replaces the value of a node.</p>
 *
 * <p>If the target T is an element node this primitive represents a replaceElementContent
 * expression (see XQUF). The children of T are deleted and a single (optional) text node
 * is inserted as the only child of T. The primitive for replaceElementContent is
 * substituted by [delete children(T), insertInto(T)].</p>
 *
 * <p>After the end of the snapshot, T has either no child node at all (if the given text
 * node has been empty), or the given (non-empty) text node as a single child. Attributes
 * of T are not affected by a replaceElementContent expression.</p>
 *
 * <p>If T is a text node and the new text value is empty, T is deleted.</p>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceValue extends NodeUpdate {
  /** New value. */
  private final byte[] value;
  /** States if this primitive represents a replaceElementContent expression. */
  public final boolean rec;

  /**
   * Constructor.
   * @param pre target node PRE value
   * @param data target data reference
   * @param ii input info
   * @param value new value
   */
  public ReplaceValue(final int pre, final Data data, final InputInfo ii, final byte[] value) {
    super(UpdateType.REPLACEVALUE, pre, data, ii);
    this.value = value;
    rec = data.kind(pre) == Data.ELEM;
  }

  @Override
  public void prepare(final MemData tmp) { }

  @Override
  public void merge(final Update update) throws QueryException {
    throw UPMULTREPV_X.get(info, node());
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public String toString() {
    return Util.info("%[%, %]", Util.className(this), node(), value);
  }

  /**
   * Checks if this primitive leads to an empty text node.
   * @return true if application of primitive results in empty text node
   */
  private boolean deleteText() {
    return value.length == 0 && data.kind(pre) == Data.TEXT;
  }

  /**
   * Checks if this primitive is substituted.
   *
   * If target T is an element node, the primitive is substituted by a sequence of
   * [delete children(T), insertInto (T)].
   * If T is a text node and the new value is empty, the primitive is substituted by
   * [delete T], as empty text nodes are not allowed, see XDM.
   *
   * @return true if primitive is substituted
   */
  private boolean substituted() {
    return rec || deleteText();
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    if(!substituted()) auc.addUpdateValue(pre, value);
  }

  @Override
  public NodeUpdate[] substitute(final MemData tmp) {
    final int k = data.kind(pre);
    // else substitute if target is an element
    if(rec) {
      final List<NodeUpdate> l = new LinkedList<>();
      // add the primitive to catch forbidden primitive merges (same target node)
      l.add(this);
      // add the delete primitives for the child nodes of the target
      // ... child axis boundaries
      final int firstChild = pre + data.attSize(pre, k);
      final int followingNode = pre + data.size(pre, k);
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
        final int p = tmp.meta.size;
        tmp.text(p, 1, value, Data.TEXT);
        tmp.insert(p);
        // add the substituting insertInto statement to the list
        final ANodeList nl = new ANodeList(new DBNode(tmp, p));
        l.add(new ReplaceContent(pre, data, info, nl));
      }
      return l.toArray(new NodeUpdate[l.size()]);
    }

    // or a text node has to be deleted
    if(deleteText()) {
      // don't forget to add this primitive to catch forbidden primitive merges
      return new NodeUpdate[] { this, new DeleteNode(pre, data, info, false) };
    }

    // no substitution
    return new NodeUpdate[] { this };
  }

  @Override
  public void update(final NamePool pool) { }
}
