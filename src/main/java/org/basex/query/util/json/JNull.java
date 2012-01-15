package org.basex.query.util.json;

import static org.basex.util.Token.*;

/**
 * JSON null.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class JNull extends JAtom {
  /**
   * Constructor.
   */
  JNull() {
    super(null);
  }

  @Override
  byte[] type() {
    return NULL;
  }
}
