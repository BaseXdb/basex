package org.basex.query.util.collation;

import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.text.*;
import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * This collations is based on the ICU collator.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UCACollation extends Collation {
  /** Name of the Collator class. */
  private static final Class<?> CEI = Reflect.find("com.ibm.icu.text.CollationElementIterator");
  /** Method. */
  private static final Method RBC_GCEI =
      Reflect.method(UCAOptions.RBC, "getCollationElementIterator", String.class);
  /** Method. */
  private static final Method CEI_GET_OFFSET = Reflect.method(CEI, "getOffset");
  /** Method. */
  private static final Method CEI_SET_OFFSET = Reflect.method(CEI, "setOffset", int.class);
  /** Method. */
  private static final Method CEI_RESET = Reflect.method(CEI, "reset");
  /** Method. */
  private static final Method CEI_NEXT = Reflect.method(CEI, "next");


  /** Collator. */
  private final Comparator<Object> collator;

  /**
   * Private Constructor.
   * @param collator collation options
   */
  UCACollation(final Comparator<Object> collator) {
    this.collator = collator;
  }

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    return collator.compare(string(string), string(compare));
  }

  @Override
  protected int indexOf(final String string, final String sub, final Mode mode,
      final InputInfo info) throws QueryException {

    final RuleBasedCollator rbc = (RuleBasedCollator) collator;
    final Object st = Reflect.invoke(RBC_GCEI, rbc, string);
    final Object sb = Reflect.invoke(RBC_GCEI, rbc, sub);
    do {
      final int cs = next(sb);
      if(cs == -1) return 0;
      int c;
      // find first equal character
      do {
        c = next(st);
        if(c == -1) return -1;
      } while(c != cs);

      final int s = (Integer) Reflect.invoke(CEI_GET_OFFSET, sb);
      if(startsWith(st, sb)) {
        if(mode == Mode.INDEX_AFTER) {
          return (int) Reflect.invoke(CEI_GET_OFFSET, st);
        } else if(mode == Mode.ENDS_WITH) {
          if(next(st) == -1) return s - 1;
        } else {
          return s - 1;
        }
      }
      Reflect.invoke(CEI_SET_OFFSET, sb, s);
      Reflect.invoke(CEI_RESET, sb);
    } while(mode != Mode.STARTS_WITH);

    return -1;
  }

  /**
   * Determines whether one string starts with another.
   * @param string string iterator (of type CollationElementIterator)
   * @param sub substring iterator (of type CollationElementIterator)
   * @return result of check
   */
  private static boolean startsWith(final Object string, final Object sub) {
    do {
      final int s = next(sub);
      if(s == -1) return true;
      if(s != next(string)) return false;
    } while(true);
  }

  /**
   * Returns the next element from an iterator.
   * @param it iterator (of type CollationElementIterator)
   * @return next element, or {@code -1}
   */
  private static int next(final Object it) {
    do {
      final int c = (int) Reflect.invoke(CEI_NEXT, it);
      if(c != 0) return c;
    } while(true);
  }
}
