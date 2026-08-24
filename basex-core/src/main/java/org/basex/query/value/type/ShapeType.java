package org.basex.query.value.type;

import static java.util.Collections.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Shape of a map: a fixed set of named, individually typed fields. A shape that is declared in a
 * query is a {@link RecordType}; all other shapes are inferred from map constructors and other
 * expressions with a statically known field set.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class ShapeType extends MapType {
  /** Maximum number of entries in generated shapes. */
  public static final int MAX_GENERATED_SIZE = 32;
  /** Maximum number of derived shapes that are cached. */
  private static final int MAX_DERIVED = 64;

  /** Fields. */
  private final TokenObjectMap<ShapeField> fields;
  /** Field names as string items (can be {@code null}). */
  private volatile Str[] keys;
  /** Cached result of {@link #detached()} (can be {@code null}). */
  private Boolean detached;
  /** Cached shapes derived from this one (can be {@code null}). */
  private Map<Derived, ShapeType> derived;

  /**
   * Constructor.
   * @param fields field declarations
   */
  public ShapeType(final TokenObjectMap<ShapeField> fields) {
    super(BasicType.STRING, unionType(fields));
    this.fields = fields;
  }

  /**
   * Creates a shape or an anonymous record.
   * @param fields field declarations
   * @param declared declared flag
   * @return shape
   */
  private static ShapeType get(final TokenObjectMap<ShapeField> fields, final boolean declared) {
    return declared ? new RecordType(fields) : new ShapeType(fields);
  }

  /**
   * Returns a type of the same kind with the specified fields.
   * @param map field declarations
   * @return shape
   */
  public ShapeType with(final TokenObjectMap<ShapeField> map) {
    return new ShapeType(map);
  }

  /**
   * Returns an inferred shape with an additional or updated field.
   * @param fieldName field name
   * @param seqType sequence type of the field
   * @return shape, or {@code null} if the maximum size of generated shapes is exceeded
   */
  public final ShapeType put(final byte[] fieldName, final SeqType seqType) {
    return copy(fieldName, seqType);
  }

  /**
   * Returns an inferred shape without the specified field.
   * @param fieldName field name
   * @return shape
   */
  public final ShapeType remove(final byte[] fieldName) {
    return copy(fieldName, null);
  }

  /**
   * Returns an inferred shape with an updated or removed field.
   * @param field name of the field to be added or updated, or of the field to be removed
   * @param seqType sequence type of the added field, {@code null} to remove the field
   * @return shape, or {@code null} if the maximum size of generated shapes is exceeded
   */
  private ShapeType copy(final byte[] field, final SeqType seqType) {
    if(seqType != null && !fields.contains(field) && fields.size() >= MAX_GENERATED_SIZE) {
      return null;
    }
    // shapes with query-scoped components must not be cached: they would be retained forever
    if(detached() && (!(seqType != null && seqType.type instanceof final ShapeType sh) ||
        sh.detached())) {
      if(derived == null) derived = new ConcurrentHashMap<>();
      // do not cache shapes that are derived from arbitrarily many field names
      if(derived.size() < MAX_DERIVED) {
        final Derived key = new Derived(field, seqType);
        ShapeType shape = derived.get(key);
        if(shape == null) {
          shape = create(field, seqType);
          derived.putIfAbsent(key, shape);
        }
        return shape;
      }
    }
    return create(field, seqType);
  }

  /**
   * Creates a shape with an updated or removed field.
   * @param field name of the field to be added or updated, or of the field to be removed
   * @param seqType sequence type of the added field, {@code null} to remove the field
   * @return shape
   */
  private ShapeType create(final byte[] field, final SeqType seqType) {
    final TokenObjectMap<ShapeField> map = new TokenObjectMap<>(fields.size() + 1L);
    for(final byte[] key : fields) {
      if(seqType != null || !Token.eq(field, key)) map.put(key, fields.get(key));
    }
    if(seqType != null) map.put(field, new ShapeField(seqType));
    return new ShapeType(map);
  }

  /**
   * Key of a shape that is derived from another one.
   * @param field name of the added, updated or removed field
   * @param seqType sequence type of the added field (can be {@code null})
   */
  private record Derived(byte[] field, SeqType seqType) {
    @Override
    public boolean equals(final Object obj) {
      return this == obj || obj instanceof final Derived d && Token.eq(field, d.field) &&
          Objects.equals(seqType, d.seqType);
    }

    @Override
    public int hashCode() {
      return Token.hashCode(field) + (seqType != null ? seqType.hashCode() * 31 : 0);
    }
  }

  /**
   * Returns a shape with field types narrowed to the specified sequence types.
   * @param seqTypes new field types, one per field
   * @return refined shape, or {@code this} if no field type is narrowed
   */
  public final ShapeType refine(final SeqType... seqTypes) {
    final int fs = fields.size();
    if(name() != null || seqTypes.length != fs) return this;

    final TokenObjectMap<ShapeField> map = new TokenObjectMap<>(fs);
    boolean narrowed = false;
    for(int f = 1; f <= fs; f++) {
      final SeqType ost = fields.value(f).seqType(), nst = seqTypes[f - 1];
      final SeqType st = nst.instanceOf(ost) ? nst : ost;
      if(!st.eq(ost)) narrowed = true;
      map.put(fields.key(f), new ShapeField(st));
    }
    return narrowed ? with(map) : this;
  }

  /**
   * Adds a field to this shape.
   * @param fieldName field name
   * @param seqType sequence type of the field
   * @return this shape
   */
  public ShapeType add(final String fieldName, final SeqType seqType) {
    // the value type is not recomputed: only used to build recursive built-in records
    fields.put(Token.token(fieldName), new ShapeField(seqType));
    keys = null;
    detached = null;
    return this;
  }

  /**
   * Returns the name of the specified field as a string item.
   * @param index field index (starting with 1)
   * @return field name
   */
  public final Str key(final int index) {
    Str[] ks = keys;
    if(ks == null) {
      final int fs = fields.size();
      ks = new Str[fs];
      for(int f = 0; f < fs; f++) ks[f] = Str.get(fields.key(f + 1));
      keys = ks;
    }
    return ks[index - 1];
  }

  /**
   * Calculate union type of field sequence types.
   * @param rfs field declarations
   * @return union type
   */
  private static SeqType unionType(final TokenObjectMap<ShapeField> rfs) {
    if(rfs.isEmpty()) return Types.ITEM_ZM;
    SeqType ust = null;
    final int fs = rfs.size();
    for(int f = 1; f <= fs; f++) {
      final SeqType st = rfs.value(f).seqType();
      ust = ust == null ? st : ust.union(st);
    }
    return ust;
  }

  /**
   * Returns all fields.
   * @return fields
   */
  public final TokenObjectMap<ShapeField> fields() {
    return fields;
  }

  /**
   * Indicates if this shape is declared in a query, i.e. if it carries a runtime type annotation
   * and constrains field access.
   * @return result of check
   */
  boolean declared() {
    return false;
  }

  /**
   * Indicates if this is the abstract {@code record(*)} type, which matches any record. Its field
   * set is unknown, so no assumptions must be made about the presence or absence of a field.
   * @return result of check
   */
  public final boolean any() {
    return this == Types.RECORD;
  }

  /**
   * Indicates if this shape enforces strict field access, i.e. it is declared and not the
   * abstract {@code record(*)} type. Lookups of undeclared fields on such records raise an error.
   * @return result of check
   */
  public boolean strict() {
    return false;
  }

  /**
   * Return the minimum number of fields that must be supplied to the constructor function.
   * @return minimum number of fields
   */
  public final int minFields() {
    int min = 0;
    for(final ShapeField rf : fields.values()) {
      // a field is an optional constructor parameter if it has an initializer, or if its type
      // permits the empty sequence (in which case an omitted argument defaults to ())
      if(rf.init() != null || !rf.seqType().oneOrMore()) return min;
      ++min;
    }
    return min;
  }

  @Override
  public final boolean eq(final Type type) {
    return eq(type, emptySet(), false);
  }

  @Override
  public final boolean equals(final Object obj) {
    return this == obj || obj instanceof final ShapeType sh && eq(sh, emptySet(), true);
  }

  /**
   * Checks if this type is equal to the given one.
   * @param type other type
   * @param pairs pairs of ShapeTypes that are currently being checked, or have been checked before
   * @param strict strict comparison (consider field order)
   * @return result of check
   */
  private boolean eq(final Type type, final Set<Pair> pairs, final boolean strict) {
    if(this == type) return true;
    if(!(type instanceof final ShapeType sh)) return false;
    // record() (empty record) and record(*) (any record) must remain distinct
    if(this == Types.RECORD != (sh == Types.RECORD) ||
        declared() != sh.declared() || fields.size() != sh.fields.size()) return false;

    final Predicate<byte[]> compareFields = key -> {
      final ShapeField rf1 = fields.get(key), rf2 = sh.fields.get(key);
      if(rf1 == null || rf2 == null) return false;
      final SeqType st1 = rf1.seqType(), st2 = rf2.seqType();
      if(st1.occ != st2.occ) return false;
      final Type tp1 = TypeRef.deref(st1.type), tp2 = TypeRef.deref(st2.type);
      if(tp1 instanceof final ShapeType sh1 && tp2 instanceof final ShapeType sh2) {
        final Pair pair = new Pair(sh1, sh2);
        return pairs.contains(pair) || sh1.eq(sh2, pair.addTo(pairs), strict);
      }
      return tp1.eq(tp2);
    };

    if(strict) {
      final Iterator<byte[]> iter = fields.iterator(), iter2 = sh.fields.iterator();
      for(byte[] key; (key = iter.next()) != null;) {
        if(!Token.eq(key, iter2.next()) || !compareFields.test(key)) return false;
      }
    } else {
      for(final byte[] key : fields) {
        if(!compareFields.test(key)) return false;
      }
    }
    return true;
  }

  @Override
  public final boolean instanceOf(final Type type) {
    return instanceOf(type, emptySet());
  }

  /**
   * Checks if the current type is an instance of the specified type.
   * @param type type to be checked
   * @param pairs pairs of ShapeTypes that are currently being checked, or have been checked before
   * @return result of check
   */
  private boolean instanceOf(final Type type, final Set<Pair> pairs) {
    if(this == type || type.oneOf(Types.MAP, Types.FUNCTION, BasicType.ITEM)) {
      return true;
    }
    if(type instanceof final ChoiceItemType cit) {
      for(final Type tp : cit.types) {
        if(instanceOf(tp, pairs)) return true;
      }
      return false;
    }
    // record(*) is only matched by types that carry a record annotation
    if(type == Types.RECORD) {
      return declared();
    }
    if(type instanceof final ShapeType sh) {
      // an inferred shape is not an instance of a record (the annotation is an extra guarantee)
      if(sh.declared() && !declared()) return false;
      if(fields.size() != sh.fields.size()) return false;
      for(final byte[] key : sh.fields) {
        if(!fields.contains(key)) return false;
        final SeqType fst = fields.get(key).seqType(), shfst = sh.fields.get(key).seqType();
        if(fst != shfst) {
          final Type ft = TypeRef.deref(fst.type), shft = TypeRef.deref(shfst.type);
          if(ft instanceof final ShapeType sh1 && shft instanceof final ShapeType sh2 &&
              !fst.emptyType()) {
            if(!fst.occ.instanceOf(shfst.occ)) return false;
            final Pair pair = new Pair(sh1, sh2);
            if(!pairs.contains(pair) && !sh1.instanceOf(sh2, pair.addTo(pairs))) return false;
          } else if(!fst.instanceOf(shfst)) {
            return false;
          }
        }
      }
      return true;
    }
    if(type instanceof final MapType mt) {
      return keyType().instanceOf(mt.keyType()) && valueType().instanceOf(mt.valueType());
    }
    if(type instanceof final FuncType ft) {
      return funcType().declType.instanceOf(ft.declType) && ft.argTypes.length == 1 &&
          ft.argTypes[0].instanceOf(Types.ANY_ATOMIC_TYPE_O);
    }
    return false;
  }

  @Override
  public final Type union(final Type type) {
    return type == this ? this : union(type, emptySet());
  }

  @Override
  public final MapType union(final Type kt, final SeqType vt) {
    return get(keyType().union(kt), valueType().union(vt));
  }

  /**
   * Computes the union between this type and the given one, i.e. the least common ancestor of both
   * types in the type hierarchy.
   * @param type other type
   * @param pairs pairs of ShapeTypes that are currently being checked, or have been checked before
   * @return union type
   */
  private Type union(final Type type, final Set<Pair> pairs) {
    if(type instanceof ChoiceItemType) return type.union(this);
    if(type == Types.MAP) return Types.MAP;
    if(type.instanceOf(this)) return this;
    if(instanceOf(type)) return type;

    if(type instanceof final ShapeType sh) {
      if(sameFields(sh)) {
        final TokenObjectMap<ShapeField> map = new TokenObjectMap<>();
        for(final byte[] key : fields) {
          final SeqType fst = fields.get(key).seqType(), shfst = sh.fields.get(key).seqType();
          final Type ft = TypeRef.deref(fst.type), shft = TypeRef.deref(shfst.type);
          final SeqType union;
          if(ft instanceof final ShapeType sh1 && shft instanceof final ShapeType sh2 &&
              !fst.zero() && !shfst.zero()) {
            final Pair pair = new Pair(sh1, sh2);
            if(pairs.contains(pair)) return Types.MAP;
            union = SeqType.get(sh1.union(sh2, pair.addTo(pairs)), fst.occ.union(shfst.occ));
          } else {
            union = fst.union(shfst);
          }
          map.put(key, new ShapeField(union));
        }
        return get(map, declared() && sh.declared());
      }
      // fallback (map supertype)
      return MapType.get(keyType().union(sh.keyType()), valueType().union(sh.valueType()));
    }
    return type instanceof final MapType mt ? mt.union(keyType(), valueType()) :
           type instanceof ArrayType ? Types.FUNCTION :
           type instanceof FuncType ? type.union(this) : BasicType.ITEM;
  }

  /**
   * Checks whether this shape and the given one declare exactly the same set of field names.
   * @param sh other shape
   * @return result of check
   */
  private boolean sameFields(final ShapeType sh) {
    if(fields.size() != sh.fields.size()) return false;
    for(final byte[] key : fields) {
      if(!sh.fields.contains(key)) return false;
    }
    return true;
  }

  @Override
  public final Type intersect(final Type type) {
    return type == this ? this : intersect(type, emptySet());
  }

  /**
   * Computes the intersection between this type and the given one, i.e. the least specific type
   * that is subtype of both types. If no such type exists, {@code null} is returned.
   * @param type other type
   * @param pairs pairs of ShapeTypes that are currently being checked, or have been checked before
   * @return intersection type or {@code null}
   */
  private Type intersect(final Type type, final Set<Pair> pairs) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(type instanceof final ShapeType sh) {
      if(sameFields(sh)) {
        final TokenObjectMap<ShapeField> map = new TokenObjectMap<>();
        for(final byte[] key : fields) {
          final SeqType is = intersect(fields.get(key).seqType(), sh.fields.get(key).seqType(),
              pairs);
          if(is == null) return null;
          map.put(key, new ShapeField(is));
        }
        final ShapeType st = name() != null || !sh.declared() || sh.name() == null && declared() ?
          this : sh;
        return st.with(map);
      }
      return null;
    }
    if(type instanceof final MapType mt) {
      if(mt.keyType().intersect(BasicType.STRING) == null) return null;
      final TokenObjectMap<ShapeField> map = new TokenObjectMap<>();
      for(final byte[] key : fields) {
        final SeqType is = intersect(fields.get(key).seqType(), mt.valueType(), pairs);
        if(is == null) return null;
        map.put(key, new ShapeField(is));
      }
      return with(map);
    }
    return null;
  }

  /**
   * Returns the intersection between two sequence types.
   * @param st1 first type
   * @param st2 second type
   * @param pairs pairs of ShapeTypes that are currently being checked, or have been checked before
   * @return intersection type or {@code null}
   */
  private static SeqType intersect(final SeqType st1, final SeqType st2, final Set<Pair> pairs) {
    final Type t1 = TypeRef.deref(st1.type), t2 = TypeRef.deref(st2.type);
    if(t1 instanceof final ShapeType sh1 && t2 instanceof final ShapeType sh2) {
      final Pair pair = new Pair(sh1, sh2);
      if(pairs.contains(pair)) return null;
      final Type it = sh1.intersect(sh2, pair.addTo(pairs));
      return it == null ? null : SeqType.get(it, st1.occ.intersect(st2.occ));
    }
    return st1.intersect(st2);
  }

  /**
   * Returns the shape of this type, as produced by map operations such as
   * {@code map:put}/{@code map:remove} that do not preserve the record annotation.
   * @return shape
   */
  public ShapeType shape() {
    return this;
  }

  /**
   * Indicates if this shape is free of initializing expressions, i.e. of references to the
   * query in which it was declared.
   * @return result of check
   */
  public final boolean detached() {
    Boolean d = detached;
    if(d == null) {
      d = true;
      for(final ShapeField rf : fields.values()) {
        if(rf.init() != null) {
          d = false;
          break;
        }
      }
      detached = d;
    }
    return d;
  }

  /**
   * Returns a version of this shape that can be attached to a materialized value.
   * @return shape without initializing expressions
   */
  public ShapeType detach() {
    return detached() ? this : new ShapeType(detachedFields());
  }

  /**
   * Returns the fields of this shape without their initializing expressions.
   * @return fields
   */
  final TokenObjectMap<ShapeField> detachedFields() {
    final TokenObjectMap<ShapeField> map = new TokenObjectMap<>(fields.size());
    for(final byte[] key : fields) map.put(key, new ShapeField(fields.get(key).seqType()));
    return map;
  }

  @Override
  public QNm name() {
    return null;
  }

  /**
   * Returns the field names of this type.
   * @return comma-separated field names
   */
  public final String fieldNames() {
    final TokenBuilder tb = new TokenBuilder();
    for(final byte[] key : fields) {
      if(!tb.isEmpty()) tb.add(", ");
      if(!tb.moreInfo()) break;
      tb.add(XMLToken.isNCName(key) ? key : QueryString.toQuoted(key));
    }
    return tb.toString();
  }

  @Override
  public String toString() {
    // an inferred shape is not a record: represent it by the map type it is an instance of
    return MapType.get(this).toString();
  }

  /**
   * An ordered pair of objects.
   * @param o1 first object.
   * @param o2 second object.
   */
  private record Pair(Object o1, Object o2) {
    /**
     * Adds this {@code Pair} to the given set of {@code Pair}s, creating a new set if the given set
     * is empty.
     * @param pairs set of {@code Pair}s
     * @return the augmented set of pairs
     */
    public Set<Pair> addTo(final Set<Pair> pairs) {
      if(pairs.isEmpty()) {
        final Set<Pair> set = new HashSet<>();
        set.add(this);
        return set;
      }
      pairs.add(this);
      return pairs;
    }

    @Override
    public String toString() {
      return new QueryString().token('[').token(o1).token(',').token(o2).token(']').toString();
    }
  }
}
