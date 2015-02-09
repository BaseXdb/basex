package org.basex.query.util.collation;

import java.text.*;
import java.util.*;

import org.basex.util.options.*;

/**
 * Project-specific collation options.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BaseXCollationOptions extends CollationOptions {
  /** Option: language. */
  public static final StringOption LANG = new StringOption("lang");
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

  @Override
  Collation get(final String args) {
    final String error = check(args);
    if(error != null) throw new IllegalArgumentException("Invalid option \"" + error + "\"");

    Locale locale = Locale.US;
    if(contains(LANG)) {
      locale = Locales.MAP.get(get(LANG));
      if(locale == null) throw error(LANG);
    }

    final Collator coll = Collator.getInstance(locale);
    if(contains(STRENGTH)) coll.setStrength(get(STRENGTH).value);
    if(contains(DECOMPOSITION)) coll.setDecomposition(get(DECOMPOSITION).value);
    return new BaseXCollation(coll);
  }
}
