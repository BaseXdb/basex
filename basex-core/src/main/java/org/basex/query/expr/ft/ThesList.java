package org.basex.query.expr.ft;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.list.*;

/**
 * Thesaurus container for full-text requests.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ThesList {
  /** Thesaurus root references. */
  private final ArrayList<Thesaurus> list = new ArrayList<>(1);

  /**
   * Adds a thesaurus.
   * @param thes thesaurus to be added
   */
  public void add(final Thesaurus thes) {
    list.add(thes);
  }

  /**
   * Merges two thesaurus definitions.
   * @param tl second thesaurus
   */
  public void merge(final ThesList tl) {
    for(final Thesaurus thes : tl.list) {
      boolean f = false;
      for(final Thesaurus th : list) f = f || th.equals(thes);
      if(!f) list.add(thes);
    }
  }

  /**
   * Finds a thesaurus term.
   * @param token token
   * @param ctx database context
   * @return result list
   * @throws QueryException query exception
   */
  byte[][] find(final byte[] token, final Context ctx) throws QueryException {
    final TokenList tl = new TokenList();
    for(final Thesaurus th : list) th.find(tl, token, ctx);
    return tl.finish();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Thesaurus th : list) {
      if(sb.length() != 0) sb.append(", ");
      sb.append(th);
    }
    return sb.toString();
  }
}
