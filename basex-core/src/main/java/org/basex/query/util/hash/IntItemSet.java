package org.basex.query.util.hash;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This set stores 32-bit integers in an int set until another item is added.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class IntItemSet implements ItemSet {
  /** Collation (can be {@code null}). */
  private final Collation coll;
  /** Input info (can be {@code null}). */
  private final InputInfo info;
  /** Integers ({@code null} after the switch to the item set). */
  private IntSet ints = new IntSet();
  /** Items ({@code null} before the switch). */
  private ItemSet items;

  /**
   * Constructor.
   * @param coll collation (can be {@code null})
   * @param info input info (can be {@code null})
   */
  IntItemSet(final Collation coll, final InputInfo info) {
    this.coll = coll;
    this.info = info;
  }

  @Override
  public boolean add(final Item key) throws QueryException {
    if(items == null && key.type == BasicType.INTEGER) {
      final int v = ((Itr) key).toInt();
      if(v != Integer.MIN_VALUE) return ints.add(v);
    }
    return items().add(key);
  }

  @Override
  public boolean contains(final Item key) throws QueryException {
    if(items != null) return items.contains(key);
    // only integral numbers can be equal to an integer
    final int v = key instanceof final ANum num ? num.toInt() : Integer.MIN_VALUE;
    return v != Integer.MIN_VALUE && ints.contains(v);
  }

  @Override
  public Iterator<Item> iterator() {
    return items != null ? items.iterator() : IntSeq.get(ints.keys()).iterator();
  }

  /**
   * Returns the item set, which is filled with the integers collected so far on the first call.
   * @return item set
   * @throws QueryException query exception
   */
  private ItemSet items() throws QueryException {
    if(items == null) {
      items = coll == null ? new HashItemSet(Mode.DEEP, info) : new CollationItemSet(coll, info);
      for(final int i : ints.keys()) items.add(Itr.get(i));
      ints = null;
    }
    return items;
  }
}
