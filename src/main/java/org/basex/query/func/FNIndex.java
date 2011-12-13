package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.Locale;

import org.basex.data.Data;
import org.basex.index.Names;
import org.basex.index.Stats;
import org.basex.index.path.PathNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FDoc;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.item.QNm;
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
  /** Name: name. */
  private static final QNm Q_NAME = new QNm(NAM);
  /** Name: count. */
  private static final QNm Q_COUNT = new QNm(COUNT);
  /** Name: type. */
  private static final QNm Q_TYPE = new QNm(TYP);
  /** Name: value. */
  private static final QNm Q_VALUE = new QNm(VAL);
  /** Name: min. */
  private static final QNm Q_MIN = new QNm(MIN);
  /** Name: max. */
  private static final QNm Q_MAX = new QNm(MAX);
  /** Name: elements. */
  private static final QNm Q_ELM = new QNm(NodeType.ELM.string());
  /** Name: attributes. */
  private static final QNm Q_ATT = new QNm(NodeType.ATT.string());

  /** Flag: flat output. */
  private static final byte[] FLAT = token("flat");

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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    switch(def) {
      case _INDEX_FACETS: return facets(ctx);
      default:            return super.item(ctx, ii);
    }
  }

  /**
   * Returns facet information about a database.
   * @param ctx query context
   * @return facet information
   * @throws QueryException query exception
   */
  private Item facets(final QueryContext ctx) throws QueryException {
    final Data data = data(0, ctx);
    if(!data.meta.pathindex) throw NOVAIDX.thrw(input);

    final boolean flat = expr.length == 2 && eq(checkStr(expr[1], ctx), FLAT);
    final NodeCache nc = new NodeCache();
    nc.add(flat ? flat(data) : tree(data, data.pthindex.root().get(0)));
    return new FDoc(nc, Token.EMPTY);
  }

  /**
   * Returns a flat facet representation.
   * @param data data reference
   * @return element
   */
  private FElem flat(final Data data) {
    final FElem elem = new FElem(new QNm(NodeType.DOC.string()));
    index(data.tagindex, Q_ELM, elem);
    index(data.atnindex, Q_ATT, elem);
    return elem;
  }

  /**
   * Evaluates name index information.
   * @param names name index
   * @param name element name
   * @param root root node
   */
  private void index(final Names names, final QNm name, final FElem root) {
    for(int i = 0; i < names.size(); ++i) {
      final FElem sub = new FElem(name);
      sub.add(new FAttr(Q_NAME, names.key(i + 1)));
      stats(names.stat(i + 1), sub);
      root.add(sub);
    }
  }

  /**
   * Returns tree facet representation.
   * @param data data reference
   * @param root root node
   * @return element
   */
  private FElem tree(final Data data, final PathNode root) {
    final FElem elem = new FElem(new QNm(ANode.type(root.kind).string()));
    final boolean elm = root.kind == Data.ELEM;
    final Names names = elm ? data.tagindex : data.atnindex;
    if(root.kind == Data.ATTR || elm) {
      elem.add(new FAttr(Q_NAME, names.key(root.name)));
    }
    stats(root.stats, elem);
    for(final PathNode p : root.ch) elem.add(tree(data, p));
    return elem;
  }

  /**
   * Attaches statistical information to the specified element.
   * @param stats statistics
   * @param elem element
   */
  private void stats(final Stats stats, final FElem elem) {
    final String k = stats.type.toString().toLowerCase(Locale.ENGLISH);
    elem.add(new FAttr(Q_TYPE, Token.token(k)));
    elem.add(new FAttr(Q_COUNT, Token.token(stats.count)));
    switch(stats.type) {
      case CATEGORY:
        for(final byte[] c : stats.cats) {
          final FElem sub = new FElem(Q_VALUE);
          sub.add(new FAttr(Q_COUNT, Token.token(stats.cats.value(c))));
          sub.add(new FTxt(c));
          elem.add(sub);
        }
        break;
      case DOUBLE:
      case INTEGER:
        elem.add(new FAttr(Q_MIN, Token.token(stats.min)));
        elem.add(new FAttr(Q_MAX, Token.token(stats.max)));
        break;
      default:
        break;
    }
  }
}
