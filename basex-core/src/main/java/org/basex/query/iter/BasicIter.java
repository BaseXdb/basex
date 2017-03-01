package org.basex.query.iter;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Basic iterator, throwing no exceptions.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for(for-each) loops. Using {@link #next()} is faster.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 * @param <I> generic item type
 */
public abstract class BasicIter<I extends Item> extends Iter implements Iterable<I> {
  /** Result size. */
  protected final long size;
  /** Current position. */
  protected int pos;

  /**
   * Constructor.
   * @param size size
   */
  public BasicIter(final long size) {
    this.size = size;
  }

  @Override
  public abstract I get(long i);

  @Override
  public I next() {
    final int p = pos;
    if(p < size) {
      final I item = get(p);
      pos++;
      return item;
    }
    return null;
  }

  @Override
  public final long size() {
    return size;
  }

  @Override
  public Value value() {
    final long s = size;
    if(s == 0) return Empty.SEQ;
    if(s == 1) return get(0);

    final ValueBuilder vb = new ValueBuilder();
    for(long i = 0; i < s; i++) vb.add(get(i));
    return vb.value();
  }

  @Override
  public final Iterator<I> iterator() {
    return new Iterator<I>() {
      private I it;

      @Override
      public boolean hasNext() {
        it = BasicIter.this.next();
        return it != null;
      }

      @Override
      public I next() {
        return it;
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }
}
