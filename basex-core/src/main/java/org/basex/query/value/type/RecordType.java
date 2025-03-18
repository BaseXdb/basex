package org.basex.query.value.type;

import static java.util.Collections.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Set;

import org.basex.query.*;
import org.basex.query.util.hash.*;
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
public final class RecordType extends MapType implements Iterable<byte[]> {
  /** Extensible flag. */
  private boolean extensible;
  /** Record fields. */
  private TokenObjMap<RecordField> fields;
  /** Record type name (can be {@code null}). */
  private final QNm name;
  /** Input info ({@code null}, if this is not an unresolved reference). */
  private InputInfo info;

  /**
   * Constructor for RecordType declarations and literal RecordType instances.
   * @param extensible extensible flag
   * @param fields field declarations
   * @param name record type name (can be {@code null})
   */
  public RecordType(final boolean extensible, final TokenObjMap<RecordField> fields,
      final QNm name) {
    super(extensible ? AtomType.ANY_ATOMIC_TYPE : AtomType.STRING,
        extensible ? SeqType.ITEM_ZM : unionType(fields));
    this.extensible = extensible;
    this.fields = fields;
    this.name = name;
    this.info = null;
  }

  /**
   * Constructor for RecordType references.
   * @param name record type name
   * @param info input info
   */
  public RecordType(final QNm name, final InputInfo info) {
    super(AtomType.ANY_ATOMIC_TYPE, SeqType.ITEM_ZM);
    this.extensible = true;
    this.fields = new TokenObjMap<>();
    this.name = name;
    this.info = info;
  }

  /**
   * Calculate union type of field sequence types.
   * @param recordFields field declarations
   * @return union type
   */
  private static SeqType unionType(final TokenObjMap<RecordField> recordFields) {
    if(recordFields.isEmpty()) return SeqType.ITEM_ZM;
    SeqType unionType = null;
    for(final RecordField recordField : recordFields.values()) {
      final SeqType st = recordField.seqType();
      unionType = unionType == null ? st : unionType.union(st);
    }
    return unionType;
  }

  @Override
  public Iterator<byte[]> iterator() {
    return fields.iterator();
  }

  /**
   * Indicates if this record type is extensible.
   * @return result of check
   */
  public boolean isExtensible() {
    return extensible;
  }

  /**
   * Returns the field with the specified key.
   * @param key key to be looked up
   * @return field, or {@code null} if nothing was found
   */
  public RecordField field(final byte[] key) {
    return fields.get(key);
  }

  /**
   * Returns the name of the record.
   * @return name (can be {@code null})
   */
  public QNm name() {
    return name;
  }

  /**
   * Checks if the specified map is an instance of this record type.
   * @param map map to check
   * @return result of check
   */
  public boolean instance(final XQMap map) {
    try {
      for(final byte[] field : fields) {
        final RecordField f = fields.get(field);
        final Item key = Str.get(field);
        if(map.contains(key)) {
          if(f.seqType != null && !f.seqType.instance(map.get(key))) return false;
        } else if(!f.optional) {
          return false;
        }
      }
      if(!extensible) {
        for(final Item key : map.keys()) {
          if(!key.instanceOf(AtomType.STRING) || !fields.contains(key.string(null))) return false;
        }
      }
      return true;
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }


  @Override
  public boolean eq(final Type type) {
    return eq(type, emptySet());
  }

  /**
   * Checks if this type is equal to the given one. This implementation uses the <code>pairs</code>
   * argument to keep track of pairs of RecordTypes being checked, in order to prevent infinite
   * recursion.
   * @param type other type
   * @param pairs pairs of RecordTypes that are currently being checked, or have been checked before
   * @return {@code true} if both types are equal, {@code false} otherwise
   */
  private boolean eq(final Type type, final Set<Pair> pairs) {
    if(this == type) return true;
    if(!(type instanceof RecordType)) return false;
    final RecordType rt = (RecordType) type;
    if(extensible != rt.extensible || fields.size() != rt.fields.size()) return false;

    for(final byte[] field : fields) {
      if(!rt.fields.contains(field)) return false;
      final var f = fields.get(field);
      final var rtf = rt.fields.get(field);
      if(f.optional != rtf.optional) return false;
      final SeqType fst = f.seqType();
      final SeqType rtfst = rtf.seqType();
      if(fst.occ != rtfst.occ) return false;
      final Type ft = fst.type, rtft = rtfst.type;
      if(ft instanceof RecordType && rtft instanceof RecordType) {
        final Pair pair = new Pair(ft, rtft);
        if(!pairs.contains(pair) && !((RecordType) ft).eq(rtft, pair.addTo(pairs))) return false;
      } else if(!ft.eq(rtft)) {
        return false;
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
    if(this == type || type.oneOf(SeqType.RECORD, SeqType.MAP, SeqType.FUNCTION, AtomType.ITEM)) {
      return true;
    }
    if(type instanceof ChoiceItemType) {
      for(final SeqType st : ((ChoiceItemType) type).types) {
        if(instanceOf(st.type, pairs)) return true;
      }
      return false;
    }
    if(type instanceof RecordType) {
      final RecordType rt = (RecordType) type;
      if(!rt.extensible) {
        if(extensible) return false;
        for(final byte[] field : fields) {
          if(!rt.fields.contains(field)) return false;
        }
      }
      for(final byte[] field : rt.fields) {
        final RecordField rtf = rt.fields.get(field);
        if(fields.contains(field)) {
          final RecordField f = fields.get(field);
          if(!rtf.optional && f.optional) return false;
          final SeqType fst = f.seqType(), rtfst = rtf.seqType();
          if(fst != rtfst) {
            if(fst.zero()) {
              if(rtfst.oneOrMore()) return false;
            } else {
              if(!fst.occ.instanceOf(rtfst.occ)) return false;
              final Type ft = fst.type, rtft = rtfst.type;
              if(ft instanceof RecordType && rtft instanceof RecordType) {
                final Pair pair = new Pair(ft, rtft);
                if(!pairs.contains(pair) && !((RecordType) ft).instanceOf(rtft, pair.addTo(pairs)))
                  return false;
              }
              else if(!ft.instanceOf(rtft)) return false;
            }
          }
        } else if(!rtf.optional || extensible && rtf.seqType() != SeqType.ITEM_ZM) {
          return false;
        }
      }
      return true;
    }
    if(!extensible && type instanceof MapType) {
      final MapType mt = (MapType) type;
      if(!mt.keyType.oneOf(AtomType.STRING, AtomType.ANY_ATOMIC_TYPE)) return false;
      for(final byte[] field : fields) {
        if(!fields.get(field).seqType().instanceOf(mt.valueType)) return false;
      }
      return true;
    }
    if(type instanceof FuncType) {
      final FuncType ft = type.funcType();
      return funcType().declType.instanceOf(ft.declType) && ft.argTypes.length == 1 &&
          ft.argTypes[0].instanceOf(SeqType.ANY_ATOMIC_TYPE_O);
    }
    return false;
  }

  @Override
  public Type union(final Type type) {
    return union(type, emptySet());
  }

  @Override
  public MapType union(final Type kt, final SeqType vt) {
    return get(keyType.union(kt), valueType.union(vt));
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
    if(type == SeqType.MAP) return SeqType.MAP;
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof RecordType) {
      final RecordType rt = (RecordType) type;
      final TokenObjMap<RecordField> map = new TokenObjMap<>();
      for(final byte[] field : fields) {
        final RecordField f = fields.get(field);
        if(rt.fields.contains(field)) {
          // common field
          final RecordField rtf = rt.fields.get(field);
          final SeqType fst = f.seqType(), rtfst  = rtf.seqType();
          final Type ft = fst.type, rtft = rtfst.type;
          final SeqType union;
          if(ft instanceof RecordType && rtft instanceof RecordType && !fst.zero()
              && !rtfst.zero()) {
            final Pair pair = new Pair(ft, rtft);
            if(pairs.contains(pair)) return SeqType.RECORD;
            union = SeqType.get(((RecordType) ft).union(rtft, pair.addTo(pairs)),
                fst.occ.union(rtfst.occ));
          } else {
            union = fst.union(rtfst);
          }
          map.put(field, new RecordField(f.optional || rtf.optional, union));
        } else {
          // field missing in type
          map.put(field, new RecordField(true, f.seqType));
        }
      }
      for(final byte[] field : rt.fields) {
        if(!fields.contains(field)) {
          // field missing in this RecordType
          map.put(field, new RecordField(true, rt.fields.get(field).seqType));
        }
      }
      return new RecordType(extensible || rt.extensible, map, null);
    }
    return type instanceof MapType ? ((MapType) type).union(keyType, valueType) :
           type instanceof ArrayType ? SeqType.FUNCTION :
           type instanceof FuncType ? type.union(this) : AtomType.ITEM;
  }

  @Override
  public Type intersect(final Type type) {
    return intersect(type, emptySet());
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

    if(!(type instanceof RecordType)) return null;
    final RecordType rt = (RecordType) type;
    final TokenObjMap<RecordField> map = new TokenObjMap<>();
    for(final byte[] field : fields) {
      final RecordField f = fields.get(field);
      if(rt.fields.contains(field)) {
        // common field
        final RecordField rtf = rt.fields.get(field);
        final SeqType fst = f.seqType(), rtfst  = rtf.seqType();
        final Type ft = fst.type, rtft = rtfst.type;
        final SeqType is;
        if(ft instanceof RecordType && rtft instanceof RecordType) {
          final Pair pair = new Pair(ft, rtft);
          if(pairs.contains(pair))
            return null;
          final Type it = ((RecordType) ft).intersect(rtft, pair.addTo(pairs));
          is = it == null ? null : SeqType.get(it, fst.occ.intersect(rtfst.occ));
        } else {
          is = fst.intersect(rtfst);
        }
        if(is == null) return null;
        map.put(field, new RecordField(f.optional && rtf.optional, is));
      } else {
        // field missing in type
        if(!rt.extensible) return null;
        map.put(field, new RecordField(false, f.seqType));
      }
    }
    for(final byte[] field : rt.fields) {
      if(!fields.contains(field)) {
        // field missing in this RecordType
        if(!extensible) return null;
        map.put(field, new RecordField(false, rt.fields.get(field).seqType));
      }
    }
    return new RecordType(extensible && rt.extensible, map, null);
  }

  @Override
  public ID id() {
    return ID.REC;
  }

  @Override
  public String toString() {
    if(name != null) return Token.string(name.prefixString());
    final QueryString qs = new QueryString().token(RECORD).token('(');
    if(this == SeqType.RECORD) {
      qs.token('*');
    } else {
      int i = 0;
      for(final byte[] field : fields) {
        if(i++ != 0) qs.token(',').token(' ');
        if(XMLToken.isNCName(field)) qs.token(field); else qs.quoted(field);
        final RecordField f = fields.get(field);
        if(f.optional) qs.token('?');
        if(f.seqType != null) qs.token(AS).token(f.seqType);
      }
      if(extensible) qs.token(',').token(' ').token('*');
    }
    return qs.token(')').toString();
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
      ref.extensible = dec.extensible;
      ref.fields = dec.fields;
      ref.info = null;
      ref.keyType = dec.keyType;
      ref.valueType = dec.valueType;
    }
  }

  /**
   * Returns the declaration of this record type. If this is an unresolved instance, the declaration
   * is expected to be present in the (already) declared named record types. Otherwise this instance
   * is returned.
   * @param declaredRecordTypes the (already) declared named record types
   * @return the declared record type
   * @throws QueryException an "unknown type" error, if this is a reference that cannot be resolved.
   */
  public RecordType getDeclaration(final QNmMap<RecordType> declaredRecordTypes)
      throws QueryException {
    if(info == null) return this;
    final RecordType rt = declaredRecordTypes.get(name);
    if(rt == null) throw TYPEUNKNOWN_X.get(info, AtomType.similar(name));
    return rt;
  }

  /**
   * An ordered pair of objects.
   */
  private static final class Pair {
    /** First object. */
    private final Object o1;
    /** Second object. */
    private final Object o2;

    /**
     * Constructor.
     * @param o1 first object
     * @param o2 second object
     */
    Pair(final Object o1, final Object o2) {
      this.o1 = o1;
      this.o2 = o2;
    }

    @Override
    public boolean equals(final Object o) {
      if(this == o) return true;
      if(!(o instanceof Pair)) return false;
      final Pair p = (Pair) o;
      return o1 == p.o1 && o2 == p.o2;
    }

    @Override
    public int hashCode() {
      return o1.hashCode() + o2.hashCode();
    }

    /**
     * Add this {@code Pair} to the given set of {@code Pair}s, creating a new set, if the given set
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