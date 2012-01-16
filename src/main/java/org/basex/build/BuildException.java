package org.basex.build;

import java.io.IOException;
import org.basex.util.Util;

/**
 * This class indicates building exceptions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BuildException extends IOException {
  /**
   * Constructs an exception with the specified message.
   * @param s message
   */
  public BuildException(final String s) {
    super(s);
  }

  /**
   * Constructs an exception with the specified message and extension.
   * @param s message
   * @param e message extension
   */
  public BuildException(final String s, final Object... e) {
    super(Util.info(s, e));
  }
}
