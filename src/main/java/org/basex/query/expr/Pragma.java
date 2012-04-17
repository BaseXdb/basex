package org.basex.query.expr;

import java.io.IOException;
import static org.basex.query.QueryText.*;
import org.basex.data.*;
import org.basex.io.serial.Serializer;
import org.basex.query.item.QNm;
import org.basex.util.TokenBuilder;

/**
 * Pragma.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Pragma extends ExprInfo {
  /** QName. */
  private final QNm qName;
  /** PragmaContents. */
  private final byte[] pContent;

  /**
   * Constructor.
   * @param qn QName
   * @param content pragma contents
   */
  public Pragma(final QNm qn, final byte[] content) {
    qName = qn;
    pContent = content;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this, VAL, pContent);
    qName.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(PRAGMA + ' ' + qName + ' ');
    if(pContent.length != 0) tb.add(pContent).add(' ');
    return tb.add(PRAGMA2).toString();
  }
}
