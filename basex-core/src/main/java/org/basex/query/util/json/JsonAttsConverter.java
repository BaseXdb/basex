package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.query.value.node.*;

/**
 * This class converts a JSON document to a XML structure. JSON keys will be stored in attributes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public class JsonAttsConverter extends JsonXmlConverter {
  /** Current name. */
  private byte[] name;

  /**
   * Constructor.
   * @param opts json options
   */
  public JsonAttsConverter(final JsonParserOptions opts) {
    super(opts);
  }

  @Override
  public void openObject() {
    addType(T_OBJECT);
  }

  @Override
  public void openPair(final byte[] n) {
    final FElem e = new FElem(T_PAIR).add(T_NAME, n);
    elem.add(e);
    elem = e;
    name = n;
  }

  @Override
  public void closePair() {
    elem = (FElem) elem.parent();
  }

  @Override
  public void closeObject() {
  }

  @Override
  public void openArray() {
    addType(T_ARRAY);
    name = null;
  }

  @Override
  public void openItem() {
    final FElem e = new FElem(T_ITEM);
    elem.add(e);
    elem = e;
  }

  @Override
  public void closeItem() {
    elem = (FElem) elem.parent();
  }

  @Override
  public void closeArray() {
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
    addType(T_NUMBER).add(value);
  }

  @Override
  public void stringLit(final byte[] value) {
    addType(T_STRING).add(value);
  }

  @Override
  public void nullLit() {
    addType(NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    addType(T_BOOLEAN).add(value);
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
