package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Insert into as first primitive.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public final class InsertIntoAsFirst extends NodeCopy {
  /**
   * Constructor.
   * @param pre target node PRE value
   * @param data target data reference
   * @param info input info (can be {@code null})
   * @param nodes insertion sequence node list
   */
  public InsertIntoAsFirst(final int pre, final Data data, final InputInfo info,
      final GNodeList nodes) {
    super(UpdateType.INSERTINTOFIRST, pre, data, info, nodes);
  }

  @Override
  public void addAtomics(final AtomicUpdateCache auc) {
    auc.addInsert(pre + data.attSize(pre, data.kind(pre)), pre, insseq);
  }
}
