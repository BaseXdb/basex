package org.basex.query.expr;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.Nod;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Uri;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodIter;
import org.basex.query.iter.NodeIter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;

/**
 * Element fragment.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class CElem extends CFrag {
  /** Namespaces. */
  final Atts nsp;
  /** Tag name. */
  Expr tag;

  /**
   * Constructor.
   * @param t tag tag
   * @param cont element content
   * @param ns namespaces
   */
  public CElem(final Expr t, final Expr[] cont, final Atts ns) {
    super(cont);
    tag = t;
    nsp = ns;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    final int s = ctx.ns.size();
    addNS(ctx);
    super.comp(ctx);
    tag = tag.comp(ctx);
    ctx.ns.size(s);
    return this;
  }
  
  /**
   * Adds namespaces to the current context.
   * @param ctx query context
   * @throws QueryException query exception
   */
  void addNS(final QueryContext ctx) throws QueryException {
    for(int n = nsp.size - 1; n >= 0; n--) {
      ctx.ns.add(new QNm(concat(XMLNSC, nsp.key[n]), Uri.uri(nsp.val[n])));
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    return new Constr().atomic(ctx);
  }

  /** Construction helper class. */
  final class Constr {
    /** Text cache. */
    final TokenBuilder text = new TokenBuilder();
    /** Node array. */
    final NodIter children = new NodIter();
    /** Attribute array. */
    final NodIter ats = new NodIter();
    /** Space separator flag. */
    boolean more;
    /** Base URI. */
    byte[] base = EMPTY;

    /**
     * Constructs the element node.
     * @param ctx query context
     * @throws QueryException xquery exception
     * @return element
     */
    Item atomic(final QueryContext ctx) throws QueryException {
      final Item it = tag.atomic(ctx);
      if(it == null) Err.empty(CElem.this);
      final int s = ctx.ns.size();
      addNS(ctx);

      final QNm tname = CAttr.name(ctx, it);
      final byte[] uri = tname.uri.str();

      if(uri.length != 0) {
        final byte[] key = tname.pre();
        if(!eq(key, XML)) {
          int i = nsp.get(key);
          if(i == -1 || !eq(nsp.val[i], uri)) nsp.add(key, uri);
        }
      }

      for(final Expr e : expr) {
        more = false;
        final Iter iter = ctx.iter(e);
        while(add(ctx, iter.next()));
      }
      if(text.size != 0) children.add(new FTxt(text.finish(), null));

      final FElem node = new FElem(tname, children, ats, base, nsp, null);
      for(int n = 0; n < children.size; n++) children.list[n].parent(node);
      for(int n = 0; n < ats.size; n++) ats.list[n].parent(node);
      ctx.ns.size(s);
      return node;
    }

    /**
     * Recursively adds nodes to the element arrays. Recursion is necessary
     * as documents are resolved to their child nodes.
     * @param ctx query context
     * @param it current item
     * @return true if item was added
     * @throws QueryException xquery exception
     */
    private boolean add(final QueryContext ctx, final Item it)
        throws QueryException {
      
      if(it == null) return false;
      
      if(it.node() && it.type != Type.TXT) {
        Nod node = (Nod) it;

        if(it.type == Type.ATT) {
          // text has already been added - no attribute allowed anymore
          if(text.size != 0 || children.size != 0) Err.or(NOATTALL);

          // split attribute name
          final QNm name = node.qname();
          final byte[] ln = name.ln();
          final byte[] pre = name.pre();
          if(eq(pre, XML) && eq(ln, BASE)) base = it.str();

          // check for duplicate attribute names
          final QNm qname = node.qname();
          for(int a = 0; a < ats.size; a++) {
            if(qname.eq(ats.list[a].qname())) {
              final byte[] nm = qname.str();
              if(!contains(nm, ':')) Err.or(ATTDUPL, nm);
              else Err.or(ATTNSDUPL, qname, ats.list[a].qname());
            }
          }
          // add attribute
          ats.add(node.copy());
        } else if(it.type == Type.DOC) {
          final NodeIter iter = node.child();
          Nod ch;
          while((ch = iter.next()) != null) add(ctx, ch);
        } else {
          // add text node
          if(text.size != 0) {
            children.add(new FTxt(text.finish(), null));
            text.reset();
          }
          node = node.copy();
          children.add(node);

          // add namespaces from ancestors
          final Atts atts = node.ns();
          if(atts != null) {
            node = node.parent();
            while(node != null && node.type == Type.ELM) {
              final Atts ns = node.ns();
              for(int a = 0; a < ns.size; a++) {
                if(!atts.contains(ns.key[a])) atts.add(ns.key[a], ns.val[a]);
              }
              node = node.parent();
            }
          }
        }
        more = false;
      } else {
        if(more && it.type != Type.TXT) text.add(' ');
        text.add(it.str());
        more = it.type != Type.TXT;
      }
      return true;
    }
  }

  @Override
  public Expr remove(final Var v) {
    tag = tag.remove(v);
    return super.remove(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    tag.plan(ser);
    for(final Expr e : expr) e.plan(ser);
    ser.closeElement();
  }

  @Override
  public String info() {
    return "element constructor";
  }

  @Override
  public String toString() {
    return toString("elem " + tag);
  }
}
