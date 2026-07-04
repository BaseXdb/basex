package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.BasicType.*;
import static org.basex.query.value.type.Occ.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Stores a sequence type definition.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Item type. */
  public final Type type;
  /** Occurrence indicator. */
  public final Occ occ;
  /** Array type (lazy instantiation). */
  private ArrayType arrayType;
  /** Map types (lazy instantiation). */
  private Map<Type, MapType> mapTypes;

  /**
   * Constructor.
   * @param type type
   * @param occ occurrence
   */
  SeqType(final Type type, final Occ occ) {
    this.type = type;
    this.occ = occ;
  }

  /**
   * Returns a sequence type.
   * @param type type
   * @param occ occurrence indicator
   * @return sequence type
   */
  public static SeqType get(final Type type, final Occ occ) {
    return occ == ZERO ? Types.EMPTY_SEQUENCE_Z : type.seqType(occ);
  }

  /**
   * Returns an array type for this sequence type.
   * @return array type
   */
  public ArrayType arrayType() {
    if(arrayType == null) arrayType = new ArrayType(this);
    return arrayType;
  }

  /**
   * Returns a map type for this sequence type and the specified key type.
   * @param keyType key type
   * @return map type
   */
  public MapType mapType(final Type keyType) {
    if(mapTypes == null) mapTypes = new ConcurrentHashMap<>();
    return mapTypes.computeIfAbsent(keyType, k -> new MapType(k, this));
  }

  /**
   * Returns a sequence type with the specified occurrence indicator.
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType with(final Occ oc) {
    return oc == occ ? this : get(type, oc);
  }

  /**
   * Returns a sequence type with a new occurrence indicator.
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType union(final Occ oc) {
    return oc == occ ? this : get(type, occ.union(oc));
  }

  /**
   * Checks if the specified value is an instance of this type.
   * @param value value to check
   * @return result of check
   */
  public boolean instance(final Value value) {
    return instance(value, false);
  }

  /**
   * Checks if the specified value is an instance of this type.
   * @param value value to check
   * @param coerce item coercion
   * @return result of check
   */
  private boolean instance(final Value value, final boolean coerce) {
    final Type dt = TypeRef.deref(this.type);
    if(eq(value.seqType())) return true;

    // check cardinality
    final long size = value.size();
    if(!occ.check(size)) return false;
    if(size == 0) return true;

    // try shortcut (type of value may be specific enough)
    if(!(coerce && dt instanceof FType || dt instanceof ChoiceItemType)) {
      if(value.type.instanceOf(dt)) return true;
    }
    // check single item
    if(size == 1) return instance((Item) value, coerce);
    // check each item
    for(final Item item : value) {
      if(!instance(item, coerce)) return false;
    }
    return true;
  }

  /**
   * Checks if the specified item is an instance of this sequence type.
   * @param item item to check
   * @param coerce item coercion
   * @return result of check
   */
  public boolean instance(final Item item, final boolean coerce) {
    final Type dt = TypeRef.deref(this.type);
    if(dt instanceof final ChoiceItemType cit) {
      for(final Type tp : cit.types) {
        if(tp.seqType().instance(item, coerce)) return true;
      }
      return false;
    }
    if(dt instanceof final EnumType et) {
      return et.instance(item);
    }
    return item.instanceOf(dt, coerce);
  }

  /**
   * Casts a sequence to this type.
   * @param value value to cast
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  public Value cast(final Value value, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {

    // check cardinality
    final long size = value.size();
    if(!occ.check(size)) {
      if(error) throw typeError(value, this, info);
      return null;
    }
    if(size == 0) return Empty.VALUE;

    // cast single items
    if(size == 1) return cast((Item) value, error, qc, info);
    // cast sequences
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      qc.checkStop();
      vb.add(cast(item, error, qc, info));
    }
    return vb.value(type);
  }

  /**
   * Casts an item to this type.
   * @param item item to cast
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Item item, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {

    final Type dt = TypeRef.deref(this.type);
    if(item.type.eq(dt)) return item;

    // enable light-weight error handling
    if(!error && info != null) info.internal(true);
    try {
      return dt.cast(item, qc, info);
    } catch(final QueryException ex) {
      if(error) throw ex;
      return null;
    } finally {
      if(!error && info != null) info.internal(false);
    }
  }

  /**
   * Converts the specified value to this type.
   * @param value value to promote
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return converted value
   * @throws QueryException if the conversion was not possible
   */
  public Value coerce(final Value value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    return coerce(value, qc, info, null, null);
  }

  /**
   * Converts the specified value to this type.
   * @param value value to promote
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param name variable name (used for error message, can be {@code null})
   * @param cc compilation context ({@code null} during runtime)
   * @return converted value
   * @throws QueryException if the conversion was not possible
   */
  public Value coerce(final Value value, final QueryContext qc, final InputInfo info,
      final QNm name, final CompileContext cc) throws QueryException {

    final Type dt = TypeRef.deref(this.type);
    // instance check
    final SeqType[] at = dt instanceof final FuncType ft ? ft.argTypes : null;
    if((at == null || ((Checks<SeqType>) st -> st.eq(Types.ITEM_ZM)).all(at)) &&
        instance(value, true)) return value;

    // coerce items if required
    final ValueBuilder vb = new ValueBuilder(qc, value.size());
    for(final Item item : value) {
      qc.checkStop();
      final Value val = coerce(item, name, qc, cc, info);
      if(val == null) throw typeError(value, this, name, info);
      vb.add(val);
    }
    final Value val = vb.value(dt);
    if(!occ.check(val.size())) throw typeError(value, this, name, info);
    return val;
  }

  /**
   * Converts the specified item to this type.
   * @param item item to promote
   * @param name variable name (used for error message, can be {@code null})
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @param info input info (can be {@code null})
   * @return converted value, or {@code null} if conversion failed
   * @throws QueryException query exception
   */
  private Value coerce(final Item item, final QNm name, final QueryContext qc,
      final CompileContext cc, final InputInfo info) throws QueryException {

    final Type dt = TypeRef.deref(this.type);
    if(dt instanceof final ChoiceItemType cit) {
      for(final Type tp : cit.types) {
        try {
          final Value value = tp.seqType().coerce(item, name, qc, cc, info);
          if(value != null) return value;
        } catch(final QueryException ex) {
          Util.debug(ex);
        }
      }
      return null;
    }
    if(dt instanceof BasicType || dt instanceof EnumType) {
      final Value value = item.atomValue(qc, info);
      if(value.size() == 1) return coerceAtomic((Item) value, qc, info);

      final ValueBuilder vb = new ValueBuilder(qc, value.size());
      for(final Item it : value) {
        final Item cast = coerceAtomic(it, qc, info);
        if(cast == null) return null;
        vb.add(cast);
      }
      return vb.value();
    }
    if(item instanceof final FItem fitem) {
      if(fitem instanceof final XQArray array) {
        if(dt instanceof final ArrayType at) return array.coerceTo(at, qc, info, cc);
      } else if(fitem instanceof final XQMap map) {
        if(dt instanceof final RecordType rt) return map.coerceTo(rt, qc, info, cc);
        if(dt instanceof final MapType mt) return map.coerceTo(mt, qc, info, cc);
      }
      if(dt instanceof final FuncType ft) {
        return fitem.coerceTo(dt == Types.FUNCTION ? fitem.funcType() : ft, qc, cc, info);
      }
    } else if(item instanceof final JNode jnode) {
      return coerce(jnode.value.unwrappedItem(qc, info), name, qc, cc, info);
    }
    return instance(item, false) ? item : null;
  }

  /**
   * Converts the specified atomized item to this type.
   * @param item item to promote
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return converted value, or {@code null} if conversion failed
   * @throws QueryException query exception
   */
  private Item coerceAtomic(final Item item, final QueryContext qc, final InputInfo info)
      throws QueryException {
    final Type at = item.type;
    if(at.instanceOf(type)) return item;
    // xs:error has an empty value space: function conversion never casts to it, so coercion
    // always fails and the caller reports a type error (XPTY0004)
    if(type == ERROR) return null;

    Item relabel = null;
    if(at == UNTYPED_ATOMIC) {
      if(type.nsSensitive()) throw NSSENS_X_X.get(info, at, type);
      // item will be cast
    } else if(
      type == DECIMAL && (at == DOUBLE || at == FLOAT) ||
      type == DOUBLE && (at == FLOAT || at.instanceOf(DECIMAL)) ||
      type == FLOAT && (at == DOUBLE || at.instanceOf(DECIMAL)) ||
      type == STRING && at == ANY_URI ||
      type == ANY_URI && at.instanceOf(STRING) ||
      type == HEX_BINARY && at == BASE64_BINARY ||
      type == BASE64_BINARY && at == HEX_BINARY ||
      type instanceof EnumType && at == ANY_URI
    ) {
      // item will be cast
    } else if(!type.union(at).oneOf(ANY_ATOMIC_TYPE, NUMERIC)) {
      // item will be relabeled: remember old type for future comparison
      relabel = item;
    } else {
      return null;
    }
    final Item cast = (Item) cast(item, true, qc, info);
    return relabel == null || cast.compare(relabel, null, false, info) == 0 ? cast : null;
  }

  /**
   * Checks if this type could be converted to the given one by function conversion.
   * @param st type to convert to
   * @return result of check
   */
  public boolean promotable(final SeqType st) {
    if(intersect(st) != null) return true;
    if(occ.intersect(st.occ) == null) return false;
    final Type tp = st.type;
    if(tp instanceof BasicType || tp instanceof ChoiceItemType) {
      if(type.isUntyped()) return !tp.nsSensitive();
      return tp == DOUBLE && (type.intersect(FLOAT) != null || type.intersect(DECIMAL) != null) ||
             tp == FLOAT && type.intersect(DECIMAL) != null ||
             tp == STRING && type.intersect(ANY_URI) != null;
    }
    return st.type instanceof FType && type instanceof FType;
  }

  /**
   * Computes the union of two sequence types, i.e. the lowest common ancestor of both types.
   * @param st second type
   * @return resulting type
   */
  public SeqType union(final SeqType st) {
    if(this == st) return this;
    // ignore general type of empty sequence
    final Type tp = st.zero() ? type : zero() ? st.type :
      TypeRef.deref(type).union(TypeRef.deref(st.type));
    final Occ oc = occ.union(st.occ);
    return get(tp, oc);
  }

  /**
   * Computes the union of the sequence type of all expressions.
   * @param exprs expressions
   * @param zero include expressions that return empty sequence
   * @return sequence type, or {@code null} if unknown
   */
  public static SeqType union(final Expr[] exprs, final boolean zero) {
    SeqType st = null;
    for(final Expr expr : exprs) {
      final SeqType st2 = expr.seqType();
      if(zero || !st2.zero()) st = st == null ? st2 : st.union(st2);
    }
    return st;
  }

  /**
   * Computes the intersection of two sequence types, i.e. the most general type that is
   * subtype of both types. If no such type exists, {@code null} is returned.
   * @param st second type
   * @return resulting type or {@code null}
   */
  public SeqType intersect(final SeqType st) {
    if(this == st) return this;
    // a void type (xs:error, xs:error+) is the bottom of the sequence-type lattice, so it is the
    // intersection (greatest lower bound) with any type, irrespective of the occurrence indicator.
    // if both are void, the occurrences are intersected, so the result is order-independent
    if(voidType()) return st.voidType() ? get(type, occ.intersect(st.occ)) : this;
    if(st.voidType()) return st;
    final Type tp = TypeRef.deref(type).intersect(TypeRef.deref(st.type));
    if(tp == null) return null;
    final Occ oc = occ.intersect(st.occ);
    if(oc == null) return null;
    return get(tp, oc);
  }

  /**
   * Tests if expressions of this type yield at most one item.
   * @return result of check
   */
  public boolean zeroOrOne() {
    return occ.max <= 1;
  }

  /**
   * Tests if expressions of this type yield zero items.
   * @return result of check
   */
  public boolean zero() {
    return occ == ZERO;
  }

  /**
   * Tests if expressions of this type yield one item.
   * @return result of check
   */
  public boolean one() {
    return occ == EXACTLY_ONE;
  }

  /**
   * Tests if expressions of this type yield one or more items.
   * @return result of check
   */
  public boolean oneOrMore() {
    return occ.min >= 1;
  }

  /**
   * Tests if this is a sequence type of the <i>void</i> category ({@code xs:error} or
   * {@code xs:error+}): it has no instances, so an expression of this type never returns a value
   * (see the subtype rules in the specification, "Subtypes of Sequence Types").
   * @return result of check
   */
  public boolean voidType() {
    return type == BasicType.ERROR && oneOrMore();
  }

  /**
   * Tests if this is a sequence type of the <i>empty</i> category ({@code empty-sequence()},
   * {@code xs:error?} or {@code xs:error*}): the empty sequence is its only instance.
   * @return result of check
   */
  public boolean emptyType() {
    return zero() || type == BasicType.ERROR && !oneOrMore();
  }

  /**
   * Tests if expressions of this type may yield numbers.
   * @return result of check
   */
  public boolean mayBeNumber() {
    if(zero()) return false;
    return type.isNumber() || type == BasicType.ITEM || type == BasicType.ANY_ATOMIC_TYPE;
  }

  /**
   * Tests if contents may be wrapped in a data structure. This includes JNodes,
   * maps, arrays and function items.
   * @return result of check
   */
  public boolean mayBeWrapped() {
    if(zero()) return false;
    final Kind kind = type.kind();
    return type == BasicType.ITEM || kind == Kind.GNODE || kind == Kind.JNODE ||
        type instanceof FType;
  }

  /**
   * Checks if this sequence type is an instance of the specified sequence type.
   * @param st sequence type to check
   * @return result of check
   */
  public boolean instanceOf(final SeqType st) {
    if(this == st) return true;
    // void category (xs:error, xs:error+): no instances, a subtype of every sequence type
    if(voidType()) return true;
    // empty category (empty-sequence(), xs:error?, xs:error*): the empty sequence is the only
    // instance, a subtype of every sequence type that permits the empty sequence
    if(emptyType()) return !st.oneOrMore();
    if(!occ.instanceOf(st.occ)) return false;
    final Type t1 = TypeRef.deref(type), t2 = TypeRef.deref(st.type);
    return t2 instanceof final ChoiceItemType cit ? cit.hasInstance(t1) : t1.instanceOf(t2);
  }

  /**
   * Checks the types for equality.
   * @param st type
   * @return result of check
   */
  public boolean eq(final SeqType st) {
    return this == st || TypeRef.deref(type).eq(TypeRef.deref(st.type)) && occ == st.occ;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final SeqType st && eq(st);
  }

  /**
   * This implementation of this method is used on the alternatives of a
   * {@link ChoiceItemType}, while {@link #mapTypes} is being maintained as a {@link HashMap}.
   * Since {@link MapType#keyType} is guaranteed to be an atomic type, we expect it to be called
   * only on {@link #SeqType} instances based on some {@link BasicType}, where suitable hash codes
   * are available for {@link #type}, and {@link #occ}.
   */
  @Override
  public int hashCode() {
    return (type == null ? 0 : type.hashCode()) + (occ == null ? 0 : occ.hashCode());
  }

  /**
   * Returns a string representation of the type.
   * @return string
   */
  public String typeString() {
    return zero() ? QueryText.EMPTY_SEQUENCE + "()" : type.toString();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(!one() && type instanceof FType && !(type instanceof MapType || type instanceof ArrayType)) {
      tb.add('(').add(typeString()).add(')');
    } else {
      tb.add(typeString());
    }
    if(!(type instanceof ListType)) tb.add(occ);
    return tb.toString();
  }
}
