package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.math.*;
import java.util.*;
import java.util.Set;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.core.parse.*;
import org.basex.core.parse.Commands.*;
import org.basex.core.users.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'jobs list' command.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JobsList extends Command {
  /**
   * Default constructor.
   */
  public JobsList() {
    super(Perm.ADMIN);
  }

  @Override
  protected boolean run() throws IOException {
    final Table table = new Table();
    table.description = JOBS_X;
    table.header.add(uc(ID));
    table.header.add(TYPE);
    table.header.add(STATE);
    table.header.add(USER);
    table.header.add(DURATION);
    table.header.add(START);
    table.header.add(END);
    table.header.add(INTERVAL);
    table.header.add(READS);
    table.header.add(WRITES);
    table.header.add(TIME);

    final JobPool jobs = context.jobs;
    for(final byte[] key : ids(context)) {
      final TokenList entry = entry(key, jobs, 0);
      if(entry != null) table.contents.add(entry);
    }
    out.println(table.sort().finish());
    return true;
  }

  /**
   * Returns all registered ids.
   * @param ctx database context
   * @return id list
   */
  public static TokenList ids(final Context ctx) {
    final JobPool jobs = ctx.jobs;
    final Set<String> set = new HashSet<>();
    set.addAll(jobs.results.keySet());
    set.addAll(jobs.active.keySet());
    set.addAll(jobs.tasks.keySet());
    final TokenList list = new TokenList(set.size());
    for(final String id : set) list.add(id);
    return sort(list);
  }

  /**
   * Creates a table entry.
   * @param key job id
   * @param jobs job pool
   * @param max maximum length of text entry
   * @return table entry, or {@code null} if the job does not exist
   */
  public static TokenList entry(final byte[] key, final JobPool jobs, final int max) {
    final String id = string(key);
    Job job = jobs.active.get(id);
    final QueryJobTask jt = jobs.tasks.get(id);
    final QueryJobResult jr = jobs.results.get(id);
    if(job == null && jr != null) job = jr.job;
    if(job == null && jt != null) job = jt.job;
    if(job == null) return null;

    final JobContext jc = job.jc();
    final long ms = jc.performance != null
        ? jc.performance.ns(false) / 1000000 : jr != null
        ? jr.time / 1000000 : -1;

    final TokenList tl = new TokenList(10);
    tl.add(id);
    tl.add(jc.type());
    tl.add(job.state.toString().toLowerCase(Locale.ENGLISH));
    tl.add(jc.context.clientName());
    tl.add(ms >= 0 ? DTDur.get(ms).string(null) : EMPTY);
    tl.add(jt != null ? dateTime(jt.start) : EMPTY);
    tl.add(jt != null && jt.end != Long.MAX_VALUE ? dateTime(jt.end) : EMPTY);
    tl.add(jt != null && jt.interval != 0 ? DTDur.get(jt.interval).string(null) : EMPTY);
    tl.add(jc.locks.reads.toString());
    tl.add(jc.locks.writes.toString());
    tl.add(dateTime(jc.time));
    if(max != 0) tl.add(chop(normalize(token(jc.toString())), max));
    return tl;
  }

  /**
   * Returns a timezone-adjusted dateTime representation.
   * @param ms milliseconds since 01/01/1970
   * @return string
   */
  private static byte[] dateTime(final long ms) {
    final Dtm dtm = Dtm.get(ms);
    final DTDur tz = new DTDur(BigDecimal.valueOf(TimeZone.getDefault().getOffset(ms) / 1000));
    try {
      dtm.timeZone(tz, true, null);
    } catch(final QueryException ex) {
      Util.debug(ex);
    }
    return dtm.string(null);
  }

  /**
   * Sorts a list of job ids.
   * @param list job id
   * @return sorted list
   */
  private static TokenList sort(final TokenList list) {
    return list.sort((token1, token2) -> {
      final byte[] id1 = substring(token1, 3), id2 = substring(token2, 3);
      final long diff = toLong(id1) - toLong(id2);
      return diff < 0 ? -1 : diff > 0 ? 1 : 0;
    }, true);
  }

  @Override
  public void addLocks() {
    // no locks needed
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init(Cmd.JOBS + " " + CmdJobs.LIST).args();
  }
}
