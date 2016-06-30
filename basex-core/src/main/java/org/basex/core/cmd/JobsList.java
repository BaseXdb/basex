package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.core.users.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Evaluates the 'jobs list' command.
 *
 * @author BaseX Team 2005-16, BSD License
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
    table.description = JOBS;
    table.header.add(uc(ID));
    table.header.add(TYPE);
    table.header.add(DURATION);
    table.header.add(STATE);
    table.header.add(USER);

    final Map<String, Job> jobs = context.jobs.jobs;
    for(final Job job : jobs.values()) {
      final TokenList tl = new TokenList(3);
      final JobContext jc = job.job();
      tl.add(jc.id());
      tl.add(jc.type());
      final JobState js;
      if(jc.performance != null) {
        final long ms = (System.nanoTime() - jc.performance.start()) / 1000000;
        tl.add(new DTDur(ms).string(null));
        js = job.state;
      } else {
        js = JobState.QUEUED;
      }
      tl.add(js.toString().toLowerCase(Locale.ENGLISH));
      tl.add(jc.context.user().name());
      table.contents.add(tl);
    }
    out.println(table.sort().finish());
    return true;
  }
}
