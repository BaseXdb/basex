package org.basex.core.jobs;

import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Job context.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class JobContext {
  /** Prints trace output to standard error. */
  private static final QueryTracer TRACER = info -> {
    Util.errln(info);
    return false;
  };

  /** Job prefix. */
  static final String PREFIX = "job";
  /** Query id. */
  private static AtomicLong jobId = new AtomicLong(-1);

  /** Registered locks. */
  public final Locks locks = new Locks();
  /** Time of creation. */
  public final long time = System.currentTimeMillis();

  /** Performance measurements. */
  public Performance performance;
  /** Database context. */
  public Context context;

  /** Root job. */
  private final Job job;
  /** Job id. Will be set while job is registered. */
  private String id;
  /** Job name (optional). */
  private String tp;
  /** Job description (optional). */
  private String desc;

  /**
   * Constructor.
   * @param job job
   */
  JobContext(final Job job) {
    this.job = job;
  }

  /**
   * Sets a custom id.
   * @param string custom id string
   */
  public void id(final String string) {
    id = string;
  }

  /**
   * Returns the id of the root job.
   * @return id
   */
  public String id() {
    if(id == null) id = PREFIX + jobId.updateAndGet(i -> Math.max(0, i + 1));
    return id;
  }

  /**
   * Sets a job type.
   * @param type type
   */
  public void type(final String type) {
    tp = type;
  }

  /**
   * Sets a job description.
   * @param description description
   */
  public void description(final String description) {
    desc = description;
  }

  /**
   * Returns the job type.
   * @return name
   */
  public String type() {
    return tp != null ? tp : Util.className(job);
  }

  /**
   * Returns the query tracer.
   * @return name
   */
  public QueryTracer tracer() {
    if(context != null) {
      final QueryTracer qt = (QueryTracer) context.getExternal(QueryTracer.class);
      if(qt != null) return qt;
    }
    return TRACER;
  }

  @Override
  public String toString() {
    return desc != null ? desc : job.toString();
  }
}
