package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;
import org.basex.query.QueryException;
import org.basex.query.item.ANode;
import org.basex.query.item.FAttr;
import org.basex.query.item.FElem;
import org.basex.query.item.FTxt;
import org.basex.query.item.QNm;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.XMLToken;
import org.basex.util.hash.TokenObjMap;

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
 */
public final class JSONConverter {
  /** Plural. */
  private static final byte[] S = { 's' };
  /** Global data type attributes. */
  private static final byte[][] ATTRS = { concat(BOOL, S), concat(NUM, S),
    concat(NULL, S), concat(ARR, S), concat(OBJ, S) };
  /** Names of data type classes. */
  private static final Class<?>[] CLASSES = {
      JBoolean.class, JNumber.class, JNull.class, JArray.class, JObject.class
  };
  /** Name: type. */
  private static final QNm Q_TYPE = new QNm(TYPE);

  /** Cached names. */
  private final TokenObjMap<QNm> qnames = new TokenObjMap<QNm>();
  /** Cached types. */
  private final TokenObjMap<Class<?>> types = new TokenObjMap<Class<?>>();
  /** Input info. */
  private final InputInfo input;

  /**
   * Constructor.
   * @param ii input info
   */
  public JSONConverter(final InputInfo ii) {
    input = ii;
  }

  /**
   * Parses the input.
   * @param q query
   * @return resulting node
   * @throws QueryException query exception
   */
  public ANode parse(final byte[] q) throws QueryException {
    final JStruct node = new JSONParser(q, input).parse();
    // find unique data types
    types.add(JSON, node.getClass());
    analyze(node);
    // create XML fragment
    final FElem root = create(JSON, node);
    // attach data types to root node
    attach(root);
    // return node
    return root;
  }

  /**
   * Analyzes the data types: caches names and their corresponding types.
   * @param value node to be analyzed
   */
  private void analyze(final JValue value) {
    if(value instanceof JStruct) {
      final JStruct n = (JStruct) value;
      for(int s = 0; s < n.size(); s++) {
        final boolean obj = value instanceof JObject;
        final byte[] name = convert(obj ? ((JObject) n).name(s) : VALUE);
        final Class<?> clz = n.value(s).getClass();
        final Class<?> type = types.get(name);
        if(type == null) {
          types.add(name, clz);
        } else if(type != JValue.class && type != clz) {
          types.add(name, JValue.class);
        }
        analyze(n.value(s));
      }
    }
  }

  /**
   * Converts the JSON tree to XML.
   * @param name of root node
   * @return root node
   * @param value node to be converted
   */
  private FElem create(final byte[] name, final JValue value) {
    final byte[] nm = convert(name);
    final FElem root = new FElem(qname(nm));
    final Class<?> clz = types.get(nm);
    final boolean type = clz == null || clz == JValue.class;

    if(value instanceof JStruct) {
      final boolean obj = value instanceof JObject;
      if(type) root.add(new FAttr(Q_TYPE, value.type()));
      final JStruct n = (JStruct) value;
      for(int s = 0; s < n.size(); s++) {
        root.add(create(obj ? ((JObject) n).name(s) : VALUE, n.value(s)));
      }
    } else {
      final JAtom a = (JAtom) value;
      if(type && !(a instanceof JString)) root.add(new FAttr(Q_TYPE, a.type()));
      final byte[] v = a.value();
      if(v != null && v.length != 0) root.add(new FTxt(v));
    }
    return root;
  }

  /**
   * Attaches the data types to the root node.
   * @param root root node
   */
  private void attach(final FElem root) {
    final TokenBuilder[] builders = new TokenBuilder[CLASSES.length];
    for(int b = 0; b < builders.length; b++) builders[b] = new TokenBuilder();

    for(int i = 1; i <= types.size(); i++) {
      final Class<?> clz = types.value(i);
      for(int b = 0; b < builders.length; b++) {
        if(clz == CLASSES[b]) {
          if(builders[b].size() != 0) builders[b].add(' ');
          builders[b].add(types.key(i));
          break;
        }
      }
    }
    for(int b = 0; b < builders.length; b++) {
      if(builders[b].size() == 0) continue;
      root.add(new FAttr(qname(ATTRS[b]), builders[b].trim().finish()));
    }
  }

  /**
   * Converts a JSON to an XML name.
   * @param name name
   * @return converted name
   */
  private byte[] convert(final byte[] name) {
    // convert name to valid XML representation
    final TokenBuilder tb = new TokenBuilder();
    for(int n = 0; n < name.length; n += cl(name, n)) {
      int cp = cp(name, n);
      if(cp == '_') {
        tb.add('_').add('_');
      } else if(n == 0 ? XMLToken.isNCStartChar(cp) : XMLToken.isNCChar(cp)) {
        tb.add(cp);
      } else {
        tb.add('_');
        final byte[] buf = new byte[4];
        int p = buf.length;
        do {
          final int b = cp & 0x0F;
          buf[--p] = (byte) (b + (b > 9 ? 0x37 : '0'));
          cp >>>= 4;
        } while(p != 0);
        tb.add(buf);
      }
    }
    if(tb.size() == 0) tb.add('_');
    return tb.finish();
  }

  /**
   * Returns a cached {@link QNm} instance for the specified name.
   * @param name name
   * @return cached QName
   */
  private QNm qname(final byte[] name) {
    // retrieve name from cache, or create new instance
    QNm qname = qnames.get(name);
    if(qname == null) {
      qname = new QNm(name);
      qnames.add(name, qname);
    }
    return qname;
  }
}
