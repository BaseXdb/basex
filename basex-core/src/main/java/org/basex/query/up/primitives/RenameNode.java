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
 * @author BaseX Team 2005-14, BSD License
 * @author Lukas Kircher
 */
public final class RenameNode extends NodeUpdate {
  /** New name. */
  private final QNm name;

  /**
   * Constructor.
   * @param pre target node pre value
   * @param data target data reference
   * @param ii input info
   * @param name new QName / new name value
   */
  public RenameNode(final int pre, final Data data, final InputInfo ii, final QNm name) {
    super(UpdateType.RENAMENODE, pre, data, ii);
    this.name = name;
  }

  @Override
  public void prepare(final MemData tmp) { }

  @Override
  public void merge(final Update update) throws QueryException {
    throw UPMULTREN_X.get(info, node());
  }

  @Override
  public void update(final NamePool pool) {
    final DBNode node = node();
    pool.add(name, node.nodeType());
    pool.remove(node);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + node() + ", " + name + ']';
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addRename(pre, name.string(), name.uri());
  }

  @Override
  public NodeUpdate[] substitute(final MemData tmp) {
    return new NodeUpdate[] { this };
  }
}
