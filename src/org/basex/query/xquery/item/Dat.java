package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import javax.xml.datatype.XMLGregorianCalendar;
import org.basex.query.xquery.XQException;
import org.basex.util.TokenBuilder;

/**
 * Date item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Dat extends Date {
  /**
   * Constructor.
   * @param d date
   */
  public Dat(final Date d) {
    super(Type.DAT);
    mon = d.mon;
    sec = d.sec - d.sec % DAYSECONDS;
    minus = d.minus;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param d date
   * @param a duration
   * @param p plus/minus flag
   */
  public Dat(final Date d, final Dur a, final boolean p) {
    this(d);
    calc(a, p);
    sec -= sec % DAYSECONDS;
  }

  /**
   * Constructor.
   * @param dt date
   * @throws XQException evaluation exception
   */
  public Dat(final byte[] dt) throws XQException {
    super(Type.DAT);
    date(dt, XPDATE);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    date(tb);
    zone(tb);
    return tb.finish();
  }

  @Override
  public XMLGregorianCalendar java() {
    return df.newXMLGregorianCalendar(mon / 12,
        mon % 12 + 1, (int) (sec / DAYSECONDS) + 1, 0, 0, 0, 0, zshift);
  }
}
