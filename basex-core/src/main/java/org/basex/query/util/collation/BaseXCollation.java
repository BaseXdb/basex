package org.basex.query.util.collation;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.text.*;
import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * This collations is based on a standard Java collator.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXCollation extends Collation {
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
  protected int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo info) throws QueryException {

    if(!(collator instanceof RuleBasedCollator)) throw CHARCOLL.get(info);
    final RuleBasedCollator rbc = (RuleBasedCollator) collator;
    final CollationElementIterator st = rbc.getCollationElementIterator(string);
    final CollationElementIterator sb = rbc.getCollationElementIterator(sub);
    do {
      final int cs = next(sb);
      if(cs == -1) return 0;
      int c;
      // find first equal character
      do {
        c = next(st);
        if(c == -1) return -1;
      } while(c != cs);

      final int s = st.getOffset();
      if(startsWith(st, sb)) {
        if(mode == Mode.INDEX_AFTER) {
          return st.getOffset();
        } else if(mode == Mode.ENDS_WITH) {
          if(next(st) == -1) return s - 1;
        } else {
          return s - 1;
        }
      }
      st.setOffset(s);
      sb.reset();
    } while(mode != Mode.STARTS_WITH);

    return -1;
  }

  /**
   * Determines whether one string starts with another.
   * @param string string iterator
   * @param sub substring iterator
   * @return result of check
   */
  private static boolean startsWith(final CollationElementIterator string,
      final CollationElementIterator sub) {
    do {
      final int s = next(sub);
      if(s == -1) return true;
      if(s != next(string)) return false;
    } while(true);
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
}
