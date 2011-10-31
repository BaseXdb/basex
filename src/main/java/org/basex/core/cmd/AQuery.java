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
   * Returns a new query instance.
   * @param query query
   * @return query instance
   */
  protected final boolean query(final String query) {
    final int runs = Math.max(1, prop.num(Prop.RUNS));
    String err = null;
    String inf = "";
    try {
      final boolean serial = prop.is(Prop.SERIALIZE);
      long hits = 0;
      int updates = 0;
      for(int i = 0; i < runs; ++i) {
        final Performance per = new Performance();

        qp = progress(new QueryProcessor(query, context));

        qp.parse();
        pars += per.getTime();
        if(i == 0) plan(false);
        qp.compile();
        comp += per.getTime();
        if(i == 0) plan(true);

        final PrintOutput po = i == 0 && serial ? out : new NullOutput();
        Serializer ser;

        if(prop.is(Prop.CACHEQUERY)) {
          result = qp.execute();
          eval += per.getTime();
          ser = qp.getSerializer(po);
          result.serialize(ser);
          hits = result.size();
        } else {
          final Iter ir = qp.iter();
          eval += per.getTime();
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
        prnt += per.getTime();
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
    return QueryProcessor.updating(ctx, qu);
  }

  /**
   * Performs the first argument as XQuery and returns a node set.
   */
  protected final void queryNodes() {
    try {
      result = new QueryProcessor(args[0], context).queryNodes();
    } catch(final QueryException ex) {
      Util.debug(ex);
      error(ex.getMessage());
    }
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
    info(QUERYSTRING + query);
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
        final ArrayOutput ao = new ArrayOutput();
        final DOTSerializer d = new DOTSerializer(ao, prop.is(Prop.DOTCOMPACT));
        qp.plan(d);
        d.close();

        final String path = context.prop.get(Prop.QUERYPATH);
        final String dot = path.isEmpty() ? "plan.dot" :
            new IOFile(path).name().replaceAll("\\..*?$", ".dot");
        new IOFile(dot).write(ao.toArray());

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
