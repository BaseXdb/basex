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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public enum ListType implements Type {
  /** NMTOKENS type. */
  NMT("NMTOKENS") {
    @Override
    public Value cast(final Item it, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return create(it, ctx, sc, ii, AtomType.NMT);
    }
  },

  /** ENTITIES type. */
  ENT("ENTITIES") {
    @Override
    public Value cast(final Item it, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return create(it, ctx, sc, ii, AtomType.ENT);
    }
  },

  /** IDREFS type. */
  IDR("IDREFS") {
    @Override
    public Value cast(final Item it, final QueryContext ctx, final StaticContext sc,
        final InputInfo ii) throws QueryException {
      return create(it, ctx, sc, ii, AtomType.IDR);
    }
  };

  /** Cached enums (faster). */
  private static final ListType[] VALUES = values();

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
   * @param sc static context
   * @param ii input info
   * @param type result type
   * @return created value
   * @throws QueryException query exception
   */
  private static Value create(final Item it, final QueryContext ctx, final StaticContext sc,
      final InputInfo ii, final AtomType type) throws QueryException {

    final byte[][] values = split(norm(it.string(ii)), ' ');
    final ValueBuilder vb = new ValueBuilder(values.length);
    for(final byte[] v : values) vb.add(type.cast(Str.get(v), ctx, sc, ii));
    return vb.value();
  }

  /**
   * Finds and returns the specified data type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static ListType find(final QNm type) {
    for(final ListType t : VALUES) if(t.name.eq(type)) return t;
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
  public abstract Value cast(final Item it, final QueryContext ctx,
      final StaticContext sc, final InputInfo ii) throws QueryException;

  @Override
  public Value cast(final Object o, final QueryContext ctx, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(Str.get(o, ctx, ii), ctx, sc, ii);
  }

  @Override
  public Value castString(final String o, final QueryContext ctx, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(o, ctx, sc, ii);
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
  public ID id() {
    return null;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  @Override
  public String toString() {
    return new TokenBuilder(NSGlobal.prefix(name.uri())).add(':').add(name.string()).toString();
  }
}
