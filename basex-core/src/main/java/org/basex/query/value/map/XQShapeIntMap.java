package org.basex.query.value.map;

import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Map with a single field and an unboxed integer value.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQShapeIntMap extends XQShapeMap {
  /** Value. */
  private final long value;

  /**
   * Constructor.
   * @param type shape with a single field
   * @param value value
   */
  XQShapeIntMap(final ShapeType type, final long value) {
    super(type);
    this.value = value;
  }

  @Override
  public Value valueAt(final long index) {
    return Itr.get(value);
  }
}
