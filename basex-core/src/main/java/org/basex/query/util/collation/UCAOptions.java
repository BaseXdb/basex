package org.basex.query.util.collation;

import static org.basex.util.Reflect.*;
import static org.basex.util.Strings.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.core.*;
import org.basex.util.Util;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * UCA collation options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UCAOptions extends CollationOptions {
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

  @Override
  @SuppressWarnings("unchecked")
  Collation get(final HashMap<String, String> args) throws BaseXException {
    assign(args);

    Locale locale = Locale.US;
    if(contains(LANG)) {
      locale = Locales.MAP.get(get(LANG));
      if(locale == null) throw error(LANG);
    }

    final Method m = method(COLLATOR, "getInstance", Locale.class);
    final Object coll = invoke(m, null, locale);

    if(!coll.getClass().equals(RBC)) throw new BaseXException(
        "Invalid collator: %.", coll.getClass().getName());

    if(contains(VERSION)) {
      final String v = get(VERSION);
      try {
        final Class<?> vc = find("com.ibm.icu.util.VersionInfo");
        final Object vi = invoke(method(vc, "getInstance", String.class), null, v);
        final Object vic = invoke(method(RBC, "getUCAVersion"), coll);
        if(vi == null || vic == null || ((Comparable<Object>) vi).compareTo(vic) > 0)
          throw error(VERSION);
      } catch(final IllegalArgumentException ex) {
        Util.debug(ex);
        throw new BaseXException("Version not supported: %.", v);
      }
    }

    if(contains(STRENGTH)) {
      final String v = get(STRENGTH);
      final int s;
      if(eq(v, "primary", "1")) s = 0;         // Collator.PRIMARY
      else if(eq(v, "secondary", "2")) s = 1;  // Collator.SECONDARY
      else if(eq(v, "tertiary", "3")) s = 2;   // Collator.TERTIARY
      else if(eq(v, "quaternary", "4")) s = 3; // Collator.QUATERNARY
      else if(eq(v, "identical", "5")) s = 15; // Collator.IDENTICAL
      else throw error(STRENGTH);
      invoke(method(RBC, "setStrength", int.class), coll, s);
    }

    if(contains(ALTERNATE)) {
      final String v = get(ALTERNATE);
      final boolean b;
      if(eq(v, "non-ignorable")) b = false;
      else if(eq(v, "shifted", "blanked")) b = true;
      else throw error(ALTERNATE);
      invoke(method(RBC, "setAlternateHandlingShifted", boolean.class), coll, b);
    }

    if(contains(BACKWARDS)) {
      invoke(method(RBC, "setFrenchCollation", boolean.class), coll, yesNo(BACKWARDS));
    }

    if(contains(NORMALIZATION)) {
      invoke(method(RBC, "setDecomposition", int.class), coll, yesNo(NORMALIZATION) ? 17 : 16);
    }

    if(contains(CASELEVEL)) {
      invoke(method(RBC, "setCaseLevel", boolean.class), coll, yesNo(CASELEVEL));
    }

    if(contains(CASEFIRST)) {
      final String v = get(CASEFIRST), f;
      switch(v) {
        case "upper": f = "setUpperCaseFirst"; break;
        case "lower": f = "setLowerCaseFirst"; break;
        default: throw error(CASEFIRST);
      }
      invoke(method(RBC, f, boolean.class), coll, true);
    }

    if(contains(NUMERIC)) {
      invoke(method(RBC, "setNumericCollation", boolean.class), coll, yesNo(NUMERIC));
    }

    if(contains(REORDER)) {
      final String v = get(REORDER);
      final IntList list = new IntList();
      final Method uscript = method(find("com.ibm.icu.lang.UScript"), "getCode", String.class);
      for(final String code : split(v, ',')) {
        switch(code) {
          case "space":    list.add(0x1000); break;
          case "punct":    list.add(0x1001); break;
          case "symbol":   list.add(0x1002); break;
          case "currency": list.add(0x1003); break;
          case "digit":    list.add(0x1004); break;
          default:
            final int[] c = code.length() == 4 ? (int[]) invoke(uscript, null, code) : null;
            if(c != null) list.add(c[0]);
            else throw error(REORDER);
            break;
        }
      }
      if(!list.isEmpty()) invoke(method(RBC, "setReorderCodes", int[].class), coll, list.finish());
    }

    return new UCACollation((Comparator<Object>) coll);
  }

  /**
   * Returns the boolean value of a yes/no option.
   * @param option option
   * @return result
   * @throws BaseXException database exception
   */
  private boolean yesNo(final StringOption option) throws BaseXException {
    final String v = get(option);
    if(v.equals(Text.YES)) return true;
    if(v.equals(Text.NO)) return false;
    throw error(option);
  }
}
