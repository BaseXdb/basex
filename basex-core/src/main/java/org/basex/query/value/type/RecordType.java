package org.basex.query.value.type;

import static java.util.Collections.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Set;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Type for record tests.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordType extends MapType {
  /** Maximum size for generated record definitions. */
  public static final int MAX_GENERATED_SIZE = 32;

  /** Record fields. */
  private TokenObjectMap<RecordField> fields;
  /** Record type name (can be {@code null}). */
  private final QNm name;
  /** Annotations. */
  private final AnnList anns;
  /** Input info ({@code null}, if this is not an unresolved reference). */
  private InputInfo info;

  /**
   * Constructor.
   * @param fields field declarations
   */
  public RecordType(final TokenObjectMap<RecordField> fields) {
    this(fields, null, AnnList.EMPTY);
  }

  /**
   * Constructor.
   * @param fields field declarations
   * @param name record type name (can be {@code null})
   * @param anns annotations
   */
  public RecordType(final TokenObjectMap<RecordField> fields, final QNm name, final AnnList anns) {
    super(BasicType.STRING, unionType(fields));
    this.fields = fields;
    this.name = name;
    this.anns = anns;
    info = null;
  }

  /**
   * Constructor for RecordType references.
   * @param name record type name
   * @param info input info
   */
  public RecordType(final QNm name, final InputInfo info) {
    super(BasicType.ANY_ATOMIC_TYPE, Types.ITEM_ZM, false);
    this.name = name;
    this.info = info;
    fields = new TokenObjectMap<>(0);
    anns = AnnList.EMPTY;
  }

  /**
   * Adds a field to this record type.
   * @param fieldName field name
   * @param optional optional flag
   * @param seqType sequence type of the field
   * @return this record type
   */
  public RecordType add(final String fieldName, final boolean optional, final SeqType seqType) {
    fields.put(Token.token(fieldName), new RecordField(seqType, optional));
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
   * Indicates if this record has optional fields.
   * @return result of check
   */
  public boolean hasOptional() {
    final int fs = fields.size();
    for(int f = 1; f <= fs; f++) {
      if(fields.value(f).isOptional()) return true;
    }
    return false;
  }

  /**
   * Returns the name of the record.
   * @return name (can be {@code null})
   */
  public QNm name() {
    return name;
  }

  /**
   * Returns the annotations of this record type.
   * @return annotations
   */
  public AnnList anns() {
    return anns;
  }

  /**
   * Return the minimum number of fields that must be present in a record of this type.
   * @return minimum number of fields
   */
  public int minFields() {
    int min = 0;
    for(final RecordField rf : fields.values()) {
      if(rf.optional || rf.expr() != null) return min;
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
    if(fields.size() != rt.fields.size()) return false;

    final Predicate<byte[]> compareFields = key -> {
      final RecordField rf1 = fields.get(key), rf2 = rt.fields.get(key);
      if(rf1 == null || rf2 == null || rf1.optional != rf2.optional) return false;
      final SeqType st1 = rf1.seqType(), st2 = rf2.seqType();
      if(st1.occ != st2.occ) return false;
      final Type tp1 = st1.type, tp2 = st2.type;
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
      for(final SeqType st : cit.types) {
        if(instanceOf(st.type, pairs)) return true;
      }
      return false;
    }
    if(type instanceof final RecordType rt) {
      for(final byte[] key : fields) {
        if(!rt.fields.contains(key)) return false;
      }
      for(final byte[] key : rt.fields) {
        final RecordField rtf = rt.fields.get(key);
        if(fields.contains(key)) {
          final RecordField f = fields.get(key);
          if(!rtf.optional && f.optional) return false;
          final SeqType fst = f.seqType(), rtfst = rtf.seqType();
          if(fst != rtfst) {
            if(fst.zero()) {
              if(rtfst.oneOrMore()) return false;
            } else {
              if(!fst.occ.instanceOf(rtfst.occ)) return false;
              final Type ft = fst.type, rtft = rtfst.type;
              if(ft instanceof final RecordType rt1 && rtft instanceof final RecordType rt2) {
                final Pair pair = new Pair(rt1, rt2);
                if(!pairs.contains(pair) && !rt1.instanceOf(rt2, pair.addTo(pairs)))
                  return false;
              } else if(!ft.instanceOf(rtft)) {
                return false;
              }
            }
          }
        } else if(!rtf.optional) {
          return false;
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
      final TokenObjectMap<RecordField> map = new TokenObjectMap<>();
      for(final byte[] key : fields) {
        final RecordField f = fields.get(key);
        if(rt.fields.contains(key)) {
          // common field
          final RecordField rtf = rt.fields.get(key);
          final SeqType fst = f.seqType(), rtfst  = rtf.seqType();
          final Type ft = fst.type, rtft = rtfst.type;
          final SeqType union;
          if(ft instanceof final RecordType rt1 && rtft instanceof final RecordType rt2 &&
              !fst.zero() && !rtfst.zero()) {
            final Pair pair = new Pair(rt1, rt2);
            if(pairs.contains(pair)) return Types.MAP;
            union = SeqType.get(rt1.union(rt2, pair.addTo(pairs)), fst.occ.union(rtfst.occ));
          } else {
            union = fst.union(rtfst);
          }
          map.put(key, new RecordField(union, f.optional || rtf.optional));
        } else {
          // field missing in type
          map.put(key, new RecordField(f.seqType, true));
        }
      }
      for(final byte[] key : rt.fields) {
        if(!fields.contains(key)) {
          // field missing in this RecordType
          map.put(key, new RecordField(rt.fields.get(key).seqType, true));
        }
      }
      return new RecordType(map);
    }
    return type instanceof final MapType mt ? mt.union(keyType(), valueType()) :
           type instanceof ArrayType ? Types.FUNCTION :
           type instanceof FuncType ? type.union(this) : BasicType.ITEM;
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
      final TokenObjectMap<RecordField> map = new TokenObjectMap<>();
      for(final byte[] key : fields) {
        final RecordField f = fields.get(key);
        if(rt.fields.contains(key)) {
          // common field
          final RecordField rtf = rt.fields.get(key);
          final SeqType fst = f.seqType(), rtfst  = rtf.seqType();
          final SeqType is = intersect(fst, rtfst, pairs);
          if(is == null) return null;
          map.put(key, new RecordField(is, f.optional && rtf.optional));
        } else {
          // field missing in type
          return null;
        }
      }
      for(final byte[] key : rt.fields) {
        if(!fields.contains(key)) {
          // field missing in this RecordType
          return null;
        }
      }
      return new RecordType(map);
    }
    if(type instanceof final MapType mt) {
      if(mt.keyType().intersect(BasicType.STRING) == null) return null;
      final TokenObjectMap<RecordField> map = new TokenObjectMap<>();
      for(final byte[] key : fields) {
        final RecordField f = fields.get(key);
        final SeqType is = intersect(f.seqType(), mt.valueType(), pairs);
        if(is == null) return null;
        map.put(key, new RecordField(is, f.optional));
      }
      return new RecordType(map);
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
    final Type t1 = st1.type, t2 = st2.type;
    if(t1 instanceof final RecordType rt1 && t2 instanceof final RecordType rt2) {
      final Pair pair = new Pair(rt1, rt2);
      if(pairs.contains(pair)) return null;
      final Type it = rt1.intersect(rt2, pair.addTo(pairs));
      return it == null ? null : SeqType.get(it, st1.occ.intersect(st2.occ));
    }
    return st1.intersect(st2);
  }

  /**
   * Creates a new compile-time instance of the record type.
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
  public String toString() {
    if(name != null) return Token.string(name.prefixString());

    final QueryString qs = new QueryString().token(RECORD).token('(');
    final TokenBuilder tb = new TokenBuilder();
    if(this != Types.RECORD) {
      for(final byte[] key : fields) {
        if(!tb.isEmpty()) tb.add(", ");
        if(!tb.moreInfo()) break;
        tb.add(XMLToken.isNCName(key) ? key : QueryString.toQuoted(key));
        if(fields.get(key).optional) tb.add('?');
      }
    }
    return qs.token(tb.finish()).token(')').toString();
  }

  /**
   * Resolve named record references with named record declarations. Throw "unknown type" when a
   * reference cannot be resolved.
   * @param recordTypeRefs record type references
   * @param declaredRecordTypes record type declarations
   * @throws QueryException query exception
   */
  public static void resolveRefs(final QNmMap<RecordType> recordTypeRefs,
      final QNmMap<RecordType> declaredRecordTypes) throws QueryException {
    for(final QNm name : recordTypeRefs) {
      final RecordType ref = recordTypeRefs.get(name);
      final RecordType dec = ref.getDeclaration(declaredRecordTypes);
      ref.fields = dec.fields;
      ref.info = null;
      ref.finalizeTypes(dec.keyType(), dec.valueType());
    }
  }

  /**
   * Returns the declaration of this record type. If this is an unresolved instance, the declaration
   * is expected to be present in the (already) declared named record types. Otherwise, this
   * instance is returned.
   * @param declaredRecordTypes the (already) declared named record types
   * @return the declared record type
   * @throws QueryException an "unknown type" error, if this is a reference that cannot be resolved.
   */
  public RecordType getDeclaration(final QNmMap<RecordType> declaredRecordTypes)
      throws QueryException {
    if(info == null) return this;
    RecordType rt = declaredRecordTypes.get(name);
    if(rt == null) rt = Records.BUILT_IN.get(name);
    if(rt == null) throw TYPEUNKNOWN_X.get(info, BasicType.similar(name));
    return rt;
  }

  /**
   * Returns constructor parameter types, one parameter per record field, in declaration order.
   * @return parameter types
   */
  public SeqType[] constructorParams() {
    final int size = fields.size();
    final SeqType[] params = new SeqType[size];
    for(int i = 0; i < size; ++i) params[i] = fields.value(i + 1).seqType();
    return params;
  }

  /**
   * Returns the signature string of the constructor function, e.g. {@code name(a,b[,c])}, for use
   * when registering it.
   * @return signature string
   */
  public String constructorDesc() {
    if(name == null) throw Util.notExpected("only named record types have a constructor function");
    final TokenBuilder tb = new TokenBuilder(name.local()).add('(');
    final int max = fields.size();
    final int min = minFields();
    for(int i = 1; i <= max; ++i) {
      if(i == min + 1) tb.add('[');
      if(i > 1) tb.add(',');
      tb.add(fields.key(i));
    }
    if(max > min) tb.add(']');
    return tb.add(')').toString();
  }

  /**
   * Returns a constructor function for this record type.
   * @return constructor function
   */
  public Supplier<? extends StandardFunc> constructor() {
    return () -> new StandardFunc() {
      @Override
      public Expr opt(final CompileContext cc) throws QueryException {
        return values(true, cc) ? cc.preEval(this) : this;
      }

      @Override
      public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
        final boolean opt = hasOptional();
        final int fs = fields.size();
        final Value[] values = opt ? null : new Value[fs];
        final MapBuilder mb = opt ? new MapBuilder() : null;
        final int el = exprs.length;
        for(int i = 1; i <= fs; ++i) {
          final RecordField rf = fields.value(i);
          final Value value;
          if(i <= el) {
            value = arg(i - 1).value(qc);
            final SeqType st = rf.seqType();
            if(!st.instance(value)) throw typeError(value, st, ii);
          } else {
            final Expr expr = rf.expr();
            value = expr == null ? null : expr.value(qc);
          }
          if(value != null) {
            if (opt) mb.put(fields.key(i), value);
            else values[i - 1] = value;
          }
        }
        if(!opt) return new XQRecordMap(values, RecordType.this);
        final XQMap map = mb.map();
        map.type = RecordType.this;
        return map;
      }

      @Override
      public long structSize() {
        return hasOptional() ? -1 : exprs.length;
      }
    };
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
