package org.basex.build;

import java.io.*;

import org.basex.util.*;

/**
 * This class indicates building exceptions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BuildException extends IOException {
  /**
   * Constructs an exception with the specified message and extension.
   * @param s message
   * @param e message extension
   */
  public BuildException(final String s, final Object... e) {
    super(Util.info(s, e));
  }
}
