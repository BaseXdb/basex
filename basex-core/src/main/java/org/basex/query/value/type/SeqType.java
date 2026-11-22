package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.BasicType.*;
import static org.basex.query.value.type.Occ.*;

import java.util.*;

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
 * Sequence type, consisting of an item type and an occurrence indicator.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SeqType {
  /** Number of cached sequence types (all occurrence indicators but {@link Occ#ZERO}). */
  private static final int OCCS = Occ.values().length - 1;
  /** Number of cached checks. */
  private static final int CHECKS = Check.values().length;

  /** Cached checks. */
  private enum Check {
    /** May yield numbers. */ NUMBER,
    /** May yield function items. */ FUNCTION,
    /** May yield JNodes. */ JNODE
  }

  /** Item type. */
  public final Type type;
  /** Occurrence indicator. */
  public final Occ occ;
  /** Array type, created lazily (can be {@code null}). */
  private ArrayType arrayType;
  /** Map types with basic key types, indexed by ordinal (can be {@code null}). */
  private MapType[] basicMapTypes;
  /** Cached checks: lower bits flag evaluated checks, upper bits their results. */
  private int checks;

  /**
   * Constructor.
   * @param type item type
   * @param occ occurrence indicator
   */
  SeqType(final Type type, final Occ occ) {
    this.type = type;
    this.occ = occ;
  }

  /**
   * Returns a sequence type with the specified item type and occurrence indicator.
   * @param type item type
   * @param occ occurrence indicator
   * @return sequence type
   */
  public static SeqType get(final Type type, final Occ occ) {
    return occ == ZERO ? Types.EMPTY_SEQUENCE_Z : type.seqType(occ);
  }

  /**
   * Creates a sequence type cache for the specified type.
   * @param type item type
   * @return cache
   */
  static SeqType[] cache(final Type type) {
    final SeqType[] cache = new SeqType[OCCS];
    cache[EXACTLY_ONE.ordinal() - 1] = new SeqType(type, EXACTLY_ONE);
    return cache;
  }

  /**
   * Returns a cached sequence type.
   * @param cache sequence type cache
   * @param type item type
   * @param occ occurrence indicator (no {@link Occ#ZERO})
   * @return sequence type
   */
  static SeqType get(final SeqType[] cache, final Type type, final Occ occ) {
    assert occ != ZERO;
    final int o = occ.ordinal() - 1;
    SeqType st = cache[o];
    if(st == null) {
      st = new SeqType(type, occ);
      cache[o] = st;
    }
    return st;
  }

  /**
   * Returns an array type with this sequence type as member type.
   * @return array type
   */
  public ArrayType arrayType() {
    if(arrayType == null) arrayType = new ArrayType(this);
    return arrayType;
  }

  /**
   * Returns a map type with the specified key type and this sequence type as value type.
   * @param keyType key type
   * @return map type
   */
  public MapType mapType(final Type keyType) {
    // only basic key types are cached: other key types may be specific to a query
    if(!(keyType instanceof final BasicType bt)) return new MapType(keyType, this);
    final MapType[] mts = basicMapTypes;
    final MapType mt = mts != null ? mts[bt.ordinal()] : null;
    return mt != null ? mt : basicMapType(bt);
  }

  /**
   * Creates and caches the canonical map type for this sequence type and a basic key type.
   * @param keyType key type
   * @return map type
   */
  private synchronized MapType basicMapType(final BasicType keyType) {
    if(basicMapTypes == null) basicMapTypes = new MapType[BasicType.values().length];
    final int i = keyType.ordinal();
    if(basicMapTypes[i] == null) basicMapTypes[i] = new MapType(keyType, this);
    return basicMapTypes[i];
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
   * Returns a sequence type with the union of this and the specified occurrence indicator.
   * @param oc occurrence indicator
   * @return sequence type
   */
  public SeqType union(final Occ oc) {
    return with(occ.union(oc));
  }

  /**
   * Checks if the specified value is an instance of this sequence type.
   * @param value value to check
   * @return result of check
   */
  public boolean instance(final Value value) {
    return instance(value, false);
  }

  /**
   * Checks if the specified value is an instance of this sequence type.
   * @param value value to check
   * @param coerce item coercion
   * @return result of check
   */
   public boolean instance(final Value value, final boolean coerce) {
    // coerce: a map is an instance of fn(xs:string) as item()* only without coercion (wrapped)

    // check cardinality
    final long size = value.size();
    if(!occ.check(size)) return false;
    if(size == 0) return true;

    // try shortcut (type of value may be specific enough)
    final Type dt = type.deref(), vt = value.type.deref();
    if(vt.keptStatic(dt, coerce)) return true;
    // check single item
    if(size == 1) return dt.instance((Item) value, coerce);
    // check each item
    for(final Item item : value) {
      if(!dt.instance(item, coerce)) return false;
    }
    // remember the more specific type to skip subsequent checks
    if(dt.attachable(coerce) && dt.instanceOf(vt)) value.type = dt;
    return true;
  }

  /**
   * Casts a value to this sequence type.
   * @param value value to cast
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return cast value (can be {@code null})
   * @throws QueryException query exception
   */
  public Value cast(final Value value, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {

    final Type dt = type.deref();

    // item(): identity, only check the cardinality of the input
    if(dt == ITEM) return occ.check(value.size()) ? value : castError(value, error, info);

    // arrays, maps, records: structural cast without atomization
    if(dt instanceof ArrayType || dt instanceof MapType) {
      final long size = value.size();
      if(!occ.check(size)) return castError(value, error, info);
      final ValueBuilder vb = new ValueBuilder(qc, size);
      for(final Item item : value) {
        qc.checkStop();
        // a JNode never matches the target type: continue with its jvalue
        final Value val = item.unwrappedValue(qc);
        if(val.size() != 1) return castError(value, error, info);
        final Item it = val.itemAt(0);
        Value cast = null;
        if(dt instanceof final ArrayType at) {
          if(it instanceof final XQArray array) cast = array.castTo(at, error, qc, info);
        } else if(dt instanceof final RecordType rt) {
          if(it instanceof final XQMap map) cast = map.castTo(rt, error, qc, info);
        } else if(dt instanceof final ShapeType sh) {
          if(it instanceof final XQMap map) cast = map.castTo(sh, error, qc, info);
        } else if(dt instanceof final MapType mt) {
          if(it instanceof final XQMap map) cast = map.castTo(mt, error, qc, info);
        }
        if(cast == null) return castError(value, error, info);
        vb.add(cast);
      }
      return vb.value(dt.toAttachable());
    }

    // generalized atomic type, list type, union type, enumeration type: atomize, then cast
    final Value atom;
    try {
      atom = value.atomValue(qc, info);
    } catch(final QueryException ex) {
      if(error) throw ex;
      Util.debug(ex);
      return null;
    }
    final long size = atom.size();
    if(!occ.check(size)) return castError(value, error, info);
    if(size == 0) return Empty.VALUE;
    if(size == 1) return castItem((Item) atom, error, qc, info);

    final ValueBuilder vb = new ValueBuilder(qc, size);
    for(final Item item : atom) {
      qc.checkStop();
      final Value cast = castItem(item, error, qc, info);
      if(cast == null) return null;
      vb.add(cast);
    }
    return vb.value(dt.toAttachable());
  }

  /**
   * Casts a component value of an array, map, or record type to this sequence type.
   * @param value value to convert
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return converted value (can be {@code null})
   * @throws QueryException query exception
   */
  public Value convert(final Value value, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {
    if(instance(value)) return value;

    // items that match the item type are preserved, all others are cast one by one
    final SeqType st = with(EXACTLY_ONE);
    final boolean castable = castTarget(type);
    final ValueBuilder vb = new ValueBuilder(qc, value.size());
    for(final Item item : value) {
      qc.checkStop();
      if(type.instance(item, false)) {
        vb.add(item);
      } else if(castable) {
        final Value cast = st.cast(item, error, qc, info);
        if(cast == null) return null;
        vb.add(cast);
      } else if(error) {
        throw INVCONVERT_X_X.get(info, this, value);
      } else {
        return null;
      }
    }
    final Value result = vb.value(type.toAttachable());
    return occ.check(result.size()) ? result : castError(value, error, info);
  }

  /**
   * Checks if the specified type is eligible as the target of a cast expression.
   * @param type type to check
   * @return result of check
   */
  private static boolean castTarget(final Type type) {
    // xs:short, array(xs:integer), (xs:int | xs:string): true; xs:anyAtomicType, element(): false
    final Type tp = type.deref();
    // item(); array, map, record types (their component types are checked while casting)
    if(tp == ITEM || tp instanceof ArrayType || tp instanceof MapType) return true;
    // choice item type: all alternatives must be eligible
    if(tp instanceof final ChoiceItemType cit) {
      for(final Type alt : cit.types) {
        if(!castTarget(alt)) return false;
      }
      return true;
    }
    // enumeration type, schema list type
    if(tp instanceof EnumType || tp instanceof ListType) return true;
    // generalized atomic type
    return tp instanceof final BasicType bt && bt.atomic() != null &&
        !bt.oneOf(NOTATION, ANY_ATOMIC_TYPE, ANY_SIMPLE_TYPE);
  }

  /**
   * Raises a cast error or returns {@code null}.
   * @param value value that could not be cast
   * @param error raise error (return {@code null} otherwise)
   * @param info input info (can be {@code null})
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Value castError(final Value value, final boolean error, final InputInfo info)
      throws QueryException {
    if(error) throw typeError(value, this, info);
    return null;
  }

  /**
   * Casts an atomized item to the item type of this sequence type.
   * @param item item to cast
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return cast value (can be {@code null})
   * @throws QueryException query exception
   */
  private Value castItem(final Item item, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {

    final Type dt = type.deref();
    if(item.type.eq(dt)) return item;

    // enable light-weight error handling (copy: the original may be used by other threads)
    final InputInfo ii = error || info == null ? info : info.internalCopy();
    try {
      return dt.cast(item, qc, ii);
    } catch(final QueryException ex) {
      if(error) throw ex;
      return null;
    }
  }

  /**
   * Coerces the specified value to this sequence type.
   * @param value value to coerce
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return coerced value
   * @throws QueryException query exception
   */
  public Value coerce(final Value value, final QueryContext qc, final InputInfo info)
      throws QueryException {
    return coerce(value, qc, info, null, null);
  }

  /**
   * Coerces the specified value to this sequence type.
   * @param value value to coerce
   * @param qc query context
   * @param info input info (can be {@code null})
   * @param name variable name for error messages (can be {@code null})
   * @param cc compilation context, unless called at runtime (can be {@code null})
   * @return coerced value
   * @throws QueryException query exception
   */
  public Value coerce(final Value value, final QueryContext qc, final InputInfo info,
      final QNm name, final CompileContext cc) throws QueryException {

    // instance check
    if(instance(value, true)) return value;

    // coerce items if required
    final ValueBuilder vb = new ValueBuilder(qc, value.size());
    for(final Item item : value) {
      qc.checkStop();
      final Value val = coerce(item, name, qc, cc, info);
      if(val == null) throw typeError(value, this, name, info);
      vb.add(val);
    }
    final Value val = vb.value(type.toAttachable());
    if(!occ.check(val.size())) throw typeError(value, this, name, info);
    return val;
  }

  /**
   * Coerces the specified item to this sequence type.
   * @param item item to coerce
   * @param name variable name for error messages (can be {@code null})
   * @param qc query context
   * @param cc compilation context, unless called at runtime (can be {@code null})
   * @param info input info (can be {@code null})
   * @return coerced value (can be {@code null})
   * @throws QueryException query exception
   */
  private Value coerce(final Item item, final QNm name, final QueryContext qc,
      final CompileContext cc, final InputInfo info) throws QueryException {

    final Type dt = type.deref();
    // generalized atomic type: atomize first
    if(!item.type.instanceOf(ANY_ATOMIC_TYPE) && dt.instanceOf(ANY_ATOMIC_TYPE)) {
      final Value value;
      try {
        value = item.atomValue(qc, info);
      } catch(final QueryException ex) {
        throw atomizeError(ex, item, name, info);
      }
      if(value.isEmpty()) return value;
      if(value instanceof final Item it) return coerce(it, name, qc, cc, info);

      final ValueBuilder vb = new ValueBuilder(qc, value.size());
      for(final Item it : value) {
        final Value cast = coerce(it, name, qc, cc, info);
        if(cast == null) return null;
        vb.add(cast);
      }
      return vb.value();
    }
    if(dt instanceof final ChoiceItemType cit) {
      // coerce to the first matching alternative
      for(final Type tp : cit.types) {
        if(tp.instance(item, false)) return tp.seqType().coerce(item, name, qc, cc, info);
      }
      for(final Type tp : cit.types) {
        try {
          final Value value = tp.seqType().coerce(item, name, qc, cc, info);
          if(value != null) return value;
        } catch(final QueryException ex) {
          // errors of initializers are raised, all others lead to the next type
          if(ex.isInitializer()) throw ex;
        }
      }
      return null;
    }
    if(dt instanceof BasicType || dt instanceof EnumType) return coerceAtomic(item, dt, qc, info);
    if(item instanceof final FItem fitem) {
      if(fitem instanceof final XQArray array) {
        if(dt instanceof final ArrayType at) return array.coerceTo(at, qc, info, cc);
      } else if(fitem instanceof final XQMap map) {
        if(dt instanceof final RecordType rt) return map.coerceTo(rt, qc, info, cc);
        if(dt instanceof final ShapeType sh) return map.coerceTo(sh, qc, info, cc);
        if(dt instanceof final MapType mt) return map.coerceTo(mt, qc, info, cc);
      }
      if(dt instanceof final FuncType ft) return fitem.coerceTo(ft, qc, cc, info);
    } else if(item instanceof final JNode jnode) {
      return coerce(jnode.value.unwrappedItem(qc, info), name, qc, cc, info);
    }
    return type.instance(item, false) ? item : null;
  }

  /**
   * Coerces the specified atomic item to the specified item type.
   * @param item item to coerce
   * @param tp dereferenced item type of this sequence type
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return coerced item (can be {@code null})
   * @throws QueryException query exception
   */
  private Item coerceAtomic(final Item item, final Type tp, final QueryContext qc,
      final InputInfo info) throws QueryException {
    final Type at = item.type;
    if(at.instanceOf(tp)) return item;
    // xs:error has no values: coercion fails, the caller raises XPTY0004
    if(tp == ERROR) return null;

    Item relabel = null;
    if(at == UNTYPED_ATOMIC) {
      if(tp.oneOf(QNAME, NOTATION)) throw NSSENS_X_X.get(info, at, type);
      // item will be cast
    } else if(
      tp == DECIMAL && (at == DOUBLE || at == FLOAT) ||
      tp == DOUBLE && (at == FLOAT || at.instanceOf(DECIMAL)) ||
      tp == FLOAT && (at == DOUBLE || at.instanceOf(DECIMAL)) ||
      tp == STRING && at == ANY_URI ||
      tp == ANY_URI && at.instanceOf(STRING) ||
      tp == HEX_BINARY && at == BASE64_BINARY ||
      tp == BASE64_BINARY && at == HEX_BINARY ||
      tp instanceof EnumType && at == ANY_URI
    ) {
      // item will be cast
    } else if(!tp.union(at).oneOf(ANY_ATOMIC_TYPE, NUMERIC)) {
      // item will be relabeled (3 → xs:short): remember old item for comparison
      relabel = item;
    } else {
      return null;
    }
    // relabeling is no cast: if the value is outside the value space, a type error is raised
    final boolean lenient = tp instanceof EnumType || relabel != null;
    final Item cast = (Item) cast(item, !lenient, qc, info);
    return cast != null && (relabel == null || cast.compare(relabel, null, false, qc, info) == 0) ?
        cast : null;
  }

  /**
   * Computes the union of this and the specified sequence type.
   * @param st other sequence type
   * @return union type
   */
  public SeqType union(final SeqType st) {
    if(this == st) return this;
    // ignore general type of empty sequence
    final Type tp = st.zero() ? type : zero() ? st.type :
      type.deref().union(st.type.deref());
    final Occ oc = occ.union(st.occ);
    return get(tp, oc);
  }

  /**
   * Computes the union of the sequence types of the specified expressions.
   * @param exprs expressions
   * @param zero include expressions that yield the empty sequence
   * @return union type (can be {@code null})
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
   * Computes the intersection of this and the specified sequence type.
   * @param st other sequence type
   * @return intersection type (can be {@code null})
   */
  public SeqType intersect(final SeqType st) {
    if(this == st) return this;
    // void types (xs:error, xs:error+) are bottom types; two void occurrences are intersected
    if(voidType()) return st.voidType() ? with(occ.intersect(st.occ)) : this;
    if(st.voidType()) return st;
    final Occ oc = occ.intersect(st.occ);
    if(oc == null) return null;
    final Type tp = type.deref().intersect(st.type.deref());
    if(tp != null) return get(tp, oc);
    // disjoint item types: the empty sequence is the only common instance
    return oc.min == 0 ? Types.EMPTY_SEQUENCE_Z : null;
  }

  /**
   * Tests if expressions of this type yield at most one item.
   * @return result of check
   */
  public boolean zeroOrOne() {
    return occ.max <= 1;
  }

  /**
   * Tests if this is the {@code empty-sequence()} type.
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
   * Tests if this is a sequence type without instances ({@code xs:error}, {@code xs:error+}).
   * @return result of check
   */
  public boolean voidType() {
    return oneOrMore() && itemless();
  }

  /**
   * Tests if expressions of this type yield no items ({@code empty-sequence()}, {@code xs:error*}).
   * @return result of check
   */
  public boolean itemless() {
    return zero() || type.deref() == ERROR;
  }

  /**
   * Tests if expressions of this type may yield numbers.
   * @return result of check
   */
  public boolean mayBeNumber() {
    return mayBe(Check.NUMBER);
  }

  /**
   * Tests if expressions of this type may yield function items.
   * @return result of check
   */
  public boolean mayBeFunction() {
    return mayBe(Check.FUNCTION);
  }

  /**
   * Tests if expressions of this type may yield items that wrap values (JNodes, function items).
   * @return result of check
   */
  public boolean mayBeWrapped() {
    return mayBeJNode() || mayBeFunction();
  }

  /**
   * Tests if expressions of this type may yield JNodes.
   * @return result of check
   */
  public boolean mayBeJNode() {
    return mayBe(Check.JNODE);
  }

  /**
   * Returns the occurrence indicator of the atomized items.
   * @return occurrence indicator
   */
  public Occ atomOcc() {
    return occ.multiply(itemAtomOcc());
  }

  /**
   * Tests if each item atomizes to exactly one item.
   * @return result of check
   */
  public boolean atomizesToOne() {
    return itemAtomOcc() == Occ.EXACTLY_ONE;
  }

  /**
   * Tests if expressions of this type yield at most one item that is not wrapped.
   * @return result of check
   */
  public boolean zeroOrOneUnwrapped() {
    return zeroOrOne() && !mayBeWrapped();
  }

  /**
   * Returns the item type to be checked for properties of atomized items.
   * @return item type
   */
  public Type atomType() {
    return mayBeWrapped() ? type.atomicOrAny() : type.deref();
  }

  /**
   * Returns the value type of JNodes of this type.
   * @return value type (can be {@code null})
   */
  public SeqType valueType() {
    return type.deref() instanceof final NodeType nt ? nt.valueType() : null;
  }

  /**
   * Returns the occurrence indicator of the atomized value of a single item.
   * @return occurrence indicator
   */
  private Occ itemAtomOcc() {
    // jnode(*, xs:integer): the atomized value; array(xs:integer): any number of items
    final SeqType vt = valueType();
    return vt != null ? vt.atomOcc() : mayBeWrapped() ? Occ.ZERO_OR_MORE : Occ.EXACTLY_ONE;
  }

  /**
   * Returns the cached result of the specified check.
   * @param check check
   * @return result of check
   */
  private boolean mayBe(final Check check) {
    final int eval = 1 << check.ordinal(), value = eval << CHECKS;
    if((checks & eval) != 0) return (checks & value) != 0;

    final boolean result = compute(check);
    if(!TypeRef.incomplete(type)) checks |= eval | (result ? value : 0);
    return result;
  }

  /**
   * Evaluates the specified check.
   * @param check check
   * @return result of check
   */
  private boolean compute(final Check check) {
    if(itemless()) return false;
    return switch(check) {
      case NUMBER -> mayBe(NUMERIC);
      case FUNCTION -> mayBe(Types.FUNCTION);
      case JNODE -> mayBe(NodeType.JNODE);
    };
  }

  /**
   * Tests if items of this type may be instances of the specified type.
   * @param tp item type to check
   * @return result of check
   */
  private boolean mayBe(final Type tp) {
    return type.intersect(tp) != null;
  }

  /**
   * Returns the type of values that match this type without being coerced.
   * @return sequence type
   */
  public SeqType matched() {
    final Type tp = matched(type);
    return tp == type ? this : get(tp, occ);
  }

  /**
   * Returns the type of items that match the specified item type without being coerced.
   * @param type item type
   * @return item type
   */
  private static Type matched(final Type type) {
    final Type tp = type.deref();
    // matching records may have another record type: no record type can be attached
    if(tp instanceof final ShapeType sh) return sh.labeled() ? Types.RECORD : type;
    if(tp instanceof final ArrayType at) {
      final SeqType st = at.memberType(), mst = st.matched();
      return mst == st ? type : ArrayType.get(mst);
    }
    if(tp instanceof final MapType mt) {
      final SeqType vt = mt.valueType(), mvt = vt.matched();
      return mvt == vt ? type : MapType.get(mt.keyType(), mvt);
    }
    if(tp instanceof final FuncType ft && ft.argTypes != null) {
      final SeqType dt = ft.declType, mdt = dt.matched();
      return mdt == dt ? type : FuncType.get(ft.anns, mdt, ft.argTypes);
    }
    if(tp instanceof final ChoiceItemType cit) {
      final int tl = cit.types.size();
      final Type[] types = new Type[tl];
      boolean changed = false;
      for(int t = 0; t < tl; t++) {
        types[t] = matched(cit.types.get(t));
        changed |= types[t] != cit.types.get(t);
      }
      return changed ? ChoiceItemType.get(types) : type;
    }
    return type;
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
    // empty category (empty-sequence(), xs:error?, xs:error*): subtype of every optional type
    if(itemless()) return !st.oneOrMore();
    if(!occ.instanceOf(st.occ)) return false;
    return type.deref().instanceOf(st.type.deref());
  }

  /**
   * Checks if values of this type are kept unchanged when coerced to the specified type.
   * @param st sequence type to check
   * @return result of check
   */
  public boolean keptBy(final SeqType st) {
    // identical records are kept, typed functions are always wrapped
    return instanceOf(st) &&
      (st.type.coercion() == Coercion.KEEP || type.keptStatic(st.type, true));
  }

  /**
   * Checks if this inferred type can replace the specified declared type.
   * @param st declared type
   * @return result of check
   */
  public boolean refines(final SeqType st) {
    // inferred function types are upper bounds and must not be enforced
    // inferred xs:integer for xs:decimal: true; inferred fn(xs:integer) for fn(*): false
    return keptBy(st) && type.coercion() != Coercion.WRAP;
  }

  /**
   * Returns this sequence type with a resolved type reference replaced by the referenced type.
   * @return sequence type
   */
  public SeqType deref() {
    return type instanceof final TypeRef ref && !TypeRef.incomplete(ref) ? get(ref.deref(), occ) :
      this;
  }

  /**
   * Checks if this sequence type is equal to the specified one.
   * @param st sequence type to compare
   * @return result of check
   */
  public boolean eq(final SeqType st) {
    if(this == st) return true;
    return occ == st.occ && TypeRef.eq(type, st.type);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final SeqType st && eq(st);
  }

  @Override
  public int hashCode() {
    // consistent with eq: structurally equal types may be different instances
    return Objects.hashCode(type.id()) * 31 + occ.hashCode();
  }

  /**
   * Returns a string representation of the item type, or {@code empty-sequence()}.
   * @return string
   */
  public String typeString() {
    return zero() ? QueryText.EMPTY_SEQUENCE + "()" : type.toString();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder();
    if(!one() && type instanceof FuncType) {
      tb.add('(').add(typeString()).add(')');
    } else {
      tb.add(typeString());
    }
    if(!(type instanceof ListType)) tb.add(occ);
    return tb.toString();
  }
}
