package org.basex.io.serial;

import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;

import java.io.*;

import org.basex.query.value.item.*;

/**
 * This class serializes items as XHTML.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class XHTMLSerializer extends XhtmlHtmlSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  XHTMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, false, V11, V10);
  }

  @Override
  byte[] htmlName(final QNm name) {
    final byte[] uri = name.uri();
    return eq(uri, XHTML_URI) || html5 && uri.length == 0 ? localName(name) : null;
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {

    // escape URI attributes
    final byte[] key = escape ? attributeKey(name) : null;
    final byte[] v = key != null && URIS.contains(key) ?
        encodeUri(value, UriEncoder.ESCAPE) : value;
    super.attribute(name, v, standalone);
  }

  @Override
  protected void startOpen(final QNm name) throws IOException {
    super.startOpen(name);
    checkHead();
  }

  @Override
  protected void finishOpen() throws IOException {
    super.finishOpen();
    printCT(false);
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(printCT(true)) return;
    if(isElement(rules.empties(), elem)) {
      // prior to HTML5, a space is inserted before the trailing slash
      if(!html5) out.print(' ');
      out.print(ELEM_SC);
    } else {
      out.print(ELEM_C);
      sep = false;
      finishClose();
    }
  }

  @Override
  protected void doctype(final QNm name) throws IOException {
    if(docsys != null) {
      printDoctype(name.local(), docpub, docsys);
    } else if(html5 && eq(htmlName(name), HTML)) {
      printDoctype(name.local(), null, null);
    }
  }
}
