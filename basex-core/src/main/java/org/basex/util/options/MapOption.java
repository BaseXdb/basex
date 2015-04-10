package org.basex.util.options;

import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

/**
 * Option containing a boolean value.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class MapOption extends Option<FItem> {
  /** Map item. */
  private final Map value;

  /**
   * Constructor without default value.
   * @param name name
   * @param value value
   */
  public MapOption(final String name, final Map value) {
    super(name);
    this.value = value;
  }

  /**
   * Constructor without default value.
   * @param name name
   */
  public MapOption(final String name) {
    super(name);
    value = null;
  }

  @Override
  public Map value() {
    return value;
  }
}
