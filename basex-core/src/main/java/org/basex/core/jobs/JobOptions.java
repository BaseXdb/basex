package org.basex.core.jobs;

import org.basex.core.*;
import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * Jobs options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobOptions extends Options {
  /** Required type of a point in time (see {@link QueryJob#toTime}). */
  private static final SeqType TIME_TYPE = ChoiceItemType.get(BasicType.STRING, BasicType.INTEGER,
      BasicType.DAY_TIME_DURATION, BasicType.TIME, BasicType.DATE_TIME).seqType();
  /** Required type of a repetition interval. */
  private static final SeqType INTERVAL_TYPE =
      ChoiceItemType.get(BasicType.STRING, BasicType.DAY_TIME_DURATION).seqType();

  /** Query base-uri. */
  public static final StringOption BASE_URI = new StringOption(CommonOptions.BASE_URI);
  /** Cache result. */
  public static final BooleanOption CACHE = new BooleanOption("cache");
  /** Start date/time/duration. */
  public static final StringOption START = new StringOption("start", null, TIME_TYPE);
  /** End date/duration. */
  public static final StringOption END = new StringOption("end", null, TIME_TYPE);
  /** Interval after which query will be repeated. */
  public static final StringOption INTERVAL = new StringOption("interval", null, INTERVAL_TYPE);
  /** Custom ID string. */
  public static final StringOption ID = new StringOption("id");
  /** Register as service. */
  public static final BooleanOption SERVICE = new BooleanOption("service");
  /** Log entry. */
  public static final StringOption LOG = new StringOption("log");
}
