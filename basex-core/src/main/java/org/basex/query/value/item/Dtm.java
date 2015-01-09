package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.math.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * DateTime item ({@code xs:dateTime}).
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class Dtm extends ADate {
  /**
   * Constructor.
   * @param date date
   */
  public Dtm(final ADate date) {
    super(AtomType.DTM, date);
    if(hou == -1) {
      hou = 0;
      min = 0;
      sec = BigDecimal.ZERO;
    }
  }

  /**
   * Constructor.
   * @param date date
   * @param time time
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final Dat date, final Tim time, final InputInfo ii) throws QueryException {
    super(AtomType.DTM, date);

    hou = time.hou;
    min = time.min;
    sec = time.sec;
    if(zon == Short.MAX_VALUE) {
      zon = time.zon;
    } else if(zon != time.zon && time.zon != Short.MAX_VALUE) {
      throw FUNZONE_X_X.get(ii, date, time);
    }
  }

  /**
   * Constructor.
   * @param date date
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final byte[] date, final InputInfo ii) throws QueryException {
    super(AtomType.DTM);
    final int i = Token.indexOf(date, 'T');
    if(i == -1) throw dateError(date, XDTM, ii);
    date(Token.substring(date, 0, i), XDTM, ii);
    time(Token.substring(date, i + 1), XDTM, ii);
  }

  /**
   * Constructor.
   * @param ms milliseconds since January 1, 1970, 00:00:00 GMT
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final long ms, final InputInfo ii) throws QueryException {
    this(Token.token(DateTime.format(new Date(ms), DateTime.FULL)), ii);
  }

  /**
   * Constructor.
   * @param date date
   * @param dur duration
   * @param plus plus/minus flag
   * @param ii input info
   * @throws QueryException query exception
   */
  public Dtm(final Dtm date, final Dur dur, final boolean plus, final InputInfo ii)
      throws QueryException {

    this(date);
    if(dur instanceof DTDur) {
      calc((DTDur) dur, plus);
      if(yea <= MIN_YEAR || yea > MAX_YEAR) throw YEARRANGE_X.get(ii, yea);
    } else {
      calc((YMDur) dur, plus, ii);
    }
  }

  @Override
  public void timeZone(final DTDur tz, final boolean spec, final InputInfo ii)
      throws QueryException {
    tz(tz, spec, ii);
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    if(!(cmp instanceof Dtm)) return false;
    final Dtm dtm = (Dtm) cmp;
    return type == dtm.type && yea == dtm.yea && mon == dtm.mon && day == dtm.day &&
        hou == dtm.hou && min == dtm.min && zon == dtm.zon &&
        sec == null ? dtm.sec == null : sec.equals(dtm.sec);
  }
}
