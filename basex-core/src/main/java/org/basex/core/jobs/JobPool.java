package org.basex.core.jobs;

import static org.basex.util.Token.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Job pool.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class JobPool {
  /** Number of queries to be queued. */
  static final int MAXQUERIES = 1000;

  /** Queued or running jobs. */
  public final Map<String, Job> active = new ConcurrentHashMap<>();
  /** Cached results. */
  public final Map<String, QueryJobResult> results = new ConcurrentHashMap<>();
  /** Timer tasks. */
  public final Map<String, QueryJobTask> tasks = new ConcurrentHashMap<>();

  /** Timer. */
  final Timer timer = new Timer(true);
  /** Timeout (ms). */
  private final long timeout;

  /**
   * Constructor.
   * @param sopts static options
   */
  public JobPool(final StaticOptions sopts) {
    timeout = sopts.get(StaticOptions.CACHETIMEOUT) * 1000L;
  }

  /**
   * Registers a job (puts it on a queue).
   * @param job job
   */
  public void register(final Job job) {
    while(active.size() >= MAXQUERIES) Performance.sleep(1);
    active.put(job.jc().id(), job);
  }

  /**
   * Unregisters a job.
   * @param job job
   */
  public void unregister(final Job job) {
    active.remove(job.jc().id());
  }

  /**
   * Stops all jobs before closing the application.
   */
  public synchronized void close() {
    // stop running tasks and queries
    timer.cancel();
    for(final Job job : active.values()) job.stop();
    while(!active.isEmpty()) Performance.sleep(1);
  }

  /**
   * Discards a result after the timeout.
   * @param job job
   */
  public void scheduleResult(final Job job) {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        results.remove(job.jc().id());
      }
    }, timeout);
  }

  /**
   * Returns all registered IDs.
   * @return sorted id list
   */
  public TokenList ids() {
    final Set<String> set = new HashSet<>();
    set.addAll(results.keySet());
    set.addAll(active.keySet());
    set.addAll(tasks.keySet());
    final TokenList ids = new TokenList(set.size());
    for(final String id : set) ids.add(id);

    // compare default job counter, or compare custom ids as strings
    final byte[] prefix = token(JobContext.PREFIX);
    final int pl = prefix.length;
    return ids.sort((id1, id2) -> startsWith(id1, prefix) && startsWith(id2, prefix) ?
        toInt(substring(id1, pl)) - toInt(substring(id2, pl)) : diff(id1, id2), true);
  }

  /**
   * Removes a job.
   * @param id id
   * @return return success flag
   */
  public boolean remove(final String id) {
    // stop scheduled task
    final TimerTask task = tasks.remove(id);
    if(task != null) task.cancel();
    // send stop signal to job
    final Job job = active.get(id);
    if(job != null) job.stop();
    // remove potentially cached result
    results.remove(id);

    return job != null || task != null;
  }
}
