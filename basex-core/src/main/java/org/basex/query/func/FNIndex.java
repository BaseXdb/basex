package org.basex.query.func;

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
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Index functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNIndex extends StandardFunc {
  /** Name: name. */
  private static final String NAME = "name";
  /** Name: type. */
  private static final String TYPE = "type";
  /** Name: count. */
  private static final String COUNT = "count";
  /** Name: value. */
  private static final String ENTRY = "entry";
  /** Name: min. */
  private static final String MIN = "min";
  /** Name: max. */
  private static final String MAX = "max";
  /** Name: elements. */
  private static final byte[] ELM = NodeType.ELM.string();
  /** Name: attributes. */
  private static final byte[] ATT = NodeType.ATT.string();
  /** Flag: flat output. */
  private static final byte[] FLAT = token("flat");

  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNIndex(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _INDEX_FACETS: return facets(qc);
      default: return super.item(qc, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _INDEX_TEXTS: return values(qc, IndexType.TEXT);
      case _INDEX_ATTRIBUTES: return values(qc, IndexType.ATTRIBUTE);
      case _INDEX_ELEMENT_NAMES: return names(qc, IndexType.TAG);
      case _INDEX_ATTRIBUTE_NAMES: return names(qc, IndexType.ATTNAME);
      default: return super.iter(qc);
    }
  }

  /**
   * Returns facet information about a database.
   * @param qc query context
   * @return facet information
   * @throws QueryException query exception
   */
  private Item facets(final QueryContext qc) throws QueryException {
    final Data data = checkData(qc);
    final boolean flat = exprs.length == 2 && eq(checkStr(exprs[1], qc), FLAT);
    return new FDoc().add(flat ? flat(data) : tree(data, data.paths.root().get(0)));
  }

  /**
   * Returns all entries of the specified value index.
   * @param qc query context
   * @param it index type
   * @return text entries
   * @throws QueryException query exception
   */
  private Iter values(final QueryContext qc, final IndexType it) throws QueryException {
    final Data data = checkData(qc);
    final byte[] entry = exprs.length < 2 ? EMPTY : checkStr(exprs[1], qc);
    if(data.inMemory()) throw BXDB_MEM.get(info, data.meta.name);

    final IndexEntries et = exprs.length < 3 ? new IndexEntries(entry, it) :
      new IndexEntries(entry, checkBln(exprs[2], qc), it);
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
  static Iter entries(final Data data, final IndexEntries entries, final StandardFunc call)
      throws QueryException {

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
    if(!avl) throw BXDB_INDEX.get(call.info, data.meta.name,
        it.toString().toLowerCase(Locale.ENGLISH));
    return entries(index, entries);
  }

  /**
   * Returns all entries of the specified name index.
   * @param qc query context
   * @param it index type
   * @return text entries
   * @throws QueryException query exception
   */
  private Iter names(final QueryContext qc, final IndexType it) throws QueryException {
    final Data data = checkData(qc);
    return entries(it == IndexType.TAG ? data.tagindex : data.atnindex,
      new IndexEntries(EMPTY, it));
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
          new FElem(ENTRY).add(COUNT, token(ei.count())).add(token);
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
    index(data.tagindex, ELM, elem);
    index(data.atnindex, ATT, elem);
    return elem;
  }

  /**
   * Evaluates name index information.
   * @param names name index
   * @param name element name
   * @param root root node
   */
  private static void index(final Names names, final byte[] name, final FElem root) {
    final int ns = names.size();
    for(int n = 1; n <= ns; n++) {
      final FElem sub = new FElem(name).add(NAME, names.key(n));
      stats(names.stat(n), sub);
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
    if(root.kind == Data.ATTR || elm) elem.add(NAME, names.key(root.name));
    stats(root.stats, elem);
    for(final PathNode p : root.children) elem.add(tree(data, p));
    return elem;
  }

  /**
   * Attaches statistical information to the specified element.
   * @param stats statistics
   * @param elem element
   */
  private static void stats(final Stats stats, final FElem elem) {
    final String k = stats.type.toString().toLowerCase(Locale.ENGLISH);
    elem.add(TYPE, k);
    elem.add(COUNT, token(stats.count));
    switch(stats.type) {
      case CATEGORY:
        for(final byte[] c : stats.cats) {
          elem.add(new FElem(ENTRY).add(COUNT, token(stats.cats.get(c))).add(c));
        }
        break;
      case DOUBLE:
      case INTEGER:
        elem.add(MIN, token(stats.min));
        elem.add(MAX, token(stats.max));
        break;
      default:
        break;
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return dataLock(visitor, 1) && super.accept(visitor);
  }
}
