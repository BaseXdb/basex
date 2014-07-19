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
  void openPair(final byte[] name) {
    final FElem e = new FElem(PAIR).add(NAME, name);
    curr.add(e);
    curr = e;
    nm = name;
  }

  @Override
  void closePair() {
    curr = (FElem) curr.parent();
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
    final FElem e = new FElem(ITEM);
    curr.add(e);
    curr = e;
  }

  @Override
  void closeItem() {
    curr = (FElem) curr.parent();
  }

  @Override
  void closeArray() {
  }

  @Override
  public void openConstr(final byte[] name) {
    openObject();
    openPair(name);
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
    addType(e, nm, type);
    return e;
  }
}
