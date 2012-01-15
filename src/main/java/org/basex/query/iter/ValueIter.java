package org.basex.query.iter;

import org.basex.query.item.Item;
import org.basex.query.item.Value;

/**
 * Value iterator interface, throwing no exceptions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class ValueIter extends Iter {
  @Override
  public abstract Item next();
  @Override
  public abstract Item get(final long i);
  @Override
  public abstract long size();
  @Override
  public abstract boolean reset();
  @Override
  public Value value() {
    final ItemCache ic = new ItemCache((int) size());
    for(Item i; (i = next()) != null;) ic.add(i);
    return ic.value();
  }
}
