package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * YearMonth item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class YMo extends Date {
  /** Date pattern. */
  private static final Pattern YEAMON = Pattern.compile(
      "(-?)([0-9]{4})-([0-9]{2})" + ZONE);

  /**
   * Constructor.
   * @param d date
   */
  public YMo(final Date d) {
    super(Type.YMO);
    mon = d.mon;
    minus = d.minus;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param dt date
   * @throws XQException evaluation exception
   */
  public YMo(final byte[] dt) throws XQException {
    super(Type.YMO);

    final Matcher mt = YEAMON.matcher(Token.string(dt).trim());
    if(!mt.matches()) Err.date(type, XPYMO);
    final int y = Token.toInt(mt.group(2));
    final int m = Token.toInt(mt.group(3)) - 1;
    if(m < 0 || m > 11) Err.range(type, dt);
    mon = y * 12 + m;
    minus = mt.group(1).length() != 0 && mon != 0;
    zone(mt, 4, dt);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    if(minus) tb.add('-');
    format(mon / 12, 4, '-', tb);
    format(mon % 12 + 1, 2, (char) 0, tb);
    zone(tb);
    return tb.finish();
  }

  @Override
  public int diff(final Item it) throws XQException {
    Err.cmp(it, this);
    return 0;
  }

  @Override
  public XMLGregorianCalendar java() {
    return df.newXMLGregorianCalendarDate(mon / 12, mon % 12 + 1, 1, zshift);
  }
}
