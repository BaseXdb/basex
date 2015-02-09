package org.basex.build;

import java.io.*;

import org.basex.util.*;

/**
 * This class indicates building exceptions.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BuildException extends IOException {
  /**
   * Constructs an exception with the specified message and extension.
   * @param message message
   * @param ext message extension
   */
  public BuildException(final String message, final Object... ext) {
    super(Util.info(message, ext));
  }
}
