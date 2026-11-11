package org.basex.query.value.type;

import static org.basex.query.QueryText.*;

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
  /** Fixed flag. */
  private final boolean fixed;

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
   * @param fixed fixed flag
   */
  public ShapeField(final SeqType seqType, final Expr init, final boolean fixed) {
    this.seqType = seqType == null ? Types.ITEM_ZM : seqType;
    this.init = init;
    this.fixed = fixed;
  }

  /**
   * Returns a field with the initializing expression and fixed flag of this field.
   * @param st field type
   * @return field
   */
  public ShapeField with(final SeqType st) {
    return new ShapeField(st, init, fixed);
  }

  /**
   * Returns a field without initializing expression.
   * @return field
   */
  public ShapeField detach() {
    return init == null ? this : new ShapeField(seqType, null, fixed);
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
    if(init == null) return Empty.VALUE;
    try {
      return init.value(qc);
    } catch(final QueryException ex) {
      throw ex.initializer();
    }
  }

  /**
   * Indicates if this field can be omitted (it has an initializer or permits the empty sequence).
   * @return result of check
   */
  public boolean optional() {
    return init != null || !seqType.oneOrMore();
  }

  /**
   * Indicates if this field is fixed.
   * @return result of check
   */
  public boolean isFixed() {
    return fixed;
  }

  /**
   * Get effective sequence type of this field.
   * @return sequence type
   */
  public SeqType seqType() {
    return seqType;
  }

  @Override
  public String toString() {
    return new QueryString().token(AS).token(seqType()).toString();
  }
}
