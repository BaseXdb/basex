package org.basex.query.util.json;

import org.basex.query.QueryException;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Empty;
import org.basex.query.item.Itr;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.item.map.Map;
import org.basex.util.InputInfo;
import org.basex.util.list.TokenList;

/**
 * <p>Provides a method for parsing a JSON string and converting it to an XQuery
 * item made of nested maps.
 *
 * <p>The mapping from JSON to XQuery is the following:
 * <p><dl>
 *   <dt>string<dd>xs:string
 *   <dt>number<dd>xs:double
 *   <dt>boolean<dd>xs:boolean
 *   <dt>null<dd>an empty sequence <code>()</code>
 *   <dt>array (e.g. {@code ["foo", true, 123]})
 *   <dd>an XQuery map with integer keys, starting by 1 (e.g.
 *     <code>map{1:='foo', 2:=true(), 3:=123}</code>)
 *   <dt>object (e.g. <code>{"foo": 42, "bar": null}</code>)
 *   <dd>an XQuery map (e.g.
 *     <code>map{'foo':=42, 'bar':=()}</code>)
 * </dl>
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class JsonMapConverter {

  /** Hidden default constructor. */
  private JsonMapConverter() { /* void */ }

  /**
   * Parses a JSON string and converts it to an XQuery item made of nested maps.
   * @param json JSON string
   * @param ii input info
   * @return XQuery item
   * @throws QueryException exception
   */
  public static Value parse(final byte[] json, final InputInfo ii)
      throws QueryException {
    return convert(new JSONParser(json, ii).parse());
  }

  /**
   * Converts a JSON AST into an XQuery expression.
   * @param nd JSON node
   * @return XQuery value
   * @throws QueryException exception
   */
  private static Value convert(final JValue nd) throws QueryException {
    if(nd instanceof JAtom) {
      final byte[] type = nd.type(), val = ((JAtom) nd).value();
      switch(type[0]) {
        case 'b': return Bln.get(val[0] == 't');
        case 'n': return type[2] == 'm' ? Dbl.get(val, null) : Empty.SEQ;
        default:  return Str.get(val);
      }
    }

    Map map = Map.EMPTY;
    final JStruct st = (JStruct) nd;
    final TokenList names = st instanceof JObject ? ((JObject) st).names : null;
    for(int i = st.size(); --i >= 0;) {
      map = map.insert(names == null ? Itr.get(i + 1) : Str.get(names.get(i)),
          convert(st.value(i)), null);
    }
    return map;
  }
}
