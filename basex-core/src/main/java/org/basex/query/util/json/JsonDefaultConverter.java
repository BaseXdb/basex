package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

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
 *       (<code>&lt;_&gt;...&lt;/_&gt;</code>).</li>
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class JsonDefaultConverter extends JsonXMLConverter {
  /** Type names. */
  private static final byte[][] NAMES = { T_ARRAY, T_OBJECT, T_STRING, T_NUMBER,
    T_BOOLEAN, NULL };
  /** The {@code types} QNames. */
  private static final byte[][] TYPES = new byte[NAMES.length][];

  static {
    final byte[] s = { 's' };
    for(int i = 0; i < NAMES.length; i++) TYPES[i] = concat(NAMES[i], s);
  }

  /**
   * Constructor.
   * @param opts json options
   * @param ii input info
   */
  public JsonDefaultConverter(final JsonOptions opts, final InputInfo ii) {
    super(opts, ii);
  }

  @Override
  public ANode convert(final String in) throws QueryException {
    final JsonDefaultHandler handler = new JsonDefaultHandler(jopts.bool(JsonOptions.LAX));
    JsonParser.parse(in, jopts, handler, info);
    final ByteList[] types = new ByteList[TYPES.length];
    for(final TypedArray arr : handler.names.values()) {
      if(arr != null) {
        for(int i = 0; i < NAMES.length; i++) {
          if(arr.type == NAMES[i] && arr.type != T_STRING) {
            if(types[i] == null) types[i] = new ByteList();
            else types[i].add(' ');
            types[i].add(arr.vals[0].qname().string());
            break;
          }
        }
      }
    }

    for(int i = 0; i < types.length; i++)
      if(types[i] != null) handler.elem.add(TYPES[i], types[i].toArray());
    return handler.elem;
  }

  /** JSON handler containing the state of the conversion. */
  static class JsonDefaultHandler implements JsonHandler {
    /** Map from element name to a pair of all its nodes and the collective node type. */
    final TokenObjMap<TypedArray> names = new TokenObjMap<TypedArray>();
    /** Lax QName conversion. */
    private final boolean lax;
    /** The next element's name. */
    private byte[] name = T_JSON;
    /** The current node. */
    FElem elem;

    /**
     * Constructor.
     * @param l lax name conversion
     */
    JsonDefaultHandler(final boolean l) {
      lax = l;
    }

    /**
     * Adds a new element with the given type.
     * @param type JSON type
     * @return the element
     */
    private FElem addElem(final byte[] type) {
      final FElem e = new FElem(name);

      if(names.contains(name)) {
        final TypedArray arr = names.get(name);
        if(arr != null && arr.type == type) {
          arr.add(e);
        } else {
          if(arr != null) {
            names.put(name, null);
            if(arr.type != T_STRING)
              for(int i = 0; i < arr.size; i++) arr.vals[i].add(T_TYPE, arr.type);
          }
          if(type != T_STRING) e.add(T_TYPE, type);
        }
      } else {
        names.put(name, new TypedArray(type, e));
      }
      if(elem != null) elem.add(e);
      else elem = e;
      name = null;
      return e;
    }

    @Override
    public void openObject() {
      elem = addElem(T_OBJECT);
    }

    @Override
    public void openPair(final byte[] key) {
      name = XMLToken.encode(key, lax);
    }

    @Override
    public void closePair() { }

    @Override
    public void closeObject() {
      final FElem par = (FElem) elem.parent();
      if(par != null) elem = par;
    }

    @Override
    public void openArray() {
      elem = addElem(T_ARRAY);
    }

    @Override
    public void openItem() {
      name = T_VALUE;
    }

    @Override
    public void closeItem() { }

    @Override
    public void closeArray() {
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
      closePair();
      closeObject();
    }

    @Override
    public void numberLit(final byte[] value) {
      addElem(T_NUMBER).add(value);
    }

    @Override
    public void stringLit(final byte[] value) {
      addElem(T_STRING).add(value);
    }

    @Override
    public void nullLit() {
      addElem(NULL);
    }

    @Override
    public void booleanLit(final byte[] b) {
      addElem(T_BOOLEAN).add(b);
    }
  }

  /**
   * A simple container for all elements having the same name.
   * @author Leo Woerteler
   */
  private static final class TypedArray {
    /** The shared JSON type. */
    final byte[] type;
    /** The nodes. */
    FElem[] vals = new FElem[8];
    /** Logical size of {@link #vals}.  */
    int size;

    /**
     * Constructor.
     * @param tp JSON type
     * @param nd element
     */
    TypedArray(final byte[] tp, final FElem nd) {
      type = tp;
      vals[0] = nd;
      size = 1;
    }

    /**
     * Adds a new element to the list.
     * @param nd element to add
     */
    void add(final FElem nd) {
      if(size == vals.length) vals = Array.copy(vals, new FElem[Array.newSize(size)]);
      vals[size++] = nd;
    }
  }
}
