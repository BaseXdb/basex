package org.basex.core.proc;

import static org.basex.core.Text.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.build.mediovis.MAB2;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.data.XMLSerializer;
import org.basex.io.NullOutput;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.IntList;
import org.basex.util.Performance;
import org.basex.util.Set;
import org.basex.util.Token;

/**
 * Evaluates the 'xquerymv' command and processes an XQuery request
 * for MedioVis queries.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class XQueryMV extends AQuery {
  /** Index reference. */
  private IDSet ids;
  /** Maximum hits. */
  private int maxh;
  /** Maximum sub hits. */
  private int maxs;
  /** Maximum hits. */
  private int maxhits;

  /**
   * Default constructor.
   * @param hits maximum number of hits
   * @param subhits maximum number of sub hits
   * @param query query to process
   */
  public XQueryMV(final String hits, final String subhits, final String query) {
    super(STANDARD, hits, subhits, query);
  }

  @Override
  protected boolean exec(final PrintOutput out) throws IOException {
    // cache verbose flag
    maxh = Token.toInt(args[0]);
    maxs = Token.toInt(args[1]);
    final String query = "(" + args[2] + ")[position() <= " + maxh * maxs + "]";
    long fini = 0;

    final int runs = Math.max(1, prop.num(Prop.RUNS));
    try {
      for(int i = 0; i < runs; i++) {
        final Performance per = new Performance();

        qp = new QueryProcessor(query, context);
        progress(qp);

        qp.parse();
        pars += per.getTime();
        qp.compile();
        comp += per.getTime();
        result = qp.queryNodes();
        eval += per.getTime();

        final Nodes ns = (Nodes) result;
        result = new Nodes(ns.nodes, ns.data);

        // get index references for mediovis attributes
        final Data data = context.data();
        final int medid = data.tagID(MAB2.MEDIUM);
        final int mvid  = data.attNameID(MAB2.MV_ID);

        // gather IDs for titles on top
        final Nodes res = (Nodes) result;
        final int[] pres = res.nodes;
        final int size = res.size();
        ids = new IDSet();

        for(int n = 0; n < size; n++) {
          // super-ordinate title found?
          int pre = data.parent(pres[n], data.kind(pres[n]));
          final boolean s = data.tagID(pre) == medid;
          if(!s) pre = pres[n];

          // add ID(s) to index
          final byte[] id = data.attValue(mvid, pre);
          if(s) {
            ids.index(id, pre, pres[n]);
          } else {
            ids.index(id, pre);
          }
          if(ids.size() == maxh) break;
        }
        maxhits = ids.size();
        fini += per.getTime();

        show(i == 0 && prop.is(Prop.SERIALIZE) ? out : new NullOutput());
        qp.close();
      }

      evalInfo(out, maxhits, runs);
      info(QUERYFINISH + Performance.getTimer(fini, runs));
      return true;
    } catch(final QueryException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Returns an MedioVis XML document.
   * @param out output reference
   * @throws IOException I/O exception
   */
  private void show(final PrintOutput out) throws IOException {
    final XMLSerializer xml = new XMLSerializer(out, false, true);

    // get index references for mediovis attributes
    final Data data = context.data();
    final int bibid = data.attNameID(MAB2.BIB_ID);
    final int maxid = data.attNameID(MAB2.MAX);
    final int medid = data.tagID(MAB2.MEDIUM);

    final Nodes res = (Nodes) result;
    final int size = res.size();

    // write root tag
    xml.openElement(MAB2.ROOT);
    xml.attribute(MAB2.HITS, Token.token(maxhits));
    xml.attribute(MAB2.MAX, Token.token(size));

    // loop through titles
    for(int i = 0; i < maxhits; i++) {
      final byte[] mv = ids.key(i + 1);
      final IntList medium = ids.get(mv);

      // write medium tag
      final int max = (int) attNum(data, maxid, medium.get(0));
      xml.openElement(MAB2.MEDIUM, MAB2.MV_ID, mv,
          MAB2.BIB_ID, data.attValue(bibid, medium.get(0)),
          MAB2.MAX, Token.token(max));

      // print medium
      final int par = medium.get(0);
      int pp = par + data.attSize(par, data.kind(par));
      while(pp != data.meta.size) {
        if(data.tagID(pp) == medid) break;
        pp = xml.node(data, pp);
      }

      // print subordinate titles of query results first
      final int maxsubs = Math.min(medium.size(), maxs + 1);
      for(int s = 1; s < maxsubs; s++) xml.node(data, medium.get(s));

      // print remaining subordinate titles
      final int leftsubs = Math.min(maxs, max) - maxsubs + 1;
      int m = 1;
      for(int s = 0; s < leftsubs;) {
        if(m < maxsubs && medium.get(m) == pp) {
          m++;
          pp += data.size(pp, data.kind(pp));
        } else {
          pp = xml.node(data, pp);
          s++;
        }
      }
      // close medium
      xml.closeElement();
    }

    // close root tag
    xml.closeElement();
    xml.close();
  }

  /**
   * Finds the specified attribute for the specified element and returns
   * the numeric attribute value.
   * @param data data reference
   * @param att attribute to be found
   * @param pre pre value
   * @return attribute value
   */
  private double attNum(final Data data, final int att, final int pre) {
    final int atts = pre + data.attSize(pre, data.kind(pre));
    int p = pre;
    while(++p != atts) if(data.attNameID(p) == att) return data.attNum(p);
    return 0;
  }

  /**
   * This is a hash map for MedioVis IDs.
   */
  static final class IDSet extends Set {
    /** Hash values. */
    private IntList[] values = new IntList[CAP];

    /**
     * Indexes the specified keys and values.
     * @param par parent key
     * @param pre pre value
     * @return index offset
     */
    int index(final byte[] par, final int pre) {
      int i = add(par);
      if(i < 0) {
        i = -i;
      } else {
        values[i] = new IntList();
        values[i].add(pre);
      }
      return i;
    }

    /**
     * Indexes the specified keys and values.
     * @param par parent key
     * @param pre pre value
     * @param sub sub id
     */
    void index(final byte[] par, final int pre, final int sub) {
      final int i = index(par, pre);
      values[i].add(sub);
    }

    /**
     * Returns the value for the specified key.
     * @param tok key to be found
     * @return value or null if nothing was found
     */
    IntList get(final byte[] tok) {
      return tok != null ? values[id(tok)] : null;
    }

    @Override
    protected void rehash() {
      super.rehash();
      values = Arrays.copyOf(values, size << 1);
    }
  }

  @Override
  public String toString() {
    return Cmd.XQUERYMV + " " + args[0] + " " + args[1] + " " + args[2];
  }
}
