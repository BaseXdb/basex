package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.util.Token.*;

import org.basex.build.json.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides a parse method to convert JSON data to XML nodes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class JsonXmlConverter extends JsonConverter {
  /** Map from element name to a pair of all its nodes and the collective node type. */
  private final TokenObjMap<TypeCache> names = new TokenObjMap<>();
  /** Store types in root. */
  private final boolean merge;
  /** Include string type. */
  private final boolean strings;

  /** Document root. */
  FDoc doc;
  /** Current element. */
  FElem curr;

  /**
   * Constructor.
   * @param opts json options
   */
  JsonXmlConverter(final JsonParserOptions opts) {
    super(opts);
    merge = jopts.get(JsonOptions.MERGE);
    strings = jopts.get(JsonOptions.STRINGS);
  }

  @Override
  final void init(final String uri) {
    doc = new FDoc(uri);
  }

  /**
   * Returns the current element.
   * @return element
   */
  final FElem element() {
    if(curr == null) curr = new FElem(JSON);
    return curr;
  }

  @Override
  FDoc finish() {
    final FElem elem = element();
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
        if(types[t] != null) elem.add(ATTRS[t], types[t].finish());
      }
    }
    return doc.add(elem);
  }

  /**
   * Adds type information to an element or the type cache.
   * @param elem element
   * @param name JSON name
   * @param type data type
   */
  final void addType(final FElem elem, final byte[] name, final byte[] type) {
    // merge type information
    if(merge) {
      // check if name exists and contains no whitespaces
      if(name != null && !contains(name, ' ')) {
        // check if name is already known
        if(names.contains(name)) {
          final TypeCache arr = names.get(name);
          if(arr != null && arr.type == type) {
            // add element if all types are identical
            arr.add(elem);
          } else {
            // different types for same element
            if(arr != null) {
              // invalidate cached elements, add type attributes
              for(int i = 0; i < arr.size; i++) addType(arr.vals[i], arr.type);
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
        // no name, or name with whitespaces: add type attribute, ignore string type
        addType(elem, type);
      }
    } else {
      // add type attribute
      addType(elem, type);
    }
  }

  /**
   * Adds a type attribute to the specified element. Ignore string types.
   * @param elem element
   * @param type type
   */
  private void addType(final FElem elem, final byte[] type) {
    if(strings || type != STRING) elem.add(TYPE, type);
  }

  /**
   * A simple container for all elements having the same name.
   * @author Leo Woerteler
   */
  private static final class TypeCache {
    /** Shared JSON type. */
    private final byte[] type;
    /** JSON name. */
    private final byte[] name;
    /** Nodes. */
    private FElem[] vals = new FElem[8];
    /** Logical size of {@link #vals}.  */
    private int size;

    /**
     * Constructor.
     * @param nm name
     * @param tp JSON type
     * @param nd element
     */
    private TypeCache(final byte[] nm, final byte[] tp, final FElem nd) {
      name = nm;
      type = tp;
      vals[0] = nd;
      size = 1;
    }

    /**
     * Adds a new element to the list.
     * @param nd element to add
     */
    private void add(final FElem nd) {
      if(size == vals.length) vals = Array.copy(vals, new FElem[Array.newCapacity(size)]);
      vals[size++] = nd;
    }
  }
}
