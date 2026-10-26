package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Insert before primitive.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class InsertBefore extends NodeCopy {
  /**
   * Constructor.
   * @param pre pre
   * @param data data
   * @param info input info (can be {@code null})
   * @param nodes node copy insertion sequence
   */
  public InsertBefore(final int pre, final Data data, final InputInfo info, final GNodeList nodes) {
    super(UpdateType.INSERTBEFORE, pre, data, info, nodes);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addInsert(pre, data.parent(pre, data.kind(pre)), insseq);
  }
}
