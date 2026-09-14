package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class converts JSON data to XML, using the format defined by fn:json-to-xml.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonW3XmlConverter extends JsonXmlConverter {
  /** Namespace declaration of the root element. */
  private static final Atts FN_NSP = new Atts().add(EMPTY, QueryText.FN_URI);

  /** Add escaped attributes. */
  private final boolean escape;
  /** Key of the next element (can be {@code null}). */
  private byte[] key;
  /** Root element has been opened. */
  private boolean rootOpened;

  /**
   * Constructor.
   * @param opts JSON options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  JsonW3XmlConverter(final JsonParserOptions opts, final XmlHandler handler) {
    super(opts, handler);
    escape = jopts.get(JsonParserOptions.ESCAPE);
  }

  @Override
  protected void init(final String uri) {
    super.init(uri);
    key = null;
    rootOpened = false;
  }

  @Override
  protected void openObject() throws IOException {
    openElem(MAP);
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
    addKey();
    if(escape && value != null && contains(value, '\\')) atts.add(ESCAPED, TRUE);
    openElem(type, null, null, nsp());
    if(value != null) handler.text(value);
    handler.closeElem();
  }

  /**
   * Opens a map or array element.
   * @param name element name
   * @throws IOException I/O exception
   */
  private void openElem(final byte[] name) throws IOException {
    addKey();
    openElem(name, null, null, nsp());
  }

  /**
   * Adds the key attributes to the next element.
   */
  private void addKey() {
    if(key != null) {
      atts.add(KEY, key);
      if(escape && contains(key, '\\')) atts.add(ESCAPED_KEY, TRUE);
      key = null;
    }
  }

  /**
   * Returns the namespace declarations of the next element.
   * @return namespace declarations
   */
  private Atts nsp() {
    if(rootOpened) return NO_NSP;
    rootOpened = true;
    return FN_NSP;
  }
}
