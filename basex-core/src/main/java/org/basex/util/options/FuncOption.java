package org.basex.util.options;

import org.basex.query.value.item.*;

/**
 * Option containing a boolean value.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FuncOption extends Option<FItem> {
  /** Function item. */
  private final FItem value;

  /**
   * Constructor without default value.
   * @param name name
   * @param value value
   */
  public FuncOption(final String name, final FItem value) {
    super(name);
    this.value = value;
  }

  /**
   * Constructor without default value.
   * @param name name
   */
  public FuncOption(final String name) {
    super(name);
    value = null;
  }

  @Override
  public FItem value() {
    return value;
  }
}
