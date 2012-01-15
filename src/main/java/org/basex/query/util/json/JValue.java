package org.basex.query.util.json;

/**
 * JSON value.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class JValue {
  /**
   * Returns the value type.
   * @return value
   */
  abstract byte[] type();
}
