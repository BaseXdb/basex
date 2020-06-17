package org.basex.query.value.item;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Dummy item (only used at compile time).
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class Dummy extends Item {
  /** Data reference (can be {@code null}). */
  private final Data data;

  /**
   * Constructor.
   * @param type type
   * @param data data reference (can be {@code null})
   */
  public Dummy(final Type type, final Data data) {
    super(type);
    this.data = data;
  }

  @Override
  public Data data() {
    return data;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public boolean bool(final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public String toJava() {
    throw Util.notExpected();
  }

  @Override
  public boolean equals(final Object obj) {
    throw Util.notExpected();
  }

  @Override
  public void plan(final QueryPlan plan) {
    throw Util.notExpected();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(Util.className(this)).add('(').add(type);
    if(data != null) tb.add(", db: ").add(data.meta.name);
    return tb.add(')').toString();
  }
}
