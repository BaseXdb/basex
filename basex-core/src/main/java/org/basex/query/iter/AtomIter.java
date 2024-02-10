package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Atomization iterator.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class AtomIter extends Iter {
  /** Query context. */
  private final QueryContext qc;
  /** Input info (can be {@code null}). */
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
   * @param info input info (can be {@code null})
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
    // shortcut if iterator will not yield any arrays
    if(size != -1) {
      final Item item = iter.next();
      return item == null ? null : item.atomItem(qc, info);
    }

    while(true) {
      if(atom == null) {
        final Item item = iter.next();
        if(item == null) return null;
        atom = item.atomValue(qc, info).iter();
      }
      final Item item = atom.next();
      if(item != null) return item;
      atom = null;
    }
  }

  @Override
  public Item get(final long i) throws QueryException {
    return iter.get(i).atomItem(qc, info);
  }

  @Override
  public long size() {
    return size;
  }
}
