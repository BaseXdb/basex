package org.basex.query.iter;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Value iterator interface, throwing no exceptions.
 *
 * This class also implements the {@link Iterable} interface, which is why all of its
 * values can also be retrieved via enhanced for(for-each) loops. Note, however, that
 * using the {@link #next()} method will give you better performance.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class ValueIter extends Iter implements Iterable<Item> {
  @Override
  public abstract Item next();

  @Override
  public abstract Item get(final long i);

  @Override
  public abstract long size();

  @Override
  public abstract Value value();

  @Override
  public final Iterator<Item> iterator() {
    return new Iterator<Item>() {
      /** Current node. */
      private Item n;

      @Override
      public boolean hasNext() {
        n = ValueIter.this.next();
        return n != null;
      }

      @Override
      public Item next() {
        return n;
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }
}
