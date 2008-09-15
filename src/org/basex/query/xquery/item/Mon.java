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
 * Month item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Mon extends Date {
  /** Date pattern. */
  private static final Pattern MONTH = Pattern.compile("--([0-9]{2})" + ZONE);

  /**
   * Constructor.
   * @param d date
   */
  public Mon(final Date d) {
    super(Type.MON);
    mon = d.mon % 12;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param dt date
   * @throws XQException evaluation exception
   */
  public Mon(final byte[] dt) throws XQException {
    super(Type.MON);

    final Matcher mt = MONTH.matcher(Token.string(dt).trim());
    if(!mt.matches()) Err.date(type, XPMON);
    final int m = Token.toInt(mt.group(1)) - 1;
    if(m < 0 || m > 11) Err.range(type, dt);
    mon = m;
    zone(mt, 2, dt);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    tb.add("--");
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
    return df.newXMLGregorianCalendarDate(1, mon % 12 + 1, 1, zshift);
  }
}
