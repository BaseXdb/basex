package org.basex.core.proc;

import static org.basex.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import org.basex.BaseX;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Prop;
import org.basex.core.Commands.CmdUpdate;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.util.Token;

/**
 * Inserts an element into the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Insert extends AUpdate {
  /**
   * Constructor.
   * @param t insertion type
   * @param a arguments
   */
  public Insert(final CmdUpdate t, final String... a) {
    this(false, t, a);
  }

  /**
   * Constructor for GUI updates.
   * @param g gui flag
   * @param t insertion type
   * @param a arguments
   */
  public Insert(final boolean g, final CmdUpdate t, final String... a) {
    super(g, t, a);
  }

  @Override
  protected boolean exec() {
    final Data data = context.data();

    // get sources from the marked node set or the specified query
    final Nodes nodes = gui ? context.marked() :
      query(args[type == CmdUpdate.PI ? 3 : 2], null);
    if(nodes == null) return false;

    boolean ok = false;
    switch(type) {
      case ATTRIBUTE: ok = attr(data, nodes); break;
      case FRAGMENT:  ok = frag(data, nodes); break;
      default:        ok = node(data, nodes); break;
    }
    if(!ok) return false;
    
    data.flush();
    return Prop.info ? info(INSERTINFO, nodes.size, perf.getTimer()) : true;
  }
  
  /**
   * Inserts attributes.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean attr(final Data data, final Nodes nodes) {
    final byte[] n = Token.token(args[0]);
    final byte[] v = Token.token(args[1]);
    final int att = data.attNameID(n);

    if(!check(n)) return error(ATTINVALID, n);

    // check if all nodes are elements
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      final int kind = data.kind(par);
      if(kind != Data.ELEM) return error(COPYTAGS);

      // check uniqueness of attribute
      final int last = par + data.attSize(par, kind);
      for(int p = par; p < last; p++) {
        if(att == data.attNameID(p)) return error(ATTDUPL, n);
      }
    }

    // perform updates
    data.meta.update();
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      data.insert(par + data.attSize(par, data.kind(par)), par, n, v);
    }
    return true;
  }
  
  /**
   * Inserts fragments.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean frag(final Data data, final Nodes nodes) {
    final int pos = gui ? 0 : Token.toInt(args[1]);
    if(pos < 0) return error(POSINVALID, args[1]);
    
    Data tmp;
    try {
      final IO io = IO.get(args[0]);
      final Parser parser = Prop.intparse || io instanceof IOContent ?
          new XMLParser(io) : new SAXWrapper(new SAXSource(io.inputSource()));

      tmp = new MemBuilder().build(parser, "tmp");
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
    
    /* check if all nodes are elements
    for(int i = nodes.size - 1; i >= 0; i--) {
      if(data.kind(nodes.nodes[i]) != Data.ELEM) return error(COPYTAGS);
    }*/

    // insert temporary instance of document
    data.meta.update();
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      data.insert(pre(par, pos, data), par, tmp);
    }
    return true;
  }
  
  /**
   * Inserts nodes.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean node(final Data data, final Nodes nodes) {
    byte[] v = token(args[0]);
    final int kind = type.ordinal();
    final boolean pi = kind == Data.PI;
    if(kind == Data.ELEM || pi) {
      if(!check(v)) return error(NAMEINVALID, v);
      if(pi) {
        final byte[] vv = token(args[1]);
        v = v.length == 0 ? vv : concat(v, SPACE, vv);
      }
    }

    // check correctness of query
    final int pos = gui ? 0 : toInt(args[pi ? 2 : 1]);
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int k = data.kind(nodes.nodes[i]);
      if(k == Data.TEXT) return error(COPYTAGS);
      if(k == Data.DOC && (kind == Data.TEXT || kind == Data.ELEM &&
          data.meta.size > 1)) return error(COPYROOT);
    }

    // perform updates
    data.meta.update();
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      final int pre = pre(par, pos, data);

      // merge text nodes if necessary
      final int up = checkText(data, pre, par, kind);
      if(up != -1) {
        data.update(up, concat(data.text(up), v));
      } else {
        data.insert(pre, par, v, kind);
      }
    }
    return true;
  }

  /**
   * Checks if the current insertion would create two adjacent text nodes.
   * @param data data reference
   * @param pre pre value
   * @param par parent value
   * @param kind node kind
   * @return update position or -1 if negative
   */
  static int checkText(final Data data, final int pre, final int par,
      final int kind) {
    // merge text nodes if necessary
    if(kind == Data.TEXT) {
      final int k = data.kind(pre - 1);
      if(k == Data.TEXT && data.parent(pre - 1, k) == par) return pre - 1;
      if(data.kind(pre) == Data.TEXT) return pre;
    }
    return -1;
  }

  @Override
  public String toString() {
    return name() + " " + type + args();
  }
}
