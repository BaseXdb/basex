package org.basex.query.func.index;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.index.path.*;
import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class IndexFacets extends IndexFn {
  /** Name: name. */
  private static final String NAME = "name";
  /** Name: type. */
  private static final String TYPE = "type";
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

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Data data = checkData(qc);
    final boolean flat = exprs.length == 2 && eq(toToken(exprs[1], qc), FLAT);
    return new FDoc().add(flat ? flat(data) : tree(data, data.paths.root().get(0)));
  }

  /**
   * Returns a flat facet representation.
   * @param data data reference
   * @return element
   */
  private static FElem flat(final Data data) {
    final FElem elem = new FElem(new QNm(NodeType.DOC.string()));
    index(data.elemNames, ELM, elem);
    index(data.attrNames, ATT, elem);
    return elem;
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
    final Names names = elm ? data.elemNames : data.attrNames;
    if(root.kind == Data.ATTR || elm) elem.add(NAME, names.key(root.name));
    stats(root.stats, elem);
    for(final PathNode p : root.children) elem.add(tree(data, p));
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
}
