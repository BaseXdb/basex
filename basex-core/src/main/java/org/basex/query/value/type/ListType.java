package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import static org.basex.util.Token.normalize;

import java.util.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XDM list types.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum ListType implements Type {
  /** NMTOKENS type. */
  NMTOKENS("NMTOKENS", BasicType.NMTOKEN),
  /** ENTITIES type. */
  ENTITIES("ENTITIES", BasicType.ENTITY),
  /** IDREFS type. */
  IDREFS("IDREFS", BasicType.IDREF);

  /** Name. */
  private final byte[] name;
  /** Atom Type. */
  private final BasicType type;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param name name
   * @param type type
   */
  ListType(final String name, final BasicType type) {
    this.name = token(name);
    this.type = type;
  }

  /**
   * Finds and returns the specified type.
   * @param qname name of type
   * @return type or {@code null}
   */
  public static ListType get(final QNm qname) {
    if(Token.eq(qname.uri(), XS_URI)) {
      final byte[] ln = qname.local();
      for(final ListType type : values()) {
        if(Token.eq(ln, type.name)) return type;
      }
    }
    return null;
  }

  /**
   * Returns the name of a type.
   * @return name
   */
  public final QNm qname() {
    return new QNm(name, XS_URI);
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
  public final Value cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {

    final byte[][] values = split(normalize(item.string(info)), ' ');
    if(values.length == 0) throw FUNCCAST_X_X_X.get(info, item.type, this, item);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] value : values) vb.add(type.cast(Str.get(value), qc, info));
    return vb.value(type);
  }

  @Override
  public final Value cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    return cast(Str.get(value, qc, info), qc, info);
  }

  @Override
  public Item read(final DataInput in, final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  public SeqType seqType(final Occ occ) {
    // cannot be instantiated statically due to circular dependencies
    if(seqTypes == null) seqTypes = new EnumMap<>(Occ.class);
    return seqTypes.computeIfAbsent(occ, o -> new SeqType(this, o));
  }

  @Override
  public final boolean eq(final Type tp) {
    return this == tp;
  }

  @Override
  public final boolean instanceOf(final Type tp) {
    return this == tp || (tp instanceof final ChoiceItemType cit ? cit.hasInstance(this) :
      tp == BasicType.ITEM);
  }

  @Override
  public final Type union(final Type tp) {
    return this == tp ? tp : tp instanceof ChoiceItemType ? tp.union(this) : BasicType.ITEM;
  }

  @Override
  public final Type intersect(final Type tp) {
    return tp instanceof ChoiceItemType ? tp.intersect(this) : instanceOf(tp) ? this :
      tp.instanceOf(this) ? tp : null;
  }

  @Override
  public final BasicType atomic() {
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
    return new TokenBuilder().add(XS_PREFIX).add(':').add(name).toString();
  }
}
