package org.basex.query.util.json;

import static org.basex.data.DataText.*;

/**
 * JSON boolean.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class JBoolean extends JAtom {
  /**
   * Constructor.
   * @param v value
   */
  JBoolean(final byte[] v) {
    super(v);
  }

  @Override
  byte[] type() {
    return BOOL;
  }
}
