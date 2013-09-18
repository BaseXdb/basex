package org.basex.query.util.json;

import java.util.*;

import org.basex.query.QueryException;
import org.basex.query.util.json.JsonParser.*;
import org.basex.query.value.Value;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.seq.Empty;
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
 *     <code>map{1:='foo', 2:=true(), 3:=123}</code>)
 *   <dt>object (e.g. <code>{"foo": 42, "bar": null}</code>)
 *   <dd>an XQuery map (e.g.
 *     <code>map{'foo':=42, 'bar':=()}</code>)
 * </dl>
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class JsonMapConverter extends JsonConverter implements JsonHandler {
  /** Stack for intermediate values. */
  private final Stack<Value> stack = new Stack<Value>();
  /** JSON spec. */
  private final Spec spec;
  /** Unescape flag. */
  private final boolean unescape;

  /**
   * Constructor.
   * @param sp JSON spec
   * @param unesc unescape flag
   * @param ii input info
   */
  public JsonMapConverter(final Spec sp, final boolean unesc, final InputInfo ii) {
    super(ii);
    spec = sp;
    unescape = unesc;
  }

  @Override
  public Item convert(final String in) throws QueryException {
    stack.clear();
    JsonParser.parse(in, spec, unescape, this, info);
    return stack.peek().isEmpty() ? null : (Item) stack.pop();
  }

  @Override
  public void openObject() {
    stack.push(Map.EMPTY);
  }

  @Override
  public void openEntry(final byte[] key) {
    stack.push(Str.get(key));
  }

  @Override
  public void closeEntry() throws QueryException {
    final Value val = stack.pop();
    final Item key = (Item) stack.pop();
    final Map map = (Map) stack.pop();
    stack.push(map.insert(key, val, null));
  }

  @Override public void closeObject() { }

  @Override
  public void openArray() {
    stack.push(Map.EMPTY);
  }

  @Override
  public void openArrayEntry() {
    stack.push(Int.get(((Map) stack.peek()).mapSize() + 1));
  }

  @Override
  public void closeArrayEntry() throws QueryException {
    closeEntry();
  }

  @Override public void closeArray() { }

  @Override
  public void openConstr(final byte[] name) {
    openObject();
    openEntry(name);
    openArray();
  }

  @Override public void openArg() {
    openArrayEntry();
  }

  @Override public void closeArg() throws QueryException {
    closeArrayEntry();
  }

  @Override
  public void closeConstr() throws QueryException {
    closeArray();
    closeEntry();
    closeObject();
  }

  @Override
  public void numberLit(final byte[] val) throws QueryException {
    stack.push(Dbl.get(val, info));
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
  public void booleanLit(final boolean b) {
    stack.push(Bln.get(b));
  }
}
