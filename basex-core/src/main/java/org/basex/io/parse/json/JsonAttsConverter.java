package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;

import org.basex.build.json.*;
import org.basex.query.value.node.*;

/**
 * This class converts a JSON document to a XML structure. JSON keys will be stored in attributes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class JsonAttsConverter extends JsonXmlConverter {
  /** Current name of a pair. */
  private byte[] nm;

  /**
   * Constructor.
   * @param opts json options
   */
  JsonAttsConverter(final JsonParserOptions opts) {
    super(opts);
  }

  @Override
  void openObject() {
    addType(OBJECT);
  }

  @Override
  void openPair(final byte[] name, final boolean add) {
    if(add) {
      final FElem elem = new FElem(PAIR).add(NAME, name);
      curr.add(elem);
      curr = elem;
      nm = name;
    }
  }

  @Override
  void closePair(final boolean add) {
    if(add) curr = (FElem) curr.parent();
  }

  @Override
  void closeObject() {
  }

  @Override
  void openArray() {
    addType(ARRAY);
    nm = null;
  }

  @Override
  void openItem() {
    final FElem elem = new FElem(ITEM);
    curr.add(elem);
    curr = elem;
  }

  @Override
  void closeItem() {
    curr = (FElem) curr.parent();
  }

  @Override
  void closeArray() {
  }

  @Override
  public void numberLit(final byte[] value) {
    addType(NUMBER).add(value);
  }

  @Override
  public void stringLit(final byte[] value) {
    addType(STRING).add(value);
  }

  @Override
  public void nullLit() {
    addType(NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    addType(BOOLEAN).add(value);
  }

  /**
   * Adds a new element with the given type.
   * @param type JSON type
   * @return the element
   */
  private FElem addType(final byte[] type) {
    final FElem elem = element();
    addType(elem, nm, type);
    return elem;
  }
}
