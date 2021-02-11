package org.basex.query.util.collation;

import static org.basex.util.Token.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.util.*;

/**
 * This collations is based on the ICU collator.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class UCACollation extends Collation {
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
  protected int indexOf(final String string, final String contains, final Mode mode,
      final InputInfo ii) {

    final Object iterS = Reflect.invoke(RBC_GCEI, collator, string);
    final Object iterC = Reflect.invoke(RBC_GCEI, collator, contains);

    final int elemC = next(iterC);
    if(elemC == -1) return 0;
    final int offC = (int) Reflect.invoke(CEI_GET_OFFSET, iterC);
    do {
      // find first equal character
      for(int elemS; (elemS = next(iterS)) != elemC;) {
        if(elemS == -1 || mode == Mode.STARTS_WITH) return -1;
      }

      final int offS = (Integer) Reflect.invoke(CEI_GET_OFFSET, iterS);
      if(startsWith(iterS, iterC)) {
        if(mode == Mode.INDEX_AFTER) {
          return (int) Reflect.invoke(CEI_GET_OFFSET, iterS);
        } else if(mode == Mode.ENDS_WITH) {
          if(next(iterS) == -1) return offS - 1;
        } else {
          return offS - 1;
        }
      }
      Reflect.invoke(CEI_SET_OFFSET, iterC, offS);
      Reflect.invoke(CEI_SET_OFFSET, iterC, offC);
    } while(true);
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

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof UCACollation &&
        collator.equals(((UCACollation) obj).collator);
  }
}
