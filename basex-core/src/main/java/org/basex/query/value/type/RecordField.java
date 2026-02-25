package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.seq.*;

/**
 * Record field definition.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class RecordField {
  /** Optional flag. */
  private final boolean optional;
  /** Field type. */
  private final SeqType seqType;
  /** Indicates whether a constructor will always add this field to the resulting record. */
  private final boolean alwaysAdded;
  /** Initializing expression (can be {@code null}). */
  private final Expr init;

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
   * @param init initializing expression (can be {@code null})
   */
  public RecordField(final SeqType seqType, final boolean optional, final Expr init) {
    this.seqType = seqType == null ? Types.ITEM_ZM : seqType;
    this.optional = optional;
    this.alwaysAdded = !optional || init != null;
    this.init = alwaysAdded ? init : Empty.VALUE;
  }

  /**
   * Indicates if this field is optional.
   * @return result of check
   */
  public boolean isOptional() {
    return optional;
  }

  /**
   * Indicates whether a constructor will always add this field to the resulting record.
   * @return result of check
   */
  public boolean alwaysAdded() {
    return alwaysAdded;
  }

  /**
   * Returns the initializing expression.
   * @return initializing expression (can be {@code null})
   */
  public Expr init() {
    return init;
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
        seqType.eq(rf.seqType) && Objects.equals(init, rf.init);
  }

  @Override
  public String toString() {
    final QueryString qs = new QueryString();
    if(optional) qs.token('?');
    qs.token(AS).token(seqType());
    return qs.toString();
  }
}