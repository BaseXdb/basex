package org.basex.query.value.type;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery list types.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public enum ListType implements Type {
  /** NMTOKENS type. */
  NMT("NMTOKENS", AtomType.NMT),
  /** ENTITIES type. */
  ENT("ENTITIES", AtomType.ENT),
  /** IDREFS type. */
  IDR("IDREFS", AtomType.IDR);

  /** Cached enums (faster). */
  private static final ListType[] VALUES = values();

  /** Atom Type. */
  private final AtomType type;
  /** Name. */
  private final QNm name;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param name name
   * @param type atomic type
   */
  ListType(final String name, final AtomType type) {
    this.name = new QNm(name, XS_URI);
    this.type = type;
  }

  @Override
  public final boolean isNumber() {
    return false;
  }

  @Override
  public final boolean isUntyped() {
    return false;
  }

  @Override
  public final boolean isNumberOrUntyped() {
    return false;
  }

  @Override
  public final boolean isStringOrUntyped() {
    return false;
  }

  @Override
  public final boolean isSortable() {
    return false;
  }

  @Override
  public final byte[] string() {
    return name.string();
  }

  @Override
  public final Value cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {

    final byte[][] values = split(normalize(item.string(info)), ' ');
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] v : values) vb.add(type.cast(Str.get(v), qc, sc, info));
    return vb.value();
  }

  @Override
  public final Value cast(final Object value, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {
    return cast(Str.get(value, qc, info), qc, sc, info);
  }

  @Override
  public final Value castString(final String value, final QueryContext qc, final StaticContext sc,
      final InputInfo info) throws QueryException {
    return cast(value, qc, sc, info);
  }

  @Override
  public SeqType seqType(final Occ occ) {
    // cannot be statically instantiated due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public final boolean eq(final Type t) {
    return this == t;
  }

  @Override
  public final boolean instanceOf(final Type t) {
    return this == t;
  }

  @Override
  public final Type union(final Type t) {
    return this == t ? t : AtomType.ITEM;
  }

  @Override
  public final Type intersect(final Type t) {
    return this == t ? this : t.instanceOf(this) ? t : null;
  }

  @Override
  public final ID id() {
    return null;
  }

  @Override
  public final boolean nsSensitive() {
    return false;
  }

  @Override
  public final String toString() {
    return new TokenBuilder(NSGlobal.prefix(name.uri())).add(':').add(name.string()).toString();
  }

  /**
   * Finds and returns the specified type.
   * @param type type
   * @return type or {@code null}
   */
  public static ListType find(final QNm type) {
    for(final ListType t : VALUES) if(t.name.eq(type)) return t;
    return null;
  }
}
