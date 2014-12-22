package org.basex.query.util.collation;

import static org.basex.util.Reflect.*;
import static org.basex.util.Strings.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * UCA collation options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class UCAOptions extends CollationOptions {
  /** Option: fallback. */
  public static final EnumOption<YesNo> FALLBACK = new EnumOption<>("fallback", YesNo.YES);
  /** Option: language. */
  public static final StringOption LANG = new StringOption("lang");
  /** Option: version. */
  public static final StringOption VERSION = new StringOption("version");
  /** Option: strength. */
  public static final StringOption STRENGTH = new StringOption("strength");
  /** Option: alternate. */
  public static final StringOption ALTERNATE = new StringOption("alternate");
  /** Option: backwards. */
  public static final StringOption BACKWARDS = new StringOption("backwards");
  /** Option: normalization. */
  public static final StringOption NORMALIZATION = new StringOption("normalization");
  /** Option: caseLevel. */
  public static final StringOption CASELEVEL = new StringOption("caseLevel");
  /** Option: caseFirst. */
  public static final StringOption CASEFIRST = new StringOption("caseFirst");
  /** Option: numeric. */
  public static final StringOption NUMERIC = new StringOption("numeric");
  /** Option: reorder. */
  public static final StringOption REORDER = new StringOption("reorder");

  /** Name of the Collator class. */
  static final Class<?> COLLATOR = find("com.ibm.icu.text.Collator");
  /** Stemmer class corresponding to the required properties. */
  static final boolean ACTIVE = COLLATOR != null;
  /** Name of the Collator class. */
  static final Class<?> RBC = find("com.ibm.icu.text.RuleBasedCollator");

  /** Rule-based collator. */
  private Object coll;

  @Override
  @SuppressWarnings("unchecked")
  Collation get(final String args) {
    final String error = check(args);
    if(error != null && (error.startsWith(FALLBACK.name() + "=") || get(FALLBACK) == YesNo.NO))
      throw new IllegalArgumentException("Invalid option \"" + error + "\"");

    final boolean nomercy = get(FALLBACK) == YesNo.NO;

    Locale locale = Locale.US;
    if(contains(LANG)) {
      locale = Locales.map.get(get(LANG));
      if(locale == null) throw error(LANG);
    }

    final Method m = method(COLLATOR, "getInstance", Locale.class);
    coll = invoke(m, null, locale);

    if(!coll.getClass().equals(RBC)) throw new IllegalArgumentException(
        "Invalid collator \"" + coll.getClass().getName() + "\"");

    if(contains(VERSION)) {
      final String v = get(VERSION);
      try {
        final Class<?> vc = find("com.ibm.icu.util.VersionInfo");
        final Object vi = invoke(method(vc, "getInstance", String.class), null, v);
        final Object vic = invoke(method(RBC, "getUCAVersion"), coll);
        if(vi == null || vic == null || ((Comparable<Object>) vi).compareTo(vic) > 0)
          throw error(VERSION);
      } catch(final IllegalArgumentException ex) {
        throw error(VERSION);
      }
    }

    if(contains(STRENGTH)) {
      final String v = get(STRENGTH);
      Integer s = null;
      if(eq(v, "primary", "1")) s = 0;         // Collator.PRIMARY
      else if(eq(v, "secondary", "2")) s = 1;  // Collator.SECONDARY
      else if(eq(v, "tertiary", "3")) s = 2;   // Collator.TERTIARY
      else if(eq(v, "quaternary", "4")) s = 3; // Collator.QUATERNARY
      else if(eq(v, "identical", "5")) s = 15; // Collator.IDENTICAL
      else if(nomercy) throw error(STRENGTH);
      if(s != null) invoke(method(RBC, "setStrength", int.class), coll, s);
    }

    if(contains(ALTERNATE)) {
      final String v = get(ALTERNATE);
      Boolean b = null;
      if(eq(v, "non-ignorable")) b = false;
      else if(eq(v, "shifted", "blanked")) b = true;
      else if(nomercy) throw error(ALTERNATE);
      if(b != null) invoke(method(RBC, "setAlternateHandlingShifted", boolean.class), coll, b);
    }

    if(contains(BACKWARDS)) {
      final Boolean b = yesNo(BACKWARDS, nomercy);
      if(b != null) invoke(method(RBC, "setFrenchCollation", boolean.class), coll, b);
    }

    if(contains(NORMALIZATION)) {
      final Boolean b = yesNo(NORMALIZATION, nomercy);
      // Collator.CANONICAL_DECOMPOSITION, Collator.NO_DECOMPOSITION
      if(b != null) invoke(method(RBC, "setDecomposition", int.class), coll, b ? 17 : 16);
    }

    if(contains(CASELEVEL)) {
      final Boolean b = yesNo(CASELEVEL, nomercy);
      if(b != null) invoke(method(RBC, "setCaseLevel", boolean.class), coll, b);
    }

    if(contains(CASEFIRST)) {
      final String v = get(CASEFIRST);
      String f = null;
      if(v.equals("upper")) f = "setUpperCaseFirst";
      else if(v.equals("lower")) f = "setLowerCaseFirst";
      else if(nomercy) throw error(CASEFIRST);
      if(f != null) invoke(method(RBC, f, boolean.class), coll, true);
    }

    if(contains(NUMERIC)) {
      final Boolean b = yesNo(NUMERIC, nomercy);
      if(b != null) invoke(method(RBC, "setNumericCollation", boolean.class), coll, b);
    }

    if(contains(REORDER)) {
      final String v = get(REORDER);
      final IntList list = new IntList();
      final Method uscript = method(find("com.ibm.icu.lang.UScript"), "getCode", String.class);
      for(final String code : Strings.split(v, ',')) {
        if(code.equals("space")) list.add(0x1000);
        else if(code.equals("punct")) list.add(0x1001);
        else if(code.equals("symbol")) list.add(0x1002);
        else if(code.equals("currency")) list.add(0x1003);
        else if(code.equals("digit")) list.add(0x1004);
        else {
          final int[] c = code.length() == 4 ? (int[]) invoke(uscript, null, code) : null;
          if(c != null) list.add(c[0]);
          else if(nomercy) throw error(REORDER);
        }
      }
      if(!list.isEmpty()) invoke(method(RBC, "setReorderCodes", int[].class), coll, list.finish());
    }

    return new UCACollation((Comparator<Object>) coll);
  }

  /**
   * Returns the value of a yes/no option.
   * @param option option
   * @param nomercy flag for showing errors
   * @return result
   */
  private Boolean yesNo(final StringOption option, final boolean nomercy) {
    final String v = get(option);
    Boolean b = null;
    if(v.equals(YesNo.YES.toString())) b = true;
    else if(v.equals(YesNo.NO.toString())) b = false;
    else if(nomercy) throw error(option);
    return b;
  }

}
