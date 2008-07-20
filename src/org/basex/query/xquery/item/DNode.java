package org.basex.query.xquery.item;

import static org.basex.util.Token.*;
import static org.basex.query.xquery.XQTokens.*;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.NodeMore;
import org.basex.query.xquery.util.Namespaces;
import org.basex.util.TokenList;

/**
 * Disk-based Node item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DNode extends Node {
  /** Root node (constructor). */
  public Node root;
  /** Data reference. */
  public Data data;
  /** Pre value. */
  public int pre;

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   * @param r parent reference
   * @param t node type
   */
  public DNode(final Data d, final int p, final Node r, final Type t) {
    super(t);
    data = d;
    pre = p;
    par = r;
  }

  @Override
  public byte[] str() {
    return data.atom(pre);
  }

  @Override
  public void serialize(final Serializer ser, final XQContext ctx,
      final int level) throws IOException {

    switch(type) {
      case ATT:
        ser.attribute(data.attName(pre), data.attValue(pre));
        break;
      case COM:
        ser.comment(str());
        break;
      case DOC:
        serElem(ser, pre, level);
        break;
      case ELM:
        serElem(ser, pre, level);
        break;
      case PI:
        ser.pi(str());
        break;
      case TXT:
        ser.text(str());
        break;
      default:
        BaseX.notexpected();
    }
  }

  @Override
  public String toString() {
    switch(type) {
      case ATT:
        return type + "(" + string(data.attName(pre)) + "=\"" +
        string(data.attValue(pre)) + "\")";
      case DOC:
        return type + "(" + data.meta.file + ")";
      case ELM:
        return type + "(" + string(data.tag(pre)) + "/" + pre + ")";
      default:
        String str = string(str());
        if(str.length() > 20) str = str.substring(0, 20) + "...";
        return type + "(" + str + ")";
    }
  }

  @Override
  public byte[] nname() {
    switch(type) {
      case ATT:
        return data.attName(pre);
      case ELM:
        return data.tag(pre);
      case PI:
        byte[] name = data.text(pre);
        final int i = indexOf(name, ' ');
        if(i != -1) name = substring(name, 0, i);
        return name;
      default:
        return EMPTY;
    }
  }

  @Override
  public QNm qname() {
    final byte[] nm = nname();
    final int i = data.ns.get(nm, pre);
    Uri uri = Uri.EMPTY;
    if(i > 0) {
      uri = Uri.uri(data.ns.key(i));
    } else if(i == 0) {
      uri = Namespaces.DEFAULT.uri(substring(nm, 0, indexOf(nm, ':')));
    }
    return new QNm(nm, uri);
  }
  
  @Override
  public QNm qname(final QNm nm) {
    final byte[] name = nname();
    nm.name(name);
    // [CG] next line slows it down pretty much...
    int n = data.ns.get(name, pre);
    if(n > 0) nm.uri = Uri.uri(data.ns.key(n));
    
    /* [CG] DNode/Namespaces: introduce correct namespace handling
    if(data.xmlnsID != 0) {
      int s = pre + data.attSize(pre, data.kind(pre));
      int p = pre;
      while(++p != s) {
        if(data.attNameID(p) == data.xmlnsID) {
          nm.uri = Uri.uri(data.attValue(p));
          break;
        }
      }
    }*/
    return nm;
  }

  @Override
  public byte[] base() {
    //return type != Type.DOC ? EMPTY : token(data.meta.file.path());
    return type != Type.DOC ? EMPTY : data.text(pre);
  }

  @Override
  public boolean is(final Node nod) {
    if(nod == this) return true;
    if(!(nod instanceof DNode)) return false;
    return data == ((DNode) nod).data && pre == ((DNode) nod).pre;
  }

  @Override
  public int diff(final Node nod) {
    if(!(nod instanceof DNode) || data != ((DNode) nod).data)
      return id - nod.id;
    return pre - ((DNode) nod).pre;
  }

  @Override
  public DNode copy() {
    // par.finish() ?..
    final DNode node = new DNode(data, pre, par, type);
    node.root = root;
    node.score(score());
    return node;
  }

  @Override
  public DNode finish() {
    return copy();
  }

  @Override
  public Node parent() {
    if(par != null) return par;
    final int p = data.parent(pre, data.kind(pre));
    
    // check if parent constructor exists; if not, include document root node
    if(p == (root != null ? 0 : -1)) return root;
    final DNode node = copy();
    node.set(p, data.kind(p));
    return node;
  }

  @Override
  public void parent(final Node p) {
    root = p;
    par = p;
  }

  /**
   * Serializes the specified node, starting from the specified pre value.
   * @param ser result reader
   * @param pr pre value
   * @param level current level
   * @throws IOException exception
   */
  public void serElem(final Serializer ser, final int pr, final int level)
      throws IOException {

    // attribute lists
    final TokenList names = new TokenList();
    final TokenList values = new TokenList();
    // stacks
    final int[] parent = new int[256];
    final byte[][] token = new byte[256][];
    // current output level
    int l = 0;

    // loop through all table entries
    int p = pr;
    final int s = pr + data.size(pr, data.kind(pr));
    while(p < s) {
      final int k = data.kind(p);
      final int pa = data.parent(p, k);
      // close opened tags...
      while(l > 0 && parent[l - 1] >= pa) ser.closeElement(token[--l]);

      if(k == Data.DOC) {
        p++;
      } else if(k == Data.TEXT) {
        ser.text(data.text(p++));
      } else if(k == Data.COMM) {
        ser.comment(data.text(p++));
      } else if(k == Data.PI) {
        ser.pi(data.text(p++));
      } else {
        // add element node
        final byte[] name = data.tag(p);
        ser.startElement(name);

        // find attributes
        final int ps = p + data.size(p, k);
        final int as = p + data.attSize(p, k);

        if(level != 0 || l != 0) {
          while(++p != as) ser.attribute(data.attName(p), data.attValue(p));
        } else {
          names.reset();
          values.reset();
          
          while(++p != as) {
            final byte[] at = data.attName(p);
            names.add(at);
            values.add(data.attValue(p));
            
            // add xmlns attribute for tag
            final int i = data.ns.get(at, pre);
            if(i > 0) {
              final byte[] pref = substring(at, 0, indexOf(at, ':'));
              final byte[] atr = concat(XMLNSCOL, pref);
              if(!names.contains(atr)) {
                names.add(atr);
                values.add(data.ns.key(i));
              }
            }
          }
          
          // add xmlns attribute for tag
          final int i = data.ns.get(name, pre);
          if(i > 0) {
            // [CG] check namespace handling..
            final int j = indexOf(name, ':');
            final byte[] uri = data.ns.key(i);
            final byte[] pref = j == -1 ? null : substring(name, 0, j);
            //final byte[] uri = ctx.ns.uri(pref).str();
            final byte[] at = j == -1 ? XMLNS : concat(XMLNSCOL, pref);
            if(!names.contains(at)) {
              names.add(at);
              values.add(uri);
            }
          }
          
          for(int n = 0; n < names.size; n++) {
            ser.attribute(names.list[n], values.list[n]);
          }
        }
  
        // check if this is an empty tag
        if(as == ps) {
          ser.emptyElement();
        } else {
          ser.finishElement();
          token[l] = name;
          parent[l++] = pa;
        }
      }
    }
    // process nodes that remain in the stack
    while(l > 0) ser.closeElement(token[--l]);
  }

  @Override
  public NodeIter attr() {
    return new NodeIter() {
      /** Temporary node. */
      private final DNode node = copy();
      /** Current pre value. */
      private int p = pre + 1;
      /** Current size value. */
      private final int s = pre + data.attSize(pre, data.kind(pre));

      @Override
      public Node next() {
        if(p == s) return null;
        node.set(p++, Data.ATTR);
        return node;
      }
    };
  }

  @Override
  public NodeMore child() {
    return new NodeMore() {
      /** Temporary node. */
      private final DNode node = copy();
      /** First call. */
      private boolean more;
      /** Current pre value. */
      private int p;
      /** Current size value. */
      private int s;

      @Override
      public boolean more() {
        if(!more) {
          final int k = data.kind(pre);
          p = pre + data.attSize(pre, k);
          s = pre + data.size(pre, k);
          more = true;
        }
        return p != s;
      }

      @Override
      public Node next() {
        if(!more()) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.size(p, k);
        return node;
      }
    };
  }

  @Override
  public NodeIter desc() {
    return new NodeIter() {
      /** Temporary node. */
      private final DNode node = copy();
      /** Current pre value. */
      private int p = pre + data.attSize(pre, data.kind(pre));
      /** Current size value. */
      private final int s = pre + data.size(pre, data.kind(pre));

      @Override
      public Node next() {
        if(p == s) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        return node;
      }
    };
  }

  @Override
  public NodeIter descOrSelf() {
    return new NodeIter() {
      /** Temporary node. */
      private final DNode node = copy();
      /** Current pre value. */
      private int p = pre;
      /** Current size value. */
      private final int s = pre + data.size(pre, data.kind(pre));

      @Override
      public Node next() {
        if(p == s) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        return node;
      }
    };
  }

  /** Node Types. */
  private static final Type[] TYPES = {
    Type.DOC, Type.ELM, Type.TXT, Type.ATT, Type.COM, Type.PI
  };
  
  /**
   * Sets the node type.
   * @param p pre value
   * @param k node kind
   */
  public void set(final int p, final int k) {
    type = TYPES[k];
    par = null;
    pre = p;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.emptyElement(this, PRE, token(pre));
  }
}
