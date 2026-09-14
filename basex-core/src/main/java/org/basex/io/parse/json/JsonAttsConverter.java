package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.parse.*;

/**
 * This class converts JSON data to XML, using the attributes format.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonAttsConverter extends JsonXmlConverter {
  /** Key of the next element (can be {@code null}). */
  private byte[] key;
  /** Root element has been opened. */
  private boolean rootOpened;

  /**
   * Constructor.
   * @param opts JSON options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  JsonAttsConverter(final JsonParserOptions opts, final XmlHandler handler) {
    super(opts, handler);
  }

  @Override
  protected void init(final String uri) {
    super.init(uri);
    key = null;
    rootOpened = false;
  }

  @Override
  protected void openObject() throws IOException {
    openElem(OBJECT);
  }

  @Override
  protected void closeObject() throws IOException {
    handler.closeElem();
  }

  @Override
  protected void openPair(final byte[] k) {
    key = k;
  }

  @Override
  protected void closePair() { }

  @Override
  protected void openArray() throws IOException {
    openElem(ARRAY);
  }

  @Override
  protected void closeArray() throws IOException {
    handler.closeElem();
  }

  @Override
  protected void openItem() { }

  @Override
  protected void closeItem() { }

  @Override
  void addValue(final byte[] type, final byte[] value) throws IOException {
    openElem(type);
    if(value != null) handler.text(value);
    handler.closeElem();
  }

  /**
   * Opens a pair, item or root element.
   * @param type JSON type
   * @throws IOException I/O exception
   */
  private void openElem(final byte[] type) throws IOException {
    final byte[] k = key, name;
    key = null;
    if(k != null) {
      atts.add(NAME, k);
      name = PAIR;
    } else if(rootOpened) {
      name = ITEM;
    } else {
      rootOpened = true;
      name = JSON;
    }
    openElem(name, k, type, NO_NSP);
  }
}
