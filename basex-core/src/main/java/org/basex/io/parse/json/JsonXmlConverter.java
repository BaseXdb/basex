package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.json.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class converts JSON data to XML events, which are sent to an {@link XmlHandler}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class JsonXmlConverter extends JsonConverter {
  /** No namespace declarations. */
  static final Atts NO_NSP = new Atts();

  /** Attributes of the next element. */
  final Atts atts = new Atts();
  /** Target of the XML events. */
  XmlHandler handler;

  /** External handler (can be {@code null}). */
  private final XmlHandler external;
  /** Store types in root. */
  private final boolean merge;
  /** Include string type. */
  private final boolean strings;
  /** Cached types of elements with the same key. */
  private final TokenObjectMap<TypeCache> names = new TokenObjectMap<>();
  /** Root element (can be {@code null}). */
  private FBuilder root;

  /**
   * Constructor.
   * @param opts JSON options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  JsonXmlConverter(final JsonParserOptions opts, final XmlHandler handler) {
    super(opts);
    external = handler;
    merge = jopts.merge();
    strings = jopts.get(JsonOptions.STRINGS);
    if(merge && handler != null) throw Util.notExpected("Types can only be merged in nodes.");
  }

  @Override
  protected void init(final String uri) {
    handler = external != null ? external : new NodeHandler(uri, merge);
    names.clear();
    root = null;
  }

  @Override
  protected Value finish() {
    if(!(handler instanceof final NodeHandler ns)) return Empty.VALUE;
    if(merge) {
      final ByteList[] types = new ByteList[ATTRS.length];
      for(final TypeCache cache : names.values()) {
        if(cache.type == null) {
          // different types: add type attributes to all elements
          final int es = cache.elems.size();
          for(int e = 0; e < es; e++) addType(cache.elems.get(e), cache.types.get(e));
        } else if(strings || cache.type != STRING) {
          // identical types: add key to the root attribute of the type
          int t = 0;
          while(TYPES[t] != cache.type) t++;
          if(types[t] == null) types[t] = new ByteList();
          else types[t].add(' ');
          types[t].add(cache.key);
        }
      }
      final int tl = types.length;
      for(int t = 0; t < tl; t++) {
        if(types[t] != null) root.attr(shared.qName(ATTRS[t]), shared.token(types[t].finish()));
      }
    }
    return ns.finish();
  }

  @Override
  protected void numberLit(final byte[] value) throws QueryException, IOException {
    addValue(NUMBER, value);
  }

  @Override
  protected void stringLit(final byte[] value) throws QueryException, IOException {
    addValue(STRING, value);
  }

  @Override
  protected void nullLit() throws QueryException, IOException {
    addValue(NULL, null);
  }

  @Override
  protected void booleanLit(final byte[] value) throws QueryException, IOException {
    addValue(BOOLEAN, value);
  }

  /**
   * Adds a value.
   * @param type JSON type
   * @param value value (can be {@code null})
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  abstract void addValue(byte[] type, byte[] value) throws QueryException, IOException;

  /**
   * Opens an element with the assigned attributes.
   * @param name element name
   * @param key key for merging types (can be {@code null})
   * @param type JSON type (can be {@code null})
   * @param nsp namespace declarations
   * @throws IOException I/O exception
   */
  final void openElem(final byte[] name, final byte[] key, final byte[] type, final Atts nsp)
      throws IOException {
    final boolean cache = merge && type != null && key != null && !contains(key, ' ');
    if(!cache && type != null && (strings || type != STRING)) atts.add(TYPE, type);
    handler.openElem(name, atts, nsp);
    atts.reset();

    if(merge) {
      final FBuilder elem = ((NodeHandler) handler).current();
      if(root == null) root = elem;
      if(cache) {
        TypeCache tc = names.get(key);
        if(tc == null) {
          tc = new TypeCache(key, type);
          names.put(key, tc);
        }
        tc.add(elem, type);
      }
    }
  }

  /**
   * Opens an element with the assigned attributes, adds a value and closes the element.
   * @param name element name
   * @param key key for merging types (can be {@code null})
   * @param type JSON type
   * @param value value (can be {@code null})
   * @throws IOException I/O exception
   */
  final void addValue(final byte[] name, final byte[] key, final byte[] type, final byte[] value)
      throws IOException {
    openElem(name, key, type, NO_NSP);
    if(value != null) handler.text(value);
    handler.closeElem();
  }

  /**
   * Adds a type attribute to the specified element.
   * @param elem element
   * @param type type
   */
  private void addType(final FBuilder elem, final byte[] type) {
    if(strings || type != STRING) elem.attr(shared.qName(TYPE), type);
  }

  /**
   * A container for all elements having the same key.
   * @author Leo Woerteler
   */
  private static final class TypeCache {
    /** Key. */
    private final byte[] key;
    /** Elements. */
    private final ArrayList<FBuilder> elems = new ArrayList<>(1);
    /** Types of the elements. */
    private final TokenList types = new TokenList(1);
    /** Common type (can be {@code null}). */
    private byte[] type;

    /**
     * Constructor.
     * @param key key
     * @param type type
     */
    private TypeCache(final byte[] key, final byte[] type) {
      this.key = key;
      this.type = type;
    }

    /**
     * Adds an element.
     * @param elem element
     * @param tp type
     */
    private void add(final FBuilder elem, final byte[] tp) {
      elems.add(elem);
      types.add(tp);
      if(tp != type) type = null;
    }
  }
}
