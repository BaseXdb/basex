package org.basex.io.serial;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.util.hash.TokenSet;
import org.basex.util.list.TokenList;

/**
 * This class serializes data as HTML.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public class HTMLSerializer extends OutputSerializer {
  /** HTML: script elements. */
  private static final TokenList SCRIPTS = new TokenList();
  /** HTML: boolean attributes. */
  private static final TokenSet BOOLEAN = new TokenSet();

  /**
   * Constructor, specifying serialization options.
   * @param os output stream reference
   * @param p serialization properties
   * @throws IOException I/O exception
   */
  HTMLSerializer(final OutputStream os, final SerializerProp p)
      throws IOException {
    super(os, p, V40, V401);
  }

  @Override
  public void attribute(final byte[] n, final byte[] v) throws IOException {
    // don't append value for boolean attributes
    final byte[] tagatt = concat(lc(tag), COLON, lc(n));
    if(BOOLEAN.id(tagatt) != 0 && eq(n, v)) return;
    // escape URI attributes
    final byte[] val = escape && URIS.id(tagatt) != 0 ? escape(v) : v;

    print(' ');
    print(n);
    print(ATT1);
    for(int k = 0; k < val.length; k += cl(val, k)) {
      final int ch = cp(val, k);
      if(ch == '<' || ch == '&' &&
          val[Math.min(k + 1, val.length - 1)] == '{') {
        print(ch);
      } else if(ch == '"') {
        print(E_QU);
      } else if(ch == 0x9 || ch == 0xA) {
        hex(ch);
      } else {
        code(ch);
      }
    }
    print(ATT2);
  }

  @Override
  public void finishComment(final byte[] n) throws IOException {
    if(ind) indent();
    print(COMM_O);
    print(n);
    print(COMM_C);
  }

  @Override
  public void finishPi(final byte[] n, final byte[] v) throws IOException {
    if(ind) indent();
    if(contains(v, '>')) SERPI.thrwSerial();
    print(PI_O);
    print(n);
    print(' ');
    print(v);
    print(ELEM_C);
  }

  @Override
  protected void code(final int ch) throws IOException {
    if(script) printChar(ch);
    else if(ch > 0x7F && ch < 0xA0) SERILL.thrwSerial(Integer.toHexString(ch));
    else if(ch == 0xA0) print(E_NBSP);
    else super.code(ch);
  }

  @Override
  protected void startOpen(final byte[] t) throws IOException {
    doctype(null);
    if(ind) indent();
    print(ELEM_O);
    print(t);
    ind = indent;
    script = SCRIPTS.contains(lc(t));
    if(content && eq(lc(tag), HEAD)) ct++;
  }

  @Override
  protected void finishOpen() throws IOException {
    super.finishOpen();
    if(ct(false, true)) return;
  }

  @Override
  protected void finishEmpty() throws IOException {
    if(ct(true, true)) return;
    print(ELEM_C);
    if(EMPTIES.contains(lc(tag))) return;
    ind = false;
    finishClose();
  }

  @Override
  protected void finishClose() throws IOException {
    super.finishClose();
    script &= !SCRIPTS.contains(lc(tag));
  }

  // HTML Serializer: cache elements
  static {
    // script elements
    SCRIPTS.add(token("script"));
    SCRIPTS.add(token("style"));
    // boolean attributes
    BOOLEAN.add(token("area:nohref"));
    BOOLEAN.add(token("button:disabled"));
    BOOLEAN.add(token("dir:compact"));
    BOOLEAN.add(token("dl:compact"));
    BOOLEAN.add(token("frame:noresize"));
    BOOLEAN.add(token("hr:noshade"));
    BOOLEAN.add(token("img:ismap"));
    BOOLEAN.add(token("input:checked"));
    BOOLEAN.add(token("input:disabled"));
    BOOLEAN.add(token("input:readonly"));
    BOOLEAN.add(token("menu:compact"));
    BOOLEAN.add(token("object:declare"));
    BOOLEAN.add(token("ol:compact"));
    BOOLEAN.add(token("optgroup:disabled"));
    BOOLEAN.add(token("option:selected"));
    BOOLEAN.add(token("option:disabled"));
    BOOLEAN.add(token("script:defer"));
    BOOLEAN.add(token("select:multiple"));
    BOOLEAN.add(token("select:disabled"));
    BOOLEAN.add(token("td:nowrap"));
    BOOLEAN.add(token("textarea:disabled"));
    BOOLEAN.add(token("textarea:readonly"));
    BOOLEAN.add(token("th:nowrap"));
    BOOLEAN.add(token("ul:compact"));
  }
}
