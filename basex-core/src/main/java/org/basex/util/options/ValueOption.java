package org.basex.util.options;

import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Option containing a boolean value.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ValueOption extends Option<Value> {
  /** Sequence type. */
  private final SeqType seqType;
  /** Default value. */
  private final Value value;

  /**
   * Constructor without default value.
   * @param name name
   * @param seqType expected sequence type
   */
  public ValueOption(final String name, final SeqType seqType) {
    this(name, seqType, Empty.VALUE);
  }

  /**
   * Default constructor.
   * @param name name
   * @param seqType expected sequence type
   * @param value value
   */
  public ValueOption(final String name, final SeqType seqType, final Value value) {
    super(name);
    this.seqType = seqType;
    this.value = value;
  }

  @Override
  public Value value() {
    return value;
  }

  /**
   * Returns the expected sequence type.
   * @return type
   */
  public SeqType seqType() {
    return seqType;
  }
}
