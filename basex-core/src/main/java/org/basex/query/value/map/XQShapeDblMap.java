package org.basex.query.value.map;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Map with a single field and an unboxed double value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQShapeDblMap extends XQShapeMap {
  /** Value. */
  private final double value;

  /**
   * Constructor.
   * @param type shape with a single field
   * @param value value
   */
  XQShapeDblMap(final ShapeType type, final double value) {
    super(type);
    this.value = value;
  }

  @Override
  public Value valueAt(final long index) {
    return Dbl.get(value);
  }
}
