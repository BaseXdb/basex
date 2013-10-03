package org.basex.build.file;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * This class contains parser properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CsvProp extends AProp {
  /** Separators. */
  public static final String[] SEPARATORS = { "comma", "semicolon", "tab", "space" };
  /** Separator mappings. */
  private static final byte[] SEPMAPPINGS = { ',', ';', '\t', ' ' };

  /** Parser option: encoding. */
  public static final Object[] ENCODING = { "encoding", Token.UTF8 };
  /** Parser option: column separator. */
  public static final Object[] SEPARATOR = { "separator", "," };
  /** Parser option: header line. */
  public static final Object[] HEADER = { "header", false };

  /**
   * Constructor.
   */
  public CsvProp() {
    super();
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string
   * @throws IOException I/O exception
   */
  public CsvProp(final String s) throws IOException {
    parse(s);
  }

  /**
   * Returns the separator character.
   * @return separator
   * @throws BaseXException database exception
   */
  public int separator() throws BaseXException {
    // set separator
    final String sep = get(CsvProp.SEPARATOR);
    final String val = sep.toLowerCase(Locale.ENGLISH);
    for(int i = 0; i < SEPARATORS.length; i++) {
      if(val.equals(SEPARATORS[i])) return SEPMAPPINGS[i];
    }
    final byte[] s = token(sep);
    final int sl = s.length;
    if(sl > 0 && cl(s, 0) == sl) return cp(s, 0);
    throw new BaseXException(INVALID_VALUE_X_X, CsvProp.SEPARATOR[0], val);
  }
}
