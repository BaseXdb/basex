package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.parse.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Abstract class for database queries.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class AQuery extends Command {
  /** Query info. */
  private final QueryInfo qi = new QueryInfo();
  /** Query result. */
  Result result;

  /** Query processor. */
  private QueryProcessor qp;
  /** Query exception. */
  private QueryException qe;

  /**
   * Protected constructor.
   * @param p required permission
   * @param d requires opened database
   * @param arg arguments
   */
  AQuery(final Perm p, final boolean d, final String... arg) {
    super(p, d, arg);
  }

  /**
   * Evaluates the specified query.
   * @param query query
   * @return success flag
   */
  final boolean query(final String query) {
    final Performance p = new Performance();
    String err;
    if(qe != null) {
      err = qe.getMessage();
    } else {
      try {
        final boolean serial = prop.is(Prop.SERIALIZE);
        qi.runs = Math.max(1, prop.num(Prop.RUNS));
        long hits = 0;
        for(int r = 0; r < qi.runs; ++r) {
          // reuse existing processor instance
          if(r != 0) qp = null;
          qp = queryProcessor(query, context);
          qp.parse();
          qi.pars += p.time();
          if(r == 0) plan(false);
          qp.compile();
          qi.cmpl += p.time();
          if(r == 0) plan(true);

          final PrintOutput po = r == 0 && serial ? out : new NullOutput();
          final Serializer ser;

          if(prop.is(Prop.CACHEQUERY)) {
            result = qp.execute();
            qi.evlt += p.time();
            ser = qp.getSerializer(po);
            result.serialize(ser);
            hits = result.size();
          } else {
            hits = 0;
            final Iter ir = qp.iter();
            qi.evlt += p.time();
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
          qi.srlz += p.time();
        }
        // dump some query info
        out.flush();
        // remove string list if global locking is used and if query is updating
        if(mprop.is(MainProp.GLOBALLOCK) && qp.updating)
          qi.readLocked = qi.writeLocked = null;
        return info(qi.toString(qp, out, hits, prop.is(Prop.QUERYINFO)));

      } catch(final QueryException ex) {
        err = Util.message(ex);
      } catch(final IOException ex) {
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
   * Returns an extended error message.
   * @param err error message
   * @return result of check
   */
  final boolean extError(final String err) {
    // will only be evaluated when an error has occurred
    final StringBuilder sb = new StringBuilder();
    if(prop.is(Prop.QUERYINFO)) {
      sb.append(info()).append(qp.info()).append(NL).append(ERROR_C).append(NL);
    }
    sb.append(err);
    return error(sb.toString());
  }

  /**
   * Checks if the query might perform updates.
   * @param ctx database context
   * @param qu query
   * @return result of check
   */
  final boolean updating(final Context ctx, final String qu) {
    // keyword found; parse query to get sure
    try {
      final Performance p = new Performance();
      qp = queryProcessor(qu, ctx);
      qp.parse();
      qi.pars = p.time();
      return qp.updating;
    } catch(final QueryException ex) {
      Util.debug(ex);
      qe = ex;
      if(qp != null) qp.close();
      return false;
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
    if(null == qp)
      lr.writeAll = true;
    else {
      qp.databases(lr);
      qi.readLocked = lr.readAll ? null : lr.read;
      qi.writeLocked = lr.writeAll ? null : lr.write;
    }
  }

  /**
   * Performs the first argument as XQuery and returns a node set.
   */
  final void queryNodes() {
    try {
      result = queryProcessor(args[0], context).queryNodes();
    } catch(final QueryException ex) {
      qp = null;
      error(Util.message(ex));
    }
  }

  /**
   * Returns a query processor instance.
   * @param query query string
   * @param ctx database context
   * @return query processor
   */
  private QueryProcessor queryProcessor(final String query, final Context ctx) {
    if(qp == null) qp = proc(new QueryProcessor(query, ctx));
    return qp;
  }

  @Override
  public final Result result() {
    final Result r = result;
    result = null;
    return r;
  }

  /**
   * Creates query plans.
   * @param c compiled flag
   */
  private void plan(final boolean c) {
    if(c != prop.is(Prop.COMPPLAN)) return;

    // show dot plan
    BufferOutput bo = null;
    try {
      if(prop.is(Prop.DOTPLAN)) {
        final String path = context.prop.get(Prop.QUERYPATH);
        final String dot = path.isEmpty() ? "plan.dot" :
            new IOFile(path).name().replaceAll("\\..*?$", ".dot");

        bo = new BufferOutput(dot);
        final DOTSerializer d = new DOTSerializer(bo, prop.is(Prop.DOTCOMPACT));
        d.serialize(qp.plan());
        d.close();

        if(prop.is(Prop.DOTDISPLAY))
          new ProcessBuilder(prop.get(Prop.DOTTY), dot).start();
      }
      // show XML plan
      if(prop.is(Prop.XMLPLAN)) {
        info(NL + QUERY_PLAN_C);
        info(qp.plan().serialize().toString());
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    } finally {
      if(bo != null) try { bo.close(); } catch(final IOException ignored) { }
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
  public boolean registered() {
    return qp.registered();
  }

  @Override
  public void registered(final boolean reg) {
    qp.registered(reg);
  }
}
