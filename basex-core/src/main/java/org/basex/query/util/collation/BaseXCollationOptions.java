package org.basex.query.util.collation;

import static org.basex.util.Strings.*;

import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.util.options.*;

/**
 * Project-specific collation options.
 *
 * @author BaseX Team 2005-21, BSD License
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

  /** Fallback parsing. */
  private final boolean fallback;

  /**
   * Constructor.
   * @param fallback fallback option
   */
  public BaseXCollationOptions(final boolean fallback) {
    this.fallback = fallback;
  }

  @Override
  public Collation get(final HashMap<String, String> args) throws BaseXException {
    return new BaseXCollation(collator(args));
  }

  /**
   * Returns a collator for the specified arguments.
   * @param args argument to be parsed
   * @return collator collator
   * @throws BaseXException database exception
   */
  public Collator collator(final HashMap<String, String> args) throws BaseXException {
    if(fallback) {
      for(final Entry<String, String> entry : args.entrySet()) {
        final String name = entry.getKey();
        String value = entry.getValue();
        if(name.equals(STRENGTH.name())) {
          if(eq(value, "1")) value = Strength.PRIMARY.toString();
          else if(eq(value, "2")) value = Strength.SECONDARY.toString();
          else if(eq(value, "3")) value = Strength.TERTIARY.toString();
          else if(eq(value, "quaternary", "4", "5")) value = Strength.IDENTICAL.toString();
          assign(name, value);
        }
      }
    } else {
      assign(args);
    }

    Locale locale = Locale.US;
    if(contains(LANG)) {
      locale = Locales.MAP.get(get(LANG));
      if(locale == null) throw error(LANG);
    }

    final Collator coll = Collator.getInstance(locale);
    if(contains(STRENGTH)) coll.setStrength(get(STRENGTH).value);
    if(contains(DECOMPOSITION)) coll.setDecomposition(get(DECOMPOSITION).value);
    return coll;
  }
}
