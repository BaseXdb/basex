package org.basex.query.value.map;

import org.basex.query.value.*;
import org.basex.query.value.type.*;

/**
 * Map with a shape that has a single, boxed field value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQShapeSingletonMap extends XQShapeMap {
  /** Value. */
  private final Value value;

  /**
   * Constructor.
   * @param type shape with a single field
   * @param value value of the field
   */
  XQShapeSingletonMap(final ShapeType type, final Value value) {
    super(type);
    this.value = value;
  }

  @Override
  public Value valueAt(final long index) {
    return value;
  }
}
