package org.basex.core.proc;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdUpdate;
import org.basex.data.Data;
import org.basex.data.Nodes;

/**
 * Evaluates the 'update' command and update nodes in the database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Update extends AUpdate {
  /**
   * Default constructor.
   * @param type node type defined in {@link CmdUpdate}
   * @param target target query
   * @param vals value(s) to update; two values are expected for
   * attributes and processing instructions
   */
  public Update(final Object type, final String target, final String... vals) {
    super(target == null, init(type.toString(), target, vals));
  }

  @Override
  protected boolean exec() {
    if(!checkDB()) return false;

    // get sources from the marked node set or the specified query
    final CmdUpdate type = getOption(CmdUpdate.class);
    if(type == null) return false;

    final Nodes nodes = gui ? context.marked() : query(args[1], null);
    if(nodes == null) return false;

    final Data data = context.data();
    boolean ok = false;
    switch(type) {
      case ATTRIBUTE: ok = attr(data, nodes); break;
      default:        ok = node(data, nodes); break;
    }
    if(!ok) return false;

    data.flush();
    context.update();
    return info(UPDATEINFO, nodes.size(), perf.getTimer());
  }

  /**
   * Updates attributes.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean attr(final Data data, final Nodes nodes) {
    final byte[] name = token(args[2]);
    final byte[] value = token(args[3]);
    if(!check(name)) return error(ATTINVALID, name);

    // check if errors can occur
    int lp = 0;
    for(int n = nodes.size() - 1; n >= 0; n--) {
      final int pre = nodes.nodes[n];
      if(data.kind(pre) != Data.ATTR) {
        final String type = CmdUpdate.values()[Data.ATTR].name().toLowerCase();
        return error(UPDATENODE, type);
      }
      // check existence of attribute
      int p = data.parent(pre, Data.ATTR);
      if(p == lp) return error(ATTDUPL, name);
      lp = p;
      final int as = p + data.attSize(p, Data.ELEM);
      final int att = data.attNameID(name);
      while(++p < as) {
        if(p != pre && att == data.attNameID(p)) return error(ATTDUPL, name);
      }
    }

    // perform updates
    for(int n = nodes.size() - 1; n >= 0; n--) {
      data.update(nodes.nodes[n], name, value);
    }
    return true;
  }

  /**
   * Updates nodes.
   * @param data data reference
   * @param nodes node reference
   * @return success flag
   */
  private boolean node(final Data data, final Nodes nodes) {
    byte[] v = token(args[2]);
    final int kind = getOption(CmdUpdate.class).ordinal();
    final boolean pi = kind == Data.PI;

    if(kind == Data.TEXT) {
      if(v.length == 0) return error(TXTINVALID, v);
    } else if(kind == Data.ELEM || pi) {
      if(!check(v)) return error(NAMEINVALID, v);
      if(pi) {
        final byte[] vv = token(args[3]);
        v = v.length == 0 ? vv : concat(v, SPACE, vv);
      }
    }

    // check if nodes to be updated have the same type
    for(int i = nodes.size() - 1; i >= 0; i--) {
      final int k = data.kind(nodes.nodes[i]);
      if(k != kind) return error(UPDATENODE,
          CmdUpdate.values()[kind].name().toLowerCase());
    }

    // perform updates
    for(int i = nodes.size() - 1; i >= 0; i--) data.update(nodes.nodes[i], v);
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(Cmd.UPDATE + " " +
          getOption(CmdUpdate.class));
    sb.append(quote(args[2]));
    if(args.length == 4) sb.append(quote(args[3]));
    return sb.append(' ' + AT + ' ' + args[1]).toString();
  }
}
