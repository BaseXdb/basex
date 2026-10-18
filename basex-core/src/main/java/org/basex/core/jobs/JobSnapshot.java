package org.basex.core.jobs;

import org.basex.core.*;

/**
 * Snapshot of a job.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @param id job ID
 * @param type job type
 * @param state job state
 * @param user client name (can be {@code null})
 * @param address client address (can be {@code null})
 * @param session client session (can be {@code null})
 * @param path path of the HTTP request (can be {@code null})
 * @param ms evaluation time (ms; unknown: {@code -1})
 * @param reads read locks
 * @param writes write locks
 * @param time time of creation (ms)
 * @param string job string
 * @param end time of the snapshot (ms)
 */
public record JobSnapshot(String id, String type, JobState state, String user, String address,
    String session, String path, long ms, String reads, String writes, long time, String string,
    long end) {

  /**
   * Creates a snapshot of a job.
   * @param jc job context
   * @param state job state
   * @param ms evaluation time (ms; unknown: {@code -1})
   * @return snapshot
   */
  public static JobSnapshot get(final JobContext jc, final JobState state, final long ms) {
    final Context ctx = jc.context;
    return new JobSnapshot(jc.id(), jc.type(), state, ctx.clientName(), ctx.clientAddress(),
        ctx.clientSession(), ctx.clientPath(), ms, jc.locks.reads.toString(),
        jc.locks.writes.toString(), jc.time, jc.toString(), System.currentTimeMillis());
  }
}
