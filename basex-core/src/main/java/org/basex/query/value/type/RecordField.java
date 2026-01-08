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
   * Constructor for a mandatory field.
   * @param seqType field type (can be {@code null})
   */
  public RecordField(final SeqType seqType) {
    this(seqType, false);
  }

  /**
   * Constructor.
   * @param seqType field type (can be {@code null})
   * @param optional optional flag
   */
  public RecordField(final SeqType seqType, final boolean optional) {
    this(seqType, optional, null);
  }

  /**
   * Constructor.
   * @param seqType field type (can be {@code null})
   * @param optional optional flag
   * @param expr initializing expression (can be {@code null})
   */
  public RecordField(final SeqType seqType, final boolean optional, final Expr expr) {
    this.seqType = seqType != null ? seqType : Types.ITEM_ZM;
    this.optional = optional;
    this.expr = expr;
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
    return seqType;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final RecordField rf && optional == rf.optional &&
        seqType.eq(rf.seqType) && Objects.equals(expr, rf.expr);
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString();
    if(optional) qs.token('?');
    qs.token(AS).token(seqType());
    return qs.toString();
  }
}