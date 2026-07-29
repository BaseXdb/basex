package org.basex.util.options;

import java.util.*;

import org.basex.query.value.type.*;

/**
 * Option containing an integer array value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class NumbersOption extends Option<int[]> {
  /** Default value. */
  private final int[] value;

  /**
   * Default constructor.
   * @param name name
   * @param value value
   */
  public NumbersOption(final String name, final int... value) {
    this(name, null, value);
  }

  /**
   * Constructor with required type.
   * @param name name
   * @param seqType required type (can be {@code null})
   * @param value value
   */
  public NumbersOption(final String name, final SeqType seqType, final int... value) {
    super(name, seqType);
    this.value = value;
  }

  @Override
  public int[] value() {
    return value;
  }

  @Override
  public int[] copy() {
    return value == null ? null : value.clone();
  }

  @Override
  SeqType defaultType() {
    return Types.INTEGER_ZM;
  }

  @Override
  public String toString() {
    return name() + Collections.singletonList(value);
  }
}
