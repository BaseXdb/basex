package org.basex.query.xquery.item;

import static org.basex.query.xquery.XQText.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.util.Err;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Year item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Yea extends Date {
  /** Date pattern. */
  private static final Pattern YEAR = Pattern.compile("(-?)([0-9]{4})" + ZONE);

  /**
   * Constructor.
   * @param d date
   */
  public Yea(final Date d) {
    super(Type.YEA);
    mon = d.mon - (d.mon % 12);
    minus = d.minus;
    zone = d.zone;
    zshift = d.zshift;
  }

  /**
   * Constructor.
   * @param dt date
   * @throws XQException evaluation exception
   */
  public Yea(final byte[] dt) throws XQException {
    super(Type.YEA);

    final Matcher mt = YEAR.matcher(Token.string(dt).trim());
    if(!mt.matches()) Err.date(type, XPYEA);
    minus = mt.group(1).length() != 0;
    mon = Token.toInt(mt.group(2)) * 12;
    zone(mt, 3, dt);
  }

  @Override
  public byte[] str() {
    final TokenBuilder tb = new TokenBuilder();
    if(minus) tb.add('-');
    format(mon / 12, 4, (char) 0, tb);
    zone(tb);
    return tb.finish();
  }

  @Override
  public int diff(final Item it) throws XQException {
    Err.cmp(it, this);
    return 0;
  }
}
