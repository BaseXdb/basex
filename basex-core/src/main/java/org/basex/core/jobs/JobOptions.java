package org.basex.core.jobs;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.options.*;

/**
 * Jobs options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class JobOptions extends Options {
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
  /** Maximum amount of megabytes that may be allocated by the query. */
  public static final NumberOption MEMORY = new NumberOption("memory", 0);
  /** Timeout in seconds. */
  public static final ValueOption TIMEOUT =
      new ValueOption("timeout", BasicType.DECIMAL.seqType(), Dec.ZERO);
  /** Permission. */
  public static final EnumOption<Perm> PERMISSION = new EnumOption<>("permission", Perm.ADMIN);
  /** Start date/time/duration. */
  public static final StringOption START = new StringOption("start", null, TIME_TYPE);
  /** End date/duration. */
  public static final StringOption END = new StringOption("end", null, TIME_TYPE);
  /** Interval after which query will be repeated. */
  public static final StringOption INTERVAL = new StringOption("interval", null, INTERVAL_TYPE);
  /** Cron expression describing when the query will be repeated. */
  public static final StringOption CRON = new StringOption("cron");
  /** Custom ID string. */
  public static final StringOption ID = new StringOption("id");
  /** Log entry. */
  public static final StringOption LOG = new StringOption("log");

  /** Runtime restrictions, which must not be assigned to a service. */
  public static final Option<?>[] RESTRICTIONS = { MEMORY, TIMEOUT, PERMISSION };
}
