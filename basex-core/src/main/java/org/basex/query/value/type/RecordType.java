package org.basex.query.value.type;

import static java.util.Collections.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.*;
import java.util.Set;

import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Type for record tests.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public class RecordType extends MapType implements Iterable<byte[]> {
  /** Extensible flag. */
  private final boolean extensible;
  /** Field declarations. */
  private final TokenObjMap<Field> fields;

  /**
   * Constructor.
   * @param extensible extensible flag
   * @param fields field declarations
   */
  public RecordType(final boolean extensible, final TokenObjMap<Field> fields) {
    super(extensible ? AtomType.ANY_ATOMIC_TYPE : AtomType.STRING,
        extensible ? SeqType.ITEM_ZM : unionType(fields));
    this.extensible = extensible;
    this.fields = fields;
  }

  /**
   * Calculate union type of field sequence types.
   * @param fields field declarations
   * @return union type
   */
  private static SeqType unionType(final TokenObjMap<Field> fields) {
    if(fields.isEmpty()) return SeqType.ITEM_ZM;
    SeqType unionType = null;
    for(final Field field : fields.values()) {
      final SeqType st = field.seqType();
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
   * Return map of field declarations.
   * @return fields map
   */
  protected TokenObjMap<Field> getFields() {
    return fields;
  }

  /**
   * Returns the field with the specified key.
   * @param key key to be looked up
   * @return field, or {@code null} if nothing was found
   */
  public Field getField(final byte[] key) {
    return fields.get(key);
  }

  /**
   * Checks if the specified map is an instance of this record type.
   * @param map map to check
   * @return result of check
   */
  public boolean instance(final XQMap map) {
    try {
      for(final byte[] name : fields) {
        final Field field = fields.get(name);
        final Item key = Str.get(name);
        if(map.contains(key)) {
          if(field.seqType != null && !field.seqType.instance(map.get(key))) return false;
        } else if(!field.optional) {
          return false;
        }
      }
      if(!extensible) {
        for(final Item item : map.keys()) {
          if(!item.instanceOf(AtomType.STRING) || !fields.contains(item.string(null))) return false;
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
  public boolean eq(final Type type, final Set<Pair> pairs) {
    if(this == type) return true;
    if(!(type instanceof RecordType)) return false;
    final RecordType rt = (RecordType) type;
    if(extensible != rt.isExtensible() || fields.size() != rt.getFields().size())
      return false;
    for(final byte[] name : fields) {
      if(!rt.getFields().contains(name)) return false;
      final var f = fields.get(name);
      final var rtf = rt.getFields().get(name);
      if(f.optional != rtf.optional) return false;
      final SeqType fst = f.seqType();
      final SeqType rtfst = rtf.seqType();
      if(fst.occ != rtfst.occ) return false;
      final Type ft = fst.type, rtft = rtfst.type;
      if(ft instanceof RecordType && rtft instanceof RecordType) {
        final Pair pair = new Pair(ft, rtft);
        if(!pairs.contains(pair) && !((RecordType) ft).eq(rtft, pair.addTo(pairs))) return false;
      }
      else if(!ft.eq(rtft)) return false;
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
  protected boolean instanceOf(final Type type, final Set<Pair> pairs) {
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
      if(!rt.isExtensible()) {
        if(extensible) return false;
        for(final byte[] name : fields) {
          if(!rt.getFields().contains(name)) return false;
        }
      }
      for(final byte[] name : rt.getFields()) {
        final Field rtf = rt.getFields().get(name);
        if(fields.contains(name)) {
          final Field f = fields.get(name);
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
      for(final byte[] name : fields) {
        if(!fields.get(name).seqType().instanceOf(mt.valueType)) return false;
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

  /**
   * Computes the union between this type and the given one, i.e. the least common ancestor of both
   * types in the type hierarchy. This implementation uses the <code>pairs</code> argument to keep
   * track of pairs of RecordTypes being checked, in order to prevent infinite recursion.
   * @param type other type
   * @param pairs pairs of RecordTypes that are currently being checked, or have been checked before
   * @return union type
   */
  public Type union(final Type type, final Set<Pair> pairs) {
    if(type instanceof ChoiceItemType) return type.union(this);
    if(type == SeqType.MAP) return SeqType.MAP;
    if(instanceOf(type)) return type;
    if(type.instanceOf(this)) return this;

    if(type instanceof RecordType) {
      final RecordType rt = (RecordType) type;
      final TokenObjMap<Field> fld = new TokenObjMap<>();
      for(final byte[] name : fields) {
        final Field f = fields.get(name);
        if(rt.getFields().contains(name)) {
          // common field
          final Field rtf = rt.getFields().get(name);
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
          fld.put(name, new Field(f.optional || rtf.optional, union));
        } else {
          // field missing in type
          fld.put(name, new Field(true, f.seqType));
        }
      }
      for(final byte[] name : rt.getFields()) {
        if(!fields.contains(name)) {
          // field missing in this RecordType
          fld.put(name, new Field(true, rt.getFields().get(name).seqType));
        }
      }
      return new RecordType(extensible || rt.isExtensible(), fld);
    }
    if(type instanceof MapType) {
      final MapType mt = (MapType) type;
      return get(keyType.union(mt.keyType), valueType.union(mt.valueType));
    }
    return type instanceof ArrayType ? SeqType.FUNCTION :
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
  public Type intersect(final Type type, final Set<Pair> pairs) {
    if(type instanceof ChoiceItemType) return type.intersect(this);
    if(instanceOf(type)) return this;
    if(type.instanceOf(this)) return type;

    if(!(type instanceof RecordType)) return null;
    final RecordType rt = (RecordType) type;
    final TokenObjMap<Field> fld = new TokenObjMap<>();
    for(final byte[] name : fields) {
      final Field f = fields.get(name);
      if(rt.getFields().contains(name)) {
        // common field
        final Field rtf = rt.getFields().get(name);
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
        fld.put(name, new Field(f.optional && rtf.optional, is));
      } else {
        // field missing in type
        if(!rt.isExtensible()) return null;
        fld.put(name, new Field(false, f.seqType));
      }
    }
    for(final byte[] name : rt.getFields()) {
      if(!fields.contains(name)) {
        // field missing in this RecordType
        if(!extensible) return null;
        fld.put(name, new Field(false, rt.getFields().get(name).seqType));
      }
    }
    return new RecordType(extensible && rt.isExtensible(), fld);
  }

  @Override
  public ID id() {
    return ID.REC;
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString().token(RECORD).token("(");
    if(this == SeqType.RECORD) return qs.token('*').token(')').toString();
    int i = 0;
    for(final byte[] name : fields) {
      if(i++ != 0) qs.token(',').token(' ');
      if(XMLToken.isNCName(name)) qs.token(name); else qs.quoted(name);
      final Field field = fields.get(name);
      if(field.optional) qs.token('?');
      if(field.seqType != null) qs.token(AS).token(field.seqType);
    }
    if(isExtensible()) qs.token(',').token(' ').token('*');
    return qs.token(')').toString();
  }

  /**
   * Resolve named record references with named record declarations. Throw "unknown type" when a
   * reference cannot be resolved.
   * @param recordTypeRefs record type references
   * @param declaredRecordTypes record type declarations
   * @throws QueryException query exception
   */
  public static void resolveRefs(final QNmMap<Ref> recordTypeRefs,
      final QNmMap<RecordType> declaredRecordTypes) throws QueryException {
    for(final QNm name : recordTypeRefs) {
      final Ref ref = recordTypeRefs.get(name);
      final RecordType rt = declaredRecordTypes.get(name);
      if(rt == null) throw QueryError.TYPEUNKNOWN_X.get(ref.info, AtomType.similar(name));
      ref.type = rt;
    }
  }

  /**
   * Field declaration.
   */
  public static class Field {
    /** Optional flag. */
    private final boolean optional;
    /** Field type (can be {@code null}). */
    private final SeqType seqType;
    /** Initializing expression (can be {@code null}). */
    private final Expr expr;

    /**
     * Constructor.
     * @param optional optional flag
     * @param seqType field type (can be {@code null})
     * @param expr initializing expression (can be {@code null})
     */
    public Field(final boolean optional, final SeqType seqType, final Expr expr) {
      this.optional = optional;
      this.seqType = seqType;
      this.expr = expr;
    }

    /**
     * Constructor.
     * @param optional optional flag
     * @param seqType field type (can be {@code null})
     */
    public Field(final boolean optional, final SeqType seqType) {
      this(optional, seqType, null);
    }

    /**
     * Indicates if this field is optional.
     * @return result of check
     */
    public boolean isOptional() {
      return optional;
    }

    /**
     * Get effective sequence type of this field.
     * @return sequence type
     */
    public SeqType seqType() {
      return seqType == null ? SeqType.ITEM_ZM : seqType;
    }

    @Override
    public boolean equals(final Object obj) {
      if(this == obj) return true;
      if(!(obj instanceof Field)) return false;
      final Field other = (Field) obj;
      return optional == other.optional && seqType().equals(other.seqType());
    }

    @Override
    public String toString() {
      final QueryString qs = new QueryString();
      if(optional) qs.token('?');
      qs.token(AS).token(seqType());
      return qs.toString();
    }
  }

  /**
   * An ordered pair of objects.
   */
  private static class Pair {
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
      if(o == null || getClass() != o.getClass()) return false;
      Pair p = (Pair) o;
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
        Set<Pair> set = new HashSet<>();
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

  /**
   * Reference to a named record type, that is initially unknown, and later resolved with the
   * declaration.
   */
  public static class Ref extends RecordType {
    /** Record type name. */
    private final QNm name;
    /** Input info. */
    private final InputInfo info;
    /** Record type (set to SeqType.RECORD, before it has been resolved with declaration). */
    private RecordType type;

    /**
     * Constructor.
     * @param name record type name
     * @param info input info
     */
    public Ref(final QNm name, final InputInfo info) {
      super(true, new TokenObjMap<>());
      this.name = name;
      this.info = info;
      this.type = SeqType.RECORD;
    }

    @Override
    public XQMap cast(final Item item, final QueryContext qc, final InputInfo ii)
        throws QueryException {
      return type.cast(item, qc, ii);
    }

    @Override
    public XQMap read(final DataInput in, final QueryContext qc)
        throws IOException, QueryException {
      return type.read(in, qc);
    }

    @Override
    public boolean eq(final Type tp) {
      return type.eq(tp);
    }

    @Override
    public boolean eq(final Type tp, final Set<Pair> pairs) {
      return type.eq(tp, pairs);
    }

    @Override
    public boolean instanceOf(final Type tp) {
      return type.instanceOf(tp);
    }

    @Override
    protected boolean instanceOf(final Type tp, final Set<Pair> pairs) {
      return type.instanceOf(tp, pairs);
    }

    @Override
    public Type union(final Type tp) {
      return type.union(tp);
    }

    @Override
    public Type union(final Type tp, final Set<Pair> pairs) {
      return type.union(tp, pairs);
    }

    @Override
    public Type intersect(final Type tp) {
      return type.intersect(tp);
    }

    @Override
    public Type intersect(final Type tp, final Set<Pair> pairs) {
      return type.intersect(tp, pairs);
    }

    @Override
    public AtomType atomic() {
      return type.atomic();
    }

    @Override
    public ID id() {
      return type.id();
    }

    @Override
    public Iterator<byte[]> iterator() {
      return type.iterator();
    }

    @Override
    public boolean isExtensible() {
      return type.extensible;
    }

    @Override
    protected TokenObjMap<Field> getFields() {
      return type.fields;
    }

    @Override
    public Field getField(final byte[] key) {
      return type.getField(key);
    }

    @Override
    public boolean instance(final XQMap map) {
      return type.instance(map);
    }

    @Override
    public String toString() {
      return name.toString();
    }
  }
}