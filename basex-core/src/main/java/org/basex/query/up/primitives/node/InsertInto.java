package org.basex.query.up.primitives.node;

import org.basex.data.*;
import org.basex.query.up.atomic.*;
import org.basex.query.up.primitives.*;
import org.basex.query.util.list.*;
import org.basex.util.*;

/**
 * Insert into primitive.
 *
 * @author BaseX Team, BSD License
 * @author Lukas Kircher
 */
public class InsertInto extends NodeCopy {
  /**
   * Constructor for an insertInto.
   * @param pre target PRE value
   * @param data target data instance
   * @param info input info (can be {@code null})
   * @param nodes node copy insertion sequence
   */
  public InsertInto(final int pre, final Data data, final InputInfo info, final GNodeList nodes) {
    super(UpdateType.INSERTINTO, pre, data, info, nodes);
  }

  @Override
  public final void addAtomics(final AtomicUpdateCache auc) {
    final int s = data.size(pre, data.kind(pre));
    auc.addInsert(pre + s, pre, insseq);
  }
}
