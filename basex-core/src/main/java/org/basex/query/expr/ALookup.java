package org.basex.query.expr;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract lookup expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class ALookup extends Arr {
  /** Wildcard string. */
  public static final Str WILDCARD = Str.get('*');

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr context expression and key specifier
   */
  public ALookup(final InputInfo info, final Expr... expr) {
    super(info, SeqType.ITEM_ZM, expr);
  }

  /**
   * Returns the looked up values for the specified input.
   * @param item input item
   * @param deep deep lookup
   * @param qc query context
   * @return supplied value builder
   * @throws QueryException query exception
   */
  final Value valueFor(final Item item, final boolean deep, final QueryContext qc)
      throws QueryException {
    if(!(item instanceof XQStruct)) throw LOOKUP_X.get(info, item);
    final XQStruct struct = (XQStruct) item;
    final Expr keys = exprs[1];

    // wildcard: add all values
    if(keys == WILDCARD) return struct.items(qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    final Iter ir = keys.atomIter(qc, info);
    for(Item key; (key = ir.next()) != null;) {
      Value value = null;
      if(struct instanceof XQMap) {
        value = ((XQMap) struct).getOrNull(key);
        if(value instanceof FuncItem) {
          value = ((FuncItem) value).toMethod(struct);
        }
      } else {
        final Item index = !deep ? key :
          key.type.isNumber() ? (Item) SeqType.INTEGER_O.cast(key, false, qc, info) : null;
        if(index != null) value = ((XQArray) struct).getOrNull(index, qc, info);
      }
      if(value != null) vb.add(value);
    }
    return vb.value(this);
  }

  @Override
  public final void toString(final QueryString qs) {
    qs.token(exprs[0]).token('?');
    if(this instanceof DeepLookup) qs.token('?');

    final Expr keys = exprs[1];
    Object key = null;
    if(keys == WILDCARD) {
      key = WILDCARD.string();
    } else if(keys instanceof Str) {
      final Str str = (Str) keys;
      if(XMLToken.isNCName(str.string())) key = str.toJava();
    } else if(keys instanceof Int) {
      final long l = ((Int) keys).itr();
      if(l >= 0) key = l;
    }
    if(key != null) qs.token(key);
    else qs.paren(keys);
  }
}
