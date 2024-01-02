package org.basex.util.options;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.func.archive.*;
import org.basex.query.func.ft.*;
import org.basex.query.func.xquery.XQueryEval.XQueryOptions;
import org.basex.query.util.collation.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.junit.jupiter.api.*;

/**
 * Tests on options.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class OptionsTest extends SandboxTest {
  /** Initializes various options. */
  @AfterAll public static void init() {
    // instantiate options at least once
    new UCAOptions();
    new CsvOptions();
    new CsvParserOptions();
    new CsvSerialOptions();
    new HtmlOptions();
    new JsonOptions();
    new JsonParserOptions();
    new JsonSerialOptions();
    new CreateOptions();
    new FtIndexOptions();
    new FtContainsOptions();
    new XQueryOptions();
    new BaseXCollationOptions(false);
    new FTDistanceOptions();
    new FTScopeOptions();
    new FTWindowOptions();
  }

  /** Tests the effect of setting system properties. */
  @Test public void systemProperty() {
    final BooleanOption name = MainOptions.STRIPWS;
    final boolean value = true;
    System.setProperty(Prop.DBPREFIX + name.name(), Boolean.toString(value));
    try {
      assertEquals(true, new MainOptions().get(name));
    } finally {
      System.clearProperty(Prop.DBPREFIX + name.name());
    }
  }

  /**
   * Tests the {@link MainOptions#WRITEBACK} option.
   * @throws Exception exception
   */
  @Test public void writeBack() throws Exception {
    final BooleanOption name = MainOptions.WRITEBACK;

    final String input = "<a/>";
    final IOFile file = new IOFile(Prop.TEMPDIR + NAME + '/' + NAME);
    file.write(input);

    // check if original file will be updated
    set(name, true);
    try {
      query("delete node doc('" + file + "')/a");
      assertEquals("", Token.string(file.read()));
    } finally {
      set(name, false);
    }

    // original file will stay untouched
    file.write(input);
    query("delete node doc('" + file + "')/a");
    assertEquals(input, Token.string(file.read()));
  }

}
