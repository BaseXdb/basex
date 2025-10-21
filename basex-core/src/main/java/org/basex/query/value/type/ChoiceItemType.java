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
  public ChoiceItemType(final List<SeqType> types) {
    this.types = types;
    Type tp = null;
    for(final SeqType st : types) {
      tp = tp == null ? st.type : tp.union(st.type);
    }
    union = tp;
  }

  /**
   * Creates a choice item type.
   * @param types alternative item types
   * @return choice item type
   */
  public static ChoiceItemType get(final SeqType... types) {
    return new ChoiceItemType(Arrays.asList(types));
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
    final ArrayList<Type> list = new ArrayList<>();
    for(final SeqType st : types) {
      final Type tp = type.intersect(st.type);
      if(tp != null) list.add(tp);
    }
    return switch(list.size()) {
      case 0 -> null;
      case 1 -> list.get(0);
      default -> new ChoiceItemType(types);
    };
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
    final Test test = seqType.test();
    if(test instanceof final UnionTest ut) {
      for(final Test t : ut.tests) {
        if(!hasInstance(SeqType.get(seqType.type, Occ.EXACTLY_ONE, t))) return false;
      }
      return true;
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
