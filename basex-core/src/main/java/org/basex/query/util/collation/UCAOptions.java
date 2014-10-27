package org.basex.query.util.collation;

import java.text.*;

import org.basex.io.serial.SerializerOptions.YesNo;
import org.basex.util.options.*;

/**
 * UCA collation options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class UCAOptions extends CollationOptions {
  /** Option: fallback. */
  public static final EnumOption<YesNo> FALLBACK = new EnumOption<>("fallback", YesNo.YES);
  /** Option: language. */
  public static final StringOption LANG = new StringOption("lang", "");
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
  /** Option: hiraganaQuaternary. */
  public static final StringOption HIRAGANAQUATERNARY = new StringOption("hiraganaQuaternary");
  /** Option: numeric. */
  public static final StringOption NUMERIC = new StringOption("numeric");
  /** Option: reorder. */
  public static final StringOption REORDER = new StringOption("reorder");

  @Override
  boolean assign(final Collator coll) {
    final boolean nomercy = get(FALLBACK) == YesNo.NO;
    if(nomercy) return false;

    if(contains(STRENGTH)) {
      final String s = get(STRENGTH);
      int v = -1;
      if(s.equals(BaseXCollationOptions.Strength.PRIMARY.toString()) || s.equals("1")) {
        v = 0;
      } else if(s.equals(BaseXCollationOptions.Strength.SECONDARY.toString()) || s.equals("2")) {
        v = 1;
      } else if(s.equals(BaseXCollationOptions.Strength.PRIMARY.toString()) || s.equals("3")) {
        v = 2;
      } else if(s.equals(BaseXCollationOptions.Strength.IDENTICAL.toString()) || s.equals("4")) {
        v = 3;
      }
      if(v == -1) {
        if(nomercy) return false;
      } else {
        coll.setStrength(v);
      }
    }
    return true;
  }
}
