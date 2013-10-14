package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides a parse method to convert JSON data to XML nodes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class JsonXmlConverter extends JsonConverter {
  /** Type names. */
  protected static final byte[][] NAMES = { T_ARRAY, T_OBJECT, T_STRING, T_NUMBER,
    T_BOOLEAN, NULL };
  /** The {@code types} QNames. */
  protected static final byte[][] TYPES = new byte[NAMES.length][];

  static {
    final byte[] s = { 's' };
    for(int i = 0; i < NAMES.length; i++) TYPES[i] = concat(NAMES[i], s);
  }

  /** Map from element name to a pair of all its nodes and the collective node type. */
  private final TokenObjMap<TypeCache> names = new TokenObjMap<TypeCache>();
  /** Store types in root. */
  private final boolean merge;
  /** Include string type. */
  private final boolean strings;
  /** Current element. */
  protected FElem elem;

  /**
   * Constructor.
   * @param opts json options
   */
  protected JsonXmlConverter(final JsonParserOptions opts) {
    super(opts);
    merge = jopts.get(JsonParserOptions.MERGE);
    strings = jopts.get(JsonParserOptions.STRINGS);
  }

  /**
   * Returns the current element.
   * @return element
   */
  protected FElem element() {
    if(elem == null) elem = new FElem(T_JSON);
    return elem;
  }

  @Override
  protected FDoc finish() {
    final FElem e = element();
    if(merge) {
      final ByteList[] types = new ByteList[TYPES.length];
      for(final TypeCache arr : names.values()) {
        if(arr != null) {
          for(int i = 0; i < NAMES.length; i++) {
            if(arr.type == NAMES[i] && (strings || arr.type != T_STRING)) {
              if(types[i] == null) types[i] = new ByteList();
              else types[i].add(' ');
              types[i].add(arr.name);
              break;
            }
          }
        }
      }
      for(int i = 0; i < types.length; i++) {
        if(types[i] != null) e.add(TYPES[i], types[i].toArray());
      }
    }
    return new FDoc().add(e);
  }

  /**
   * Adds type information to an element or the type cache.
   * @param e element
   * @param name JSON name
   * @param type data type
   */
  protected void addType(final FElem e, final byte[] name, final byte[] type) {
    // merge type information
    if(merge) {
      // check if name exists and contains no whitespaces
      if(name != null && !contains(name, ' ')) {
        // check if name is already known
        if(names.contains(name)) {
          final TypeCache arr = names.get(name);
          if(arr != null && arr.type == type) {
            // add element if all types are identical
            arr.add(e);
          } else {
            // different types for same element
            if(arr != null) {
              // invalidate cached elements, add type attributes
              for(int i = 0; i < arr.size; i++) addType(arr.vals[i], arr.type);
              names.put(name, null);
            }
            // add type attribute, ignore string type
            addType(e, type);
          }
        } else {
          // new name: create new type cache
          names.put(name, new TypeCache(name, type, e));
        }
      } else {
        // no name, or name with whitespaces: add type attribute, ignore string type
        addType(e, type);
      }
    } else {
      // add type attribute
      addType(e, type);
    }
  }

  /**
   * Adds a type attribute to the specified element. Ignore string types.
   * @param e element
   * @param type type
   */
  private void addType(final FElem e, final byte[] type) {
    if(strings || type != T_STRING) e.add(T_TYPE, type);
  }

  /**
   * A simple container for all elements having the same name.
   * @author Leo Woerteler
   */
  protected static final class TypeCache {
    /** Shared JSON type. */
    public final byte[] type;
    /** JSON name. */
    public final byte[] name;
    /** Nodes. */
    public FElem[] vals = new FElem[8];
    /** Logical size of {@link #vals}.  */
    public int size;

    /**
     * Constructor.
     * @param nm name
     * @param tp JSON type
     * @param nd element
     */
    public TypeCache(final byte[] nm, final byte[] tp, final FElem nd) {
      name = nm;
      type = tp;
      vals[0] = nd;
      size = 1;
    }

    /**
     * Adds a new element to the list.
     * @param nd element to add
     */
    public void add(final FElem nd) {
      if(size == vals.length) vals = Array.copy(vals, new FElem[Array.newSize(size)]);
      vals[size++] = nd;
    }
  }
}
