package org.basex.build;

import org.basex.io.IO;

/**
 * This class defines an abstract parser, specifying a target path.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class TargetParser extends Parser {
  /** Target path (empty or suffixed with a single slash). */
  protected final String trg;

  /**
   * Constructor.
   * @param source document source
   * @param target target path
   */
  public TargetParser(final IO source, final String target) {
    super(source);
    trg = target.isEmpty() ? "" : (target + '/').replaceAll("//+", "/");
  }
}
