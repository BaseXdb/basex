package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.query.value.node.*;

/**
 * This class converts a JSON document to a XML structure. JSON keys will be stored in attributes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
final class JsonAttsConverter extends JsonXmlConverter {
  /** Current name of a pair. */
  private byte[] name;

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
  void openPair(final byte[] n) {
    final FElem e = new FElem(PAIR).add(NAME, n);
    elem.add(e);
    elem = e;
    name = n;
  }

  @Override
  void closePair() {
    elem = (FElem) elem.parent();
  }

  @Override
  void closeObject() {
  }

  @Override
  void openArray() {
    addType(ARRAY);
    name = null;
  }

  @Override
  void openItem() {
    final FElem e = new FElem(ITEM);
    elem.add(e);
    elem = e;
  }

  @Override
  void closeItem() {
    elem = (FElem) elem.parent();
  }

  @Override
  void closeArray() {
  }

  @Override
  public void openConstr(final byte[] nm) {
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
    final FElem e = element();
    addType(e, name, type);
    return e;
  }
}
