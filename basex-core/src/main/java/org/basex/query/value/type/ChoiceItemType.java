package org.basex.query.value.type;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.expr.path.*;
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
  public final List<SeqType> types;
  /** Common ancestor type. */
  private final Type union;

  /** Sequence types (lazy instantiation). */
  private EnumMap<Occ, SeqType> seqTypes;

  /**
   * Constructor.
   * @param types alternative item types
   */
  private ChoiceItemType(final List<SeqType> types) {
    this.types = types;
    Type tp = null;
    for(final SeqType st : this.types) {
      tp = tp == null ? st.type : tp.union(st.type);
    }
    union = tp;
  }

  /**
   * Creates a choice item type.
   * @param types alternative item types
   * @return choice item type
   */
  public static Type get(final SeqType... types) {
    final Builder builder = new Builder();
    for(final SeqType st : types) builder.add(st);
    return builder.build();
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
    return this == type || type instanceof final ChoiceItemType cit && types.equals(cit.types);
  }

  /**
   * Checks if this choice item type is an instance of the specified sequence type.
   * @param seqType sequence type to check
   * @return result of check
   */
  public boolean instanceOf(final SeqType seqType) {
    if(!seqType.one()) throw Util.notExpected();
    for(final SeqType st : types) {
      if(!st.instanceOf(seqType)) return false;
    }
    return true;
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
    final Builder builder = new Builder();
    for(final SeqType st : types) {
      final Type tp = type.intersect(st.type);
      if(tp != null) builder.add(tp.seqType());
    }
    return builder.types.isEmpty() ? null : builder.build();
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
  public BasicType atomic() {
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
   * Checks if the given type is an instance of this type.
   * @param type type to be checked
   * @return result of check
   */
  boolean hasInstance(final Type type) {
    for(final SeqType st : types) {
      if(type.instanceOf(st.type)) return true;
    }
    return false;
  }

  /**
   * Checks if the given sequence type is an instance of this type.
   * @param seqType sequence type to be checked
   * @return result of check
   */
  boolean hasInstance(final SeqType seqType) {
    if(!seqType.one()) throw Util.notExpected();
    if(seqType.type instanceof final NodeType nt) {
      if(nt.test instanceof final UnionTest ut) {
        for(final Test t : ut.tests) {
          if(!hasInstance(NodeType.get(t))) return false;
        }
        return true;
      }
    }
    for(final SeqType st : types) {
      if(seqType.instanceOf(st)) return true;
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
    return toString("|", (Object[]) types.toArray(SeqType[]::new));
  }

  /**
   * Builder for choice item types, removing error types if necessary. May create a single item type
   * if only one type remains. Also merges consecutive enums, as well as any node types of the same
   * kind by replacing with the union, and flattens nested ChoiceItemTypes.
   */
  public static final class Builder {
    /** Alternative item types. */
    private final ArrayList<SeqType> types = new ArrayList<>();

    /**
     * Builds and returns the resulting choice item type.
     * @return choice item type, or the single item type if the list contains only one type
     */
    public Type build() {
      if(types.size() != 1) types.remove(Types.ERROR_O);
      return switch(types.size()) {
        case 0 -> throw Util.notExpected();
        case 1 -> types.get(0).type;
        default -> new ChoiceItemType(types);
      };
    }

    /**
     * Adds the specified sequence type to this builder, merging enum types only if consecutive, and
     * merging document, element, attribute, and pi types anywhere in the list, ignoring duplicates,
     * and flattening nested ChoiceItemTypes.
     * @param st sequence type to be added
     * @return this builder
     */
    public Builder add(final SeqType st) {
      // flatten nested ChoiceItemTypes
      if(st.type instanceof ChoiceItemType ct) {
        for(final SeqType s : ct.types) add(s);
        return this;
      }
      // ignore duplicates
      if(types.contains(st)) return this;
      // non-mergeable types or first entry
      final Type.ID id = st.type.id();
      if(types.isEmpty()
          || !id.oneOf(Type.ID.ENM, Type.ID.DOC, Type.ID.ELM, Type.ID.ATT, Type.ID.PI)) {
        types.add(st);
        return this;
      }
      // enums: merge consecutive entries only, to preserve position of alternatives
      if(id == Type.ID.ENM) {
        final int last = types.size() - 1;
        final Type tp = types.get(last).type;
        if(tp.id() == Type.ID.ENM) {
          // remove and re-add, for duplicate removal
          types.remove(last);
          return add(tp.union(st.type).seqType());
        }
     // not consecutive: add as-is
        types.add(st);
        return this;
      }
      // nodes (DOC/ELM/ATT/PI): merge with existing alternative of same kind anywhere, in-place
      for(int i = 0; i < types.size(); i++) {
        final SeqType existing = types.get(i);
        if(existing.type.id() == id) {
          types.set(i, existing.type.union(st.type).seqType());
          return this;
        }
      }
      // no existing node kind found
      types.add(st);
      return this;
    }
  }
}
