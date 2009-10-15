package org.basex.core.proc;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import javax.xml.transform.sax.SAXSource;
import org.basex.build.MemBuilder;
import org.basex.build.Parser;
import org.basex.build.xml.SAXWrapper;
import org.basex.build.xml.XMLParser;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdUpdate;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.PrintOutput;
import org.basex.util.Token;

/**
 * Evaluates the 'insert' command and inserts nodes into the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Insert extends AUpdate {
  /**
   * Default constructor.
   * @param type node type, defined in {@link CmdUpdate}
   * @param position target position
   * @param target target query
   * @param vals value(s) to update; two values are expected for
   * attributes and processing instructions
   */
  public Insert(final Object type, final String target, final int position,
      final String... vals) {
    super(target == null, init(type.toString(), target, vals));
    pos = position;
  }

  /**
   * Constructor, using 0 as target position.
   * @param type node type, defined in {@link CmdUpdate}.
   * @param target target query
   * @param vals value(s) to update; two values are expected for
   * attributes and processing instructions
   */
  public Insert(final Object type, final String target, final String... vals) {
    this(type.toString(), target, 0, vals);
  }

  @Override
  protected boolean exec(final PrintOutput out) {
    if(!checkDB()) return false;

    // get sources from the marked node set or the specified query
    final CmdUpdate type = getOption(CmdUpdate.class);
    if(type == null) return false;

    final Data data = context.data();
    Nodes nodes;
    if(gui) {
      nodes = context.marked();
      context.copy(null);
    } else {
      nodes = query(args[1], null);
      if(nodes == null) return false;
    }

    boolean ok = false;
    switch(type) {
      case ATTRIBUTE: ok = attr(data, nodes); break;
      case FRAGMENT:  ok = frag(data, nodes); break;
      default:        ok = node(data, nodes); break;
    }
    if(!ok) return false;

    data.flush();
    context.update();
    return info(INSERTINFO, nodes.size(), perf.getTimer());
  }

  /**
   * Inserts attributes.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean attr(final Data data, final Nodes nodes) {
    final byte[] name = Token.token(args[2]);
    final byte[] val = Token.token(args[3]);
    final int att = data.attNameID(name);

    if(!check(name)) return error(ATTINVALID, name);

    // check if all nodes are elements
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      final int kind = data.kind(par);
      if(kind != Data.ELEM) return error(COPYTAGS);

      // check uniqueness of attribute names
      final int last = par + data.attSize(par, kind);
      for(int p = par + 1; p < last; p++) {
        if(att == data.attNameID(p)) return error(ATTDUPL, name);
      }
    }

    // perform updates
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      data.insert(par + data.attSize(par, data.kind(par)), par, name, val);
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
    Data tmp;
    try {
      final IO io = IO.get(args[2]);
      final Parser parser = prop.is(Prop.INTPARSE) ||
        io instanceof IOContent ? new XMLParser(io, prop) :
        new SAXWrapper(new SAXSource(io.inputSource()), prop);

      tmp = new MemBuilder(parser).build();
    } catch(final IOException ex) {
      Main.debug(ex);
      return error(ex.getMessage());
    }

    // insert temporary instance of document
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      // documents are always added at the end
      final int p = par == 0 ? 0 : pos;
      data.insert(pre(par, p, data), par, tmp);
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
    byte[] val = token(args[2]);
    final int kind = getOption(CmdUpdate.class).ordinal();
    final boolean pi = kind == Data.PI;
    if(kind == Data.ELEM || pi) {
      if(!check(val)) return error(NAMEINVALID, val);
      if(pi) {
        final byte[] vv = token(args[3]);
        val = val.length == 0 ? vv : concat(val, SPACE, vv);
      }
    }

    // check correctness of query
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int k = data.kind(nodes.nodes[i]);
      if(k == Data.TEXT) return error(COPYTAGS);
      if(k == Data.DOC && (kind == Data.TEXT || kind == Data.ELEM &&
          data.meta.size > 1)) return error(COPYROOT);
    }

    // perform updates
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int par = nodes.nodes[i];
      final int pre = pre(par, pos, data);

      // merge text nodes if necessary
      final int txt = checkText(data, pre, par, kind);
      if(txt != -1) {
        data.update(txt, concat(data.text(txt), val));
      } else {
        data.insert(pre, par, val, kind);
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
      if(pre < data.meta.size && data.kind(pre) == Data.TEXT) return pre;
    }
    return -1;
  }

  @Override
  public String toString() {
    final CmdUpdate type = getOption(CmdUpdate.class);
    final StringBuilder sb = new StringBuilder(Cmd.INSERT + " " + type);
    sb.append(quote(args[2]));
    if(args.length == 4) sb.append(quote(args[3]));
    sb.append(" " + INTO + " " + args[1]);
    if(pos != 0) sb.append(" " + AT + " " + pos);
    return sb.toString();
  }
}
