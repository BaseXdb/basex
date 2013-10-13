package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Rename node primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class RenameNode extends UpdatePrimitive {
  /** New name. */
  private final QNm name;

  /**
   * Constructor.
   * @param p target node pre value
   * @param d target data reference
   * @param i input info
   * @param nm new QName / new name value
   */
  public RenameNode(final int p, final Data d, final InputInfo i,
      final QNm nm) {
    super(PrimitiveType.RENAMENODE, p, d, i);
    name = nm;
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    UPMULTREN.thrw(info, getTargetNode());
  }

  @Override
  public void update(final NamePool pool) {
    final DBNode node = getTargetNode();
    pool.add(name, node.nodeType());
    pool.remove(node);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + getTargetNode() + ", " + name + ']';
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void addAtomics(final AtomicUpdateList l) {
    l.addRename(targetPre, data.kind(targetPre), name.string(), name.uri());
  }

  @Override
  public UpdatePrimitive[] substitute(final MemData tmp) {
    return new UpdatePrimitive[] { this };
  }
}
