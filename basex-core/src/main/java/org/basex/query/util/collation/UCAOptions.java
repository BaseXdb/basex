package org.basex.query.util.collation;

import static org.basex.util.Reflect.*;
import static org.basex.util.Strings.*;

import java.util.*;

import org.basex.core.*;
import org.basex.util.Util;
import org.basex.util.list.*;
import org.basex.util.options.*;

import com.ibm.icu.lang.*;
import com.ibm.icu.text.*;
import com.ibm.icu.util.*;

/**
 * UCA collation options.
 *
 * @author BaseX Team 2005-24, BSD License
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
  private static final Class<?> COLLATOR = find("com.ibm.icu.text.Collator");
  /** Whether the Collator class is on the class path. */
  static final boolean ACTIVE = COLLATOR != null;

  /**
   * Constructor.
   * @param fallback fallback option
   */
  public UCAOptions(final boolean fallback) {
    super(fallback);
  }

  @Override
  Collation get(final HashMap<String, String> args) throws BaseXException {
    assign(args);

    Locale locale = Locale.US;
    if(contains(LANG)) {
      final Locale l = Locales.MAP.get(get(LANG));
      if(l != null) locale = l;
      else if(!fallback) throw error(LANG);
    }

    final Collator coll = Collator.getInstance(locale);
    if(!coll.getClass().equals(RuleBasedCollator.class)) throw new BaseXException(
        "Invalid collator: %.", coll.getClass().getName());
    final RuleBasedCollator rbc = (RuleBasedCollator) coll;

    if(contains(VERSION)) {
      final String v = get(VERSION);
      try {
        final VersionInfo vi = VersionInfo.getInstance(v);
        final VersionInfo vic = rbc.getUCAVersion();
        if((vi == null || vic == null || vi.compareTo(vic) > 0) && !fallback)
          throw error(VERSION);
      } catch(final IllegalArgumentException ex) {
        Util.debug(ex);
        if(!fallback) throw new BaseXException("Version not supported: %.", v);
      }
    }

    if(contains(STRENGTH)) {
      final String v = get(STRENGTH);
      final Integer s;
      if(eq(v, "primary", "1")) s = Collator.PRIMARY;
      else if(eq(v, "secondary", "2")) s = Collator.SECONDARY;
      else if(eq(v, "tertiary", "3")) s = Collator.TERTIARY;
      else if(eq(v, "quaternary", "4")) s = Collator.QUATERNARY;
      else if(eq(v, "identical", "5")) s = Collator.IDENTICAL;
      else s = null;
      if(s != null) rbc.setStrength(s);
      else if(!fallback) throw error(STRENGTH);
    }

    if(contains(ALTERNATE)) {
      final String v = get(ALTERNATE);
      final Boolean b;
      if(eq(v, "non-ignorable")) b = Boolean.FALSE;
      else if(eq(v, "shifted", "blanked")) b = Boolean.TRUE;
      else b = null;
      if(b != null) {
        rbc.setAlternateHandlingShifted(b);
        if(eq(v, "blanked") && rbc.getStrength() == Collator.QUATERNARY) {
          rbc.setStrength(Collator.TERTIARY);
        }
      } else if(!fallback) throw error(ALTERNATE);
    }

    if(contains(BACKWARDS)) {
      final Boolean b = yesNo(BACKWARDS);
      if(b != null) rbc.setFrenchCollation(b);
    }

    if(contains(NORMALIZATION)) {
      final Boolean n = yesNo(NORMALIZATION);
      if(n != null) {
        rbc.setDecomposition(n ? Collator.CANONICAL_DECOMPOSITION : Collator.NO_DECOMPOSITION);
      }
    }

    if(contains(CASELEVEL)) {
      final Boolean c = yesNo(CASELEVEL);
      if(c != null) rbc.setCaseLevel(c);
    }

    if(contains(CASEFIRST)) {
      final String v = get(CASEFIRST);
      switch(v) {
        case "upper": rbc.setUpperCaseFirst(true); break;
        case "lower": rbc.setLowerCaseFirst(true); break;
        default: if(!fallback) throw error(CASEFIRST);
      }
    }

    if(contains(NUMERIC)) {
      final Boolean n = yesNo(NUMERIC);
      if(n != null) rbc.setNumericCollation(n);
    }

    if(contains(REORDER)) {
      final String v = get(REORDER);
      final IntList list = new IntList();
      for(final String code : split(v, ',')) {
        switch(code) {
          case "space":    list.add(Collator.ReorderCodes.SPACE);       break;
          case "punct":    list.add(Collator.ReorderCodes.PUNCTUATION); break;
          case "symbol":   list.add(Collator.ReorderCodes.SYMBOL);      break;
          case "currency": list.add(Collator.ReorderCodes.CURRENCY);    break;
          case "digit":    list.add(Collator.ReorderCodes.DIGIT);       break;
          default:
            final int[] c = code.length() == 4 ? UScript.getCode(code) : null;
            if(c != null) list.add(c[0]);
            else if(!fallback) throw error(REORDER);
            break;
        }
      }
      if(!list.isEmpty()) rbc.setReorderCodes(list.finish());
    }

    return new UCACollation(rbc);
  }

  /**
   * Returns the boolean value of a yes/no option, or {@code null}, if not recognized and fallback
   * is allowed.
   * @param option option
   * @return result
   * @throws BaseXException database exception
   */
  private Boolean yesNo(final StringOption option) throws BaseXException {
    final String v = get(option);
    if(v.equals(Text.YES)) return Boolean.TRUE;
    if(v.equals(Text.NO)) return Boolean.FALSE;
    if(!fallback) throw error(option);
    return null;
  }
}
