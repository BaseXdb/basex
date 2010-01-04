package org.basex.build;

import java.io.IOException;
import org.basex.core.Main;

/**
 * This class indicates building exceptions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
    super(Main.info(s, e));
  }
}
