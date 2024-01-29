package org.basex.query.util.collation;

import static com.ibm.icu.text.CollationElementIterator.*;
import static com.ibm.icu.text.Collator.*;
import static org.basex.util.Token.*;

import org.basex.util.*;

import com.ibm.icu.text.*;

/**
 * This collation is based on the ICU collator.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class UCACollation extends Collation {
  /** Primary order mask. */
  private static final int PRIMARY_ORDER_MASK = mask(PRIMARY);

  /** Collator. */
  private final RuleBasedCollator collator;
  /** Collator strength. */
  private final int strength;
  /** Strength mask. */
  private final int strengthMask;
  /** Whether the the alternate handling behavior is "shifted". */
  private final boolean isShifted;
  /** Variable top. */
  private final int variableTop;

  /**
   * Private Constructor.
   * @param collator collation options
   */
  UCACollation(final RuleBasedCollator collator) {
    this.collator = collator;
    strength = this.collator.getStrength();
    strengthMask = mask(strength);
    isShifted = this.collator.isAlternateHandlingShifted();
    variableTop = this.collator.getVariableTop();
  }

  /**
   * Return a bit mask for the relevant parts of a collation element per the collator's strength.
   * @see <a href=
   *      "https://unicode-org.github.io/icu/userguide/collation/architecture#collation-elements"
   *      >Collation Elements</a>
   * @param strength collator strength
   * @return bit mask
   */
  private static int mask(final int strength) {
    switch(strength) {
      case Collator.PRIMARY:
        return 0xFFFF0000;
      case Collator.SECONDARY:
        return 0xFFFFFF00;
      default:
        return 0xFFFFFFFF;
    }
  }

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    return collator.compare(string(string), string(compare));
  }

  @Override
  protected int indexOf(final String string, final String contains, final Mode mode,
      final InputInfo ii) {

    final CollationElementIterator iterS = collator.getCollationElementIterator(string);
    final CollationElementIterator iterC = collator.getCollationElementIterator(contains);

    final int elemC = next(iterC);
    if(elemC == NULLORDER) return 0;
    final int offC = iterC.getOffset();
    while(true) {
      // find first equal character
      for(int elemS; (elemS = next(iterS)) != elemC;) {
        if(elemS == NULLORDER || mode == Mode.STARTS_WITH) return -1;
      }

      final int offS = iterS.getOffset();
      if(startsWith(iterS, iterC)) {
        if(mode == Mode.INDEX_AFTER) {
          return iterS.getOffset();
        } else if(mode == Mode.ENDS_WITH) {
          if(next(iterS) == NULLORDER) return offS - 1;
        } else {
          return offS - 1;
        }
      }
      iterS.setOffset(offS);
      iterC.setOffset(offC);
    }
  }

  /**
   * Determines whether one string starts with another.
   * @param string string iterator
   * @param sub substring iterator
   * @return result of check
   */
  private boolean startsWith(final CollationElementIterator string,
      final CollationElementIterator sub) {
    while(true) {
      final int s = next(sub);
      if(s == NULLORDER) return true;
      if(s != next(string)) return false;
    }
  }

  /**
   * Returns the next element from an iterator, and transforms it according to the collator's
   * properties. This was modeled after method getCE in {@link StringSearch}.
   * @param it iterator
   * @return next element, or {@link CollationElementIterator#NULLORDER}
   */
  private int next(final CollationElementIterator it) {
    while(true) {
      int c = it.next();
      if(c == NULLORDER) return c;
      c &= strengthMask;
      if(isShifted) {
        if(variableTop > c) {
          if(strength >= QUATERNARY) {
            c &= PRIMARY_ORDER_MASK;
          } else {
            c = IGNORABLE;
          }
        }
      } else if(strength >= QUATERNARY && c == IGNORABLE) {
        c = 0xFFFF;
      }
      if(c != IGNORABLE) return c;
    }
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof UCACollation &&
        collator.equals(((UCACollation) obj).collator);
  }
}
