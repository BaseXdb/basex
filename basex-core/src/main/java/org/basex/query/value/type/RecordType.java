package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
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
  /** Record name (can be {@code null}). */
  private final QNm name;
  /** Annotations. */
  private final AnnList anns;
  /** Shape without the type annotation (can be {@code null}). */
  private ShapeType shape;

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
      final QueryString param = new QueryString().token(XMLToken.isNCName(key) ? key :
        QueryString.toQuoted(key));
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
