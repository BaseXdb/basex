package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.query.QueryException;
import org.basex.query.xquery.XQueryProcessor;
import org.basex.util.Performance;
import org.basex.util.Token;

/**
 * Evaluates the (undocumented) 'xqenv' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class XQEnv extends XQuery {
  /** Static XQuery instance. */
  private static XQueryProcessor qu = new XQueryProcessor("");
  /** Output flag. */
  public boolean output;

  @Override
  protected boolean exec() {
    String query = cmd.args();

    long pars = 0;
    long comp = 0;
    long eval = 0;

    try {
      for(int i = 0; i < Prop.runs; i++) {
        if(query.length() == 0) {
          qu = new XQueryProcessor(query);
          query = "()";
        }
  
        qu.query = Token.token(query);
        progress(qu);
  
        final Nodes nodes = context.current();
        qu.parse();
        pars += per.getTime();
        qu.compile(nodes);
        comp += per.getTime();
        result = qu.eval(nodes);
        eval += per.getTime();
      }
      if(Prop.info) {
        info(qu.getInfo());
        info(NL + QUERYPARSE + Performance.getTimer(pars, Prop.runs));
        info(NL + QUERYCOMPILE + Performance.getTimer(comp, Prop.runs));
        info(NL + QUERYEVALUATE + Performance.getTimer(eval, Prop.runs));
      }
    } catch(final QueryException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    } catch(final Exception ex) {
      // to be removed as soon as implementation is complete...
      ex.printStackTrace();
      final String msg = ex.getMessage();
      return error("Implementation Bug? " +  ex.getClass().getSimpleName() +
          (msg != null ? ": " + msg : ""));
    }
    return true;
  }
}
