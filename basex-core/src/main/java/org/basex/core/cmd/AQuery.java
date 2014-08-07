package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.io.serial.dot.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract class for database queries.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class AQuery extends Command {
  /** Variables. */
  private final HashMap<String, String[]> vars = new HashMap<>();
  /** HTTP context. */
  private Object http;
  /** Query processor. */
  private QueryProcessor qp;
  /** Query info. */
  private QueryInfo info;

  /** Query result. */
  protected Result result;

  /**
   * Protected constructor.
   * @param perm required permission
   * @param openDB requires opened database
   * @param args arguments
   */
  AQuery(final Perm perm, final boolean openDB, final String... args) {
    super(perm, openDB, args);
  }

  /**
   * Evaluates the specified query.
   * @param query query
   * @return success flag
   */
  final boolean query(final String query) {
    final Performance p = new Performance();
    String err;
    if(cause != null) {
      err = Util.message(cause);
    } else {
      try {
        long hits = 0;
        final boolean run = options.get(MainOptions.RUNQUERY);
        final boolean serial = options.get(MainOptions.SERIALIZE);
        final int runs = Math.max(1, options.get(MainOptions.RUNS));
        for(int r = 0; r < runs; ++r) {
          // reuse existing processor instance
          if(r != 0) qp = null;
          qp(query, context);
          parse(p);
          if(r == 0) plan(false);

          qp.compile();
          info.compiling += p.time();
          if(r == 0) plan(true);
          if(!run) continue;

          final PrintOutput po = r == 0 && serial ? out : new NullOutput();
          final Serializer ser;

          if(options.get(MainOptions.CACHEQUERY)) {
            result = qp.execute();
            info.evaluating += p.time();
            ser = qp.getSerializer(po);
            result.serialize(ser);
            hits = result.size();
          } else {
            hits = 0;
            final Iter ir = qp.iter();
            info.evaluating += p.time();
            Item it = ir.next();
            ser = qp.getSerializer(po);
            while(it != null) {
              checkStop();
              ser.serialize(it);
              it = ir.next();
              ++hits;
            }
          }
          ser.close();
          qp.close();
          info.serializing += p.time();
        }
        // dump some query info
        out.flush();
        // remove string list if global locking is used and if query is updating
        if(goptions.get(GlobalOptions.GLOBALLOCK) && qp.updating) {
          info.readLocked = null;
          info.writeLocked = null;
        }
        return info(info.toString(qp, out.size(), hits, options.get(MainOptions.QUERYINFO)));

      } catch(final QueryException | IOException ex) {
        cause = ex;
        err = Util.message(ex);
      } catch(final ProcException ex) {
        err = INTERRUPTED;
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        err = BASX_STACKOVERFLOW.desc;
      } catch(final RuntimeException ex) {
        extError("");
        Util.debug(info());
        throw ex;
      } finally {
        // close processor after exceptions
        if(qp != null) qp.close();
      }
    }
    return extError(err);
  }

  /**
   * Parses the query.
   * @param p performance
   * @throws QueryException query exception
   */
  private void parse(final Performance p) throws QueryException {
    qp.http(http);
    for(final String name : vars.keySet()) {
      final String[] value = vars.get(name);
      if(name == null) qp.context(value[0], value[1]);
      else qp.bind(name, value[0], value[1]);
    }
    qp.parse();
    if(p != null) info.parsing += p.time();
  }

  /**
   * Checks if the query possibly performs updates.
   * @param ctx database context
   * @param query query string
   * @return result of check
   */
  final boolean updating(final Context ctx, final String query) {
    try {
      final Performance p = new Performance();
      qp(query, ctx);
      parse(p);
      return qp.updating;
    } catch(final QueryException ex) {
      Util.debug(ex);
      cause = ex;
      qp.close();
      return false;
    }
  }

  /**
   * Evaluates the query and returns the result as {@link DBNodes} instance.
   * @return result, or {@code null} if result cannot be represented as {@link DBNodes} instance.
   */
  final DBNodes dbNodes() {
    try {
      final Result res = qp(args[0], context).execute();
      if(res instanceof DBNodes) return (DBNodes) res;
      // return empty result set
      if(res.size() == 0) return new DBNodes(context.data());
    } catch(final QueryException ex) {
      error(Util.message(ex));
    } finally {
      qp.close();
      qp = null;
    }
    return null;
  }

  /**
   * Returns a query processor instance.
   * @param query query string
   * @param ctx database context
   * @return query processor
   */
  private QueryProcessor qp(final String query, final Context ctx) {
    if(qp == null) {
      qp = proc(new QueryProcessor(query, ctx));
      if(info == null) info = qp.qc.info;
    }
    return qp;
  }

  /**
   * Returns the serialization parameters.
   * @param ctx context
   * @return serialization parameters
   */
  public SerializerOptions parameters(final Context ctx) {
    try {
      qp(args[0], ctx);
      parse(null);
      return qp.qc.serParams();
    } catch(final QueryException ex) {
      error(Util.message(ex));
    } finally {
      qp = null;
    }
    return SerializerOptions.get(true);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound
   * @return reference
   */
  public AQuery bind(final String name, final String value) {
    return bind(name, value, null);
  }

  /**
   * Binds a variable.
   * @param name name of variable (if {@code null}, value will be bound as context value)
   * @param value value to be bound
   * @param type type
   * @return reference
   */
  public AQuery bind(final String name, final String value, final String type) {
    vars.put(name, new String[] { value, type });
    return this;
  }

  /**
   * Binds the HTTP context.
   * @param value HTTP context
   */
  public void http(final Object value) {
    http = value;
  }

  /**
   * Returns an extended error message.
   * @param err error message
   * @return result of check
   */
  private boolean extError(final String err) {
    // will only be evaluated when an error has occurred
    final StringBuilder sb = new StringBuilder();
    if(options.get(MainOptions.QUERYINFO)) {
      sb.append(info()).append(qp.info()).append(NL).append(ERROR).append(COL).append(NL);
    }
    sb.append(err);
    return error(sb.toString());
  }

  /**
   * Creates query plans.
   * @param comp compiled flag
   */
  private void plan(final boolean comp) {
    if(comp != options.get(MainOptions.COMPPLAN)) return;

    // show dot plan
    try {
      if(options.get(MainOptions.DOTPLAN)) {
        final String path = context.options.get(MainOptions.QUERYPATH);
        final String dot = path.isEmpty() ? "plan.dot" :
            new IOFile(path).name().replaceAll("\\..*?$", ".dot");

        try(final BufferOutput bo = new BufferOutput(dot)) {
          final DOTSerializer d = new DOTSerializer(bo, options.get(MainOptions.DOTCOMPACT));
          d.serialize(qp.plan());
          d.close();
        }
      }

      // show XML plan
      if(options.get(MainOptions.XMLPLAN)) {
        info(NL + QUERY_PLAN + COL);
        info(qp.plan().serialize().toString());
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    return args[0] != null && updating(ctx, args[0]);
  }

  @Override
  public boolean updated(final Context ctx) {
    return qp != null && qp.updates() != 0;
  }

  @Override
  public void databases(final LockResult lr) {
    if(qp == null) {
      lr.writeAll = true;
    } else {
      qp.databases(lr);
      info.readLocked = lr.readAll ? null : lr.read;
      info.writeLocked = lr.writeAll ? null : lr.write;
    }
  }

  @Override
  public void build(final CmdBuilder cb) {
    cb.init().xquery(0);
  }

  @Override
  public boolean stoppable() {
    return true;
  }

  @Override
  public final Result finish() {
    final Result r = result;
    result = null;
    return r;
  }
}
