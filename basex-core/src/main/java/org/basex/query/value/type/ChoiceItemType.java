package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery union type.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public final class ChoiceItemType implements Type {
  /** Alternative item types. */
  public final List<SeqType> types;
  /** Common ancestor type. */
  private final Type union;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param types alternative item types
   */
  public ChoiceItemType(final List<SeqType> types) {
    this.types = types;
    Type tp = null;
    for(final SeqType st : types) {
      tp = tp == null ? st.type : tp.union(st.type);
    }
    union = tp;
  }

  @Override
  public Value cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    for(final SeqType st : types) {
      final Value val = st.cast(item, false, qc, info);
      if(val != null) return val;
    }
    throw FUNCCAST_X_X_X.get(info, item.type, this, item);
  }

  @Override
  public Value cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    for(final SeqType st : types) {
      try {
        return st.type.cast(value, qc, info);
      } catch(final QueryException ex) {
        Util.debug(ex);
      }
    }
    throw FUNCCAST_X_X.get(info, this, value);
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
  public boolean eq(final Type type) {
    return this == type ||
        type instanceof ChoiceItemType && types.equals(((ChoiceItemType) type).types);
  }

  @Override
  public boolean instanceOf(final Type type) {
    for(final SeqType st : types) {
      if(!st.type.instanceOf(type)) return false;
    }
    return true;
  }

  @Override
  public Type union(final Type type) {
    return union.union(type);
  }

  @Override
  public Type intersect(final Type type) {
    final ArrayList<Type> list = new ArrayList<>();
    for(final SeqType st : types) {
      final Type tp = type.intersect(st.type);
      if(tp != null) list.add(tp);
    }
    if(list.isEmpty()) return null;
    Type is = AtomType.ITEM;
    for(final Type tp : list) {
      is = is.intersect(tp);
      if(is == null) return null;
    }
    return is;
  }

  @Override
  public boolean isNumber() {
    return union.isNumber();
  }

  @Override
  public boolean isUntyped() {
    return union.isNumberOrUntyped();
  }

  @Override
  public boolean isNumberOrUntyped() {
    return union.isNumberOrUntyped();
  }

  @Override
  public boolean isStringOrUntyped() {
    return union.isStringOrUntyped();
  }

  @Override
  public boolean isSortable() {
    return union.isSortable();
  }

  @Override
  public AtomType atomic() {
    return union.atomic();
  }

  @Override
  public ID id() {
    return ID.CIT;
  }

  @Override
  public boolean nsSensitive() {
    for(final SeqType st : types) {
      if(st.type.nsSensitive()) return true;
    }
    return false;
  }

  /**
   * Checks if the given type is an instance of the this type.
   * @param type type to be checked
   * @return result of check
   */
  boolean hasInstance(final Type type) {
    for(final SeqType st : types) {
      if(type.instanceOf(st.type)) return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return types.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof ChoiceItemType &&
        types.equals(((ChoiceItemType) obj).types);
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token('(');
    int n = types.size();
    for(final SeqType st : types) {
      qs.token(st.toString());
      if(--n != 0) qs.token('|');
    }
    return qs.token(')').toString();
  }
}
