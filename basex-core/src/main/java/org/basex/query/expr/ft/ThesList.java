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
  /** Indicates if no thesaurus is to be used. */
  private final boolean none;

  /**
   * Constructor.
   * @param none indicates if no thesaurus is to be used
   */
  public ThesList(final boolean none) {
    this.none = none;
  }

  /**
   * Adds a thesaurus accessor.
   * @param thes thesaurus accessor to be added
   */
  public void add(final ThesAccessor thes) {
    list.add(thes);
  }

  /**
   * Merges the accessors of two lists.
   * @param tl second list
   * @return new list, or this list if no thesaurus is to be used
   */
  public ThesList merge(final ThesList tl) {
    if(none) return this;
    final ThesList merged = new ThesList(false);
    merged.list.addAll(list);
    for(final ThesAccessor thes : tl.list) {
      if(!merged.list.contains(thes)) merged.list.add(thes);
    }
    return merged;
  }

  /**
   * Finds a thesaurus term.
   * @param term term to be found
   * @param opt full-text options
   * @param qc query context
   * @return result list
   * @throws QueryException query exception
   */
  byte[][] find(final byte[] term, final FTOpt opt, final QueryContext qc) throws QueryException {
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
    if(none) return QueryText.NO + ' ' + QueryText.THESAURUS;
    if(list.isEmpty()) return QueryText.THESAURUS + ' ' + QueryText.DEFAULT;
    return new QueryString().token(QueryText.THESAURUS).
      tokens(list.toArray(), QueryText.SEP, list.size() > 1).toString();
  }
}
