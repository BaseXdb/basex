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
 * @author BaseX Team 2005-24, BSD License
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
      final ANode doc = new DBNode(Parser.singleParser(file, options, ""));
      final ANode root = children(doc, Q_JOBS).next();
      if(root == null) {
        Util.errln(file + ": No '%' root element.", Q_JOBS);
      } else {
        for(final ANode child : children(root)) {
          final QNm qname = child.qname();
          if(qname.eq(Q_JOB)) {
            final JobOptions opts = options(child);
            if(opts != null) {
              add(new QueryJobSpec(opts, new HashMap<>(), new IOContent(child.string())));
            }
          } else {
            Util.errln(file + ": invalid element: %.", qname);
          }
        }
      }
    }
  }

  /**
   * Schedules all registered jobs.
   */
  public void init() {
    // start all jobs
    boolean error = false;
    for(int l = 0; l < list.size(); l++) {
      final QueryJobSpec spec = list.get(l);
      try {
        new QueryJob(spec, context, null, null, null);
      } catch(final QueryException ex) {
        // drop failing jobs
        Util.errln(ex);
        list.remove(l);
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
   * Adds a query job to the list.
   * @param spec job info
   */
  public void add(final QueryJobSpec spec) {
    for(int l = 0; l < list.size(); l++) {
      final QueryJobSpec job = list.get(l);
      // job exists: replace existing entry
      if(job.equals(spec)) {
        list.set(l, job);
        return;
      }
    }
    list.add(spec);
  }

  /**
   * Removes all jobs with the specified id from the list.
   * @param id job id
   */
  public void remove(final String id) {
    list.removeIf(job -> id.equals(job.options.get(JobOptions.ID)));
  }

  /**
   * Assign jobs options.
   * @param job job element
   * @return jobs options, or {@code null} if an error occurred
   */
  private JobOptions options(final ANode job) {
    final JobOptions opts = new JobOptions();
    for(final ANode attr : job.attributeIter()) {
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
  public void write() throws IOException {
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
        elem.add(new QNm(option.name()), spec.options.get(option));
      }
      root.add(elem.add(spec.query));
    }
    return root.finish();
  }
}
