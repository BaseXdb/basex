package org.basex.query.util.collation;

import static org.basex.util.Strings.*;

import java.util.*;

import org.basex.core.*;
import org.basex.util.*;
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
  /** Option: maxVariable. */
  public static final StringOption MAXVARIABLE = new StringOption("maxVariable", "punct");
  /** Option: numeric. */
  public static final StringOption NUMERIC = new StringOption("numeric");
  /** Option: reorder. */
  public static final StringOption REORDER = new StringOption("reorder");

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
      Integer s = null;
      switch(get(STRENGTH)) {
        case "primary":    case "1": s = Collator.PRIMARY; break;
        case "secondary":  case "2": s = Collator.SECONDARY; break;
        case "tertiary":   case "3": s = Collator.TERTIARY; break;
        case "quaternary": case "4": s = Collator.QUATERNARY; break;
        case "identical":  case "5": s = Collator.IDENTICAL; break;
        default: if(!fallback) throw error(STRENGTH);
      }
      if(s != null) rbc.setStrength(s);
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
      } else if(!fallback) {
        throw error(ALTERNATE);
      }
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
      switch(get(CASEFIRST)) {
        case "upper": rbc.setUpperCaseFirst(true); break;
        case "lower": rbc.setLowerCaseFirst(true); break;
        default: if(!fallback) throw error(CASEFIRST);
      }
    }

    if(contains(MAXVARIABLE)) {
      int m = 0;
      switch(get(MAXVARIABLE)) {
        case "space":    m = Collator.ReorderCodes.SPACE; break;
        case "punct":    m = Collator.ReorderCodes.PUNCTUATION; break;
        case "symbol":   m = Collator.ReorderCodes.SYMBOL; break;
        case "currency": m = Collator.ReorderCodes.CURRENCY; break;
        default: if(!fallback) throw error(MAXVARIABLE);
      }
      if(m != 0) rbc.setMaxVariable(m);
    }

    if(contains(NUMERIC)) {
      final Boolean n = yesNo(NUMERIC);
      if(n != null) rbc.setNumericCollation(n);
    }

    if(contains(REORDER)) {
      final IntList list = new IntList();
      for(final String code : split(get(REORDER), ',')) {
        int c = 0;
        switch(code) {
          case "space":    c = Collator.ReorderCodes.SPACE;       break;
          case "punct":    c = Collator.ReorderCodes.PUNCTUATION; break;
          case "symbol":   c = Collator.ReorderCodes.SYMBOL;      break;
          case "currency": c = Collator.ReorderCodes.CURRENCY;    break;
          case "digit":    c = Collator.ReorderCodes.DIGIT;       break;
          default:
            final int[] tmp = code.length() == 4 ? UScript.getCode(code) : null;
            if(tmp != null) c = tmp[0];
            else if(!fallback) throw error(REORDER);
        }
        if(c != 0) list.add(c);
      }
      if(!list.isEmpty()) rbc.setReorderCodes(list.finish());
    }

    return new UCACollation(rbc);
  }

  /**
   * Returns the boolean value of a yes/no option.
   * @param option option
   * @return boolean value, or {@code null} if not recognized and fallback is allowed.
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
