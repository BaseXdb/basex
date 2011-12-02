package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * YearMonth Duration item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class YMd extends Dur {
  /** YearMonth pattern. */
  private static final Pattern DUR = Pattern.compile(
      "(-?)P(([0-9]+)Y)?(([0-9]+)M)?");

  /**
   * Constructor.
   * @param it duration item
   */
  YMd(final Dur it) {
    super(AtomType.YMD);
    mon = it.mon;
  }

  /**
   * Constructor.
   * @param it duration item
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   */
  public YMd(final YMd it, final YMd a, final boolean p) {
    this(it);
    mon += p ? a.mon : -a.mon;
  }

  /**
   * Constructor.
   * @param it duration item
   * @param f factor
   * @param m multiplication/division flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public YMd(final Dur it, final double f, final boolean m, final InputInfo ii)
      throws QueryException {

    this(it);
    if(Double.isNaN(f)) DATECALC.thrw(ii, description(), f);
    if(m ? f == 1 / 0d || f == -1 / 0d : f == 0)
      DATEZERO.thrw(ii, description());
    mon = (int) StrictMath.round(m ? mon * f : mon / f);
  }

  /**
   * Constructor.
   * @param v value
   * @param ii input info
   * @throws QueryException query exception
   */
  YMd(final byte[] v, final InputInfo ii) throws QueryException {
    super(AtomType.YMD);
    final String val = Token.string(v).trim();
    final Matcher mt = DUR.matcher(val);
    if(!mt.matches() || val.endsWith("P")) dateErr(v, XYMD, ii);

    final int y = mt.group(2) != null ? Token.toInt(mt.group(3)) : 0;
    final int m = mt.group(4) != null ? Token.toInt(mt.group(5)) : 0;

    mon = y * 12 + m;
    if(!mt.group(1).isEmpty()) mon = -mon;
  }

  /**
   * Returns the years and months.
   * @return year
   */
  public int ymd() {
    return mon;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    final TokenBuilder tb = new TokenBuilder();
    if(mon < 0) tb.add('-');
    tb.add('P');
    if(yea() != 0) { tb.addLong(Math.abs(yea())); tb.add('Y'); }
    if(mon() != 0) { tb.addLong(Math.abs(mon())); tb.add('M'); }
    if(mon == 0) tb.add("0M");
    return tb.finish();
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    if(it.type != type) Err.diff(ii, it, this);
    return mon - ((Dur) it).mon;
  }
}
