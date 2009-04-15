package org.basex.ft;

import org.basex.util.Array;
import org.basex.util.TokenList;

/**
 * Simple Thesaurus for fulltext requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class ThesQuery {
  /** Thesaurus root references. */
  private Thesaurus[] thes = {};

  /**
   * Merges two thesaurus definitions.
   * @param th second thesaurus
   */
  public void add(final Thesaurus th) {
    thes = Array.add(thes, th);
  }

  /**
   * Merges two thesaurus definitions.
   * @param th second thesaurus
   */
  public void merge(final ThesQuery th) {
    for(final Thesaurus t : th.thes) {
      boolean f = false;
      for(final Thesaurus tt : thes) f |= tt.eq(t);
      if(!f) thes = Array.add(thes, t);
    }
  }

  /**
   * Finds a thesaurus term.
   * @param term term to be found
   * @return result list
   */
  public byte[][] find(final byte[] term) {
    final TokenList tl = new TokenList();
    for(final Thesaurus th : thes) th.find(tl, term);
    return tl.finish();
  }
}
