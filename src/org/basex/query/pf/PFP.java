package org.basex.query.pf;

import static org.basex.Text.*;
import static org.basex.query.pf.PFT.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.basex.BaseX;
import org.basex.build.MemBuilder;
import org.basex.build.xml.XMLParser;
import org.basex.core.Prop;
import org.basex.data.MemData;
import org.basex.io.CachedInput;
import org.basex.query.QueryProcessor;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.util.Array;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * This class extends the {@link QueryProcessor} class and
 * answers XQuery requests via Pathfinder (early state).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PFP extends QueryProcessor {
  /** Input stream. */ byte[] xml;
  /** Error stream. */ byte[] err;

  /**
   * XPath Query Constructor.
   * @param q query
   */
  public PFP(final String q) {
    super(q);
  }

  @Override
  public QueryContext create() throws QueryException {
    try {
      // create process
      final Performance p = new Performance();
      final Process pr = new ProcessBuilder(Prop.pfpath, PFARGS).start();

      // send query
      final OutputStream out = pr.getOutputStream();
      out.write(query);
      out.close();
      if(Prop.allInfo) BaseX.outln(DBGSEND, p.getTimer());

      // receive input stream
      final Thread t1 = new Thread() {
        @Override
        public void run() {  xml = getStream(pr.getInputStream()); }
      };

      // receive error stream
      final Thread t2 = new Thread() {
        @Override
        public void run() { err = getStream(pr.getErrorStream()); }
      };

      // waiting for input
      t1.start();
      t2.start();
      t1.join();
      t2.join();
      if(Prop.allInfo) BaseX.outln(DBGINPUT, p.getTimer());

      // ignore warnings (execute xml if it exists)...
      if(err.length != 0 && xml.length == 0) {
        final String error = Token.string(err);
        throw new QueryException(error.contains(PF404) ? PFPATH :
          PFPARSE + error);
      }

      // create temporary table instance
      final CachedInput in = new CachedInput(xml);
      final XMLParser parser = new XMLParser(in);
      final MemData d = (MemData) new MemBuilder().build(parser, PLANXML);
      if(Prop.allInfo) BaseX.outln(DBGTABLE, p.getTimer());
      return new PFC(d, this);
    } catch(final Exception ex) {
      final QueryException qe = new QueryException(ex.toString());
      qe.initCause(ex);
      throw qe;
    }
  }

  /**
   * Returns content of an input stream.
   * @param input stream reference
   * @return content
   */
  byte[] getStream(final InputStream input) {
    try {
      final TokenBuilder tb = new TokenBuilder();
      final byte[] buf = new byte[2048];
      int i = 0;
      while((i = input.read(buf)) != -1) {
        tb.add(i == buf.length ? buf : Array.finish(buf, i));
      }
      return tb.finish();
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return Token.EMPTY;
    }
  }
}
