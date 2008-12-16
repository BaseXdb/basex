package org.basex.query.xquery.item;

import static org.basex.util.Token.*;
import static org.basex.query.xquery.XQTokens.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.io.IO;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.NodeMore;
import org.basex.query.xquery.util.NSGlobal;
import org.basex.util.Atts;

/**
 * Disk-based Node item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DBNode extends Nod {
  /** Node Types. */
  private static final Type[] TYPES = {
    Type.DOC, Type.ELM, Type.TXT, Type.ATT, Type.COM, Type.PI
  };
  /** Root node (constructor). */
  public Nod root;
  /** Namespaces. */
  private Atts nsp;
  /** Data reference. */
  public Data data;
  /** Pre value. */
  public int pre;

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   */
  public DBNode(final Data d, final int p) {
    this(d, p, null, TYPES[d.kind(p)]);
  }

  /**
   * Constructor.
   * @param d data reference
   * @param p pre value
   * @param r parent reference
   * @param t node type
   */
  public DBNode(final Data d, final int p, final Nod r, final Type t) {
    super(t);
    data = d;
    pre = p;
    par = r;
  }

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
  public byte[] str() {
    return data.atom(pre);
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    final int s = ser.ns.size;
    final byte[] dn = ser.dn;
    
    ser.node(data, pre);
    ser.ns.size = s;
    ser.dn = dn;
  }

  @Override
  public String toString() {
    switch(type) {
      case ATT:
        return type + "(" + string(data.attName(pre)) + "=\"" +
        string(data.attValue(pre)) + "\")";
      case DOC:
        return type + "(\"" + data.meta.file + "\")";
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
    return qname(new QNm());
  }
  
  @Override
  public QNm qname(final QNm name) {
    final byte[] nm = nname();
    name.name(nm);
    if(!name.ns() && data.ns.size() == 0) {
      name.uri = Uri.EMPTY;
    } else {
      final int n = data.ns.get(nm, pre);
      name.uri = Uri.uri(n > 0 ? data.ns.key(n) : NSGlobal.uri(pre(nm)));
    }
    return name;
  }

  @Override
  public Atts ns() {
    if(type != Type.ELM || nsp != null) return nsp;
    nsp = new Atts();
    final int[] ns = data.ns(pre);
    for(int n = 0; n < ns.length; n += 2) {
      nsp.add(data.ns.key(ns[n]), data.ns.key(ns[n + 1]));
    }
    return nsp;
  }

  @Override
  public byte[] base() {
    if(type != Type.DOC) return EMPTY;
    final IO dir = IO.get(data.meta.file.path());
    final IO file = IO.get(string(data.text(pre)));
    return token(dir.merge(file).path());
  }

  @Override
  public boolean is(final Nod nod) {
    if(nod == this) return true;
    if(!(nod instanceof DBNode)) return false;
    return data == ((DBNode) nod).data && pre == ((DBNode) nod).pre;
  }

  @Override
  public int diff(final Nod nod) {
    if(!(nod instanceof DBNode) || data != ((DBNode) nod).data)
      return id - nod.id;
    return pre - ((DBNode) nod).pre;
  }

  @Override
  public DBNode copy() {
    // par.finish() ?..
    final DBNode node = new DBNode(data, pre, par, type);
    node.root = root;
    node.score(score());
    return node;
  }

  @Override
  public DBNode finish() {
    return copy();
  }

  @Override
  public Nod parent() {
    if(par != null) return par;
    final int p = data.parent(pre, data.kind(pre));
    
    // check if parent constructor exists; if not, include document root node
    if(p == (root != null ? 0 : -1)) return root;
    final DBNode node = copy();
    node.set(p, data.kind(p));
    return node;
  }

  @Override
  public void parent(final Nod p) {
    root = p;
    par = p;
  }

  @Override
  public NodeIter attr() {
    return new NodeIter() {
      /** Temporary node. */
      private final DBNode node = copy();
      /** Current pre value. */
      private int p = pre + 1;
      /** Current size value. */
      private final int s = pre + data.attSize(pre, data.kind(pre));

      @Override
      public Nod next() {
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
      private final DBNode node = copy();
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
      public Nod next() {
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
      private final DBNode node = copy();
      /** Current pre value. */
      private int p = pre + data.attSize(pre, data.kind(pre));
      /** Current size value. */
      private final int s = pre + data.size(pre, data.kind(pre));

      @Override
      public Nod next() {
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
      private final DBNode node = copy();
      /** Current pre value. */
      private int p = pre;
      /** Current size value. */
      private final int s = pre + data.size(pre, data.kind(pre));

      @Override
      public Nod next() {
        if(p == s) return null;
        final int k = data.kind(p);
        node.set(p, k);
        p += data.attSize(p, k);
        return node;
      }
    };
  }

  @Override
  public String color() {
    return "9999FF";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, NAM, token(data.meta.file.name()), PRE, token(pre));
  }
}
