package org.basex.query.value.type;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Labeled record type: a shape that is assigned to its records as type annotation (label).
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordType extends ShapeType {
  /** No initializing expressions. */
  private static final Expr[] NO_INITS = {};
  /** No static fields. */
  private static final int[] NO_SLOTS = {};

  /** Record name ({@code null} for {@code record(*)}). */
  private final QNm name;
  /** Annotations. */
  private final AnnList anns;
  /** Identity of the record declaration (can be {@code null}). */
  private final Object identity;
  /** Integrity constraint: reference to a focus function (can be {@code null}). */
  private final Expr constraint;
  /** Shape without the type annotation (can be {@code null}). */
  private ShapeType shape;
  /** Type without initializing expressions and integrity constraint (can be {@code null}). */
  private RecordType detachedType;
  /** Initializers and constraints of this and all nested records (can be {@code null}). */
  private volatile Expr[] inits;
  /** Storage slots of the fields (can be {@code null}). */
  private volatile int[] slots;

  /**
   * Constructor for the abstract record type {@code record(*)}.
   */
  RecordType() {
    super(new TokenObjectMap<>(0));
    name = null;
    anns = AnnList.EMPTY;
    identity = null;
    constraint = null;
  }

  /**
   * Constructor for a labeled record type.
   * @param fields field declarations
   * @param name record name
   * @param anns annotations
   * @param identity identity of the record declaration (can be {@code null})
   * @param constraint integrity constraint (can be {@code null})
   */
  public RecordType(final TokenObjectMap<ShapeField> fields, final QNm name, final AnnList anns,
      final Object identity, final Expr constraint) {
    super(fields);
    this.name = name;
    this.anns = anns;
    this.identity = identity;
    this.constraint = constraint;
  }

  /**
   * Indicates if this type has an integrity constraint.
   * @return result of check
   */
  public boolean constrained() {
    return constraint != null;
  }

  @Override
  public boolean detached() {
    return constraint == null && super.detached();
  }

  @Override
  public XQMap build(final Value[] values, final boolean error, final QueryContext qc,
      final InputInfo ii) throws QueryException {
    // the labeled candidate record is the focus of the integrity constraint
    final XQMap record = XQRecordMap.get(this, values, qc);
    if(constraint == null ||
        ((FItem) constraint.item(qc, ii)).invoke(qc, ii, record).ebv(qc, ii)) return record;
    if(error) throw RECORDCONSTRAINT_X_X.get(ii, this, record);
    return null;
  }

  @Override
  boolean labeled() {
    return true;
  }

  @Override
  public boolean matches(final XQMap map, final boolean coerce) {
    // the map must be a record with a matching label
    return map.type instanceof final RecordType rt && SeqType.kept(rt, this, coerce);
  }

  @Override
  public Coercion coercion() {
    // the fields of records are coerced when the records are constructed
    return any() ? Coercion.KEEP : Coercion.REBUILD;
  }

  @Override
  public boolean global() {
    return false;
  }

  @Override
  public RecordType with(final TokenObjectMap<ShapeField> map) {
    return new RecordType(map, name, anns, identity, constraint);
  }

  /**
   * Returns the annotations of this record.
   * @return annotations
   */
  public AnnList anns() {
    return anns;
  }

  @Override
  public ShapeType shape() {
    // the field set of record(*) is unknown: there is no shape to reduce it to
    if(any()) return this;
    if(shape == null) shape = new ShapeType(fields());
    return shape;
  }

  @Override
  public ShapeType add(final String fieldName, final SeqType seqType) {
    shape = null;
    detachedType = null;
    inits = null;
    slots = null;
    return super.add(fieldName, seqType);
  }

  /**
   * Returns the storage slots of the fields: an instance slot, or {@code -1} for static fields.
   * @return slots, or {@code null} if the record has no static fields
   */
  public int[] slots() {
    int[] s = slots;
    if(s == null) {
      final TokenObjectMap<ShapeField> fields = fields();
      final int fs = fields.size();
      s = new int[fs];
      int i = 0;
      for(int f = 0; f < fs; f++) s[f] = fields.value(f + 1).isStatic() ? -1 : i++;
      if(i == fs) s = NO_SLOTS;
      slots = s;
    }
    return s == NO_SLOTS ? null : s;
  }

  @Override
  public ShapeType detach() {
    if(detached()) return this;
    if(detachedType == null) {
      detachedType = new RecordType(detachedFields(), name, anns, identity, null);
    }
    return detachedType;
  }

  @Override
  public QNm name() {
    return name;
  }

  @Override
  Object identity() {
    return identity;
  }

  /**
   * Returns the initializing expressions and integrity constraints of records that may be evaluated
   * when values are coerced or cast to the specified type.
   * @param type type
   * @return initializing expressions
   */
  public static Expr[] inits(final Type type) {
    final RecordType rt = type instanceof final RecordType r ? r : null;
    if(rt != null && rt.inits != null) return rt.inits;
    if(!(type instanceof FType || type instanceof ChoiceItemType || type instanceof TypeRef)) {
      return NO_INITS;
    }
    final Collector collector = new Collector();
    collector.collect(type);
    final Expr[] in = collector.list != null ? collector.list.finish() : NO_INITS;
    // results with unresolved type references are not cached
    if(rt != null && collector.complete) rt.inits = in;
    return in;
  }

  /** Collector of the initializers and constraints of records that are reachable from a type. */
  private static final class Collector {
    /** Initializing expressions (can be {@code null}). */
    private ExprList list;
    /** Visited shapes (can be {@code null}). */
    private Set<ShapeType> visited;
    /** Indicates if no unresolved type reference was found. */
    private boolean complete = true;

    /**
     * Collects the initializers and constraints of records that are reachable from a type.
     * @param type type
     */
    private void collect(final Type type) {
      switch(type) {
        case final TypeRef ref -> {
          if(TypeRef.incomplete(ref)) complete = false;
          else collect(ref.deref());
        }
        case final ShapeType sh -> {
          if(visited == null) visited = Collections.newSetFromMap(new IdentityHashMap<>());
          if(visited.add(sh)) {
            if(sh instanceof final RecordType rt && rt.constraint != null) add(rt.constraint);
            for(final ShapeField rf : sh.fields().values()) {
              if(rf.init() != null) add(rf.init());
              collect(rf.seqType().type);
            }
          }
        }
        case final MapType mt -> collect(mt.valueType().type);
        case final ArrayType at -> collect(at.valueType().type);
        case final FuncType ft -> {
          collect(ft.declType.type);
          if(ft.argTypes != null) {
            for(final SeqType st : ft.argTypes) collect(st.type);
          }
        }
        case final ChoiceItemType ct -> {
          for(final Type tp : ct.types) collect(tp);
        }
        default -> { }
      }
    }

    /**
     * Adds an expression.
     * @param expr expression
     */
    private void add(final Expr expr) {
      if(list == null) list = new ExprList();
      list.add(expr);
    }
  }

  /**
   * Adds the declaration of this record to a query string.
   * @param qs query string
   * @return query string
   */
  public QueryString declaration(final QueryString qs) {
    final TokenObjectMap<ShapeField> fields = fields();
    final Object[] params = new Object[fields.size()];
    int f = 0;
    for(final byte[] key : fields) {
      final ShapeField field = fields.get(key);
      final QueryString param = new QueryString();
      if(field.isStatic()) param.token("%static");
      param.token(key);
      if(!field.seqType().eq(Types.ITEM_ZM)) param.token(AS).token(field.seqType());
      if(field.init() != null) param.token(":=").token(field.init());
      params[f++] = param;
    }
    qs.token(DECLARE).token(anns).token(RECORD).token(name.prefixId()).params(params);
    if(constraint != null) qs.token(WHERE).token(constraint);
    return qs.token(';');
  }

  @Override
  public String toString() {
    return name != null ? Token.string(name.prefixString()) : super.toString();
  }
}
