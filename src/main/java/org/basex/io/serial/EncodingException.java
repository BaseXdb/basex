package org.basex.io.serial;

/**
 * This class indicates exceptions during the decoding of the input stream.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class EncodingException extends InputException {
  /**
   * Constructor.
   * @param cp code points
   */
  public EncodingException(final int cp) {
    super("Invalid XML character (#" + cp + ')');
  }
}
