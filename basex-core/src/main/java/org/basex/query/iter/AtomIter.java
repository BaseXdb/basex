package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Atomization iterator.
 *
 * @author BaseX Team 2005-17, BSD License
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
  /** Size. Arrays may be encountered if value is {@code -1}. */
  private final long size;

  /**
   * Constructor.
   * @param iter input iterator
   * @param qc query context
   * @param info input info
   * @param size iterator size (can be {@code -1})
   */
  public AtomIter(final Iter iter, final QueryContext qc, final InputInfo info, final long size) {
    this.iter = iter;
    this.info = info;
    this.qc = qc;
    this.size = size;
  }

  @Override
  public Item next() throws QueryException {
    // shortcut if no arrays will be returned
    if(size != -1) {
      final Item it = iter.next();
      return it == null ? null : it.atomItem(info);
    }

    while(true) {
      if(atom == null) {
        final Item it = iter.next();
        if(it == null) return null;
        atom = it.atomValue(info).iter();
      }
      final Item it = atom.next();
      if(it != null) return it;
      atom = null;
      qc.checkStop();
    }
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public Item get(final long i) throws QueryException {
    return iter.get(i);
  }
}
