package org.basex.io.parse.json;

import static org.basex.query.QueryError.*;

import java.math.*;
import java.util.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonParserOptions.*;
import org.basex.query.*;
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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class JsonW3Converter extends JsonConverter {
  /** Maximum long value as BigDecimal. */
  private static final BigDecimal MAX_LONG_BD = BigDecimal.valueOf(Long.MAX_VALUE);
  /** Minimum long value as BigDecimal. */
  private static final BigDecimal MIN_LONG_BD = BigDecimal.valueOf(Long.MIN_VALUE);

  /** Stack for intermediate values. */
  private final Stack<Value> stack = new Stack<>();
  /** Stack for intermediate arrays. */
  private final Stack<ArrayBuilder> arrays = new Stack<>();
  /** Stack for intermediate maps. */
  private final Stack<MapBuilder> maps = new Stack<>();
  /** Number format. */
  private JsonNumberFormat fmt;

  /**
   * Constructor.
   * @param opts JSON options
   * @throws QueryException query exception
   */
  JsonW3Converter(final JsonParserOptions opts) throws QueryException {
    super(opts);
    fmt = jopts.get(JsonParserOptions.NUMBER_FORMAT);
    final JsonDuplicates dupl = jopts.get(JsonParserOptions.DUPLICATES);
    if(dupl == JsonDuplicates.RETAIN) {
      throw OPTION_JSON_X.get(null, JsonParserOptions.DUPLICATES.name(), dupl);
    }
  }

  @Override
  protected void init(final String uri) {
  }

  @Override
  public Item finish() {
    final Value value = stack.pop();
    return value.isEmpty() ? Empty.VALUE : (Item) value;
  }

  @Override
  protected void openObject() {
    maps.push(new MapBuilder());
  }

  @Override
  protected void openPair(final byte[] key, final boolean add) {
    stack.push(Str.get(shared.token(key)));
  }

  @Override
  protected void closePair(final boolean add) throws QueryException {
    final Value value = stack.pop();
    final Item key = (Item) stack.pop();
    if(add) maps.peek().put(key, value);
  }

  @Override
  protected void closeObject() {
    stack.push(maps.pop().map());
  }

  @Override
  protected void openArray() {
    arrays.push(new ArrayBuilder(job));
  }

  @Override
  protected void openItem() {
  }

  @Override
  protected void closeItem() {
    arrays.peek().add(stack.pop());
  }

  @Override
  protected void closeArray() {
    stack.push(arrays.pop().array());
  }

  @Override
  public void numberLit(final byte[] string) throws QueryException {
    Value value = null;
    if(fmt != JsonNumberFormat.DOUBLE) {
      final BigDecimal bd = Dec.parse(string, null, false);
      if(bd != null) {
        value = decimalOrInteger(bd);
      } else if(fmt == JsonNumberFormat.DECIMAL) {
        try {
          value = decimalOrInteger(new BigDecimal(Dbl.parse(string, null)));
        } catch(final NumberFormatException ex) {
          Util.debug(ex);
          throw FUNCCAST_X_X.get(info, "xs:decimal", string);
        }
      }
    }
    if(value == null) value = Dbl.get(Dbl.parse(string, null));
    stack.push(value);
  }

  /**
   * Converts a decimal to an integer if possible.
   * @param bd decimal value
   * @return integer or decimal item
   */
  private static Item decimalOrInteger(final BigDecimal bd) {
    final BigDecimal normalized = bd.stripTrailingZeros();
    if(normalized.scale() <= 0 &&
        normalized.compareTo(MIN_LONG_BD) >= 0 &&
        normalized.compareTo(MAX_LONG_BD) <= 0) {
      return Itr.get(normalized.longValue());
    }
    return Dec.get(bd);
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
