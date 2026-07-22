package org.basex.util.options;

import org.basex.query.value.type.*;

/**
 * Option containing a boolean value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BooleanOption extends Option<Boolean> {
  /** Default value (can be {@null}). */
  private final Boolean value;

  /**
   * Constructor without default value.
   * @param name name
   */
  public BooleanOption(final String name) {
    this(name, null);
  }

  /**
   * Default constructor.
   * @param name name
   * @param value value (can be {@null})
   */
  public BooleanOption(final String name, final Boolean value) {
    super(name);
    this.value = value;
  }

  @Override
  public Boolean value() {
    return value;
  }

  @Override
  SeqType defaultType() {
    return Types.BOOLEAN_O;
  }
}
