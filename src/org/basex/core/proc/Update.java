package org.basex.core.proc;

import static org.basex.Text.*;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.Nodes;
import org.basex.util.Token;

/**
 * Evaluates the 'update' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Update extends Proc {
  @Override
  protected boolean exec() {
    final String type = cmd.arg(0).toLowerCase();
    byte kind = -1;
    for(byte i = 1; i < Insert.KINDS.length; i++)
      if(type.equals(Insert.KINDS[i])) kind = i;
    if(kind == -1) throw new IllegalArgumentException();

    // retrieve nodes to be updated...
    final Data data = context.data();
    Nodes nodes;
    final int args = cmd.nrArgs();
    if(args == (kind == Data.ATTR || kind == Data.PI ? 4 : 3)) {
      // ...from query
      nodes = query(cmd.arg(args - 1), null);
    } else if(args == (kind == Data.ATTR || kind == Data.PI ? 3 : 2) &&
        context.marked().size != 0) {
      // ...or from marked node set
      nodes = context.marked();
    } else {
      throw new IllegalArgumentException();
    }
    if(nodes == null) return false;

    data.noIndex();

    if(kind == Data.ELEM) return tag(nodes, cmd.arg(1));
    if(kind == Data.ATTR) return attribute(nodes, cmd.arg(1), cmd.arg(2));
    if(kind == Data.PI) return pi(nodes, cmd.arg(1), cmd.arg(2));
    return node(kind, nodes, cmd.arg(1));
  }

  /**
   * Updates a tag name.
   * @param nodes nodes to be modified
   * @param tag tag name
   * @return success of operation
   */
  private boolean tag(final Nodes nodes, final String tag) {
    final Data data = context.data();
    final byte[] t = Token.token(tag);
    if(!Insert.check(t)) return error(TAGINVALID, tag);
    
    // check if all nodes are elements
    for(int i = nodes.size - 1; i >= 0; i--) {
      if(data.kind(nodes.pre[i]) != Data.ELEM) return error(UPDATEELEM);
    }

    // perform updates
    for(int i = nodes.size - 1; i >= 0; i--) data.update(nodes.pre[i], t);
    data.flush();

    return Prop.info ? info(UPDATEINFO, nodes.size, perf.getTimer()) : true;
  }

  /**
   * Updates an attribute.
   * @param nodes nodes to be modified
   * @param name attribute name
   * @param val attribute value
   * @return success of operation
   */
  private boolean attribute(final Nodes nodes, final String name,
      final String val) {
    final Data data = context.data();
    final byte[] n = Token.token(name);
    if(!Insert.check(n)) return error(ATTINVALID, name);
    final byte[] v = Token.token(val);
    
    // check if errors can occur
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int pre = nodes.pre[i];
      if(data.kind(pre) != Data.ATTR) return error(UPDATEATTR);
      // check existence of attribute
      int p = data.parent(pre, Data.ATTR);
      final int last = p + data.attSize(p, Data.ELEM);
      final int att = data.attNameID(n);
      while(++p < last) {
        if(p != pre && att == data.attNameID(p)) return error(ATTDUPL, n);
      }
    }
    
    // perform updates
    for(int i = nodes.size - 1; i >= 0; i--) data.update(nodes.pre[i], n, v);
    data.flush();
    
    return Prop.info ? info(UPDATEINFO, nodes.size, perf.getTimer()) : true;
  }

  /**
   * Inserts a processing instruction.
   * @param nodes target nodes
   * @param nam attribute name to be inserted
   * @param val value value to be inserted
   * @return success of operation
   */
  private boolean pi(final Nodes nodes, final String nam, final String val) {
    if(!Insert.check(Token.token(nam))) return error(PIINVALID, nam);
    final String v = val.length() == 0 ? nam : nam + " " + val;
    return node(Data.PI, nodes, v);
  }

  /**
   * Updates a text node or comment.
   * @param kind node kind
   * @param nodes nodes to be modified
   * @param txt text
   * @return success of operation
   */
  private boolean node(final int kind, final Nodes nodes, final String txt) {
    final Data data = context.data();
    final byte[] t = Token.token(txt);
    if(kind == Data.TEXT && t.length == 0) return error(TXTINVALID, t);

    // check if errors can occur
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int k = data.kind(nodes.pre[i]);
      if(k != kind) return error(UPDATENODE, Insert.KINDS[kind]);
    }

    // perform updates
    for(int i = nodes.size - 1; i >= 0; i--) data.update(nodes.pre[i], t);
    data.flush();

    return Prop.info ? info(UPDATEINFO, nodes.size, perf.getTimer()) : true;
  }
}
