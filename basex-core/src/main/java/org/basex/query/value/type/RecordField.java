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
  /** Field type. */
  private final SeqType seqType;
  /** Initializing expression (can be {@code null}). */
  private final Expr init;

  /**
   * Constructor for a field without initializer.
   * @param seqType field type (can be {@code null})
   */
  public RecordField(final SeqType seqType) {
    this(seqType, null);
  }

  /**
   * Constructor.
   * @param seqType field type (can be {@code null})
   * @param init initializing expression (can be {@code null})
   */
  public RecordField(final SeqType seqType, final Expr init) {
    this.seqType = seqType == null ? Types.ITEM_ZM : seqType;
    this.init = init;
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
    return this == obj || obj instanceof final RecordField rf &&
        seqType.eq(rf.seqType) && Objects.equals(init, rf.init);
  }

  @Override
  public String toString() {
    return new QueryString().token(AS).token(seqType()).toString();
  }
}
