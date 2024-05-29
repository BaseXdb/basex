package org.basex.io.parse.json;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonParserOptions.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Provides a method for parsing a JSON string and converting it to an XQuery
 * item made of nested maps. The mapping from JSON to XQuery is the following:
 * <dl>
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
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class JsonXQueryConverter extends JsonConverter {
  /** Stack for intermediate values. */
  private final Stack<Value> stack = new Stack<>();
  /** Stack for intermediate arrays. */
  private final Stack<ValueList> arrays = new Stack<>();
  /** Stack for intermediate maps. */
  private final Stack<XQMap> maps = new Stack<>();

  /**
   * Constructor.
   * @param opts json options
   * @throws QueryException query exception
   */
  JsonXQueryConverter(final JsonParserOptions opts) throws QueryException {
    super(opts);
    final JsonDuplicates dupl = jopts.get(JsonParserOptions.DUPLICATES);
    if(dupl == JsonDuplicates.RETAIN) {
      throw JSON_OPTIONS_X.get(null, JsonParserOptions.DUPLICATES.name(), dupl);
    }
  }

  @Override
  void init(final String uri) {
  }

  @Override
  public Item finish() {
    final Value value = stack.pop();
    return value.isEmpty() ? Empty.VALUE : (Item) value;
  }

  @Override
  void openObject() {
    maps.push(XQMap.empty());
  }

  @Override
  void openPair(final byte[] key, final boolean add) {
    stack.push(Str.get(shared.token(key)));
  }

  @Override
  void closePair(final boolean add) throws QueryException {
    final Value value = stack.pop();
    final Item key = (Item) stack.pop();
    if(add) maps.push(maps.pop().put(key, value));
  }

  @Override
  void closeObject() {
    stack.push(maps.pop());
  }

  @Override
  void openArray() {
    arrays.push(new ValueList());
  }

  @Override
  void openItem() {
  }

  @Override
  void closeItem() {
    arrays.peek().add(stack.pop());
  }

  @Override
  void closeArray() {
    final ArrayBuilder ab = new ArrayBuilder();
    for(final Value value : arrays.pop()) ab.append(value);
    stack.push(ab.array());
  }

  @Override
  public void numberLit(final byte[] value) throws QueryException {
    stack.push(numberParser != null ? numberParser.apply(value) : Dbl.get(Dbl.parse(value, null)));
  }

  @Override
  public void stringLit(final byte[] value) {
    stack.push(Str.get(shared.token(value)));
  }

  @Override
  public void nullLit() {
    stack.push(nullValue);
  }

  @Override
  public void booleanLit(final byte[] value) {
    stack.push(Bln.get(Token.eq(value, Token.TRUE)));
  }
}
