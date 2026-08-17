package org.basex.core.jobs;

import static org.basex.core.jobs.JobsText.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLAccess.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.serial.*;
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
public final class Jobs {
  /** File mutex; prevents concurrent modifications. */
  private static final Object FILE = new Object();

  /** Query jobs. */
  private final ArrayList<QueryJobSpec> list = new ArrayList<>();
  /** Database context. */
  private final Context context;
  /** File. */
  private final IOFile file;

  /**
   * Constructor.
   * @param context database context
   * @throws IOException I/O exception
   */
  public Jobs(final Context context) throws IOException {
    this.context = context;

    synchronized(FILE) {
      file = context.soptions.dbPath(string(Q_JOBS.string()) + IO.XMLSUFFIX);
      if(!file.exists()) return;

      final MainOptions options = new MainOptions(false);
      options.set(MainOptions.INTPARSE, true);
      options.set(MainOptions.STRIPWS, true);
      final XNode doc = new DBNode(Parser.singleParser(file, options, ""));
      if(children(doc, Q_JOBS).next() instanceof final XNode root) {
        for(final GNode child : children(root)) {
          final QNm qname = child.qname();
          if(qname.eq(Q_JOB)) {
            final JobOptions opts = options(child);
            if(opts != null) {
              add(new QueryJobSpec(opts, new HashMap<>(), new IOContent(child.string()), null));
            }
          } else {
            Util.errln(file + ": invalid element: %.", qname);
          }
        }
      } else {
        Util.errln(file + ": No '%' root element.", Q_JOBS);
      }
    }
  }

  /**
   * Schedules all registered jobs.
   * @param context database context
   * @throws IOException I/O exception
   */
  public static void init(final Context context) throws IOException {
    synchronized(FILE) {
      new Jobs(context).start();
    }
  }

  /**
   * Schedules all jobs of the list.
   */
  private void start() {
    // start all jobs
    boolean error = false;
    final Iterator<QueryJobSpec> iter = list.iterator();
    while(iter.hasNext()) {
      try {
        new QueryJob(iter.next(), context, null, null, null);
      } catch(final QueryException ex) {
        // drop failing jobs
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
        Util.errln(file + ": %", ex);
      }
    }
  }

  /**
   * Registers a job as service.
   * @param context database context
   * @param spec job info
   * @throws IOException I/O exception
   */
  public static void register(final Context context, final QueryJobSpec spec) throws IOException {
    synchronized(FILE) {
      final Jobs jobs = new Jobs(context);
      jobs.add(spec);
      jobs.write();
    }
  }

  /**
   * Unregisters all services with the specified job ID.
   * @param context database context
   * @param id job ID
   * @throws IOException I/O exception
   */
  public static void unregister(final Context context, final String id) throws IOException {
    synchronized(FILE) {
      final Jobs jobs = new Jobs(context);
      jobs.remove(id);
      jobs.write();
    }
  }

  /**
   * Adds a query job to the list.
   * @param spec job info
   */
  private void add(final QueryJobSpec spec) {
    // skip job if an equal entry exists
    for(final QueryJobSpec job : list) {
      if(job.equals(spec)) return;
    }
    list.add(spec);
  }

  /**
   * Removes all jobs with the specified ID from the list.
   * @param id job ID
   */
  private void remove(final String id) {
    list.removeIf(job -> id.equals(job.options.get(JobOptions.ID)));
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
        Util.errln(file + ": Job attribute cannot be assigned: %", ex);
        return null;
      }
    }
    return opts;
  }

  /**
   * Writes jobs to disk.
   * @throws IOException I/O exception
   */
  private void write() throws IOException {
    synchronized(FILE) {
      // only create jobs file if jobs are registered
      if(list.isEmpty() && file.exists()) {
        file.delete();
        return;
      }
      // write jobs file
      file.parent().md();
      file.write(toXml().serialize(SerializerMode.INDENT.get()).finish());
    }
  }

  /**
   * Returns an XML representation of all jobs.
   * @return root element
   */
  public FNode toXml() {
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
}
