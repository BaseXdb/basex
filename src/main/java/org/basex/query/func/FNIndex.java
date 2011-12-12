package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.index.Kind;
import org.basex.index.Names;
import org.basex.index.StatsKey;
import org.basex.index.path.PathNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.FAttr;
import org.basex.query.item.FDoc;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.QNm;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Index functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 * @author Dimitar Popov
 * @author Andreas Weiler
 */
public final class FNIndex extends FuncCall {
    /** Value token. */
    byte[] value = token("value");
    /** Count token. */
    byte[] count = token("count");
    /** Type token. */
    byte[] type2 = token("type");
    /** Min token. */
    byte[] min = token("min");
    /** Max token. */
    byte[] max = token("max");
    /** Cate token. */
    byte[] cate = token("categorical");
    /** Text token. */
    byte[] text = token("textual");
    /** Metric token. */
    byte[] metric = token("metric");

  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNIndex(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      default:            return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      default:     return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    switch(def) {
      case _INDEX_FACETS:       return facets(ctx);
      default:               return super.item(ctx, ii);
    }
  }

  /**
   * Returns facet information about db.
   * @param ctx query context
   * @return facet information
   * @throws QueryException query exception
   */
  private Item facets(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    double d = checkDbl(expr[1], ctx);
    final NodeCache nc = new NodeCache();
    if(d == 1) {
      PathNode root = data.pthindex.root;
      if(root != null) {
        PathNode start = root.ch[0];
        if(start != null) {
          FElem felem = new FElem(new QNm(data.tagindex.key(start.name)));
          getTree(data, start, felem, 0);
          nc.add(felem);
        }
      } else {
        throw NOVAIDX.thrw(null);
      }
    } else if(d == 2) {
      getFlat(data, nc);
    }
    return new FDoc(nc, Token.EMPTY);
  }

  /**
   * Returns flat facet representation.
   * @param data data reference
   * @param nc NodeCache
   * @throws QueryException Exception
   */
  private void getFlat(final Data data, final NodeCache nc)
      throws QueryException {
    Names tags = data.tagindex;
    for(int i = 1; i <= tags.size(); i++) {
      StatsKey s = tags.stat(i);
      if(!s.advIndex) {
        throw NOVAIDX.thrw(null);
      }
      if(s.kind == Kind.NONE) continue;
      FElem nd = new FElem(new QNm(tags.key(i)));
      if(s.kind == Kind.CAT) {
        nd.add(new FAttr(new QNm(type2), cate));
        for(int j = 0; j < s.cats.size(); j++) {
          FElem node = new FElem(new QNm(value));
          node.add(new FTxt(s.cats.get(j)));
          node.add(new FAttr(new QNm(count), token(s.vasize.get(j))));
          nd.add(node);
        }
      } else if(s.kind == Kind.DBL || s.kind == Kind.INT) {
        nd.add(new FAttr(new QNm(type2), metric));
        nd.add(new FAttr(new QNm(min), token(s.min)));
        nd.add(new FAttr(new QNm(max), token(s.max)));
      } else if(s.kind == Kind.TEXT) {
        nd.add(new FAttr(new QNm(type2), text));
      }
      nd.add(new FAttr(new QNm(count), token(s.counter)));
      nc.add(nd);
    }
  }

  /**
   * Returns tree facet representation.
   * @param data data reference
   * @param pn path node
   * @param par parent node
   * @param stage root level
   */
  private void getTree(final Data data, final PathNode pn, final FElem par,
      final int stage) {
    final int k = pn.kind;
    if(k == Data.ELEM) {
      final byte[] name = data.tagindex.key(pn.name);
      final FElem nd = stage == 0 ? par : new FElem(new QNm(name));
      for(final PathNode ch : pn.ch) getTree(data, ch, nd, 1);
      if(nd != par) {
        nd.add(new FAttr(new QNm(count), token(pn.size)));
        par.add(nd);
      }
    } else if(k == Data.TEXT) {
      if(pn.tkind == Kind.TEXT) {
        par.add(new FAttr(new QNm(type2), text));
      } else if(pn.tkind == Kind.CAT) {
        par.add(new FAttr(new QNm(type2), cate));
        for(int i = 0; i < pn.values.size(); i++) {
          FElem node = new FElem(new QNm(value));
          node.add(new FTxt(pn.values.get(i)));
          node.add(new FAttr(new QNm(count), token(pn.vasize.get(i))));
          par.add(node);
        }
      } else if(pn.tkind == Kind.INT || pn.tkind == Kind.DBL) {
        par.add(new FAttr(new QNm(type2), metric));
        par.add(new FAttr(new QNm(min), token(pn.min)));
        par.add(new FAttr(new QNm(max), token(pn.max)));
      }
    } else if(k == Data.ATTR) {
      // to be implemented
    }
  }
  
  /**
   * Returns the data instance for the specified argument.
   * @param i index of argument
   * @param ctx query context
   * @return data instance
   * @throws QueryException query exception
   */
  private Data data(final int i, final QueryContext ctx)
      throws QueryException {

    final Item it = checkEmpty(expr[i].item(ctx, input));
    final Type ip = it.type;
    if(ip.isNode()) return checkDBNode(it).data;
    if(ip.isString())  {
      final String name = string(it.string(input));
      if(!MetaData.validName(name, false)) INVDB.thrw(input, name);
      return ctx.resource.data(name, input);
    }
    throw STRNODTYPE.thrw(input, this, ip);
  }

  @Override
  public boolean isVacuous() {
    return false;
  }

  @Override
  public boolean uses(final Use u) {
    return super.uses(u);
  }

  @Override
  public boolean iterable() {
    // index functions will always yield ordered and duplicate-free results
    return super.iterable();
  }
}
