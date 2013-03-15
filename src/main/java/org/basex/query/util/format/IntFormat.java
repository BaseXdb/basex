package org.basex.query.util.format;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Parser for formatting integers.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IntFormat extends FormatParser {
  /**
   * Constructor.
   * @param p picture
   * @param ii input info
   * @throws QueryException query exception
   */
  public IntFormat(final byte[] p, final InputInfo ii) throws QueryException {
    super(ii);

    final int sc = lastIndexOf(p, ';');
    final byte[] pres = presentation(sc == -1 ? p : substring(p, 0, sc));
    if(sc != -1) format(substring(p, sc + 1));

    // skip correction of case if modifier has more than one codepoint (Ww)
    cs = cl(pres, 0) < pres.length ? Case.STANDARD :
        (ch(pres, 0) & ' ') == 0 ? Case.UPPER : Case.LOWER;
    primary = lc(pres);
    if(digit == -1) digit = ch(primary, 0);
  }

  /**
   * Parses and returns the presentation modifier.
   * @param pic picture
   * @return presentation modifier
   * @throws QueryException query exception
   */
  protected byte[] presentation(final byte[] pic) throws QueryException {
    final int cl = pic.length;
    if(cl == 0) PICEMPTY.thrw(info, pic);

    // find primary format
    final TokenParser tp = new TokenParser(pic);
    int ch = tp.next();
    // check single character
    if(!tp.more()) {
      // Latin, Greek and other alphabets
      if(sequence(ch) != null ||
        // Roman sequences (lower/upper case)
        ch == 'i' || ch == 'I' ||
        // Word output (lower/upper case)
        ch == 'w' || ch == 'W' ||
        // circled, parenthesized or full stop digits
        ch == '\u2460' || ch == '\u2474' || ch == '\u2488' ||
        // Japanese numbering
        ch == KANJI[1]) return pic;
    } else {
      // Word output (title case)
      if(ch == 'W' && tp.consume('w')) return pic;
    }

    // find digit of decimal-digit-pattern
    tp.reset();
    digit = -1;
    while(digit == -1 && tp.more()) digit = zeroes(tp.next());
    // no digit found: return default primary token
    if(digit == -1) return ONE;

    // flags for mandatory-digit-sign and group-separator-sign
    boolean mds = false, gss = true;
    tp.reset();
    while(tp.more()) {
      ch = tp.next();
      final int d = zeroes(ch);
      if(d != -1) {
        // mandatory-digit-sign
        if(digit != d) DIFFMAND.thrw(info, pic);
        mds = true;
        gss = false;
      } else if(ch == '#') {
        // optional-digit-sign
        if(mds) OPTAFTER.thrw(info, pic);
        gss = false;
      } else if(!Character.isLetter(ch)) {
        // grouping-separator-sign
        if(gss) INVGROUP.thrw(info, pic);
        gss = true;
      } else {
        // any other letter: return default primary token
        INVDDPATTERN.thrw(info, pic);
      }
    }
    if(gss) INVGROUP.thrw(info, pic);
    return pic;
  }

  /**
   * Parses the format modifier.
   * @param format picture
   * @throws QueryException query exception
   */
  protected void format(final byte[] format) throws QueryException {
    final TokenParser tp = new TokenParser(format);
    // parse cardinal/ordinal flag
    if(tp.consume('c') || tp.consume('o')) {
      final TokenBuilder ord = new TokenBuilder();
      if(tp.consume('(')) {
        while(!tp.consume(')')) {
          if(!tp.more()) INVORDINAL.thrw(info, format);
          ord.add(tp.next());
        };
      }
      ordinal = ord.finish();
    }
    // parse alphabetical/traditional flag
    if(!tp.consume('a')) tp.consume('t');
    if(tp.more()) INVORDINAL.thrw(info, format);
  }
}
