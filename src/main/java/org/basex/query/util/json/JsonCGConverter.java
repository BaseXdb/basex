package org.basex.query.util.json;

import org.basex.query.QueryException;
import org.basex.query.util.*;
import org.basex.query.util.json.JsonParser.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

import static org.basex.util.Token.*;

import org.basex.util.*;
import org.basex.util.hash.TokenObjMap;
import org.basex.util.list.ByteList;

import static org.basex.data.DataText.*;

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
public final class JsonCGConverter extends JsonXMLConverter {
  /** Type names. */
  private static final byte[][] NAMES = { T_ARRAY, T_OBJECT, T_STRING, T_NUMBER,
    T_BOOLEAN, NULL };
  /** The underscore. */
  static final byte[] UNDERSCORE = { '_' };
  /** The {@code type} QName. */
  static final QNm TYPE = new QNm(T_TYPE);
  /** The {@code entry} QName. */
  static final QNm VALUE = new QNm(T_VALUE);
  /** The {@code types} QNames. */
  private static final QNm[] TYPES = new QNm[NAMES.length];
  static {
    final byte[] s = { 's' };
    for(int i = 0; i < NAMES.length; i++) TYPES[i] = new QNm(concat(NAMES[i], s));
  }

  /** Spec to use. */
  private final Spec spec;
  /** Flag for interpreting character escape sequences. */
  private final boolean unescape;

  /**
   * Constructor.
   * @param sp JSON spec to use
   * @param unesc unescape flag
   * @param ii input info
   */
  public JsonCGConverter(final Spec sp, final boolean unesc, final InputInfo ii) {
    super(ii);
    spec = sp;
    unescape = unesc;
  }

  @Override
  public ANode convert(final String in) throws QueryException {
    final JsonCGHandler handler = new JsonCGHandler();
    JsonParser.parse(in, spec, unescape, handler, null);
    final ByteList[] types = new ByteList[TYPES.length];
    for(int n = 0; n < handler.names.size(); n++) {
      final TypedArray arr = handler.names.value(n + 1);
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

  /**
   * Adds the given 16-bit char to the token builder in encoded form.
   * @param tb token builder
   * @param cp char
   * @return the token builder for convenience
   */
  static TokenBuilder addEsc(final TokenBuilder tb, final int cp) {
    tb.addByte(UNDERSCORE[0]);
    final int a = cp >>> 12;
    tb.addByte((byte) (a + (a > 9 ? 'A' : '0')));
    final int b = (cp >>> 8) & 0x0F;
    tb.addByte((byte) (b + (b > 9 ? 'A' : '0')));
    final int c = (cp >>> 4) & 0x0F;
    tb.addByte((byte) (c + (c > 9 ? 'A' : '0')));
    final int d = cp & 0x0F;
    tb.addByte((byte) (d + (d > 9 ? 'A' : '0')));
    return tb;
  }

  /** JSON handler containing the state of the conversion. */
  private static class JsonCGHandler implements JsonHandler {
    /** Map from element name to a pair of all its nodes and the collective node type. */
    final TokenObjMap<TypedArray> names = new TokenObjMap<TypedArray>();
    /** Cache for QNames. */
    private final TokenObjMap<QNm> nameCache = new TokenObjMap<QNm>();
    /** The next element's name. */
    private QNm name;
    /** The current node. */
    FElem elem;

    /** Constructor. */
    JsonCGHandler() {
      nameCache.add(T_JSON, name = new QNm(T_JSON));
    }

    /**
     * Adds a new element with the given type.
     * @param type JSON type
     * @return the element
     */
    FElem addElem(final byte[] type) {
      final FElem e = new FElem(name);
      final byte[] nm = name.string();

      if(names.contains(nm)) {
        final TypedArray arr = names.get(nm);
        if(arr != null && arr.type == type) {
          arr.add(e);
        } else {
          if(arr != null) {
            names.add(nm, null);
            if(arr.type != T_STRING)
              for(int i = 0; i < arr.size; i++) arr.vals[i].add(TYPE, arr.type);
          }
          if(type != T_STRING) e.add(TYPE, type);
        }
      } else {
        names.add(nm, new TypedArray(type, e));
      }
      if(elem != null) elem.add(e);
      else elem = e;
      name = null;
      return e;
    }

    /**
     * Creates a valid XML NCName from the given token.
     * @param tok token
     * @return valid NCName
     */
    private static byte[] name(final byte[] tok) {
      if(tok.length == 0) return UNDERSCORE;
      for(int i = 0, cp; i < tok.length; i += cl(tok, i)) {
        cp = cp(tok, i);
        if(cp == '_' || !(i == 0 ? XMLToken.isNCStartChar(cp) : XMLToken.isNCChar(cp))) {
          final TokenBuilder tb = new TokenBuilder(tok.length << 1).add(tok, 0, i);
          for(int j = i; j < tok.length; j += cl(tok, j)) {
            cp = cp(tok, j);
            if(cp == '_') tb.addByte(UNDERSCORE[0]).addByte(UNDERSCORE[0]);
            else if(j == 0 ? XMLToken.isNCStartChar(cp) :
              XMLToken.isNCChar(cp)) tb.add(cp);
            else if(cp < 0x10000) addEsc(tb, cp);
            else {
              final int r = cp - 0x10000;
              addEsc(addEsc(tb, (r >>> 10) + 0xD800), (r & 0x3FF) + 0xDC00);
            }
          }
          return tb.finish();
        }
      }
      return tok;
    }

    @Override
    public void openObject() throws QueryException {
      elem = addElem(T_OBJECT);
    }

    @Override
    public void openEntry(final byte[] key) throws QueryException {
      name = nameCache.get(key);
      if(name == null) nameCache.add(key, name = new QNm(name(key)));
    }

    @Override
    public void closeEntry() throws QueryException { }

    @Override
    public void closeObject() throws QueryException {
      final FElem par = (FElem) elem.parent();
      if(par != null) elem = par;
    }

    @Override
    public void openArray() throws QueryException {
      elem = addElem(T_ARRAY);
    }

    @Override
    public void openArrayEntry() throws QueryException {
      name = VALUE;
    }

    @Override
    public void closeArrayEntry() throws QueryException { }

    @Override
    public void closeArray() throws QueryException {
      closeObject();
    }

    @Override
    public void openConstr(final byte[] nm) throws QueryException {
      // [LW] what can be done here?
      openObject();
      openEntry(nm);
      openArray();
    }

    @Override
    public void openArg() throws QueryException {
      openArrayEntry();
    }

    @Override
    public void closeArg() throws QueryException {
      closeArrayEntry();
    }

    @Override
    public void closeConstr() throws QueryException {
      closeArray();
      closeEntry();
      closeObject();
    }

    @Override
    public void numberLit(final byte[] value) throws QueryException {
      addElem(T_NUMBER).add(value);
    }

    @Override
    public void stringLit(final byte[] value) throws QueryException {
      addElem(T_STRING).add(value);
    }

    @Override
    public void nullLit() throws QueryException {
      addElem(NULL);
    }

    @Override
    public void booleanLit(final boolean b) throws QueryException {
      addElem(T_BOOLEAN).add(token(b));
    }
  }

  /**
   * A simple container for all elements having the same name.
   * @author Leo Woerteler
   */
  private static class TypedArray {
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
    protected TypedArray(final byte[] tp, final FElem nd) {
      type = tp;
      vals[0] = nd;
      size = 1;
    }

    /**
     * Adds a new element to the list.
     * @param nd element to add
     */
    protected void add(final FElem nd) {
      if(size == vals.length) {
        final FElem[] nVals = new FElem[size << 1];
        System.arraycopy(vals, 0, nVals, 0, size);
        vals = nVals;
      }
      vals[size++] = nd;
    }
  }
}
