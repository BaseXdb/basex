package org.basex.core.jobs;

import static org.basex.core.jobs.JobsText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class organizes persistent query jobs.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Services {
  /** Query jobs. */
  private final ArrayList<QueryJobSpec> list = new ArrayList<>();
  /** File. */
  private final IOFile file;

  /**
   * Constructor.
   * @param sopts static options
   */
  public Services(final StaticOptions sopts) {
    file = sopts.dbPath(string(Q_JOBS.string()) + IO.XMLSUFFIX);
    read();
  }

  /**
   * Schedules all registered jobs.
   * @param context database context
   */
  public synchronized void init(final Context context) {
    // start all jobs, drop failing ones
    boolean error = false;
    final Iterator<QueryJobSpec> iter = list.iterator();
    while(iter.hasNext()) {
      try {
        new QueryJob(iter.next(), context, null, null, null);
      } catch(final QueryException ex) {
        Util.errln(ex);
        iter.remove();
        error = true;
      }
    }
    // write jobs if list has changed
    if(error) {
      try {
        write();
      } catch(final IOException ex) {
        Util.errln("%: %", file, ex);
      }
    }
  }

  /**
   * Registers a job as service.
   * @param spec job info
   * @throws IOException I/O exception
   */
  public synchronized void register(final QueryJobSpec spec) throws IOException {
    // skip registration if an equal entry exists
    for(final QueryJobSpec job : list) {
      if(job.equals(spec)) return;
    }
    list.add(spec);
    write();
  }

  /**
   * Unregisters all services with the specified job ID.
   * @param id job ID
   * @throws IOException I/O exception
   */
  public synchronized void unregister(final String id) throws IOException {
    if(list.removeIf(job -> id.equals(job.options.get(JobOptions.ID)))) write();
  }

  /**
   * Returns an XML representation of all services.
   * @return root element
   */
  public synchronized FNode toXml() {
    final FBuilder root = FElem.build(Q_JOBS);
    for(final QueryJobSpec spec : list) {
      final FBuilder elem = FElem.build(Q_JOB);
      for(final Option<?> option : spec.options) {
        elem.attr(new QNm(option.name()), spec.options.get(option));
      }
      root.node(elem.text(spec.query));
    }
    return root.finish();
  }

  /**
   * Reads all registered jobs.
   */
  private synchronized void read() {
    final XNode root = XMLAccess.root(file, Q_JOBS);
    if(root == null) return;
    for(final GNode child : XMLAccess.children(root)) {
      final QNm qname = child.qname();
      if(qname.eq(Q_JOB)) {
        final JobOptions opts = options(child);
        if(opts != null) {
          list.add(new QueryJobSpec(opts, new HashMap<>(), new IOContent(child.string()), null));
        }
      } else {
        Util.errln("%: invalid element <%/>.", file, qname);
      }
    }
  }

  /**
   * Writes all registered jobs to disk.
   * @throws IOException I/O exception
   */
  private void write() throws IOException {
    // only create jobs file if jobs are registered
    if(list.isEmpty()) {
      if(file.exists()) file.delete();
    } else {
      XMLAccess.write(file, toXml());
    }
  }

  /**
   * Assign jobs options.
   * @param job job element
   * @return jobs options, or {@code null} if an error occurred
   */
  private JobOptions options(final GNode job) {
    final JobOptions opts = new JobOptions();
    for(final GNode attr : job.attributeIter()) {
      try {
        opts.assign(string(attr.name()), string(attr.string()));
      } catch(final BaseXException ex) {
        Util.errln("%: Job attribute cannot be assigned: %", file, ex);
        return null;
      }
    }
    return opts;
  }
}
