package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * XQuery union type.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class ChoiceItemType implements Type {
  /** Alternative item types. */
  public final List<Type> types;
  /** Common ancestor type. */
  private final Type union;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param types alternative item types
   */
  private ChoiceItemType(final List<Type> types) {
    this.types = types;
    Type ut = null;
    for(final Type tp : this.types) {
      ut = ut == null ? tp : ut.union(tp);
    }
    union = ut;
  }

  /**
   * Creates a choice item type.
   * @param types alternative item types
   * @return choice item type
   */
  public static Type get(final Type... types) {
    final Builder builder = new Builder();
    for(final Type tp : types) builder.add(tp);
    return builder.build();
  }

  @Override
  public Value cast(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    for(final Type tp : types) {
      final Value val = tp.seqType().cast(item, false, qc, info);
      if(val != null) return val;
    }
    throw FUNCCAST_X_X_X.get(info, item.type, this, item);
  }

  @Override
  public Value cast(final Object value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    for(final Type tp : types) {
      try {
        return tp.cast(value, qc, info);
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
    return this == type || type instanceof final ChoiceItemType cit && types.equals(cit.types);
  }

  @Override
  public boolean instanceOf(final Type type) {
    final Type norm = expand(type);
    if(norm instanceof final ChoiceItemType ct) {
      for(final Type tp : types) {
        if(!ct.hasInstance(tp)) return false;
      }
    } else {
      for(final Type tp : types) {
        if(!tp.instanceOf(norm)) return false;
      }
    }
    return true;
  }

  /**
   * Checks if this choice type is a supertype of the given type.
   * @param type type to be checked
   * @return result of check
   */
  boolean hasInstance(final Type type) {
    final Type norm = expand(type);
    if(norm instanceof final ChoiceItemType ct) return ct.instanceOf(this);
    for(final Type tp : types) {
      if(norm.instanceOf(tp)) return true;
    }
    return false;
  }

  /**
   * Expand the given type for comparison with choice item types, if necessary, as specified in the
   * subtyping rules for choice item types.
   * @param type type to expand
   * @return expanded type
   */
  private static Type expand(final Type type) {
    if(type == BasicType.NUMERIC) return Types.NUMERIC_EXPANSION;
    if(type == BasicType.ANY_ATOMIC_TYPE) return Types.ANY_ATOMIC_TYPE_EXPANSION;
    if(type == BasicType.ITEM) return Types.ITEM_EXPANSION;
    if(type == NodeType.NODE) return Types.NODE_EXPANSION;
    if(type == NodeType.GNODE) return Types.GNODE_EXPANSION;
    return type;
  }

  @Override
  public Type union(final Type type) {
    return union.union(type);
  }

  @Override
  public Type intersect(final Type type) {
    final Builder builder = new Builder();
    for(final Type tp : types) {
      final Type is = type.intersect(tp);
      if(is != null) builder.add(is);
    }
    return builder.types.isEmpty() ? null : builder.build();
  }

  @Override
  public boolean isNumber() {
    return union.isNumber();
  }

  @Override
  public boolean isUntyped() {
    return union.isUntyped();
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
  public BasicType atomic() {
    return union.atomic();
  }

  @Override
  public ID id() {
    return ID.CIT;
  }

  @Override
  public boolean nsSensitive() {
    for(final Type tp : types) {
      if(tp.nsSensitive()) return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return types.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final ChoiceItemType cit && types.equals(cit.types);
  }

  @Override
  public String name() {
    return "";
  }

  @Override
  public String toString() {
    return toString("|", (Object[]) types.toArray(Type[]::new));
  }

  /**
   * Builder for choice item types, removing error types if necessary. May create a single item type
   * if only one type remains. Also merges consecutive enums, as well as any node types of the same
   * kind by replacing with the union, and flattens nested ChoiceItemTypes.
   */
  public static final class Builder {
    /** Alternative item types. */
    private final ArrayList<Type> types = new ArrayList<>();

    /**
     * Builds and returns the resulting choice item type.
     * @return choice item type, or the single item type if the list contains only one type
     */
    public Type build() {
      if(types.size() != 1) types.remove(BasicType.ERROR);
      return switch(types.size()) {
        case 0 -> throw Util.notExpected();
        case 1 -> types.get(0);
        default -> new ChoiceItemType(types);
      };
    }

    /**
     * Adds the specified type to this builder, merging enum types only if consecutive, and merging
     * document, element, attribute, and pi types anywhere in the list, ignoring duplicates, and
     * flattening nested ChoiceItemTypes.
     * @param type type to be added
     * @return this builder
     */
    public Builder add(final Type type) {
      // flatten nested ChoiceItemTypes
      if(type instanceof final ChoiceItemType ct) {
        for(final Type tp : ct.types) add(tp);
        return this;
      }
      // ignore duplicates
      if(types.contains(type)) return this;
      // non-mergeable types or first entry
      final Type.ID id = type.id();
      if(types.isEmpty()
          || !id.oneOf(Type.ID.ENM, Type.ID.DOC, Type.ID.ELM, Type.ID.ATT, Type.ID.PI)) {
        types.add(type);
        return this;
      }
      // enums: merge consecutive entries only, to preserve position of alternatives
      if(id == Type.ID.ENM) {
        final int last = types.size() - 1;
        final Type tp = types.get(last);
        if(tp.id() == Type.ID.ENM) {
          // remove and re-add, for duplicate removal
          types.remove(last);
          return add(tp.union(type));
        }
        // not consecutive: add as-is
        types.add(type);
        return this;
      }
      // nodes (DOC/ELM/ATT/PI): merge with existing alternative of same kind anywhere, in-place
      for(int i = 0; i < types.size(); i++) {
        final Type existing = types.get(i);
        if(existing.id() == id) {
          types.set(i, existing.union(type));
          return this;
        }
      }
      // no existing node kind found
      types.add(type);
      return this;
    }
  }
}
