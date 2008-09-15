package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.query.xquery.XQException;
import org.basex.util.TokenBuilder;

/**
 * Time item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Tim extends Date {
  /**
   * Constructor.
   * @param d date
   */
  public Tim(final Date d) {
    super(Type.TIM);
    sec = d.sec % DAYSECONDS;
    mil = d.mil;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration to be added/subtracted
   * @param p plus/minus flag
   */
  public Tim(final Tim d, final DTd a, final boolean p) {
    this(d);
    sec = p ^ a.minus ? sec + a.sec % DAYSECONDS : sec - a.sec % DAYSECONDS;
    mil = p ^ a.minus ? mil + a.mil : mil - a.mil;
    if(mil > 1) { mil--; sec++; } else if(mil < 0) { mil++; sec--; }
    if(sec < 0) sec += DAYSECONDS;
    sec %= DAYSECONDS;
  }

  /**
   * Constructor.
   * @param tim time
   * @throws XQException evaluation exception
   */
  public Tim(final byte[] tim) throws XQException {
    super(Type.TIM);
    time(tim, XPTIME);
    sec %= DAYSECONDS;
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    time(tb);
    zone(tb);
    return tb.finish();
  }

  @Override
  public XMLGregorianCalendar java() {
    final int t = (int) (sec % DAYSECONDS);
    return df.newXMLGregorianCalendarTime(t / 3600, t % 3600 / 60,
        t % 60, (int) mil, zshift);
  }
}
