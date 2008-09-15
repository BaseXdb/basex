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
 * MonthDay item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class MDa extends Date {
  /** MonthDay input pattern. */
  private static final Pattern MONDAY = Pattern.compile(
      "--([0-9]{2})-([0-9]{2})" + ZONE);

  /**
   * Constructor.
   * @param d date
   */
  public MDa(final Date d) {
    super(Type.MDA);
    mon = d.mon % 12;
    sec = d.sec - d.sec % DAYSECONDS;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param dt date
   * @throws XQException evaluation exception
   */
  public MDa(final byte[] dt) throws XQException {
    super(Type.MDA);

    final Matcher mt = MONDAY.matcher(Token.string(dt).trim());
    if(!mt.matches()) Err.date(type, XPMDA);
    final int m = Token.toInt(mt.group(1)) - 1;
    final long d = Token.toInt(mt.group(2)) - 1;
    if(m < 0 || m > 11 || d < 0 || d > DAYS[m] + (m == 1 ? 1 : 0))
      Err.range(type, dt);
    mon = m;
    sec = d * DAYSECONDS;
    zone(mt, 3, dt);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add("--");
    format(mon % 12 + 1, 2, '-', tb);
    format((int) (sec / DAYSECONDS) + 1, 2, (char) 0, tb);
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
    return df.newXMLGregorianCalendarDate(1, mon % 12 + 1,
        (int) (sec / DAYSECONDS) + 1, zshift);
  }
}
