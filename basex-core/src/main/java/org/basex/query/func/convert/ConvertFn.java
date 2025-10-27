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
 * @author BaseX Team, BSD License
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
    final byte[] value = toToken(arg(0), qc);
    final String encoding = toEncodingOrNull(arg(1), CONVERT_ENCODING_X, qc);
    try {
      return toBinary(value, encoding);
    } catch(final CharacterCodingException ex) {
      Util.debug(ex);
      throw CONVERT_BINARY_X_X.get(info, value, encoding);
    }
  }

  /**
   * Converts the first argument from a string to a byte array.
   * @param in input string
   * @param encoding encoding (can be {@code null})
   * @return resulting value
   * @throws CharacterCodingException character coding exception
   */
  public static byte[] toBinary(final byte[] in, final String encoding)
      throws CharacterCodingException {
    if(encoding == null || encoding == Strings.UTF8) return in;
    final Charset cs = Charset.forName(encoding == Strings.UTF16 ? Strings.UTF16BE : encoding);
    final ByteBuffer bb = cs.newEncoder().encode(CharBuffer.wrap(string(in)));
    return Arrays.copyOfRange(bb.array(), bb.position(), bb.limit());
  }

  /**
   * Converts the specified input to a string in the specified encoding.
   * @param is input stream
   * @param encoding encoding (can be {@code null})
   * @param validate validate string
   * @return resulting value
   * @throws IOException I/O exception
   */
  public static byte[] toString(final InputStream is, final String encoding, final boolean validate)
      throws IOException {
    try(TextInput ti = new TextInput(is, encoding)) {
      return ti.validate(validate).content();
    }
  }
}
