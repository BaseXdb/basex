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
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract class for database queries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class AQuery extends Command {
  /** External variable bindings. */
  protected final HashMap<String, Object> vars = new HashMap<>();

  /** Query processor. */
  private QueryProcessor qp;
  /** Query info. */
  private QueryInfo info;
  /** Query result. */
  private Value result;
  /** Query plan was serialized. */
  private boolean plan;

  /**
   * Protected constructor.
   * @param openDB requires opened database
   * @param args arguments
   */
  AQuery(final boolean openDB, final String... args) {
    super(Perm.NONE, openDB, args);
  }

  /**
   * Evaluates the specified query.
   * @param query query
   * @return success flag
   */
  final boolean query(final String query) {
    String error;
    if(exception != null) {
      error = Util.message(exception);
    } else {
      try {
        long hits = 0;
        final boolean run = options.get(MainOptions.RUNQUERY);
        final boolean serial = options.get(MainOptions.SERIALIZE);
        final boolean compplan = options.get(MainOptions.COMPPLAN);
        final int runs = Math.max(1, options.get(MainOptions.RUNS));
        for(int r = 0; r < runs; ++r) {
          // reuse existing processor instance
          if(r != 0) {
            qp = null;
            popJob();
          }
          init(query, context);
          if(!compplan) queryPlan();

          final Performance perf = new Performance();
          for(final Entry<String, Object> entry : vars.entrySet()) {
            final String name = entry.getKey();
            final Object value = entry.getValue();
            if(value instanceof Value) {
              final Value val = (Value) value;
              if(name == null) qp.context(val);
              else qp.bind(name, val);
            } else {
              // will always be a string array
              final String[] strings = (String[]) value;
              if(name == null) qp.context(strings[0], strings[1]);
              else qp.bind(name, strings[0], strings[1]);
            }
          }

          qp.compile();
          info.compiling += perf.ns();
          if(compplan) queryPlan();
          if(!run) continue;

          final PrintOutput po = r == 0 && serial ? out : new NullOutput();
          try(Serializer ser = qp.getSerializer(po)) {
            if(maxResults >= 0) {
              result = qp.cache(maxResults);
              info.evaluating += perf.ns();
              result.serialize(ser);
              hits = result.size();
            } else {
              hits = 0;
              final Iter iter = qp.iter();
              info.evaluating += perf.ns();
              for(Item item; (item = iter.next()) != null;) {
                ser.serialize(item);
                ++hits;
                checkStop();
              }
            }
          }
          qp.close();
          info.serializing += perf.ns();
        }
        return info(info.toString(qp, out.size(), hits, jc().locks));

      } catch(final QueryException | IOException ex) {
        exception = ex;
        error = Util.message(ex);
      } catch(final JobException ex) {
        error = ex.getMessage();
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        error = BASEX_OVERFLOW.message;
      } catch(final RuntimeException ex) {
        extError("");
        throw ex;
      } finally {
        // close processor after exceptions
        if(qp != null) qp.close();
      }
    }
    queryPlan();
    return extError(error);
  }

  /**
   * Checks if the query is updating.
   * @param ctx database context
   * @param query query string
   * @return result of check
   */
  final boolean updates(final Context ctx, final String query) {
    try {
      init(query, ctx);
      return qp.updating;
    } catch(final QueryException ex) {
      Util.debug(ex);
      exception = ex;
      qp.close();
      return false;
    }
  }

  /**
   * Initializes the query processor, .
   * @param query query string
   * @param ctx database context
   * @throws QueryException query exception
   */
  private void init(final String query, final Context ctx) throws QueryException {
    final Performance perf = new Performance();
    if(qp == null) qp = pushJob(new QueryProcessor(query, uri, ctx));
    if(info == null) info = qp.qc.info;
    qp.parse();
    qp.qc.info.parsing += perf.ns();
  }

  /**
   * Returns the serialization parameters.
   * @param ctx context
   * @return serialization parameters
   */
  public final String parameters(final Context ctx) {
    try {
      init(args[0], ctx);
      return qp.qc.serParams().toString();
    } catch(final QueryException ex) {
      error(Util.message(ex));
    } finally {
      qp = null;
      popJob();
    }
    return SerializerMode.DEFAULT.get().toString();
  }

  /**
   * Returns an extended error message.
   * @param message error message
   * @return result of check
   */
  private boolean extError(final String message) {
    // will only be evaluated when an error has occurred
    final StringBuilder sb = new StringBuilder();
    if(options.get(MainOptions.QUERYINFO)) {
      sb.append(info()).append(qp.info()).append(NL).append(ERROR).append(COL).append(NL);
    }
    sb.append(message);
    return error(sb.toString());
  }

  /**
   * Generates a query plan.
   */
  private void queryPlan() {
    if(!plan && qp != null) {
      try {
        // show XML plan
        if(options.get(MainOptions.XMLPLAN)) {
          info(NL + QUERY_PLAN + COL);
          info(qp.plan().serialize().toString());
        }
        plan = true;
      } catch(final Exception ex) {
        Util.stack(ex);
      }
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    return updates(ctx, args[0]);
  }

  @Override
  public final boolean updated(final Context ctx) {
    return qp != null && qp.updates() != 0;
  }

  @Override
  public void addLocks() {
    if(qp == null) {
      jc().locks.writes.addGlobal();
    } else {
      qp.addLocks();
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().add(0);
  }

  @Override
  public boolean stoppable() {
    return true;
  }

  @Override
  public final Value result() {
    final Value r = result;
    result = null;
    return r;
  }
}
