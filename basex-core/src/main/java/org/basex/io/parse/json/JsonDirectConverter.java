package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * <p>This class converts a JSON document to XML. The converted XML document is
 * both well readable and lossless, i.e., the converted document can be
 * serialized back to the original JSON representation.</p>
 *
 * <p>The specified JSON input is first transformed into a tree representation
 * and then converted to an XML document, according to the following rules:</p>
 *
 * <ol>
 * <li>The resulting document has a {@code <json/>} root node.</li>
 * <li>Names (keys) of objects are represented as elements:
 * <ol>
 *   <li>Empty names are represented by a single underscore
 *       ({@code &lt;_&gt;...&lt;/_&gt;}).</li>
 *   <li>Underscore characters are rewritten to two underscores ({@code __}).
 *   </li>
 *   <li>A character that cannot be represented as NCName character is
 *       rewritten to an underscore and its four-digit Unicode.</li>
 * </ol></li>
 * <li>As arrays have no names, {@code <value/>} is used as element name.
 * <li>JSON values are represented as text nodes.</li>
 * <li>The types of values are represented in attributes:
 * <ol>
 *   <li>The value types <i>number</i>, <i>boolean</i>, <i>null</i>,
 *       <i>object</i> and <i>array</i> are represented by a
 *       {@code type} attribute.</li>
 *   <li>The <i>string</i> type is omitted, as it is treated as default type.
 *   </li>
 *   <li>If a name has the same type throughout the document, the {@code type}
 *       attribute will be omitted. Instead, the name will be listed in
 *       additional, type-specific attributes in the root node. The attributes
 *       are named by their type in the plural (<i>numbers</i>, <i>booleans</i>,
 *       <i>nulls</i>, <i>objects</i> and <i>arrays</i>), and the attribute
 *       value contains all names with that type, separated by whitespace.</li>
 * </ol></li>
 * </ol>
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class JsonDirectConverter extends JsonXmlConverter {
  /** Lax QName conversion. */
  private final boolean lax;

  /**
   * Constructor.
   * @param opts json options
   * @throws QueryIOException query I/O exception
   */
  JsonDirectConverter(final JsonParserOptions opts) throws QueryIOException {
    super(opts);
    lax = jopts.get(JsonOptions.LAX);
    name = JSON;
  }

  @Override
  void openObject() {
    openOuter(OBJECT);
  }

  @Override
  void closeObject() {
    closeOuter();
  }

  @Override
  void openPair(final byte[] key, final boolean add) {
    addValues.add(add);
    if(add) name = shared.token(XMLToken.encode(key, lax));
  }

  @Override
  void closePair(final boolean add) {
    addValues.pop();
  }

  @Override
  void openArray() {
    openOuter(ARRAY);
  }

  @Override
  void closeArray() {
    closeOuter();
  }

  @Override
  void openItem() {
    name = VALUE;
  }

  @Override
  void closeItem() { }

  @Override
  void addValue(final byte[] type, final byte[] value) {
    if(addValues.peek()) {
      final byte[] val = value != null ? shared.token(value) : null;
      final FBuilder elem = element(type).add(val);
      if(curr != null) curr.add(elem);
      else curr = elem;
    }
  }

  /**
   * Opens an outer entry.
   * @param type JSON type
   */
  private void openOuter(final byte[] type) {
    curr = element(type);
    stack.push(curr);
  }

  /**
   * Closes an outer entry.
   */
  private void closeOuter() {
    curr = stack.pop();
    if(!stack.isEmpty()) curr = stack.peek().add(curr);
  }

  /**
   * Adds a new element with the given type.
   * @param type JSON type
   * @return the element
   */
  private FBuilder element(final byte[] type) {
    final FBuilder elem = FElem.build(shared.qName(name));
    processType(elem, type);
    return elem;
  }
}
