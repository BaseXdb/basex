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
  /** Sequence type (lazy). */
  private SeqType seq;

  /**
   * Constructor.
   * @param type atomic type
   * @param nm string representation
   */
  ListType(final String nm, final AtomType type) {
    name = new QNm(nm, XSURI);
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
  public final byte[] string() {
    return name.string();
  }

  @Override
  public final Value cast(final Item it, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    final byte[][] values = split(norm(it.string(ii)), ' ');
    final ValueBuilder vb = new ValueBuilder(values.length);
    for(final byte[] v : values) vb.add(type.cast(Str.get(v), qc, sc, ii));
    return vb.value();
  }

  @Override
  public final Value cast(final Object o, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(Str.get(o, qc, ii), qc, sc, ii);
  }

  @Override
  public final Value castString(final String o, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(o, qc, sc, ii);
  }

  @Override
  public final SeqType seqType() {
    if(seq == null) seq = SeqType.get(this, Occ.ZERO_MORE);
    return seq;
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
  public final boolean isNode() {
    return false;
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
   * Finds and returns the specified data type.
   * @param type type as string
   * @return type or {@code null}
   */
  public static ListType find(final QNm type) {
    for(final ListType t : VALUES) if(t.name.eq(type)) return t;
    return null;
  }
}
