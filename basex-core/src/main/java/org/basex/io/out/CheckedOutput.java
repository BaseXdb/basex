package org.basex.io.out;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.util.hash.*;

/**
 * This class checks if printed characters can be represented in a specific encoding.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CheckedOutput extends PrintOutput {
  /** Output stream reference. */
  private final PrintOutput po;
  /** Charset encoder. */
  private final CharsetEncoder encoder;
  /** Results of encoding checks. */
  private final IntObjectMap<Boolean> encodable = new IntObjectMap<>();
  /** Indicates if the encoding can represent all ASCII characters. */
  private final boolean ascii;

  /**
   * Constructor, given an output stream.
   * @param po output stream reference
   * @param charset character set
   */
  public CheckedOutput(final PrintOutput po, final Charset charset) {
    super(po);
    this.po = po;
    encoder = charset.newEncoder();
    ascii = charset.contains(StandardCharsets.US_ASCII);
  }

  /**
   * Checks if a character can be represented in the encoding.
   * @param cp codepoint
   * @return result of check
   */
  public boolean encodable(final int cp) {
    if(cp < 0x80 && ascii) return true;
    Boolean enc = encodable.get(cp);
    if(enc == null) {
      enc = encoder.canEncode(Character.toString(cp));
      encodable.put(cp, enc);
    }
    return enc;
  }

  @Override
  public void print(final int cp) throws IOException {
    if(!encodable(cp)) throw SERENC_X_X.getIO(Integer.toHexString(cp), encoder.charset());
    po.print(cp);
  }

  @Override
  public long lineLength() {
    return po.lineLength();
  }

  @Override
  public boolean finished() {
    return po.finished();
  }
}
