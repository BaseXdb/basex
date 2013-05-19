package org.basex.query.value.type;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * XQuery list types.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum ListType implements Type {
  /** NMTOKENS type. */
  NMT("NMTOKENS") {
    @Override
    public Value cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return create(it, ctx, ii, AtomType.NMT);
    }
  },

  /** ENTITIES type. */
  ENT("ENTITIES") {
    @Override
    public Value cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return create(it, ctx, ii, AtomType.ENT);
    }
  },

  /** IDREFS type. */
  IDR("IDREFS") {
    @Override
    public Value cast(final Item it, final QueryContext ctx, final InputInfo ii)
        throws QueryException {
      return create(it, ctx, ii, AtomType.IDR);
    }
  };

  /** Name. */
  private final QNm name;
  /** Sequence type (lazy). */
  private SeqType seq;

  /**
   * Constructor.
   * @param nm string representation
   */
  ListType(final String nm) {
    name = new QNm(nm, XSURI);
  }

  /**
   * Creates a sequence with the resulting list items.
   * @param it item
   * @param ctx query context
   * @param ii input info
   * @param type result type
   * @return created value
   * @throws QueryException query exception
   */
  protected static Value create(final Item it, final QueryContext ctx, final InputInfo ii,
      final AtomType type) throws QueryException {

    final byte[][] values = split(norm(it.string(ii)), ' ');
    final ValueBuilder vb = new ValueBuilder(values.length);
    for(final byte[] v : values) {
      vb.add(type.cast(Str.get(v), ctx, ii));
    }
    return vb.value();
  }

  /**
   * Finds and returns the specified data type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static ListType find(final QNm type) {
    for(final ListType t : values()) {
      if(t.name.eq(type)) return t;
    }
    return null;
  }

  @Override
  public boolean isNumber() {
    return false;
  }

  @Override
  public boolean isUntyped() {
    return false;
  }

  @Override
  public boolean isNumberOrUntyped() {
    return false;
  }

  @Override
  public boolean isStringOrUntyped() {
    return false;
  }

  @Override
  public byte[] string() {
    return name.string();
  }

  @Override
  public abstract Value cast(final Item it, final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  @Override
  public Value cast(final Object o, final InputInfo ii) throws QueryException {
    return cast(Str.get(ii, o), null, ii);
  }

  @Override
  public Value castString(final String o, final InputInfo ii) throws QueryException {
    return cast(o, ii);
  }

  @Override
  public SeqType seqType() {
    if(seq == null) seq = SeqType.get(this, Occ.ZERO_MORE);
    return seq;
  }

  @Override
  public boolean eq(final Type t) {
    return this == t;
  }

  @Override
  public final boolean instanceOf(final Type t) {
    return this == t;
  }

  @Override
  public Type union(final Type t) {
    return this == t ? t : AtomType.ITEM;
  }

  @Override
  public Type intersect(final Type t) {
    return this == t ? this : t.instanceOf(this) ? t : null;
  }

  @Override
  public final boolean isNode() {
    return false;
  }

  @Override
  public Type.ID id() {
    return null;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  @Override
  public String toString() {
    return new TokenBuilder(NSGlobal.prefix(name.uri())).add(':').add(name.string()).
        toString();
  }
}
