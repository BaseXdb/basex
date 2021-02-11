package org.basex.query.util.collation;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * This collations is based on a standard Java collator.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class BaseXCollation extends Collation {
  /** Collator. */
  private final Comparator<Object> collator;

  /**
   * Private Constructor.
   * @param collator collator
   */
  BaseXCollation(final Comparator<Object> collator) {
    this.collator = collator;
  }

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    return collator.compare(string(string), string(compare));
  }

  @Override
  protected int indexOf(final String string, final String contains, final Mode mode,
      final InputInfo ii) throws QueryException {

    if(!(collator instanceof RuleBasedCollator)) throw CHARCOLL.get(ii);
    final RuleBasedCollator rbc = (RuleBasedCollator) collator;
    final CollationElementIterator iterS = rbc.getCollationElementIterator(string);
    final CollationElementIterator iterC = rbc.getCollationElementIterator(contains);

    final int elemC = next(iterC);
    if(elemC == -1) return 0;
    final int offC = iterC.getOffset();
    do {
      // find first equal character
      for(int elemS; (elemS = next(iterS)) != elemC;) {
        if(elemS == -1 || mode == Mode.STARTS_WITH) return -1;
      }

      final int offS = iterS.getOffset();
      if(startsWith(iterS, iterC)) {
        if(mode == Mode.INDEX_AFTER) {
          return iterS.getOffset();
        } else if(mode == Mode.ENDS_WITH) {
          if(next(iterS) == -1) return offS - 1;
        } else {
          return offS - 1;
        }
      }
      iterS.setOffset(offS);
      iterC.setOffset(offC);
    } while(true);
  }

  /**
   * Determines whether one string starts with another.
   * @param string string iterator
   * @param sub substring iterator
   * @return result of check
   */
  private static boolean startsWith(final CollationElementIterator string,
      final CollationElementIterator sub) {

    for(int s; (s = next(sub)) != -1;) {
      if(s != next(string)) return false;
    }
    return true;
  }

  /**
   * Returns the next element from an iterator.
   * @param it iterator
   * @return next element, or {@code -1}
   */
  private static int next(final CollationElementIterator it) {
    do {
      final int c = it.next();
      if(c != 0) return c;
    } while(true);
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof BaseXCollation &&
        collator.equals(((BaseXCollation) obj).collator);
  }
}
