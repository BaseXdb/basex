package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;

/**
 * Record field definition.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordField {
  /** Optional flag. */
  final boolean optional;
  /** Field type (can be {@code null}). */
  final SeqType seqType;
  /** Initializing expression (can be {@code null}). */
  private final Expr expr;

  /**
   * Constructor.
   * @param optional optional flag
   * @param seqType field type (can be {@code null})
   * @param expr initializing expression (can be {@code null})
   */
  public RecordField(final boolean optional, final SeqType seqType, final Expr expr) {
    this.optional = optional;
    this.seqType = seqType;
    this.expr = expr;
  }

  /**
   * Constructor.
   * @param optional optional flag
   * @param seqType field type (can be {@code null})
   */
  public RecordField(final boolean optional, final SeqType seqType) {
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
  public Expr expr() {
    return expr;
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
    if(!(obj instanceof RecordField)) return false;
    final RecordField rf = (RecordField) obj;
    return optional == rf.optional && Objects.equals(seqType, rf.seqType) &&
        Objects.equals(expr, rf.expr);
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString();
    if(optional) qs.token('?');
    qs.token(AS).token(seqType());
    return qs.toString();
  }
}