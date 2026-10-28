package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Insert after primitive.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class InsertAfter extends NodeCopy {
  /**
   * Constructor.
   * @param pre target PRE value
   * @param data target data instance
   * @param info input info (can be {@code null})
   * @param nodes node copy insertion sequence
   */
  public InsertAfter(final int pre, final Data data, final InputInfo info, final GNodeList nodes) {
    super(UpdateType.INSERTAFTER, pre, data, info, nodes);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    final int k = data.kind(pre);
    final int s = data.size(pre, k);
    auc.addInsert(pre + s, data.parent(pre, k), insseq);
  }
}
