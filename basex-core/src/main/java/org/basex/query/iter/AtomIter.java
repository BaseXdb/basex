package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Atomization iterator.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class AtomIter extends Iter {
  /** Query context. */
  private final QueryContext qc;
  /** Input info. */
  private final InputInfo info;
  /** Atomizing iterator. */
  private final Iter iter;
  /** Atomizing iterator. */
  private Iter atom;

  /**
   * Constructor.
   * @param iter input iterator
   * @param qc query context
   * @param info input info
   */
  public AtomIter(final Iter iter, final QueryContext qc, final InputInfo info) {
    this.iter = iter;
    this.info = info;
    this.qc = qc;
  }

  @Override
  public Item next() throws QueryException {
    while(true) {
      if(atom == null) {
        final Item it = iter.next();
        if(it == null) return it;
        atom = it.atomValue(info).iter();
      }
      qc.checkStop();
      final Item it = atom.next();
      if(it != null) return it;
      atom = null;
    }
  }
}
