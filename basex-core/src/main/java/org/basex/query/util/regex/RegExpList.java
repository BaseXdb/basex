package org.basex.query.util.regex;

import org.basex.util.list.*;

/**
 * Resizable-array implementation for regular expressions.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class RegExpList extends ObjectList<RegExp, RegExpList> {
  /**
   * Constructor.
   */
  public RegExpList() {
    super(new RegExp[1]);
  }

  @Override
  protected RegExp[] newList(final int s) {
    return new RegExp[s];
  }
}
