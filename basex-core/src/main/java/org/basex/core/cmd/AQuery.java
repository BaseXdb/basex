package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.core.parse.*;
import org.basex.core.users.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.io.serial.dot.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract class for database queries.
 *
 * @author BaseX Team 2005-15, BSD License
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
  private Value result;

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
    String error;
    if(exception != null) {
      error = Util.message(exception);
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
          try(final Serializer ser = qp.getSerializer(po)) {
            if(maxResults >= 0) {
              result = qp.cache(maxResults);
              info.evaluating += p.time();
              result.serialize(ser);
              hits = result.size();
            } else {
              hits = 0;
              final Iter ir = qp.iter();
              info.evaluating += p.time();
              for(Item it; (it = ir.next()) != null;) {
                ser.serialize(it);
                ++hits;
                checkStop();
              }
            }
          }
          qp.close();
          info.serializing += p.time();
        }
        // dump some query info
        out.flush();
        // remove string list if global locking is used and if query is updating
        if(soptions.get(StaticOptions.GLOBALLOCK) && qp.updating) {
          info.readLocked = null;
          info.writeLocked = null;
        }
        return info(info.toString(qp, out.size(), hits, options.get(MainOptions.QUERYINFO)));

      } catch(final QueryException | IOException ex) {
        exception = ex;
        error = Util.message(ex);
      } catch(final ProcException ex) {
        error = INTERRUPTED;
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        error = BASX_STACKOVERFLOW.desc;
      } catch(final RuntimeException ex) {
        extError("");
        Util.debug(info());
        throw ex;
      } finally {
        // close processor after exceptions
        if(qp != null) qp.close();
      }
    }
    return extError(error);
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
      exception = ex;
      qp.close();
      return false;
    }
  }

  /**
   * Returns a query processor instance.
   * @param query query string
   * @param ctx database context
   * @return query processor
   */
  protected QueryProcessor qp(final String query, final Context ctx) {
    if(qp == null) {
      qp = proc(new QueryProcessor(query, ctx));
      if(info == null) info = qp.qc.info;
    }
    return qp;
  }

  /**
   * Closes the query processor.
   */
  protected void closeQp() {
    if(qp != null) {
      qp.close();
      qp = null;
    }
  }

  /**
   * Returns the serialization parameters.
   * @param ctx context
   * @return serialization parameters
   */
  public String parameters(final Context ctx) {
    try {
      qp(args[0], ctx);
      parse(null);
      return qp.qc.serParams().toString();
    } catch(final QueryException ex) {
      error(Util.message(ex));
    } finally {
      qp = null;
    }
    return SerializerOptions.get(true).toString();
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
        final String path = options.get(MainOptions.QUERYPATH);
        final String dot = path.isEmpty() ? "plan.dot" :
            new IOFile(path).name().replaceAll("\\..*?$", ".dot");

        try(final BufferOutput bo = new BufferOutput(dot)) {
          try(final DOTSerializer d = new DOTSerializer(bo, options.get(MainOptions.DOTCOMPACT))) {
            d.serialize(qp.plan());
          }
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
  public final Value finish() {
    final Value r = result;
    result = null;
    return r;
  }
}
