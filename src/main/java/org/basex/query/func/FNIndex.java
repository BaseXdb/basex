package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.query.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Index functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class FNIndex extends StandardFunc {
  /** Name: name. */
  static final QNm Q_NAME = new QNm(QueryText.NAM);
  /** Name: count. */
  static final QNm Q_COUNT = new QNm(QueryText.COUNT);
  /** Name: type. */
  static final QNm Q_TYPE = new QNm(QueryText.TYP);
  /** Name: value. */
  static final QNm Q_ENTRY = new QNm("entry");
  /** Name: min. */
  static final QNm Q_MIN = new QNm(QueryText.MIN);
  /** Name: max. */
  static final QNm Q_MAX = new QNm(QueryText.MAX);
  /** Name: elements. */
  static final QNm Q_ELM = new QNm(NodeType.ELM.string());
  /** Name: attributes. */
  static final QNm Q_ATT = new QNm(NodeType.ATT.string());
  /** Flag: flat output. */
  static final byte[] FLAT = token("flat");

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
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _INDEX_FACETS: return facets(ctx);
      default: return super.item(ctx, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _INDEX_TEXTS: return values(ctx, IndexType.TEXT);
      case _INDEX_ATTRIBUTES: return values(ctx, IndexType.ATTRIBUTE);
      case _INDEX_ELEMENT_NAMES: return names(ctx, IndexType.TAG);
      case _INDEX_ATTRIBUTE_NAMES: return names(ctx, IndexType.ATTNAME);
      default: return super.iter(ctx);
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
    final boolean flat = expr.length == 2 && eq(checkStr(expr[1], ctx), FLAT);
    return new FDoc().add(flat ? flat(data) : tree(data, data.paths.root().get(0)));
  }

  /**
   * Returns all entries of the specified value index.
   * @param ctx query context
   * @param it index type
   * @return text entries
   * @throws QueryException query exception
   */
  private Iter values(final QueryContext ctx, final IndexType it) throws QueryException {
    final Data data = data(0, ctx);
    final byte[] entry = expr.length < 2 ? Token.EMPTY : checkStr(expr[1], ctx);
    if(data.inMemory()) BXDB_MEM.thrw(info, data.meta.name);

    final IndexEntries et = expr.length < 3 ? new IndexEntries(entry, it) :
      new IndexEntries(entry, checkBln(expr[2], ctx), it);
    return entries(data, et, this);
  }

  /**
   * Returns all entries of the specified value index.
   * @param data data reference
   * @param entries container for returning index entries
   * @param call calling function
   * @return text entries
   * @throws QueryException query exception
   */
  static Iter entries(final Data data, final IndexEntries entries,
      final StandardFunc call) throws QueryException {

    final Index index;
    final boolean avl;
    final IndexType it = entries.type();
    if(it == IndexType.TEXT) {
      index = data.txtindex;
      avl = data.meta.textindex;
    } else if(it == IndexType.ATTRIBUTE) {
      index = data.atvindex;
      avl = data.meta.attrindex;
    } else {
      index = data.ftxindex;
      avl = data.meta.ftxtindex;
    }
    if(!avl) BXDB_INDEX.thrw(call.info, data.meta.name,
        it.toString().toLowerCase(Locale.ENGLISH));
    return entries(index, entries);
  }

  /**
   * Returns all entries of the specified name index.
   * @param ctx query context
   * @param it index type
   * @return text entries
   * @throws QueryException query exception
   */
  private Iter names(final QueryContext ctx, final IndexType it) throws QueryException {
    final Data data = data(0, ctx);
    return entries(it == IndexType.TAG ? data.tagindex : data.atnindex,
      new IndexEntries(Token.EMPTY, it));
  }

  /**
   * Returns all entries of the specified index.
   * @param index index
   * @param entries entries token
   * @return entry iterator
   */
  private static Iter entries(final Index index, final IndexEntries entries) {
    return new Iter() {
      final EntryIterator ei = index.entries(entries);
      @Override
      public ANode next() {
        final byte[] token = ei.next();
        return token == null ? null :
          new FElem(Q_ENTRY).add(Q_COUNT, token(ei.count())).add(token);
      }
    };
  }

  /**
   * Returns a flat facet representation.
   * @param data data reference
   * @return element
   */
  private static FElem flat(final Data data) {
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
  private static void index(final Names names, final QNm name, final FElem root) {
    for(int i = 0; i < names.size(); ++i) {
      final FElem sub = new FElem(name).add(Q_NAME, names.key(i + 1));
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
  private static FElem tree(final Data data, final PathNode root) {
    final FElem elem = new FElem(new QNm(ANode.type(root.kind).string()));
    final boolean elm = root.kind == Data.ELEM;
    final Names names = elm ? data.tagindex : data.atnindex;
    if(root.kind == Data.ATTR || elm) elem.add(Q_NAME, names.key(root.name));
    stats(root.stats, elem);
    for(final PathNode p : root.ch) elem.add(tree(data, p));
    return elem;
  }

  /**
   * Attaches statistical information to the specified element.
   * @param stats statistics
   * @param elem element
   */
  private static void stats(final Stats stats, final FElem elem) {
    final String k = stats.type.toString().toLowerCase(Locale.ENGLISH);
    elem.add(Q_TYPE, k);
    elem.add(Q_COUNT, token(stats.count));
    switch(stats.type) {
      case CATEGORY:
        for(final byte[] c : stats.cats) {
          elem.add(new FElem(Q_ENTRY).add(Q_COUNT, token(stats.cats.value(c))).add(c));
        }
        break;
      case DOUBLE:
      case INTEGER:
        elem.add(Q_MIN, token(stats.min));
        elem.add(Q_MAX, token(stats.max));
        break;
      default:
        break;
    }
  }

  @Override
  public boolean uses(final Use u) {
    // skip pre-evaluation, because cached results may get very large
    return u == Use.CTX && (sig == _INDEX_TEXTS || sig == _INDEX_ATTRIBUTES) ||
        super.uses(u);
  }

  @Override
  public boolean databases(final StringList db, final boolean rootContext) {
    if(!(expr[0] instanceof Str)) return false;
    db.add(string(((Str) expr[0]).string()));
    return true;
  }
}
