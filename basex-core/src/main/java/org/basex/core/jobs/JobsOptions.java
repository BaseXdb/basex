package org.basex.core.jobs;

import org.basex.util.options.*;

/**
 * Jobs options.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JobsOptions extends Options {
  /** Query base-uri. */
  public static final StringOption BASE_URI = new StringOption("base-uri");
  /** Cache result. */
  public static final BooleanOption CACHE = new BooleanOption("cache");
  /** Start date/time/duration. */
  public static final StringOption START = new StringOption("start");
  /** End date/duration. */
  public static final StringOption END = new StringOption("end");
  /** Interval after which query will be repeated. */
  public static final StringOption INTERVAL = new StringOption("interval");
  /** Custom id string. */
  public static final StringOption ID = new StringOption("id");
  /** Register as service. */
  public static final BooleanOption SERVICE = new BooleanOption("service");
  /** Log entry. */
  public static final StringOption LOG = new StringOption("log");
}
