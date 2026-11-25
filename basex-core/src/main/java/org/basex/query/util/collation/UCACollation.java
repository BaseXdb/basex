package org.basex.query.util.collation;

import static com.ibm.icu.text.CollationElementIterator.*;
import static com.ibm.icu.text.Collator.*;
import static org.basex.util.Token.*;

import org.basex.util.*;

import com.ibm.icu.text.*;

/**
 * This collation is based on the ICU collator.
 *
 * @author BaseX Team, BSD License
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
  /** Whether the alternate handling behavior is "shifted". */
  private final boolean isShifted;
  /** Variable top. */
  private final int variableTop;

  /**
   * Constructor.
   * @param collator collator
   * @param uri collation URI
   */
  UCACollation(final RuleBasedCollator collator, final byte[] uri) {
    super(uri);
    this.collator = collator;
    strength = collator.getStrength();
    strengthMask = mask(strength);
    isShifted = collator.isAlternateHandlingShifted();
    variableTop = collator.getVariableTop();
  }

  /**
   * Returns a bit mask for the relevant parts of a collation element per the collator's strength.
   * @param strength collator strength
   * @return bit mask
   * @see <a href=
   *      "https://unicode-org.github.io/icu/userguide/collation/architecture#collation-elements"
   *      >Collation Elements</a>
   */
  private static int mask(final int strength) {
    return switch(strength) {
      case Collator.PRIMARY -> 0xFFFF0000;
      case Collator.SECONDARY -> 0xFFFFFF00;
      default -> 0xFFFFFFFF;
    };
  }

  @Override
  public int compare(final byte[] string, final byte[] compare) {
    return collator.compare(string(string), string(compare));
  }

  @Override
  CollationElements elements(final String string, final InputInfo info) {
    final CollationElementIterator it = collator.getCollationElementIterator(string);
    return new CollationElements(() -> {
      final int element = it.next();
      return element == NULLORDER ? element : transform(element);
    }, it::getOffset);
  }

  @Override
  public byte[] key(final byte[] string, final InputInfo info) {
    return collator.getCollationKey(Token.string(string)).toByteArray();
  }

  /**
   * Transforms a collation element per the collator's properties, as in {@link StringSearch}.
   * @param element collation element
   * @return transformed element, or {@link CollationElementIterator#IGNORABLE}
   */
  private int transform(final int element) {
    final int c = element & strengthMask;
    if(isShifted) {
      return variableTop <= c ? c : strength >= QUATERNARY ? c & PRIMARY_ORDER_MASK : IGNORABLE;
    }
    return strength >= QUATERNARY && c == IGNORABLE ? 0xFFFF : c;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final UCACollation uca && collator.equals(uca.collator);
  }
}
