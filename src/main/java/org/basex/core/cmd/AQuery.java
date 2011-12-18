package org.basex.core.cmd;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.CommandBuilder;
import org.basex.core.Context;
import org.basex.core.ProgressException;
import org.basex.core.Prop;
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
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * Abstract class for database queries.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
abstract class AQuery extends Command {
  /** Query result. */
  protected Result result;

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
  protected AQuery(final int flags, final String... arg) {
    super(flags, arg);
  }

  /**
   * Evaluates the specified query.
   * @param query query
   * @return success flag
   */
  protected final boolean query(final String query) {
    final Performance p = new Performance();

    String err = null;
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
          pars += init + p.getTime();
          init = 0;
          if(r == 0) plan(false);
          qp.compile();
          comp += p.getTime();
          if(r == 0) plan(true);

          final PrintOutput po = r == 0 && serial ? out : new NullOutput();
          Serializer ser;

          if(prop.is(Prop.CACHEQUERY)) {
            result = qp.execute();
            eval += p.getTime();
            ser = qp.getSerializer(po);
            result.serialize(ser);
            hits = result.size();
          } else {
            final Iter ir = qp.iter();
            eval += p.getTime();
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
          prnt += p.getTime();
        }
        // dump some query info
        if(prop.is(Prop.QUERYINFO)) evalInfo(query, hits, updates, runs);
        out.flush();
        return info(NL + QUERYEXEC, perf.getTimer(runs));
      } catch(final QueryException ex) {
        Util.debug(ex);
        err = ex.getMessage();
      } catch(final IOException ex) {
        Util.debug(ex);
        err = ex.getMessage();
      } catch(final ProgressException ex) {
        err = PROGERR;
        // store any useful info (e.g. query plan):
        inf = info();
      } catch(final RuntimeException ex) {
        Util.debug(qp.info());
        throw ex;
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        err = XPSTACK.desc;
      }
      // close processor after exceptions
      if(qp != null) try { qp.close(); } catch(final QueryException ex) { }
    }

    error(err);
    if(Util.debug || err.startsWith(PROGERR)) {
      info(NL);
      info(QUERYSTRING + query);
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
  protected final boolean updating(final Context ctx, final String qu) {
    // keyword found; parse query to get sure
    try {
      final Performance p = new Performance();
      qp = progress(new QueryProcessor(qu, ctx));
      qp.parse();
      init = p.getTime();
      return qp.ctx.updating;
    } catch(final QueryException ex) {
      Util.debug(ex);
      qe = ex;
      if(qp != null) try { qp.close(); } catch(final QueryException e) { }
      return false;
    }
  }

  /**
   * Performs the first argument as XQuery and returns a node set.
   */
  protected final void queryNodes() {
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
  protected final QueryProcessor queryProcessor(final String query,
      final Context ctx) {
    if(qp == null) qp = progress(new QueryProcessor(query, ctx));
    return qp;
  }

  @Override
  public final Result result() {
    return result;
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
    info(QUERYSTRING + QueryProcessor.removeComments(query, Integer.MAX_VALUE));
    info(qp.info());
    info(QUERYPARSE + Performance.getTimer(pars, runs));
    info(QUERYCOMPILE + Performance.getTimer(comp, runs));
    info(QUERYEVALUATE + Performance.getTimer(eval, runs));
    info(QUERYPRINT + Performance.getTimer(prnt, runs));
    info(QUERYTOTAL + Performance.getTimer(total, runs) + NL);
    info(QUERYHITS + hits + " " + (hits == 1 ? VALHIT : VALHITS));
    info(QUERYUPDATED + updates + " " + (updates == 1 ? VALHIT : VALHITS));
    info(QUERYPRINTED + Performance.format(out.size()));
  }

  /**
   * Creates query plans.
   * @param c compiled flag
   */
  private void plan(final boolean c) {
    if(c != prop.is(Prop.COMPPLAN)) return;

    // show dot plan
    try {
      if(prop.is(Prop.DOTPLAN)) {
        final String path = context.prop.get(Prop.QUERYPATH);
        final String dot = path.isEmpty() ? "plan.dot" :
            new IOFile(path).name().replaceAll("\\..*?$", ".dot");

        final BufferOutput bo = new BufferOutput(dot);
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
        info(NL + QUERYPLAN);
        info(ao.toString());
      }
    } catch(final Exception ex) {
      Util.stack(ex);
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
