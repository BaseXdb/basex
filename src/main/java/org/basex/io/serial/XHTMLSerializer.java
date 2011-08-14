package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class serializes data as XHTML.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class XHTMLSerializer extends OutputSerializer {
  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  XHTMLSerializer(final OutputStream os, final SerializerProp p)
      throws IOException {
    super(os, p, V10, V11);
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException {
    print(' ');
    print(n);

    // escape URI attributes
    final byte[] val = escape && URIS.id(concat(lc(tag), COLON, lc(n))) != 0 ?
        escape(v) : v;

    print(ATT1);
    for(int k = 0; k < val.length; k += cl(val, k)) {
      final int ch = cp(val, k);
      switch(ch) {
        case '"': print(E_QU);  break;
        case 0x9:
        case 0xA: hex(ch); break;
        default:  ch(ch);
      }
    }
    print(ATT2);
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    super.startOpen(t);
    if(content && eq(lc(t), HEAD)) {
      emptyElement(META, HTTPEQUIV, CONTTYPE, CONTENT,
          concat(token(media), CHARSET, token(enc)));
    }
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(EMPTIES.contains(lc(tag))) {
      print(' ');
      print(ELEM_SC);
    } else {
      print(ELEM_C);
      ind = false;
      finishClose();
    }
  }
}
