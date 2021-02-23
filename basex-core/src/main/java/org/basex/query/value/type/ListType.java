package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum ListType implements Type {
  /** NMTOKENS type. */
  NMTOKENS("NMTOKENS", AtomType.NMTOKEN),
  /** ENTITIES type. */
  ENTITIES("ENTITIES", AtomType.ENTITY),
  /** IDREFS type. */
  IDREFS("IDREFS", AtomType.IDREF);

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
  public final Value cast(final Item item, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {

    final byte[][] values = split(normalize(item.string(ii)), ' ');
    if(values.length == 0) throw FUNCCAST_X_X_X.get(ii, item.type, this, item);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] value : values) vb.add(type.cast(Str.get(value), qc, sc, ii));
    return vb.value(type);
  }

  @Override
  public final Value cast(final Object value, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(Str.get(value, qc, ii), qc, sc, ii);
  }

  @Override
  public final Value castString(final String value, final QueryContext qc, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return cast(value, qc, sc, ii);
  }

  @Override
  public SeqType seqType(final Occ occ) {
    // cannot statically be instantiated due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public final boolean eq(final Type tp) {
    return this == tp;
  }

  @Override
  public final boolean instanceOf(final Type tp) {
    return this == tp;
  }

  @Override
  public final Type union(final Type tp) {
    return this == tp ? tp : AtomType.ITEM;
  }

  @Override
  public final Type intersect(final Type tp) {
    return this == tp ? this : tp.instanceOf(this) ? tp : null;
  }

  @Override
  public final AtomType atomic() {
    return type;
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
    return Strings.concat(NSGlobal.prefix(name.uri()), ':', name.string());
  }

  /**
   * Finds and returns the specified type.
   * @param qname name of type
   * @return type or {@code null}
   */
  public static ListType find(final QNm qname) {
    for(final ListType lt : VALUES) {
      if(lt.name.eq(qname)) return lt;
    }
    return null;
  }
}
