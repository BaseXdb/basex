package org.basex.query.value.type;

import static java.util.Collections.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Record type: a map type with a fixed set of named, individually typed fields. A record type may
 * be sealed (named or declared) or inferred from a map constructor; see {@link #sealed()}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordType extends MapType {
  /** Maximum number of entries in generated records. */
  public static final int MAX_GENERATED_SIZE = 32;

  /** Sealed flag: a sealed record type carries a runtime type annotation and constrains
   * field access (lookup of an undeclared field raises a type error). Inferred record types
   * (e.g. from map constructors) are not sealed and behave like ordinary maps. */
  private final boolean sealed;
  /** Record fields. */
  private final TokenObjectMap<RecordField> fields;
  /** Record type name (can be {@code null}). */
  private final QNm name;
  /** Annotations. */
  private final AnnList anns;

  /**
   * Constructor for an inferred (non-sealed) record type.
   * @param fields field declarations
   */
  public RecordType(final TokenObjectMap<RecordField> fields) {
    this(false, fields, null, AnnList.EMPTY);
  }

  /**
   * Constructor for an anonymous record type.
   * @param sealed sealed flag
   * @param fields field declarations
   */
  public RecordType(final boolean sealed, final TokenObjectMap<RecordField> fields) {
    this(sealed, fields, null, AnnList.EMPTY);
  }

  /**
   * Constructor for a named (sealed) record type.
   * @param fields field declarations
   * @param name record type name (can be {@code null})
   * @param anns annotations
   */
  public RecordType(final TokenObjectMap<RecordField> fields, final QNm name, final AnnList anns) {
    this(true, fields, name, anns);
  }

  /**
   * Constructor.
   * @param sealed sealed flag
   * @param fields field declarations
   * @param name record type name (can be {@code null})
   * @param anns annotations
   */
  public RecordType(final boolean sealed, final TokenObjectMap<RecordField> fields, final QNm name,
      final AnnList anns) {
    super(BasicType.STRING, unionType(fields));
    this.sealed = sealed;
    this.fields = fields;
    this.name = name;
    this.anns = anns;
  }

  /**
   * Adds a field to this record type.
   * @param fieldName field name
   * @param seqType sequence type of the field
   * @return this record type
   */
  public RecordType add(final String fieldName, final SeqType seqType) {
    fields.put(Token.token(fieldName), new RecordField(seqType));
    return this;
  }

  /**
   * Calculate union type of field sequence types.
   * @param rfs field declarations
   * @return union type
   */
  private static SeqType unionType(final TokenObjectMap<RecordField> rfs) {
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
   * Returns all record fields.
   * @return record fields
   */
  public TokenObjectMap<RecordField> fields() {
    return fields;
  }

  /**
   * Indicates if this record type is sealed (i.e. carries a runtime type annotation and
   * constrains field access).
   * @return result of check
   */
  public boolean sealed() {
    return sealed;
  }

  /**
   * Indicates if this record type enforces strict field access, i.e. it is sealed and not the
   * abstract {@code record(*)} type. Lookups of undeclared fields on such records raise an error.
   * @return result of check
   */
  public boolean strict() {
    return sealed && this != Types.RECORD;
  }

  /**
   * Returns the annotations of this record type.
   * @return annotations
   */
  public AnnList anns() {
    return anns;
  }

  /**
   * Return the minimum number of fields that must be supplied to the constructor function.
   * @return minimum number of fields
   */
  public int minFields() {
    int min = 0;
    for(final RecordField rf : fields.values()) {
      // a field is an optional constructor parameter if it has an initializer, or if its type
      // permits the empty sequence (in which case an omitted argument defaults to ())
      if(rf.init() != null || !rf.seqType().oneOrMore()) return min;
      ++min;
    }
    return min;
  }

  @Override
  public boolean eq(final Type type) {
    return eq(type, emptySet(), false);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final RecordType rt && eq(rt, emptySet(), true);
  }

  /**
   * Checks if this type is equal to the given one. This implementation uses the <code>pairs</code>
   * argument to keep track of pairs of RecordTypes being checked, in order to prevent infinite
   * recursion.
   * @param type other type
   * @param pairs pairs of RecordTypes that are currently being checked, or have been checked before
   * @param strict strict comparison (consider order and name of record)
   * @return result of check
   */
  private boolean eq(final Type type, final Set<Pair> pairs, final boolean strict) {
    if(this == type) return true;
    if(!(type instanceof final RecordType rt)) return false;
    // record() (empty record) and record(*) (any record) must remain distinct
    if((this == Types.RECORD) != (rt == Types.RECORD) ||
        sealed != rt.sealed || fields.size() != rt.fields.size()) return false;

    final Predicate<byte[]> compareFields = key -> {
      final RecordField rf1 = fields.get(key), rf2 = rt.fields.get(key);
      if(rf1 == null || rf2 == null) return false;
      final SeqType st1 = rf1.seqType(), st2 = rf2.seqType();
      if(st1.occ != st2.occ) return false;
      final Type tp1 = TypeRef.deref(st1.type), tp2 = TypeRef.deref(st2.type);
      if(tp1 instanceof final RecordType rt1 && tp2 instanceof final RecordType rt2) {
        final Pair pair = new Pair(rt1, rt2);
        return pairs.contains(pair) || rt1.eq(rt2, pair.addTo(pairs), strict);
      }
      return tp1.eq(tp2);
    };

    if(strict) {
      final Iterator<byte[]> iter = fields.iterator(), iter2 = rt.fields.iterator();
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
  public boolean instanceOf(final Type type) {
    return instanceOf(type, emptySet());
  }

  /**
   * Checks if the current type is an instance of the specified type. This implementation uses the
   * <code>pairs</code> argument to keep track of pairs of RecordTypes being checked, in order to
   * prevent infinite recursion.
   * @param type type to be checked
   * @param pairs pairs of RecordTypes that are currently being checked, or have been checked before
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
    if(type == Types.RECORD) {
      return true;
    }
    if(type instanceof final RecordType rt) {
      // an open record is not an instance of a sealed record (the seal is an extra guarantee)
      if(rt.sealed && !sealed) return false;
      if(fields.size() != rt.fields.size()) return false;
      for(final byte[] key : rt.fields) {
        if(!fields.contains(key)) return false;
        final SeqType fst = fields.get(key).seqType(), rtfst = rt.fields.get(key).seqType();
        if(fst != rtfst) {
          if(!fst.occ.instanceOf(rtfst.occ)) return false;
          final Type ft = TypeRef.deref(fst.type), rtft = TypeRef.deref(rtfst.type);
          if(ft instanceof final RecordType rt1 && rtft instanceof final RecordType rt2) {
            final Pair pair = new Pair(rt1, rt2);
            if(!pairs.contains(pair) && !rt1.instanceOf(rt2, pair.addTo(pairs))) return false;
          } else if(!ft.instanceOf(rtft)) {
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
  public Type union(final Type type) {
    return type == this ? this : union(type, emptySet());
  }

  @Override
  public MapType union(final Type kt, final SeqType vt) {
    return get(keyType().union(kt), valueType().union(vt));
  }

  /**
   * Computes the union between this type and the given one, i.e. the least common ancestor of both
   * types in the type hierarchy. This implementation uses the <code>pairs</code> argument to keep
   * track of pairs of RecordTypes being checked, in order to prevent infinite recursion.
   * @param type other type
   * @param pairs pairs of RecordTypes that are currently being checked, or have been checked before
   * @return union type
   */
  private Type union(final Type type, final Set<Pair> pairs) {
    if(type instanceof ChoiceItemType) return type.union(this);
    if(type == Types.MAP) return Types.MAP;
    if(type.instanceOf(this)) return this;
    if(instanceOf(type)) return type;

    if(type instanceof final RecordType rt) {
      if(sameFields(rt)) {
        final TokenObjectMap<RecordField> map = new TokenObjectMap<>();
        for(final byte[] key : fields) {
          final SeqType fst = fields.get(key).seqType(), rtfst = rt.fields.get(key).seqType();
          final Type ft = TypeRef.deref(fst.type), rtft = TypeRef.deref(rtfst.type);
          final SeqType union;
          if(ft instanceof final RecordType rt1 && rtft instanceof final RecordType rt2 &&
              !fst.zero() && !rtfst.zero()) {
            final Pair pair = new Pair(rt1, rt2);
            if(pairs.contains(pair)) return Types.MAP;
            union = SeqType.get(rt1.union(rt2, pair.addTo(pairs)), fst.occ.union(rtfst.occ));
          } else {
            union = fst.union(rtfst);
          }
          map.put(key, new RecordField(union));
        }
        return new RecordType(sealed && rt.sealed, map);
      }
      // fallback (map supertype)
      return MapType.get(keyType().union(rt.keyType()), valueType().union(rt.valueType()));
    }
    return type instanceof final MapType mt ? mt.union(keyType(), valueType()) :
           type instanceof ArrayType ? Types.FUNCTION :
           type instanceof FuncType ? type.union(this) : BasicType.ITEM;
  }

  /**
   * Checks whether this record type and the given one declare exactly the same set of field names.
   * @param rt other record type
   * @return result of check
   */
  private boolean sameFields(final RecordType rt) {
    if(fields.size() != rt.fields.size()) return false;
    for(final byte[] key : fields) {
      if(!rt.fields.contains(key)) return false;
    }
    return true;
  }

  @Override
  public Type intersect(final Type type) {
    return type == this ? this : intersect(type, emptySet());
  }

  /**
   * Computes the intersection between this type and the given one, i.e. the least specific type
   * that is subtype of both types. If no such type exists, {@code null} is returned. This
   * implementation uses the <code>pairs</code> argument to keep track of pairs of RecordTypes being
   * checked, in order to prevent infinite recursion.
   * @param type other type
   * @param pairs pairs of RecordTypes that are currently being checked, or have been checked before
   * @return intersection type or {@code null}
   */
  private Type intersect(final Type type, final Set<Pair> pairs) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(type instanceof final RecordType rt) {
      if(sameFields(rt)) {
        final TokenObjectMap<RecordField> map = new TokenObjectMap<>();
        for(final byte[] key : fields) {
          final SeqType is = intersect(fields.get(key).seqType(), rt.fields.get(key).seqType(),
              pairs);
          if(is == null) return null;
          map.put(key, new RecordField(is));
        }
        return new RecordType(sealed || rt.sealed, map);
      }
      return null;
    }
    if(type instanceof final MapType mt) {
      if(mt.keyType().intersect(BasicType.STRING) == null) return null;
      final TokenObjectMap<RecordField> map = new TokenObjectMap<>();
      for(final byte[] key : fields) {
        final SeqType is = intersect(fields.get(key).seqType(), mt.valueType(), pairs);
        if(is == null) return null;
        map.put(key, new RecordField(is));
      }
      return new RecordType(sealed, map);
    }
    return null;
  }

  /**
   * Returns the intersection between two sequence types, or {@code null}, if no such type exists.
   * Uses <code>pairs</code> to keep track of pairs of RecordTypes being checked, in order to
   * prevent infinite recursion.
   * @param st1 first type
   * @param st2 second type
   * @param pairs pairs of RecordTypes that are currently being checked, or have been checked before
   * @return intersection type or {@code null}
   */
  private static SeqType intersect(final SeqType st1, final SeqType st2, final Set<Pair> pairs) {
    final Type t1 = TypeRef.deref(st1.type), t2 = TypeRef.deref(st2.type);
    if(t1 instanceof final RecordType rt1 && t2 instanceof final RecordType rt2) {
      final Pair pair = new Pair(rt1, rt2);
      if(pairs.contains(pair)) return null;
      final Type it = rt1.intersect(rt2, pair.addTo(pairs));
      return it == null ? null : SeqType.get(it, st1.occ.intersect(st2.occ));
    }
    return st1.intersect(st2);
  }

  /**
   * Returns a non-sealed (open) version of this record type, as produced by map operations such as
   * {@code map:put}/{@code map:remove} that do not preserve the record annotation.
   * @return open record type
   */
  public RecordType open() {
    return strict() ? new RecordType(false, fields) : this;
  }

  /**
   * Creates a new compile-time instance of the record type. The result is not sealed, as it is
   * produced by map operations such as {@code map:put}.
   * @param remove key to remove (can be {@code null})
   * @param put key to add or replace (can be {@code null})
   * @param seqType sequence type of the field to add or replace (ignored if put is {@code null})
   * @param cc compilation context
   * @return new type or {@code null} if number of fields exceeds limit
   */
  public RecordType copy(final byte[] remove, final byte[] put, final SeqType seqType,
      final CompileContext cc) {

    final TokenObjectMap<RecordField> map = new TokenObjectMap<>(fields.size());
    for(final byte[] key : fields) {
      if(remove == null || !Token.eq(remove, key)) map.put(key, fields.get(key));
    }
    if(put != null) map.put(put, new RecordField(seqType));

    return map.size() > MAX_GENERATED_SIZE ? null :
      cc.qc.shared.record(new RecordType(map));
  }

  @Override
  public QNm name() {
    return name;
  }

  @Override
  public String toString() {
    if(name != null) return Token.string(name.prefixString());

    final QueryString qs = new QueryString().token(RECORD).token('(');
    final TokenBuilder tb = new TokenBuilder();
    if(this != Types.RECORD) {
      for(final byte[] key : fields) {
        if(!tb.isEmpty()) tb.add(", ");
        if(!tb.moreInfo()) break;
        tb.add(XMLToken.isNCName(key) ? key : QueryString.toQuoted(key));
      }
    } else {
      tb.add('*');
    }
    return qs.token(tb.finish()).token(')').toString();
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
