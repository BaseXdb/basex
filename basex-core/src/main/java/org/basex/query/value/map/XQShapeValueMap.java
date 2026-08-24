package org.basex.query.value.map;

import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Map with a shape whose field values are stored in an array.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQShapeValueMap extends XQShapeMap {
  /** Values. */
  private final Value[] values;

  /**
   * Constructor.
   * @param type shape
   * @param values values, one per field
   */
  XQShapeValueMap(final ShapeType type, final Value... values) {
    super(type);
    this.values = values;
  }

  @Override
  public Value valueAt(final long index) {
    return values[(int) index];
  }
}
