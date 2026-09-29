package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Record: a shape that is declared in a query and carries a runtime type annotation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordType extends ShapeType {
  /** Record name (can be {@code null}). */
  private final QNm name;
  /** Annotations. */
  private final AnnList anns;
  /** Identity of a nominative record type, shared by its copies (can be {@code null}). */
  private final Object identity;
  /** Shape without the record annotation (can be {@code null}). */
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
    this(fields, name, anns, name != null ? new Object() : null);
  }

  /**
   * Constructor for a copy of a record.
   * @param fields field declarations
   * @param name record name (can be {@code null})
   * @param anns annotations
   * @param identity identity of the nominative record type (can be {@code null})
   */
  private RecordType(final TokenObjectMap<ShapeField> fields, final QNm name, final AnnList anns,
      final Object identity) {
    super(fields);
    this.name = name;
    this.anns = anns;
    this.identity = identity;
  }

  @Override
  boolean declared() {
    return true;
  }

  @Override
  Object identity() {
    return identity;
  }

  @Override
  public RecordType with(final TokenObjectMap<ShapeField> map) {
    return new RecordType(map, name, anns, identity);
  }

  @Override
  public boolean strict() {
    return !any();
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
    return detached() ? this : new RecordType(detachedFields(), name, anns, identity);
  }

  @Override
  public QNm name() {
    return name;
  }

  @Override
  public String toString() {
    if(name != null) return Token.string(name.prefixString());
    return new QueryString().token(RECORD).token('(').
        token(any() ? "*" : fieldNames()).token(')').toString();
  }
}
