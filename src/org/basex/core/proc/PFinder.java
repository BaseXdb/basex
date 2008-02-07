package org.basex.core.proc;

import java.lang.reflect.Constructor;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Nodes;
import org.basex.query.QueryProcessor;
import org.basex.query.QueryException;

/**
 * Evaluates the 'xquery' command.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PFinder extends XPath {
  /** Pathfinder class path. */
  public static final String PFP = "org.basex.query.pfpipe.PipeProcessor";
  /** Pathfinder class path. */
  public static final String PF = "org.basex.query.pf.PFP";
  /** Pipelining. */
  public static final String PIPE = "pipe";

  @Override
  protected boolean exec() {
    QueryProcessor qu = null;
    try {
      final boolean pipe = cmd.arg(0).equals(PIPE);
      final String query = pipe ? cmd.arg(1) : cmd.args();
      qu = pipe ? xquery(PFP, query) : xquery(PF, query);
      if(qu == null) return false;

      final Nodes nodes = context.current();
      for(int i = 0; i < Prop.runs; i++) result = qu.query(nodes);
      return info(qu.getInfo());
    } catch(final Exception ex) {
      BaseX.debug(ex);
      qu.getInfo();
      return error(ex.getMessage());
    }
  }
  
  /**
   * Return PipeQuery instance.
   * @param cls query class
   * @param qu query
   * @return query instance
   * @throws QueryException query exception
   */
  private QueryProcessor xquery(final String cls, final String qu)
      throws QueryException {
    try {
      final Constructor<?> con = Class.forName(cls).getConstructor(
          new Class[] { String.class });
      return (QueryProcessor) con.newInstance(new Object[] { qu });
    } catch(final Exception ex) {
      final QueryException qe = new QueryException(ex.toString());
      qe.initCause(ex);
      throw qe;
    }
  }
}
