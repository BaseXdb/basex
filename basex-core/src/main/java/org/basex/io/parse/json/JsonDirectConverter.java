package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
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
 *       value contains all names with that type, separated by whitespaces.</li>
 * </ol></li>
 * </ol>
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class JsonDirectConverter extends JsonXmlConverter {
  /** Lax QName conversion. */
  private final boolean lax;

  /** Name of next element. */
  private byte[] name = JSON;

  /**
   * Constructor.
   * @param opts json options
   */
  public JsonDirectConverter(final JsonParserOptions opts) {
    super(opts);
    lax = jopts.get(JsonOptions.LAX);
  }

  /**
   * Adds a new element with the given type.
   * @param type JSON type
   * @return the element
   */
  private FElem addElem(final byte[] type) {
    final FElem e = new FElem(name);
    addType(e, e.name(), type);

    if(curr != null) curr.add(e);
    else curr = e;
    name = null;
    return e;
  }

  @Override
  void openObject() {
    curr = addElem(OBJECT);
  }

  @Override
  void openPair(final byte[] key) {
    name = XMLToken.encode(key, lax);
  }

  @Override
  void closePair(final boolean add) { }

  @Override
  void closeObject() {
    final FElem par = (FElem) curr.parent();
    if(par != null) curr = par;
  }

  @Override
  void openArray() {
    curr = addElem(ARRAY);
  }

  @Override
  void openItem() {
    name = VALUE;
  }

  @Override
  void closeItem() { }

  @Override
  void closeArray() {
    closeObject();
  }

  @Override
  public void openConstr(final byte[] nm) {
    // [LW] what can be done here?
    openObject();
    openPair(nm);
    openArray();
  }

  @Override
  public void openArg() {
    openItem();
  }

  @Override
  public void closeArg() {
    closeItem();
  }

  @Override
  public void closeConstr() {
    closeArray();
    closePair(true);
    closeObject();
  }

  @Override
  public void numberLit(final byte[] value) {
    addElem(NUMBER).add(value);
  }

  @Override
  public void stringLit(final byte[] value) {
    addElem(STRING).add(value);
  }

  @Override
  public void nullLit() {
    addElem(NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    addElem(BOOLEAN).add(value);
  }
}
