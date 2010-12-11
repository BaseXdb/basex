package org.basex.query.iter;

import org.basex.query.item.Item;

/**
 * Value iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
}
