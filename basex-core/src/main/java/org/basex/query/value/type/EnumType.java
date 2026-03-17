package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * XQuery enum type.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class EnumType implements Type {
  /** The enumeration values (at least one). */
  private final TokenSet values;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param values enumeration values (at least one)
   */
  public EnumType(final TokenSet values) {
    this.values = values;
  }

  /**
   * Returns an enumeration type.
   * @param <T> type of enumeration values
   * @param values enumeration values (at least one)
   * @return enum type
   */
  @SafeVarargs
  public static <T> EnumType get(final T... values) {
    final TokenSet set = new TokenSet(values.length);
    for(final T value : values) set.add(value.toString());
    return new EnumType(set);
  }

  /**
   * Checks if the given item is an instance of this type.
   * @param item item to be checked
   * @return result of check
   */
  public boolean instance(final Item item) {
    try {
      return item.type.instanceOf(BasicType.STRING) && values.contains(item.string(null));
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public Value cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    final byte[] string = item.string(info);
    if(!values.contains(string)) throw typeError(item, this, info);
    return item.type.eq(this) ? item : Str.get(string, new EnumType(new TokenSet(string)));
  }

  @Override
  public Value cast(final Object value, final QueryContext qc, final InputInfo info)
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
  public boolean eq(final Type type) {
    return equals(type);
  }

  @Override
  public boolean instanceOf(final Type type) {
    if(type == this) return true;
    if(type instanceof final ChoiceItemType cit) return cit.hasInstance(this);
    if(BasicType.STRING.instanceOf(type)) return true;
    if(!(type instanceof final EnumType et)) return false;
    for(final byte[] value : values) {
      if(!et.values.contains(value)) return false;
    }
    return true;
  }

  @Override
  public Type union(final Type type) {
    if(type == this) return this;
    if(type instanceof ChoiceItemType) return type.union(this);
    if(type instanceof final EnumType et) {
      final TokenSet tv = et.values;
      final TokenSet ts = new TokenSet();
      for(final byte[] value : values) ts.add(value);
      for(final byte[] value : tv) ts.add(value);
      final int sz = ts.size();
      return sz == values.size() ? this : sz == tv.size() ? et : new EnumType(ts);
    }
    return type.instanceOf(BasicType.STRING) ? BasicType.STRING :
      type.instanceOf(BasicType.ANY_ATOMIC_TYPE) ? BasicType.ANY_ATOMIC_TYPE : BasicType.ITEM;
  }

  @Override
  public Type intersect(final Type type) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type instanceof final EnumType et) {
      final TokenSet ts = new TokenSet();
      final TokenSet tv = et.values;
      for(final byte[] value : values) {
        if(tv.contains(value)) ts.add(value);
      }
      for(final byte[] value : tv) {
        if(values.contains(value)) ts.add(value);
      }
      final int sz = ts.size();
      if(sz > 0) return sz == values.size() ? this : sz == tv.size() ? et : new EnumType(ts);
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
    return true;
  }

  @Override
  public boolean isSortable() {
    return true;
  }

  @Override
  public BasicType atomic() {
    return BasicType.STRING;
  }

  @Override
  public ID id() {
    return ID.ENM;
  }

  @Override
  public boolean nsSensitive() {
    return false;
  }

  @Override
  public int hashCode() {
    int h = 0;
    for(final byte[] v : values) {
      h = h * 31 + Token.hashCode(v);
    }
    return h;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof final EnumType et)) return false;
    final TokenSet tv = et.values;
    if(values.size() != tv.size()) return false;
    for(final byte[] value : values) {
      if(!tv.contains(value)) return false;
    }
    return true;
  }

  @Override
  public String name() {
    return QueryText.ENUM;
  }

  @Override
  public String toString() {
    final TokenList list = new TokenList();
    for(final byte[] v : values) list.add(QueryString.toQuoted(v));
    return toString(", ", (Object[]) list.finish());
  }
}
