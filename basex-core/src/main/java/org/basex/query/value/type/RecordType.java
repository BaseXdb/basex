package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Record type: a shape that is declared in a query and assigned to its records as type annotation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordType extends ShapeType {
  /** No initializing expressions. */
  private static final Expr[] NO_INITS = {};

  /** Record name (can be {@code null}). */
  private final QNm name;
  /** Annotations. */
  private final AnnList anns;
  /** Shape without the type annotation (can be {@code null}). */
  private ShapeType shape;
  /** Initializing expressions of this and all nested records (can be {@code null}). */
  private volatile Expr[] inits;

  /**
   * Constructor for a structural record type.
   * @param fields field declarations
   */
  public RecordType(final TokenObjectMap<ShapeField> fields) {
    this(fields, null, AnnList.EMPTY);
  }

  /**
   * Constructor for a nominative record type.
   * @param fields field declarations
   * @param name record name (can be {@code null})
   * @param anns annotations
   */
  public RecordType(final TokenObjectMap<ShapeField> fields, final QNm name, final AnnList anns) {
    super(fields);
    this.name = name;
    this.anns = anns;
  }

  @Override
  boolean declared() {
    return true;
  }

  @Override
  public boolean coercive() {
    return strict();
  }

  @Override
  public boolean wraps() {
    // the fields of records are coerced when the records are constructed
    return false;
  }

  @Override
  public RecordType with(final TokenObjectMap<ShapeField> map) {
    return new RecordType(map, name, anns);
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
    inits = null;
    return super.add(fieldName, seqType);
  }

  @Override
  public ShapeType detach() {
    return detached() ? this : new RecordType(detachedFields(), name, anns);
  }

  @Override
  public QNm name() {
    return name;
  }

  /**
   * Returns the initializing expressions of record fields that may be evaluated when values are
   * coerced or cast to the specified type.
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

  /** Collector of the initializing expressions of record fields that are reachable from a type. */
  private static final class Collector {
    /** Initializing expressions (can be {@code null}). */
    private ExprList list;
    /** Visited shapes (can be {@code null}). */
    private Set<ShapeType> visited;
    /** Indicates if no unresolved type reference was found. */
    private boolean complete = true;

    /**
     * Collects the initializing expressions of record fields that are reachable from a type.
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
            for(final ShapeField rf : sh.fields().values()) {
              if(rf.init() != null) {
                if(list == null) list = new ExprList();
                list.add(rf.init());
              }
              collect(rf.seqType().type);
            }
          }
        }
        case final MapType mt -> collect(mt.valueType().type);
        case final ArrayType at -> collect(at.valueType().type);
        case final FuncType ft -> {
          if(ft.declType != null) collect(ft.declType.type);
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
      param.token(XMLToken.isNCName(key) ? key : QueryString.toQuoted(key));
      if(!field.seqType().eq(Types.ITEM_ZM)) param.token(AS).token(field.seqType());
      if(field.init() != null) param.token(":=").token(field.init());
      params[f++] = param;
    }
    return qs.token(DECLARE).token(anns).token(RECORD).token(name.prefixId()).params(params).
        token(';');
  }

  @Override
  public String toString() {
    if(name != null) return Token.string(name.prefixString());
    return new QueryString().token(RECORD).token('(').
        token(any() ? "*" : fieldNames()).token(')').toString();
  }
}
