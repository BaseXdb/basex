package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;

import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Replace node primitive.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class ReplaceNode extends NodeCopy {
  /**
   * Constructor.
   * @param p target node pre value
   * @param d target data instance
   * @param i input info
   * @param c node copy insertion sequence
   */
  public ReplaceNode(final int p, final Data d, final InputInfo i, final ANodeList c) {
    super(PrimitiveType.REPLACENODE, p, d, i, c);
  }

  @Override
  public void update(final NamePool pool) {
    if(insseq == null) return;
    add(pool);
    pool.remove(getTargetNode());
  }

  @Override
  public void merge(final UpdatePrimitive p) throws QueryException {
    throw UPMULTREPL.get(info, getTargetNode());
  }

  @Override
  public void addAtomics(final AtomicUpdateCache l) {
    l.addReplace(targetPre, insseq);
  }

  @Override
  public UpdatePrimitive[] substitute(final MemData tmp) {
    return new UpdatePrimitive[] { this };
  }
}
