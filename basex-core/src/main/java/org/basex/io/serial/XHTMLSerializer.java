package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * This class serializes items as XHTML.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class XHTMLSerializer extends MarkupSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  XHTMLSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts, V10, V11);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {

    // escape URI attributes
    final byte[] nm = concat(lc(elem.local()), COLON, lc(name));
    final byte[] val = escuri && HTMLSerializer.URIS.contains(nm) ? escape(value) : value;
    super.attribute(name, val, standalone);
  }

  @Override
  protected void startOpen(final QNm value) throws IOException {
    super.startOpen(value);
    if(content && eq(lc(elem.local()), HEAD)) skip++;
  }

  @Override
  protected void finishOpen() throws IOException {
    super.finishOpen();
    printCT(false, false);
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(printCT(true, false)) return;
    final byte[] lc = lc(elem.local());
    if(html5 && HTMLSerializer.EMPTIES5.contains(lc)) {
      out.print(ELEM_SC);
    } else if(!html5 && HTMLSerializer.EMPTIES.contains(lc) && eq(elem.uri(), XHTML_URI)) {
      out.print(' ');
      out.print(ELEM_SC);
    } else {
      out.print(ELEM_C);
      sep = false;
      finishClose();
    }
  }

  @Override
  protected void doctype(final byte[] type) throws IOException {
    if(html5 && docsys == null) {
      printDoctype(type, null, null);
    } else if(docsys != null) {
      printDoctype(type, docpub, docsys);
    }
  }

  @Override
  boolean inline() {
    return contains(HTMLSerializer.INLINES, closed) ||
        opening && contains(HTMLSerializer.INLINES, elem) ||
        super.inline();
  }

  @Override
  boolean suppressIndentation(final QNm qname) throws QueryIOException {
    return contains(HTMLSerializer.FORMATTEDS, qname) || super.suppressIndentation(qname);
  }

  /**
   * Checks whether the token set contains the specified element.
   * @param elements the token set of element local names
   * @param element the element QName
   * @return {@code true} if the element is contained in the token set
   */
  private boolean contains(final TokenSet elements, final QNm element) {
    final byte[] uri = element.uri(), local = element.local();
    return eq(uri, XHTML_URI) && elements.contains(local) ||
        html5 && eq(uri, EMPTY) && elements.contains(lc(local));
  }
}
