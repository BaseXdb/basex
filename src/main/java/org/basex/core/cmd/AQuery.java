package org.basex.core.cmd;

import org.basex.core.*;

import static org.basex.core.Text.*;
import org.basex.data.Result;
import org.basex.io.IOFile;
import org.basex.io.out.ArrayOutput;
import org.basex.io.out.BufferOutput;
import org.basex.io.out.NullOutput;
import org.basex.io.out.PrintOutput;
import org.basex.io.serial.DOTSerializer;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import static org.basex.query.util.Err.XPSTACK;
import org.basex.util.Performance;
import org.basex.util.Util;

import java.io.IOException;

/**
 * Abstract class for database queries.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class AQuery extends Command {
  /** Query result. */
  Result result;

  /** Query processor. */
  private QueryProcessor qp;
  /** Query exception. */
  private QueryException qe;
  /** Initial parsing time. */
  private long init;
  /** Parsing time. */
  private long pars;
  /** Compilation time. */
  private long comp;
  /** Evaluation time. */
  private long eval;
  /** Printing time. */
  private long prnt;

  /**
   * Protected constructor.
   * @param flags command flags
   * @param arg arguments
   */
  AQuery(final int flags, final String... arg) {
    super(flags, arg);
  }

  /**
   * Evaluates the specified query.
   * @param query query
   * @return success flag
   */
  final boolean query(final String query) {
    final Performance p = new Performance();
    String err;
    String inf = "";
    if(qe != null) {
      err = qe.getMessage();
    } else {
      try {
        final boolean serial = prop.is(Prop.SERIALIZE);
        long hits = 0;
        int updates = 0;
        final int runs = Math.max(1, prop.num(Prop.RUNS));
        for(int r = 0; r < runs; ++r) {
          // reuse existing processor instance
          if(r != 0) qp = null;
          qp = queryProcessor(query, context);
          qp.parse();
          pars += init + p.time();
          init = 0;
          if(r == 0) plan(false);
          qp.compile();
          comp += p.time();
          if(r == 0) plan(true);

          final PrintOutput po = r == 0 && serial ? out : new NullOutput();
          final Serializer ser;

          if(prop.is(Prop.CACHEQUERY)) {
            result = qp.execute();
            eval += p.time();
            ser = qp.getSerializer(po);
            result.serialize(ser);
            hits = result.size();
          } else {
            final Iter ir = qp.iter();
            eval += p.time();
            hits = 0;
            Item it = ir.next();
            ser = qp.getSerializer(po);
            while(it != null) {
              checkStop();
              ser.openResult();
              it.serialize(ser);
              ser.closeResult();
              it = ir.next();
              ++hits;
            }
          }
          updates = qp.updates();
          ser.close();
          qp.close();
          prnt += p.time();
        }
        // dump some query info
        if(prop.is(Prop.QUERYINFO)) evalInfo(query, hits, updates, runs);
        out.flush();
        final long time = pars + comp + eval + prnt;
        return info(NL + QUERY_EXECUTED_X, Performance.getTime(time, runs));
      } catch(final QueryException ex) {
        Util.debug(ex);
        err = ex.getMessage();
      } catch(final IOException ex) {
        Util.debug(ex);
        err = ex.getMessage();
      } catch(final ProgressException ex) {
        err = INTERRUPTED;
        // store any useful info (e.g. query plan):
        inf = info();
      } catch(final RuntimeException ex) {
        Util.debug(qp.info());
        throw ex;
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        err = XPSTACK.desc;
      } finally {
        // close processor after exceptions
        if(qp != null) try { qp.close(); } catch(final QueryException ex) { }
      }
    }

    error(err);
    if(Util.debug || err.startsWith(INTERRUPTED)) {
      info(NL);
      info(QUERY_CC + query);
      info(qp.info());
      info(inf);
    }
    return false;
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
      qp = progress(new QueryProcessor(qu, ctx));
      qp.parse();
      init = p.time();
      return qp.updating;
    } catch(final QueryException ex) {
      Util.debug(ex);
      qe = ex;
      if(qp != null) try { qp.close(); } catch(final QueryException e) { }
      return false;
    }
  }

  @Override
  public boolean updating(final Context ctx) {
    return super.updating(ctx) || args[0] != null && updating(ctx, args[0]);
  }

  @Override
  public String pinned(final Context ctx) {
    return null;
  }

  @Override
  public boolean updated() {
    return qp == null || qp.updates() != 0;
  }

  /**
   * Performs the first argument as XQuery and returns a node set.
   */
  final void queryNodes() {
    try {
      result = queryProcessor(args[0], context).queryNodes();
    } catch(final QueryException ex) {
      Util.debug(ex);
      qp = null;
      error(ex.getMessage());
    }
  }

  /**
   * Returns a query processor instance.
   * @param query query string
   * @param ctx database context
   * @return query processor
   */
  private QueryProcessor queryProcessor(final String query, final Context ctx) {
    if(qp == null) qp = progress(new QueryProcessor(query, ctx));
    return qp;
  }

  @Override
  public final Result result() {
    final Result r = result;
    result = null;
    return r;
  }

  /**
   * Adds evaluation information to the information string.
   * @param query query string
   * @param hits information
   * @param updates updated items
   * @param runs number of runs
   */
  private void evalInfo(final String query, final long hits, final long updates,
      final int runs) {

    final long total = pars + comp + eval + prnt;
    info(NL);
    info(QUERY_CC + QueryProcessor.removeComments(query, Integer.MAX_VALUE));
    info(qp.info());
    info(PARSING_CC + Performance.getTime(pars, runs));
    info(COMPILING_CC + Performance.getTime(comp, runs));
    info(EVALUATING_CC + Performance.getTime(eval, runs));
    info(PRINTING_CC + Performance.getTime(prnt, runs));
    info(TOTAL_TIME_CC + Performance.getTime(total, runs) + NL);
    info(HITS_X_CC + hits + ' ' + (hits == 1 ? ITEM : ITEMS));
    info(UPDATED_CC + updates + ' ' + (updates == 1 ? ITEM : ITEMS));
    info(PRINTED_CC + Performance.format(out.size()));
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
        qp.plan(d);
        d.close();

        if(prop.is(Prop.DOTDISPLAY))
          new ProcessBuilder(prop.get(Prop.DOTTY), dot).start();
      }
      // show XML plan
      if(prop.is(Prop.XMLPLAN)) {
        final ArrayOutput ao = new ArrayOutput();
        qp.plan(Serializer.get(ao));
        info(NL + QUERY_PLAN_C);
        info(ao.toString());
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    } finally {
      if(bo != null) try { bo.close(); } catch(final IOException ex) { }
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().xquery(0);
  }

  @Override
  public boolean stoppable() {
    return true;
  }
}
