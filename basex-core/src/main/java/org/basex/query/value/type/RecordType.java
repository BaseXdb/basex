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
  /** No fixed fields. */
  private static final int[] NO_SLOTS = {};

  /** Record name ({@code null} for {@code record(*)}). */
  private final QNm name;
  /** Annotations. */
  private final AnnList anns;
  /** Identity of the record declaration (can be {@code null}). */
  private final Object identity;
  /** Integrity constraint: reference to a focus function (can be {@code null}). */
  private final Expr constraint;
  /** Base record type (can be {@code null}). */
  private final RecordType base;
  /** Integrity constraints of this and all base types. */
  private final Expr[] constraints;
  /** Shape without the type annotation (can be {@code null}). */
  private ShapeType shape;
  /** Type without initializing expressions and integrity constraints (can be {@code null}). */
  private volatile RecordType detachedType;
  /** Initializers and constraints of this and all nested records (can be {@code null}). */
  private volatile Expr[] inits;
  /** Storage slots of the fields (can be {@code null}). */
  private volatile int[] slots;

  /**
   * Constructor for the abstract record type {@code record(*)}.
   */
  RecordType() {
    this(new TokenObjectMap<>(0), null, AnnList.EMPTY, null, null, null);
  }

  /**
   * Constructor for a labeled record type.
   * @param fields field declarations
   * @param name record name
   * @param anns annotations
   * @param identity identity of the record declaration (can be {@code null})
   * @param constraint integrity constraint (can be {@code null})
   * @param base base record type (can be {@code null})
   */
  public RecordType(final TokenObjectMap<ShapeField> fields, final QNm name, final AnnList anns,
      final Object identity, final Expr constraint, final RecordType base) {
    // records of derived types may have additional fields of any type
    super(fields, Types.ITEM_ZM);
    this.name = name;
    this.anns = anns;
    this.identity = identity;
    this.constraint = constraint;
    this.base = base;
    final Expr[] ec = base != null ? base.constraints : NO_INITS;
    constraints = constraint != null ? Array.add(ec, constraint) : ec;
  }

  /**
   * Indicates if this type or a type it extends has an integrity constraint.
   * @return result of check
   */
  public boolean constrained() {
    return constraints.length != 0;
  }

  @Override
  public boolean detached() {
    return !constrained() && super.detached();
  }

  @Override
  public XQMap build(final Value[] values, final boolean error, final QueryContext qc,
      final InputInfo ii) throws QueryException {
    // the labeled candidate record is the focus of the integrity constraints of all base types
    final XQMap record = XQRecordMap.get(this, values, qc);
    for(final Expr constr : constraints) {
      if(!((FItem) constr.item(qc, ii)).invoke(qc, ii, record).ebv(qc, ii)) {
        if(error) throw RECORDCONSTRAINT_X_X.get(ii, this, record);
        return null;
      }
    }
    return record;
  }

  /**
   * Builds a record from field values that are overwritten by the coerced entries of a map.
   * @param map map
   * @param values field values (missing values are {@code null})
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @param cc compilation context ({@code null} during runtime)
   * @return record
   * @throws QueryException query exception
   */
  public XQMap build(final XQMap map, final Value[] values, final QueryContext qc,
      final InputInfo ii, final CompileContext cc) throws QueryException {
    final TokenObjectMap<ShapeField> fields = fields();
    map.forEach((key, value) -> {
      qc.checkStop();
      // undeclared keys and fixed fields are rejected
      final int f = index(key);
      if(f == 0 || fields.value(f).isFixed()) throw typeError(map, this, ii);
      values[f - 1] = coerce(f, value, qc, ii, cc);
    });
    final int fs = values.length;
    for(int f = 0; f < fs; f++) {
      if(values[f] == null) {
        final ShapeField rf = fields.value(f + 1);
        if(!rf.optional()) throw typeError(map, this, ii);
        values[f] = rf.init(qc);
      }
    }
    return build(values, true, qc, ii);
  }

  @Override
  public boolean labeled() {
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
  RecordType base() {
    return base;
  }

  /**
   * Checks if a type is a record type derived from the specified type.
   * @param type type
   * @param target target type
   * @return result of check
   */
  public static boolean derives(final Type type, final Type target) {
    return TypeRef.deref(type) instanceof final RecordType rt && rt.base != null &&
        TypeRef.deref(target) instanceof final RecordType tt && rt.base.ancestor(tt) != null;
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

  /**
   * Returns the storage slots of the fields: an instance slot, or {@code -1} for fixed fields.
   * @return slots, or {@code null} if the record has no fixed fields
   */
  public int[] slots() {
    int[] s = slots;
    if(s == null) {
      final TokenObjectMap<ShapeField> fields = fields();
      final int fs = fields.size();
      s = new int[fs];
      int i = 0;
      for(int f = 0; f < fs; f++) s[f] = fields.value(f + 1).isFixed() ? -1 : i++;
      if(i == fs) s = NO_SLOTS;
      slots = s;
    }
    return s == NO_SLOTS ? null : s;
  }

  @Override
  public RecordType detach() {
    if(detached()) return this;
    RecordType dt = detachedType;
    if(dt == null) {
      dt = new RecordType(detachedFields(), name, anns, identity, null,
        base != null ? base.detach() : null);
      detachedType = dt;
    }
    return dt;
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
            if(sh instanceof final RecordType rt) {
              for(final Expr constr : rt.constraints) add(constr);
            }
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
      if(!list.contains(expr)) list.add(expr);
    }
  }

  /**
   * Adds the declaration of this record to a query string.
   * @param qs query string
   * @return query string
   */
  public QueryString declaration(final QueryString qs) {
    // inherited fields are omitted, unless they are redeclared
    final TokenObjectMap<ShapeField> fields = fields();
    final TokenObjectMap<ShapeField> efields = base != null ? base.fields() : null;
    final ArrayList<QueryString> params = new ArrayList<>();
    for(int f = 1; f <= fields.size(); f++) {
      final ShapeField field = fields.value(f);
      if(efields != null && f <= efields.size() && field == efields.value(f)) continue;
      final QueryString param = new QueryString();
      if(field.isFixed()) param.token('%' + FIXED);
      param.token(fields.key(f));
      if(!field.seqType().eq(Types.ITEM_ZM)) param.token(AS).token(field.seqType());
      if(field.init() != null) param.token(":=").token(field.init());
      params.add(param);
    }
    qs.token(DECLARE).token(anns).token(RECORD).token(name.prefixId());
    if(base != null) qs.token(EXTENDS).token(base.name().prefixId());
    qs.params(params.toArray());
    if(constraint != null) qs.token(WHERE).token(constraint);
    return qs.token(';');
  }

  @Override
  public String toString() {
    return name != null ? Token.string(name.prefixString()) : super.toString();
  }
}
