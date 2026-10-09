package org.basex.query.expr.ft;

import java.util.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * List of thesaurus accessors.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ThesList {
  /** Thesaurus accessors. */
  private final ArrayList<ThesAccessor> list = new ArrayList<>(1);

  /**
   * Adds a thesaurus accessor.
   * @param thes thesaurus accessor to be added
   */
  public void add(final ThesAccessor thes) {
    list.add(thes);
  }

  /**
   * Merges two list.
   * @param tl second list
   */
  public void merge(final ThesList tl) {
    for(final ThesAccessor thes : tl.list) {
      boolean f = false;
      for(final ThesAccessor th : list) f = f || th.equals(thes);
      if(!f) list.add(thes);
    }
  }

  /**
   * Finds a thesaurus term.
   * @param term term to be found
   * @param opt full-text options
   * @param qc query context
   * @return result list
   * @throws QueryException query exception
   */
  byte[][] find(final byte[] term, final FTOpt opt, final QueryContext qc)
      throws QueryException {
    final TokenList tl = new TokenList();
    for(final ThesAccessor th : list) tl.add(th.find(term, opt, qc));
    return tl.finish();
  }

  /**
   * Passes on the thesaurus databases to the visitor.
   * @param visitor visitor
   * @return result of check
   */
  public boolean accept(final ASTVisitor visitor) {
    for(final ThesAccessor th : list) {
      if(!th.accept(visitor)) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final ThesAccessor th : list) {
      if(!sb.isEmpty()) sb.append(", ");
      sb.append(th);
    }
    return sb.toString();
  }
}
