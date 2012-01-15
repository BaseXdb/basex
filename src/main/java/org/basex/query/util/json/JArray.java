package org.basex.query.util.json;

import static org.basex.data.DataText.*;

/**
 * JSON array.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class JArray extends JStruct {
  @Override
  byte[] type() {
    return ARR;
  }
}
