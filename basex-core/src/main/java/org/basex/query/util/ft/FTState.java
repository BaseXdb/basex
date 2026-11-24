package org.basex.query.util.ft;

import org.basex.util.ft.*;

/**
 * Full-text state of a query context.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTState {
  /** Cached position data (can be {@code null}). */
  public FTPosData posData;
  /** Current lexer (can be {@code null}). */
  public FTLexer lexer;
  /** Token positions (needed for highlighting full-text results). */
  public int pos;
  /** Scoring flag. */
  public boolean scoring;

  /**
   * Constructor.
   * @param parent full-text state of the parent context (can be {@code null})
   */
  public FTState(final FTState parent) {
    if(parent != null) posData = parent.posData;
  }
}
