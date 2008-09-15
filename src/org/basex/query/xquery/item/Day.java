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
 * Day item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Day extends Date {
  /** Date pattern. */
  private static final Pattern DAY = Pattern.compile("---([0-9]{2})" + ZONE);

  /**
   * Constructor.
   * @param d date
   */
  public Day(final Date d) {
    super(Type.DAY);
    sec = d.sec - d.sec % DAYSECONDS;
    minus = d.minus;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param dt date
   * @throws XQException evaluation exception
   */
  public Day(final byte[] dt) throws XQException {
    super(Type.DAY);

    final Matcher mt = DAY.matcher(Token.string(dt).trim());
    if(!mt.matches()) Err.date(type, XPDAY);
    final long d = Token.toInt(mt.group(1)) - 1;
    if(d < 0 || d > 30) Err.range(type, dt);
    sec = d * DAYSECONDS;
    zone(mt, 2, dt);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add("---");
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
    return df.newXMLGregorianCalendarDate(1, 1,
        (int) (sec / DAYSECONDS) + 1, zshift);
  }
}
