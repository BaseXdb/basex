package org.basex.query.up.primitives.node;

import static org.basex.query.QueryError.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Replace node primitive.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceNode extends NodeCopy {
  /**
   * Constructor.
   * @param pre target node PRE value
   * @param data target data instance
   * @param info input info (can be {@code null})
   * @param nodes node copy insertion sequence
   */
  public ReplaceNode(final int pre, final Data data, final InputInfo info, final ANodeList nodes) {
    super(UpdateType.REPLACENODE, pre, data, info, nodes);
  }

  @Override
  public void update(final NamePool pool) {
    if(insseq == null) return;
    add(pool);
    pool.remove(node());
  }

  @Override
  public void merge(final Update update) throws QueryException {
    throw UPMULTREPL_X.get(info, node());
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addReplace(pre, insseq);
  }

  @Override
  public int size() {
    return Math.max(1, insseq.fragments);
  }
}
