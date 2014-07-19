package org.basex.io.parse.json;

import java.util.*;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.seq.*;
import org.basex.util.*;

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
 *     <code>{1:'foo', 2:true(), 3:123}</code>)
 *   <dt>object (e.g. <code>{"foo": 42, "bar": null}</code>)
 *   <dd>an XQuery map (e.g.
 *     <code>{'foo':42, 'bar':()}</code>)
 * </dl>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class JsonMapConverter extends JsonConverter {
  /** Stack for intermediate values. */
  private final Stack<Value> stack = new Stack<>();

  /**
   * Constructor.
   * @param opts json options
   */
  public JsonMapConverter(final JsonParserOptions opts) {
    super(opts);
  }

  @Override
  public Item finish() {
    return stack.peek().isEmpty() ? null : (Item) stack.pop();
  }

  @Override
  void openObject() {
    stack.push(Map.EMPTY);
  }

  @Override
  void openPair(final byte[] key) {
    stack.push(Str.get(key));
  }

  @Override
  void closePair() throws QueryIOException {
    final Value val = stack.pop();
    final Item key = (Item) stack.pop();
    final Map map = (Map) stack.pop();
    try {
      stack.push(map.insert(key, val, null));
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  void closeObject() { }

  @Override
  void openArray() {
    stack.push(Map.EMPTY);
  }

  @Override
  void openItem() {
    stack.push(Int.get(((Map) stack.peek()).mapSize() + 1));
  }

  @Override
  void closeItem() throws QueryIOException {
    closePair();
  }

  @Override
  void closeArray() { }

  @Override
  public void openConstr(final byte[] name) {
    openObject();
    openPair(name);
    openArray();
  }

  @Override public void openArg() {
    openItem();
  }

  @Override public void closeArg() throws QueryIOException {
    closeItem();
  }

  @Override
  public void closeConstr() throws QueryIOException {
    closeArray();
    closePair();
    closeObject();
  }

  @Override
  public void numberLit(final byte[] value) throws QueryIOException {
    try {
      stack.push(Dbl.get(value, null));
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  public void stringLit(final byte[] value) {
    stack.push(Str.get(value));
  }

  @Override
  public void nullLit() {
    stack.push(Empty.SEQ);
  }

  @Override
  public void booleanLit(final byte[] value) {
    stack.push(Bln.get(Token.eq(value, Token.TRUE)));
  }
}
