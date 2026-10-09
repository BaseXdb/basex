package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Field definition of a shape.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class ShapeField {
  /** Field type. */
  private final SeqType seqType;
  /** Initializing expression (can be {@code null}). */
  private final Expr init;
  /** Static flag. */
  private final boolean statik;

  /**
   * Constructor for a field without initializer.
   * @param seqType field type (can be {@code null})
   */
  public ShapeField(final SeqType seqType) {
    this(seqType, null, false);
  }

  /**
   * Constructor.
   * @param seqType field type (can be {@code null})
   * @param init initializing expression (can be {@code null})
   * @param statik static flag
   */
  public ShapeField(final SeqType seqType, final Expr init, final boolean statik) {
    this.seqType = seqType == null ? Types.ITEM_ZM : seqType;
    this.init = init;
    this.statik = statik;
  }

  /**
   * Returns a field with the initializing expression and static flag of this field.
   * @param st field type
   * @return field
   */
  public ShapeField with(final SeqType st) {
    return new ShapeField(st, init, statik);
  }

  /**
   * Returns a field without initializing expression.
   * @return field
   */
  public ShapeField detach() {
    return init == null ? this : new ShapeField(seqType, null, statik);
  }

  /**
   * Returns the initializing expression.
   * @return initializing expression (can be {@code null})
   */
  public Expr init() {
    return init;
  }

  /**
   * Returns the value of the initializing expression.
   * @param qc query context
   * @return value (empty sequence if there is no initializing expression)
   * @throws QueryException query exception
   */
  public Value init(final QueryContext qc) throws QueryException {
    return init != null ? init.value(qc) : Empty.VALUE;
  }

  /**
   * Indicates if this field is static.
   * @return result of check
   */
  public boolean isStatic() {
    return statik;
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
    return this == obj || obj instanceof final ShapeField rf &&
        seqType.eq(rf.seqType) && Objects.equals(init, rf.init) && statik == rf.statik;
  }

  @Override
  public String toString() {
    return new QueryString().token(AS).token(seqType()).toString();
  }
}
