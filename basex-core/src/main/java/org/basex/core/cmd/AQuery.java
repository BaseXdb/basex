package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract class for database queries.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class AQuery extends Command {
  /** External variable bindings. */
  protected final HashMap<String, Entry<Object, String>> bindings = new HashMap<>();

  /** Query string. */
  private final String query;
  /** Query processor. */
  private QueryProcessor qp;
  /** Query info. */
  private QueryInfo info;
  /** Query plan was serialized. */
  private boolean plan;
  /** Maximum number of results (ignored if negative). */
  private int maxResults = -1;

  /**
   * Protected constructor.
   * @param openDB requires opened database
   * @param arg argument
   * @param query query string (can be identical to argument)
   */
  AQuery(final boolean openDB, final String arg, final String query) {
    super(Perm.NONE, openDB, arg);
    this.query = query;
  }

  @Override
  protected boolean run() {
    final boolean queryinfo = options.get(MainOptions.QUERYINFO);
    String error = null;
    long hits = 0;
    if(exception != null) {
      error = Util.message(exception);
    } else {
      try {
        final boolean runquery = options.get(MainOptions.RUNQUERY);
        final boolean serialize = options.get(MainOptions.SERIALIZE);
        final boolean optplan = options.get(MainOptions.OPTPLAN);
        final int runs = Math.max(1, options.get(MainOptions.RUNS));
        for(int r = 0; r < runs; ++r) {
          // reuse existing processor instance
          if(r != 0) {
            qp = null;
            popJob();
          }
          init(context);

          queryPlan(!optplan);
          qp.optimize();
          queryPlan(optplan);
          if(!runquery) continue;

          final PrintOutput po = r == 0 && serialize ? out : new NullOutput();
          try(Serializer ser = qp.serializer(po)) {
            if(maxResults >= 0) {
              qp.cache(this, maxResults);
              hits = result.size();
              result.serialize(ser);
              if(exception instanceof QueryException) throw (QueryException) exception;
              if(exception instanceof JobException) throw (JobException) exception;
            } else {
              hits = 0;
              final Iter iter = qp.iter();
              for(Item item; (item = iter.next()) != null;) {
                ser.serialize(item);
                ++hits;
                checkStop();
              }
            }
          }
          qp.close();
        }
      } catch(final QueryException | JobException | IOException ex) {
        exception = ex;
        error = Util.message(ex);
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        error = BASEX_OVERFLOW.message;
      } catch(final RuntimeException ex) {
        exception = ex;
      } finally {
        // close processor after exceptions
        if(qp != null) qp.close();
      }
    }
    // add query plan, if not done yet, and info string
    queryPlan(true);
    info(info.toString(qp, out.size(), hits, jc().locks, error == null));

    // error
    if(error != null) return error(queryinfo ? info() + ERROR + COL + NL + error : error);
    // critical error
    if(exception instanceof RuntimeException) throw (RuntimeException) exception;
    // success
    return true;
  }

  /**
   * Enforces a maximum number of query results. This method is only required by the GUI.
   * @param max maximum number of results (ignored if negative)
   */
  public final void maxResults(final int max) {
    maxResults = max;
  }

  /**
   * Returns the serialization parameters.
   * @param ctx context
   * @return serialization parameters
   */
  public final String parameters(final Context ctx) {
    try {
      init(ctx);
      return qp.qc.parameters().toString();
    } catch(final QueryException ex) {
      error(Util.message(ex));
    }
    return SerializerMode.DEFAULT.get().toString();
  }

  @Override
  public final boolean updating(final Context ctx) {
    try {
      init(ctx);
      return qp.updating;
    } catch(final QueryException | JobException ex) {
      qp.close();
      exception = ex;
      return false;
    } catch(final RuntimeException ex) {
      qp.close();
      exception = ex;
      throw ex;
    }
  }

  @Override
  public final boolean updated(final Context ctx) {
    return qp.updates() != 0;
  }

  @Override
  public final void addLocks() {
    qp.addLocks();
  }

  @Override
  public final void build(final CmdBuilder cb) {
    cb.init().add(0);
  }

  @Override
  public final boolean stoppable() {
    return true;
  }

  /**
   * Initializes the query processor.
   * @param ctx database context
   * @throws QueryException query exception
   */
  private void init(final Context ctx) throws QueryException {
    if(qp != null) return;

    if(info == null) info = new QueryInfo(ctx);
    else info.reset();

    qp = pushJob(new QueryProcessor(query, uri, ctx, info));

    for(final Entry<String, Entry<Object, String>> entry : bindings.entrySet()) {
      final Entry<Object, String> value = entry.getValue();
      qp.variable(entry.getKey(), value.getKey(), value.getValue());
    }
    qp.parse();
    qp.compile();
  }

  /**
   * Generates a query plan.
   * @param create create plan
   */
  private void queryPlan(final boolean create) {
    if(create && !plan && options.get(MainOptions.XMLPLAN)) {
      try {
        info(NL + QUERY_PLAN + COL);
        info(qp.toXml().serialize(SerializerMode.INDENT.get()).toString());
        plan = true;
      } catch(final QueryIOException ex) {
        Util.stack(ex);
      }
    }
  }
}
