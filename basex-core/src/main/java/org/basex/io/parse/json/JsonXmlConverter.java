package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonParserOptions.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides a parse method to convert JSON data to XML nodes.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class JsonXmlConverter extends JsonConverter {
  /** QName. */
  static final QNm Q_JSON = new QNm(JSON);
  /** QName. */
  static final QNm Q_PAIR = new QNm(PAIR);
  /** QName. */
  static final QNm Q_ITEM = new QNm(ITEM);
  /** QName. */
  static final QNm Q_NAME = new QNm(NAME);
  /** QName. */
  static final QNm Q_KEY = new QNm(KEY);
  /** QName. */
  static final QNm Q_ESCAPED_KEY = new QNm(ESCAPED_KEY);
  /** QName. */
  static final QNm Q_ESCAPED = new QNm(ESCAPED);

  /** Stack for intermediate nodes. */
  final Stack<FBuilder> stack = new Stack<>();
  /** Add value pairs. */
  final BoolList addValues = new BoolList();

  /** Map from element name to a pair of all its nodes and the collective node type. */
  private final TokenObjMap<TypeCache> names = new TokenObjMap<>();
  /** Store types in root. */
  private final boolean merge;
  /** Include string type. */
  private final boolean strings;

  /** Document root. */
  FBuilder doc;
  /** Current element. */
  FBuilder curr;
  /** Name of current element/attribute. */
  byte[] name;

  /**
   * Constructor.
   * @param opts json options
   * @throws QueryException query exception
   */
  JsonXmlConverter(final JsonParserOptions opts) throws QueryException {
    super(opts);
    merge = jopts.get(JsonOptions.MERGE);
    strings = jopts.get(JsonOptions.STRINGS);
    addValues.add(true);

    final JsonDuplicates dupl = jopts.get(JsonParserOptions.DUPLICATES);
    if(dupl == JsonDuplicates.USE_LAST) {
      throw JSON_OPTIONS_X.get(null, JsonParserOptions.DUPLICATES.name(), dupl);
    }
  }

  @Override
  final void init(final String uri) {
    doc = FDoc.build(token(uri));
  }

  @Override
  FNode finish() {
    if(merge) {
      final ByteList[] types = new ByteList[ATTRS.length];
      for(final TypeCache arr : names.values()) {
        if(arr != null) {
          final int tl = TYPES.length;
          for(int i = 0; i < tl; i++) {
            if(arr.type == TYPES[i] && (strings || arr.type != STRING)) {
              if(types[i] == null) types[i] = new ByteList();
              else types[i].add(' ');
              types[i].add(arr.name);
              break;
            }
          }
        }
      }
      final int tl = types.length;
      for(int t = 0; t < tl; t++) {
        if(types[t] != null) curr.add(shared.qName(ATTRS[t]), shared.token(types[t].finish()));
      }
    }
    return doc.add(curr).finish();
  }

  @Override
  void numberLit(final byte[] value) throws QueryException {
    addValue(NUMBER, value);
  }

  @Override
  void stringLit(final byte[] value) throws QueryException {
    addValue(STRING, value);
  }

  @Override
  void nullLit() throws QueryException {
    addValue(NULL, null);
  }

  @Override
  void booleanLit(final byte[] value) throws QueryException {
    addValue(BOOLEAN, value);
  }

  /**
   * Adds a value.
   * @param type JSON type
   * @param value optional value
   * @throws QueryException query exception
   */
  abstract void addValue(byte[] type, byte[] value) throws QueryException;

  /**
   * Adds type information to an element or the type cache.
   * @param elem element
   * @param type data type
   */
  final void processType(final FBuilder elem, final byte[] type) {
    // merge type information
    // check if name exists and contains no whitespace
    if(merge && name != null && !contains(name, ' ')) {
      // check if name is already known
      if(names.contains(name)) {
        final TypeCache cache = names.get(name);
        if(cache != null && cache.type == type) {
          // add element if all types are identical
          cache.add(elem);
        } else {
          // different types for same element
          if(cache != null) {
            // invalidate cached elements, add type attributes
            for(final FBuilder val : cache.vals) addType(val, cache.type);
            names.put(name, null);
          }
          // add type attribute, ignore string type
          addType(elem, type);
        }
      } else {
        // new name: create new type cache
        names.put(name, new TypeCache(name, type, elem));
      }
    } else {
      // no name, or name with whitespace: add type attribute, ignore string type
      addType(elem, type);
    }
  }

  /**
   * Adds a type attribute to the specified element. Ignore string types.
   * @param elem element
   * @param type type
   */
  private void addType(final FBuilder elem, final byte[] type) {
    if(strings || type != STRING) elem.add(shared.qName(TYPE), type);
  }

  /**
   * A simple container for all elements having the same name.
   * @author Leo Woerteler
   */
  private static final class TypeCache {
    /** Nodes. */
    private final ArrayList<FBuilder> vals = new ArrayList<>(1);
    /** Shared JSON type. */
    private final byte[] type;
    /** JSON name. */
    private final byte[] name;

    /**
     * Constructor.
     * @param name name
     * @param type JSON type
     * @param elem element
     */
    private TypeCache(final byte[] name, final byte[] type, final FBuilder elem) {
      this.name = name;
      this.type = type;
      add(elem);
    }

    /**
     * Adds a new element to the list.
     * @param elem element to add
     */
    private void add(final FBuilder elem) {
      vals.add(elem);
    }
  }
}
