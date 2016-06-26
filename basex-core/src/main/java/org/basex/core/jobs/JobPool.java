package org.basex.core.jobs;

import java.util.*;
import java.util.concurrent.*;

import org.basex.util.list.*;

/**
 * Job pool.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobPool {
  /** Running jobs. */
  private final Map<String, Job> jobs = new ConcurrentHashMap<>();

  /**
   * Adds a job.
   * @param job job
   */
  public void add(final Job job) {
    jobs.put(Long.toString(job.hashCode()), job);
  }

  /**
   * Returns a job.
   * @param id process id
   * @return job, or {@code null}
   */
  public Job get(final String id) {
    return jobs.get(id);
  }

  /**
   * Removes a job.
   * @param job job
   */
  public void remove(final Job job) {
    jobs.remove(Long.toString(job.hashCode()));
  }

  /**
   * Returns the number of jobs.
   * @return number of jobs
   */
  public int size() {
    return jobs.size();
  }

  /**
   * Returns all query ids.
   * @return query ids
   */
  public TokenList ids() {
    final TokenList list = new TokenList(size());
    for(final String id : jobs.keySet()) list.add(id);
    return list;
  }

  /**
   * Stops all jobs before closing the application.
   */
  public void close() {
    // stop running queries
    for(final Job job : jobs.values()) job.stop();
    while(!jobs.isEmpty()) Thread.yield();
  }
}
