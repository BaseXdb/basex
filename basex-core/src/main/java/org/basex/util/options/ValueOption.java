package org.basex.util.options;

import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Option containing an XQuery value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ValueOption extends Option<Value> {
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
    super(name, seqType);
    this.value = value;
  }

  @Override
  public Value value() {
    return value;
  }
}
