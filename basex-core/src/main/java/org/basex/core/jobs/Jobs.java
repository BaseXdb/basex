package org.basex.core.jobs;

import static org.basex.core.jobs.JobsText.*;
import static org.basex.util.Token.*;
import static org.basex.util.XMLAccess.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * This class organizes persistent query jobs.
 *
 * @author BaseX Team 2005-21, BSD License
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
    file = context.soptions.dbPath(string(JOBS) + IO.XMLSUFFIX);

    // parse jobs file
    final IOContent content;
    synchronized(FILE) {
      if(!file.exists()) return;
      content = new IOContent(file.read(), file.path());
    }

    final MainOptions options = new MainOptions(false);
    options.set(MainOptions.INTPARSE, true);
    final ANode doc = new DBNode(Parser.singleParser(content, options, ""));
    final ANode root = children(doc, JOBS).next();
    if(root == null) {
      Util.errln(file + ": No '%' root element.", JOBS);
    } else {
      for(final ANode child : children(root)) {
        final byte[] qname = child.qname().id();
        if(eq(qname, JOB)) {
          final JobsOptions opts = options(child);
          if(opts != null) {
            add(new QueryJobSpec(opts, new HashMap<>(), new IOContent(child.string())));
          }
        } else {
          Util.errln(file + ": invalid element: %.", qname);
        }
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
    list.removeIf(job -> id.equals(job.options.get(JobsOptions.ID)));
  }

  /**
   * Schedules all registered jobs.
   */
  public void run() {
    boolean error = false;
    // request size every time (list may shrink)
    for(int l = 0; l < list.size(); l++) {
      final QueryJobSpec spec = list.get(l);
      try {
        new QueryJob(spec, context, null, null);
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
   * Assign jobs options.
   * @param job job element
   * @return jobs options, or {@code null} if an error occurred
   */
  private JobsOptions options(final ANode job) {
    final JobsOptions opts = new JobsOptions();
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
    final FElem xml = toXML();
    synchronized(FILE) {
      // only create jobs file if jobs are registered
      if(list.isEmpty()) {
        if(file.exists()) {
          file.delete();
          return;
        }
      }

      // write jobs file
      file.parent().md();
      file.write(xml.serialize().finish());
    }
  }

  /**
   * Returns an XML representation of all jobs.
   * @return root element
   */
  public FElem toXML() {
    final FElem root = new FElem(JOBS);
    for(final QueryJobSpec spec : list) {
      final FElem elem = new FElem(JOB);
      for(final Option<?> option : spec.options) {
        final Object value = spec.options.get(option);
        if(value != null) elem.add(option.name(), value.toString());
      }
      root.add(elem.add(spec.query));
    }
    return root;
  }
}
