package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Functions for converting data to other formats.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class ConvertFn extends StandardFunc {
  /**
   * Converts the first argument from a string to a byte array.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  final byte[] stringToBinary(final QueryContext qc) throws QueryException {
    final byte[] token = toToken(exprs[0], qc);
    final String encoding = toEncodingOrNull(1, CONVERT_ENCODING_X, qc);
    if(encoding == null || encoding == Strings.UTF8) return token;
    try {
      return toBinary(token, encoding);
    } catch(final CharacterCodingException ex) {
      Util.debug(ex);
      throw CONVERT_BINARY_X_X.get(info, normalize(token, info), encoding);
    }
  }

  /**
   * Converts the first argument from a string to a byte array.
   * @param in input string
   * @param enc encoding
   * @return resulting value
   * @throws CharacterCodingException character coding exception
   */
  public static byte[] toBinary(final byte[] in, final String enc) throws CharacterCodingException {
    if(enc == Strings.UTF8) return in;
    final ByteBuffer bb = Charset.forName(enc).newEncoder().encode(CharBuffer.wrap(string(in)));
    final int il = bb.limit();
    final byte[] tmp = bb.array();
    return tmp.length == il  ? tmp : Arrays.copyOf(tmp, il);
  }

  /**
   * Converts the specified input to a string in the specified encoding.
   * @param is input stream
   * @param encoding encoding
   * @param validate validate string
   * @return resulting value
   * @throws IOException I/O exception
   */
  public static byte[] toString(final InputStream is, final String encoding, final boolean validate)
      throws IOException {
    try(TextInput ti = new TextInput(is)) {
      return ti.encoding(encoding).validate(validate).content();
    }
  }
}
