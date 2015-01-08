package org.basex.util.options;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.build.csv.*;
import org.basex.build.html.*;
import org.basex.build.json.*;
import org.basex.build.text.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.query.func.archive.*;
import org.basex.query.func.ft.*;
import org.basex.query.func.xquery.XQueryEval.XQueryOptions;
import org.basex.query.util.collation.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.junit.*;
import org.junit.Test;

/**
 * Tests on options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class OptionsTest extends SandboxTest {
  /** Initializes various options. */
  @AfterClass
  public static void init() {
    // instantiate options at least once
    new UCAOptions();
    new CsvOptions();
    new CsvParserOptions();
    new HtmlOptions();
    new JsonOptions();
    new JsonParserOptions();
    new JsonSerialOptions();
    new TextOptions();
    new ArchOptions();
    new FtIndexOptions();
    new FtOptions();
    new XQueryOptions();
    new BaseXCollationOptions();
    new FTDistanceOptions();
    new FTScopeOptions();
    new FTWindowOptions();
  }

  /** Tests the effect of setting system properties. */
  @Test
  public void systemProperty() {
    final BooleanOption name = MainOptions.CHOP;
    final Boolean value = Boolean.FALSE;
    System.setProperty(Prop.DBPREFIX + name.name(), value.toString());
    try {
      assertEquals(value, new MainOptions().get(name));
    } finally {
      System.clearProperty(Prop.DBPREFIX + name.name());
    }
  }

  /**
   * Tests the {@link MainOptions#WRITEBACK} option.
   * @throws Exception exception
   */
  @Test
  public void writeBack() throws Exception {
    final BooleanOption name = MainOptions.WRITEBACK;

    final String input = "<a/>";
    final IOFile file = new IOFile(Prop.TMP + NAME + '/' + NAME);
    file.write(Token.token(input));

    // check if original file will be updated
    context.options.set(name, true);
    try {
      new XQuery("delete node doc('" + file + "')/a").execute(context);
      assertEquals("", Token.string(file.read()));
    } finally {
      context.options.set(name, false);
    }

    // original file will stay untouched
    file.write(Token.token(input));
    new XQuery("delete node doc('" + file + "')/a").execute(context);
    assertEquals(input, Token.string(file.read()));
  }

}
