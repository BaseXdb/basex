package org.basex.build.csv;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.Types.*;

import java.util.*;
import java.util.stream.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Options of the W3 CSV functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class W3CsvOptions extends CsvParserOptions {
  /** Options that can be assigned by name. */
  private static final Map<String, Option<?>> DEFINITIONS = Stream.<Option<?>>of(SEPARATOR,
      QUOTE_CHARACTER, COMMENT_MARKER, TRIM_WHITESPACE, HEADER, SELECT_COLUMNS, TRIM_ROWS).
      collect(Collectors.toUnmodifiableMap(Option::name, option -> option));

  /**
   * Returns options with the W3 format.
   * @return options
   */
  public static W3CsvOptions w3() {
    final W3CsvOptions options = new W3CsvOptions();
    options.set(FORMAT, CsvFormat.W3);
    return options;
  }

  /**
   * Returns options with the W3 XML format.
   * @return options
   */
  public static W3CsvOptions w3Xml() {
    final W3CsvOptions options = new W3CsvOptions();
    options.set(FORMAT, CsvFormat.W3_XML);
    return options;
  }

  @Override
  protected Map<String, Option<?>> definitions() {
    return DEFINITIONS;
  }

  @Override
  protected void adjust(final InputInfo info) throws QueryException {
    // literal single characters
    for(final StringOption option : List.of(SEPARATOR, QUOTE_CHARACTER, COMMENT_MARKER)) {
      final String string = get(option);
      if(string != null && string.codePointCount(0, string.length()) != 1) {
        throw CSV_SINGLECHAR_X_X.get(info, option.name(), string);
      }
    }
    // header: boolean or column names
    final Value header = get(HEADER);
    if(!BOOLEAN_O.instance(header) && !header.seqType().type.isStringOrUntyped()) {
      throw typeError(header, BasicType.STRING, info);
    }
  }
}
