package org.basex.core.proc;

import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.build.MemBuilder;
import org.basex.build.xml.XMLParser;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.Nodes;
import org.basex.io.IO;
import org.basex.util.Token;

/**
 * Evaluates the 'insert' command.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Insert extends Proc {
  /** Insert option. */
  public static final String FRG = "fragment";
  /** Insert option. */
  public static final String ELEM = "element";
  /** Insert option. */
  public static final String TXT = "text";
  /** Insert option. */
  public static final String ATT = "attribute";
  /** Insert option. */
  public static final String COM = "comment";
  /** Insert option. */
  public static final String PI = "pi";
  /** Node Kinds; order equals numbering in the {@link Data} class. */
  public static final String[] KINDS = { FRG, ELEM, TXT, ATT, COM, PI };

  @Override
  protected boolean exec() {
    final String type = cmd.arg(0).toLowerCase();
    byte kind = -1;
    for(byte i = 0; i < KINDS.length; i++) if(type.equals(KINDS[i])) kind = i;
    if(kind == -1) throw new IllegalArgumentException();

    final Data data = context.data();
    final int args = cmd.nrArgs();
    Nodes nodes;

    // retrieve nodes to be updated...
    final boolean gui = args == (kind == Data.ATTR ||
        kind == Data.PI ? 3 : 2) && context.marked().size != 0;

    if(gui) {
      // ...from the marked node set...
      nodes = context.marked();
    } else if(args == (kind == Data.PI ? 5 : 4)) {
      // ...or the query
      nodes = query(cmd.arg(args - 1), null);
    } else {
      throw new IllegalArgumentException();
    }
    if(nodes == null) return false;

    data.noIndex();

    final String spos = kind == Data.PI ? cmd.arg(3) : cmd.arg(2);
    final int pos = gui || kind == Data.ATTR ? 0 : Token.toInt(spos);
    if(pos < 0) return error(POSINVALID, spos);
    
    if(kind == 0) return frag(nodes, pos, cmd.arg(1));
    if(kind == Data.ATTR) return attribute(nodes, cmd.arg(1), cmd.arg(2));
    if(kind == Data.PI) return pi(nodes, pos, cmd.arg(1), cmd.arg(2));
    return node(kind, nodes, pos, cmd.arg(1));
  }

  /**
   * Inserts a document fragment.
   * @param nodes target nodes
   * @param pos inserting position
   * @param val value/document to be inserted
   * @return success of operation
   */
  boolean frag(final Nodes nodes, final int pos, final String val) {
    // create temporary instance of document to be inserted
    try {
      // parse xml input or filename
      final IO file = new IO(val);
      final XMLParser parser = new XMLParser(file);
      final Data tmp = new MemBuilder().build(parser, "tmp");
      final Data data = context.data();

      // check if all nodes are elements
      for(int i = nodes.size - 1; i >= 0; i--) {
        if(data.kind(nodes.pre[i]) != Data.ELEM) return error(COPYTAGS);
      }

      // perform updates
      for(int i = nodes.size - 1; i >= 0; i--) {
        final int par = nodes.pre[i];
        data.insert(Insert.pre(par, pos, data), par, tmp);
      }
      data.flush();

      return Prop.info ? info(INSERTINFO, nodes.size, perf.getTimer()) : true;
    } catch(final IOException ex) {
      BaseX.debug(ex);
      return error(ex.getMessage());
    }
  }

  /**
   * Inserts an attribute.
   * @param nodes target nodes
   * @param nam attribute name to be inserted
   * @param val value value to be inserted
   * @return success of operation
   */
  private boolean attribute(final Nodes nodes, final String nam,
      final String val) {

    final byte[] n = Token.token(nam);
    final byte[] v = Token.token(val);
    final Data data = context.data();
    final int att = data.attNameID(n);

    if(!Insert.check(n)) return error(ATTINVALID, nam);

    // check if all nodes are elements
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int par = nodes.pre[i];
      final int kind = data.kind(par);
      if(kind != Data.ELEM) return error(COPYTAGS);
      
      // check uniqueness of attribute
      final int last = par + data.attSize(par, kind);
      for(int p = par; p < last; p++) {
        if(att == data.attNameID(p)) return error(ATTDUPL, n);
      }
    }

    // perform updates
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int par = nodes.pre[i];
      data.insert(par + data.attSize(par, data.kind(par)), par, n, v);
    }
    data.flush();
    return Prop.info ? info(INSERTINFO, nodes.size, perf.getTimer()) : true;
  }

  /**
   * Inserts a processing instruction.
   * @param nodes target nodes
   * @param nam attribute name to be inserted
   * @param pos inserting position
   * @param val value value to be inserted
   * @return success of operation
   */
  private boolean pi(final Nodes nodes, final int pos, final String nam,
      final String val) {

    if(!Insert.check(Token.token(nam))) return error(PIINVALID, nam);
    final String v = val.length() == 0 ? nam : nam + " " + val;
    return node(Data.PI, nodes, pos, v);
  }

  /**
   * Inserts an element, text node, comment or processing instruction.
   * @param kind node kind of the data to be inserted
   * @param nodes target nodes
   * @param pos inserting position
   * @param val value value to be inserted
   * @return success of operation
   */
  private boolean node(final byte kind, final Nodes nodes, final int pos,
      final String val) {

    final byte[] v = Token.token(val);
    if(kind == Data.ELEM && !check(v)) return error(TAGINVALID, v);
    
    // check correctness of query
    final Data data = context.data();
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int k = data.kind(nodes.pre[i]);
      if(k == Data.TEXT) return error(COPYTAGS);
      if(k == Data.DOC && (kind == Data.TEXT || kind == Data.ELEM &&
          data.size > 1)) return error(COPYROOT);
    }
    
    // perform updates
    for(int i = nodes.size - 1; i >= 0; i--) {
      final int par = nodes.pre[i];
      final int pre = pre(par, pos, data);

      // merge text nodes if necessary
      final int up = checkText(data, pre, par, kind);
      if(up != -1) {
        data.update(up, Token.concat(data.text(up), v));
      } else {
        data.insert(pre, par, v, kind);
      }
    }
    data.flush();
    return Prop.info ? info(INSERTINFO, nodes.size, perf.getTimer()) : true;
  }
  
  /**
   * Checks if the current insertion would create two adjacent text nodes.
   * @param data data reference
   * @param pre pre value
   * @param par parent value
   * @param kind node kind
   * @return update position or -1 if negative
   */
  public static int checkText(final Data data, final int pre, final int par,
      final int kind) {
    // merge text nodes if necessary
    if(kind == Data.TEXT) {
      if(data.kind(pre) == Data.TEXT) return pre;
      if(data.parent(pre - 1, data.kind(pre - 1)) == par &&
          data.kind(pre - 1) == Data.TEXT) return pre - 1;
    }
    return -1;
  }

  /**
   * Retrieves the pre value for the specified child position.
   * @param par parent node
   * @param pos child position
   * @param data data reference
   * @return pre value
   */
  public static int pre(final int par, final int pos, final Data data) {
    int k = data.kind(par);
    if(pos == 0) return par + data.size(par, k);
    int pre = par + data.attSize(par, k);
    for(int p = 1; p < pos; pre += data.size(pre, k), p++) {
      k = data.kind(pre);
      if(data.parent(pre, k) != par) break;
    }
    return pre;
  }

  /**
   * Creates a memory data instance from the specified database and pre value.
   * @param data data reference
   * @param pre pre value
   * @return database instance
   */
  public static Data copy(final Data data, final int pre) {
    // size of the data instance
    final int size = data.size(pre, data.kind(pre));
    // create temporary data instance, adopting the indexes of the source data
    final MemData tmp = new MemData(size, data.tags, data.atts, data.ns);

    // copy all nodes
    for(int p = pre; p < pre + size; p++) {
      final int k = data.kind(p);
      final int d = p - data.parent(p, k);
      switch(k) {
        case Data.DOC:
          tmp.addDoc(data.text(p), data.size(p, k), k);
          break;
        case Data.ELEM:
          tmp.addElem(data.tagID(p), data.tagNS(p), d, data.attSize(p, k),
              data.size(p, k), k);
          break;
        case Data.ATTR:
          tmp.addAtt(data.attNameID(p), data.attNS(p), data.attValue(p), d, k);
          break;
        case Data.TEXT:
        case Data.COMM:
        case Data.PI:
          tmp.addText(data.text(p), d, k);
          break;
      }
    }
    return tmp;
  }

  /**
   * Checks the validity of the specified name.
   * @param name name to be checked
   * @return result of check
   */
  public static boolean check(final byte[] name) {
    if(name.length == 0) return false;
    int i = -1;
    while(++i != name.length) {
      final byte c = name[i];
      if(Token.letter(c) || c == ':') continue;
      if(i == 0) break;
      if(!Token.digit(c) && c != '-' && c != '.') break;
    }
    return i == name.length;
  }
}
