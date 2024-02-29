package org.basex.query.value.item;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dummy item (only used at compile time).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class Dummy extends Item {
  /** Data reference (can be {@code null}). */
  private final Data data;
  /** Sequence type. */
  private final SeqType seqType;

  /**
   * Constructor.
   * @param seqType sequence type
   * @param data data reference (can be {@code null})
   */
  public Dummy(final SeqType seqType, final Data data) {
    super(seqType.type);
    this.seqType = seqType;
    this.data = data;
  }

  @Override
  public SeqType seqType() {
    return seqType;
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
  public boolean equal(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) {
    throw Util.notExpected();
  }

  @Override
  public boolean atomicEqual(final Item item) {
    throw Util.notExpected();
  }

  @Override
  public int compare(final Item item, final Collation coll, final boolean transitive,
      final InputInfo info) throws QueryException {
    throw Util.notExpected();
  }

  @Override
  public boolean comparable(final Item item) {
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
  public void toXml(final QueryPlan plan) {
    throw Util.notExpected();
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenList list = new TokenList().add(seqType.toString());
    if(data != null) list.add(data.meta.name);
    qs.token(getClass()).params(list.finish());
  }
}
