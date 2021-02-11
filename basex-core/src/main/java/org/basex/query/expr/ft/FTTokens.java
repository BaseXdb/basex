package org.basex.query.expr.ft;

import org.basex.util.list.*;

/**
 * This is a container for full-text tokens.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTTokens extends ObjectList<TokenList, FTTokens> {
  /**
   * Constructor.
   */
  public FTTokens() {
    super(new TokenList[1]);
  }

  /**
   * Returns the number of tokens of the first entry.
   * @return number of tokens
   */
  int firstSize() {
    return list[0].size();
  }

  @Override
  protected TokenList[] newArray(final int s) {
    return new TokenList[s];
  }
}
