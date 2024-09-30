package org.basex.query.value.type;

import static java.util.Collections.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Set;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.var.*;
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
  private boolean extensible;
  /** Field declarations. */
  private TokenObjMap<Field> fields;
  /** Record type name (can be {@code null}). */
  public final QNm recordName;
  /** Input info (can be {@code null}). */
  private InputInfo info;

  /**
   * Constructor for RecordType declarations and literal RecordType instances.
   * @param extensible extensible flag
   * @param fields field declarations
   * @param name record type name (can be {@code null})
   */
  public RecordType(final boolean extensible, final TokenObjMap<Field> fields, final QNm name) {
    super(extensible ? AtomType.ANY_ATOMIC_TYPE : AtomType.STRING,
        extensible ? SeqType.ITEM_ZM : unionType(fields));
    this.extensible = extensible;
    this.fields = fields;
    this.recordName = name;
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
    this.recordName = name;
    this.info = info;
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
    if(extensible != rt.extensible || fields.size() != rt.fields.size())
      return false;
    for(final byte[] name : fields) {
      if(!rt.fields.contains(name)) return false;
      final var f = fields.get(name);
      final var rtf = rt.fields.get(name);
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
      if(!rt.extensible) {
        if(extensible) return false;
        for(final byte[] name : fields) {
          if(!rt.fields.contains(name)) return false;
        }
      }
      for(final byte[] name : rt.fields) {
        final Field rtf = rt.fields.get(name);
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
        if(rt.fields.contains(name)) {
          // common field
          final Field rtf = rt.fields.get(name);
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
      for(final byte[] name : rt.fields) {
        if(!fields.contains(name)) {
          // field missing in this RecordType
          fld.put(name, new Field(true, rt.fields.get(name).seqType));
        }
      }
      return new RecordType(extensible || rt.extensible, fld, null);
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
      if(rt.fields.contains(name)) {
        // common field
        final Field rtf = rt.fields.get(name);
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
        if(!rt.extensible) return null;
        fld.put(name, new Field(false, f.seqType));
      }
    }
    for(final byte[] name : rt.fields) {
      if(!fields.contains(name)) {
        // field missing in this RecordType
        if(!extensible) return null;
        fld.put(name, new Field(false, rt.fields.get(name).seqType));
      }
    }
    return new RecordType(extensible && rt.extensible, fld, null);
  }

  @Override
  public ID id() {
    return ID.REC;
  }

  @Override
  public String toString() {
    if(recordName != null) return recordName.toString();
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
    if(extensible) qs.token(',').token(' ').token('*');
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
      final RecordType rt = declaredRecordTypes.get(name);
      if(rt == null) throw QueryError.TYPEUNKNOWN_X.get(ref.info, AtomType.similar(name));
      ref.extensible = rt.extensible;
      ref.fields = rt.fields;
      ref.info = null;
      ref.keyType = rt.keyType;
      ref.valueType = rt.valueType;
    }
  }

  /**
   * Named record type constructor function.
   */
  public static class RecordConstructor extends Arr {
    /** Record type. */
    private RecordType recordType;

    /**
     * Constructor.
     * @param recordType record type
     * @param info input info (can be {@code null})
     * @param args function arguments
     */
    public RecordConstructor(final InputInfo info, final RecordType recordType, final Expr[] args) {
      super(info, recordType.seqType(), args);
      this.recordType = recordType;
    }

    @Override
    public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
      final MapBuilder mb = new MapBuilder();
      final Expr[] args = args();
      int i = 0;
      for(byte[] key : recordType.fields) {
        final Value value = args[i++].value(qc);
        final boolean omit;
        if(value.isEmpty()) {
          final Field field = recordType.getField(key);
          omit = field.isOptional() && field.initExpr == null;
        } else {
          omit = false;
        }
        if(!omit) mb.put(key, value);
      }
      final XQMap map = mb.map();
      if(!recordType.isExtensible()) return map;
      final XQMap options = toMap(arg(i), qc);
      return map.addAll(options, MergeDuplicates.USE_FIRST, qc, ii);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
      return copyType(new RecordConstructor(info, recordType, copyAll(cc, vm, args())));
    }

    @Override
    public void toString(final QueryString qs) {
      qs.token(recordType.recordName).params(exprs);
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
    private final Expr initExpr;

    /**
     * Constructor.
     * @param optional optional flag
     * @param seqType field type (can be {@code null})
     * @param initExpr initializing expression (can be {@code null})
     */
    public Field(final boolean optional, final SeqType seqType, final Expr initExpr) {
      this.optional = optional;
      this.seqType = seqType;
      this.initExpr = initExpr;
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
     * Returns the initializing expression.
     * @return initializing expression (can be {@code null})
     */
    public Expr getInitExpr() {
      return initExpr;
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
}