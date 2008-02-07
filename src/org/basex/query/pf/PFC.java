package org.basex.query.pf;

import java.io.OutputStream;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.data.PrintSerializer;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.io.IOConstants;
import org.basex.query.QueryException;
import org.basex.query.QueryContext;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;
import static org.basex.query.pf.PFT.*;
import static org.basex.util.Token.toInt;

/**
 * Pathfinder query context.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class PFC extends QueryContext {
  /** Main class. */
  private final PFP pfp;
  /** XPath query handler. */
  private final PF queries;
  /** Data reference. */
  private final MemData pftable;
  /** Data fragment. */
  private Data frag;

  /**
   * Constructor.
   * @param d pathfinder table reference
   * @param p main class
   */
  PFC(final MemData d, final PFP p) {
    queries = new PF(d);
    pftable = d;
    pfp = p;
  }
  
  @Override
  public PFC compile(final Nodes nodes) {
    return this;
  }

  @Override
  public Result eval(final Nodes nodes) throws QueryException {
    // initialize fragment constructor
    Opr[] ops = null;
    Opr.pf = this;

    int i = 0;
    try {
      // get ids of all xml nodes
      final int[] ni = q(XPNODES, 0);
      final int il = ni.length;
  
      // arrange nodes in ascending id order
      final int[] nd = new int[il];
      for(i = 0; i < il; i++) nd[Token.toInt(v(ID, ni[i])) - 1] = ni[i];
      
      // create operator instances
      ops = new Opr[il];
      for(i = 0; i < il; i++) ops[i] = OprB.e(v(KIND, nd[i]));
      
      // fill operators with initial contents
      for(i = 0; i < il; i++) {
        // add arguments (edges)
        final int[] ai = q(XPEDGE, nd[i]);
        final Opr[] args = new Opr[ai.length];
        for(int a = 0; a < ai.length; a++) {
          args[a] = ops[toInt(pftable.attValue(ai[a])) - 1];
        }
        ops[i].init(nd[i], args, i);
      }
      
      // evaluate query plan and return result
      e(ops[0]);
      
      Frag.finish();
      Opr.pf = null;
      
      final Opr op = ops[0];
      final Col c = op.tbl.c(op.tbl.p(i(ITEM)));

      final PFR seq = new PFR();
      for(int r = 0; r < c.sz; r++) {
        final V v = c.r(r);
        if(v instanceof N) {
          seq.add(new PFN(frag, v.i()));
        } else {
          seq.add(v);
        }
      }
      return seq;
    } catch(final Exception ex) {
      /* try/catch used for debugging; should be removed as soon as
      implementation is completed and bug free. */
      BaseX.debug(ex);
      
      if(Prop.allInfo) {
        if(ops != null) {
          BaseX.errln("Not evaluated:");
          for(i = ops.length - 1; i >= 0; i--) ops[i].dbg();
        }
      }
      Frag.finish();
      Opr.pf = null;
      
      final QueryException ee = new QueryException(ex.toString());
      ee.initCause(ex);
      throw ee;
    }
  }
  
  @Override
  public void plan(final Serializer ser) throws Exception { }

  /**
   * Evaluates the specified operator once and outputs some query info.
   * @param op expression to be evaluated
   * @throws QueryException query exception
   */
  void e(final Opr op) throws QueryException {
    String perf = op.ev();
    if(perf != null) {
      if(Prop.allInfo) {
        while(perf.length() < 10) perf = " " + perf;
        BaseX.outln("%: %", perf, op);
      }
    }
  }

  /**
   * Adds the specified data fragment to the global fragment array.
   * @param d data
   */
  void a(final Data d) { frag = d; }

  /**
   * Returns the last data fragment.
   * @return data fragment
   */
  Data f() { return frag; }
  
  /**
   * Evaluates the specified XPath query and returns the result nodes.
   * @param q XPath query
   * @param id node to start from
   * @return result nodes
   * @throws QueryException query exception
   */
  int[] q(final byte[] q, final int id) throws QueryException {
    return queries.get(q, id);
  }
  
  /**
   * Evaluates the specified XPath query and returns the first node as token.
   * @param q XPath query
   * @param id node to start from
   * @return result nodes
   * @throws QueryException query exception
   */
  byte[] t(final byte[] q, final int id) throws QueryException {
    final int[] r = q(q, id);
    return r.length == 0 ? new byte[] {} : pftable.atom(r[0]);
  }

  /**
   * Returns the value of the specified attribute.
   * @param att attribute to be found
   * @param id node to start from
   * @return value
   */
  byte[] v(final byte[] att, final int id) {
    return pftable.attValue(pftable.attNameID(att), id);
  }

  /**
   * Finds the specified attribute in the table and returns its value id.
   * @param att attribute to be found
   * @param id pre value
   * @return value
   */
  int a(final byte[] att, final int id) {
    return i(v(att, id));
  }

  /**
   * Finds the specified attribute in the name index and returns its value id.
   * Values are indexed, so each attribute reference is unique.
   * @param att attribute to be found
   * @return value id
   */
  int i(final byte[] att) {
    return pftable.attID(att);
  }

  /**
   * Dumps information on the specified node.
   * @param ser serializer
   * @param i node to be printed
   * @throws Exception exception
   */
  void d(final PrintSerializer ser, final int i) throws Exception {
    new Nodes(i, pftable).serialize(ser);
  }

  /**
   * Returns the specified table table as string.
   * @param tbl table to be dumped
   * @return string
   */
  String dump(final Tbl tbl) {
    final TokenBuilder tb = new TokenBuilder();
    for(int l = 0; l < tbl.size; l++) {
      if(l != 0) tb.add("\n");
      tb.add("- ");
      tb.add(pftable.attToken(tbl.c(l).nm));
      tb.add(": ");
      tb.add(tbl.c(l).toString());
    }
    return tb.toString();
  }

  @Override
  public void planXML(final String file) throws Exception {
    IOConstants.write(file, pfp.xml);
  }

  @Override
  public void planDot(final String file) throws Exception {
    // create process
    final Process pr = new ProcessBuilder(Prop.pfpath, PFDOTARGS).start();

    // send query
    final OutputStream os = pr.getOutputStream();
    os.write(pfp.query);
    os.close();

    // receive input & errors
    final byte[] dot = pfp.getStream(pr.getInputStream());

    // receive errors
    final byte[] error = pfp.getStream(pr.getErrorStream());
    if(error.length != 0) {
      BaseX.outln(Token.string(error));
    } else {
      // increase font size
      final byte[] fs = Token.token("fontsize=16");
      dot[Token.indexOf(dot, fs) + fs.length + 1] = '6';
      dot(file, dot);
    }
  }
}

