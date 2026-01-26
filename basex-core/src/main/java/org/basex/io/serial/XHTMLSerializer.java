package org.basex.io.serial;

import static org.basex.util.Token.*;
import static org.basex.util.XMLToken.*;

import java.io.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * This class serializes items as XHTML.
 *
 * @author BaseX Team, BSD License
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
    super(os, sopts, V11, V10);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone)
      throws IOException {

    final byte[] v;
    if(escape && HTMLSerializer.URIS.contains(concat(lc(elem.local()), AT, lc(name)))) {
      // escape URI attributes
      v = encodeUri(value, UriEncoder.ESCAPE);
    } else {
      v = value;
    }
    super.attribute(name, v, standalone);
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
  protected void doctype(final QNm name) throws IOException {
    if(html5 && docsys == null) {
      printDoctype(name.local(), null, null);
    } else if(docsys != null) {
      printDoctype(name.local(), docpub, docsys);
    }
  }

  @Override
  boolean inline() {
    final TokenSet inlines = html5 ? HTMLSerializer.INLINES5 : HTMLSerializer.INLINES;
    return contains(inlines, closed) || opening && contains(inlines, elem) || super.inline();
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
    return eq(element.uri(), html5 ? EMPTY : XHTML_URI) &&
        elements.contains(html5 ? lc(element.local()) : element.local());
  }
}
