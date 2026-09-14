package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;

import java.io.*;

import org.basex.build.json.*;
import org.basex.io.parse.*;
import org.basex.util.*;

/**
 * This class converts JSON data to XML, using the direct format.
 *
 * <p>The converted XML document is described in the {@link org.basex.query.func.json}
 * package documentation.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JsonDirectConverter extends JsonXmlConverter {
  /** Lax QName conversion. */
  private final boolean lax;
  /** Name of the next element. */
  private byte[] name;

  /**
   * Constructor.
   * @param opts JSON options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  JsonDirectConverter(final JsonParserOptions opts, final XmlHandler handler) {
    super(opts, handler);
    lax = jopts.get(JsonOptions.LAX);
  }

  @Override
  protected void init(final String uri) {
    super.init(uri);
    name = JSON;
  }

  @Override
  protected void openObject() throws IOException {
    openElem(name, name, OBJECT, NO_NSP);
  }

  @Override
  protected void closeObject() throws IOException {
    handler.closeElem();
  }

  @Override
  protected void openPair(final byte[] key) {
    name = XMLToken.encode(key, lax);
  }

  @Override
  protected void closePair() { }

  @Override
  protected void openArray() throws IOException {
    openElem(name, name, ARRAY, NO_NSP);
  }

  @Override
  protected void closeArray() throws IOException {
    handler.closeElem();
  }

  @Override
  protected void openItem() {
    name = VALUE;
  }

  @Override
  protected void closeItem() { }

  @Override
  void addValue(final byte[] type, final byte[] value) throws IOException {
    addValue(name, name, type, value);
  }
}
