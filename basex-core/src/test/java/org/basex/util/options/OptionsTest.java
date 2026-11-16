package org.basex.util.options;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.func.archive.*;
import org.basex.query.func.ft.*;
import org.basex.query.func.xquery.XQueryEval.XQueryOptions;
import org.basex.query.util.collation.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.http.MediaType;
import org.junit.jupiter.api.*;

/**
 * Tests on options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class OptionsTest extends SandboxTest {
  /** Initializes various options. */
  @AfterAll public static void init() {
    // instantiate options at least once
    new UCAOptions(false);
    new CsvOptions();
    new CsvParserOptions();
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
      final MainOptions mopts = new MainOptions();
      mopts.setSystem();
      assertEquals(true, mopts.get(name));
    } finally {
      System.clearProperty(Prop.DBPREFIX + name.name());
    }
  }

  /** Tests that invalid values of system properties are reported. */
  @Test public void systemPropertyInvalid() {
    final EnumOption<MainOptions.MainParser> name = MainOptions.PARSER;
    final ArrayOutput ao = new ArrayOutput();
    System.setProperty(Prop.DBPREFIX + name.name(), "JSON");
    try {
      System.setErr(new PrintStream(ao));
      final MainOptions mopts = new MainOptions();
      mopts.setSystem();
      assertEquals(MainOptions.MainParser.XML, mopts.get(name));
      assertTrue(ao.toString().contains("JSON"));

      System.setProperty(Prop.DBPREFIX + name.name(), "json");
      mopts.setSystem();
      assertEquals(MainOptions.MainParser.JSON, mopts.get(name));
    } finally {
      System.setErr(ERR);
      System.clearProperty(Prop.DBPREFIX + name.name());
    }
  }

  /** Tests that invalid media-type parameters are ignored. */
  @Test public void mediaTypeInvalid() {
    final JsonParserOptions jopts = new JsonParserOptions();
    jopts.assign(new MediaType("application/json; format=bogus; liberal=maybe; lax=yes"));
    assertEquals(JsonOptions.JsonFormat.DIRECT, jopts.get(JsonOptions.FORMAT));
    assertFalse(jopts.get(JsonParserOptions.LIBERAL));
    assertTrue(jopts.get(JsonOptions.LAX));
  }

  /** Tests that option definitions cannot be modified. */
  @Test public void immutableDefinitions() {
    final Iterator<Option<?>> iter = new MainOptions().iterator();
    iter.next();
    assertThrows(UnsupportedOperationException.class, iter::remove);
    assertNotNull(new MainOptions().option(MainOptions.STRIPWS.name()));
  }

  /** Tests that option values are instance-specific. */
  @Test public void instanceValues() {
    final MainOptions opts1 = new MainOptions(), opts2 = new MainOptions();
    opts1.set(MainOptions.STRIPWS, true);
    assertTrue(opts1.get(MainOptions.STRIPWS));
    assertFalse(opts2.get(MainOptions.STRIPWS));
  }

  /**
   * Tests the {@link MainOptions#WRITEBACK} option.
   * @throws Exception exception
   */
  @Test public void writeBack() throws Exception {
    final BooleanOption name = MainOptions.WRITEBACK;

    final String input = "<a/>";
    final IOFile file = new IOFile(sandbox(), NAME);
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
