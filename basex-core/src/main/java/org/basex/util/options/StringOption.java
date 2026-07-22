package org.basex.util.options;

import org.basex.query.value.type.*;

/**
 * Option containing a string value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringOption extends Option<String> {
  /** Default value (can be {@null}). */
  private final String value;

  /**
   * Constructor without default value.
   * @param name name
   */
  public StringOption(final String name) {
    this(name, null);
  }

  /**
   * Default constructor.
   * @param name name
   * @param value value (can be {@null})
   */
  public StringOption(final String name, final String value) {
    this(name, value, null);
  }

  /**
   * Constructor with required type.
   * @param name name
   * @param value value (can be {@null})
   * @param seqType required type (can be {@code null})
   */
  public StringOption(final String name, final String value, final SeqType seqType) {
    super(name, seqType);
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  SeqType defaultType() {
    return Types.STRING_O;
  }
}
