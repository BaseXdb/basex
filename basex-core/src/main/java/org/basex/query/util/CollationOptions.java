package org.basex.query.util;

import java.text.*;
import java.util.*;

import org.basex.util.options.*;

/**
 * UCA collation options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class CollationOptions extends Options {
  /** Option: language. */
  public static final StringOption LANG = new StringOption("lang", "");
  /** Option: strength. */
  public static final EnumOption<Strength> STRENGTH = new EnumOption<>("strength", Strength.class);
  /** Option: decomposition. */
  public static final EnumOption<Decomposition> DECOMPOSITION =
      new EnumOption<>("decomposition", Decomposition.class);

  /** Strength. */
  public enum Strength {
    /** Primary.    */ PRIMARY(Collator.PRIMARY),
    /** Secondary.  */ SECONDARY(Collator.SECONDARY),
    /** Tertiary.   */ TERTIARY(Collator.TERTIARY),
    /** Identical.  */ IDENTICAL(Collator.IDENTICAL);

    /** Strength. */
    public final int value;

    /**
     * Constructor.
     * @param v strength
     */
    Strength(final int v) {
      value = v;
    }

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }
  }

  /** Decomposition. */
  public enum Decomposition {
    /** None.     */ NONE(Collator.NO_DECOMPOSITION),
    /** Full.     */ FULL(Collator.FULL_DECOMPOSITION),
    /** Standard. */ STANDARD(Collator.CANONICAL_DECOMPOSITION);

    /** Decomposition. */
    public final int value;

    /**
     * Constructor.
     * @param d decomposition
     */
    Decomposition(final int d) {
      value = d;
    }

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH);
    }
  }
}
