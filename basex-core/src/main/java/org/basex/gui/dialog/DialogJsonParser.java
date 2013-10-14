package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXLayout.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.JsonFormat;
import org.basex.build.JsonOptions.JsonSpec;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * JSON parser panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class DialogJsonParser extends DialogParser {
  /** Example string for JSON conversion. */
  private static final String EXAMPLE = "{ \"Person\": \"John\\nAdam\",\n" +
      "  \"Born\"  : 1984,\n  \"X?_\"  : [ true, null ] }";
  /** Example string for JSONML conversion. */
  private static final String EXAMPLEML = "[ \"Person\",\n" +
      "  { \"born\": \"1984\" },\n  \"John\\nAdam\"\n]";

  /** Options. */
  private JsonParserOptions jopts;
  /** JSON example. */
  private final Editor example;
  /** JSON: encoding. */
  private final BaseXCombo encoding;
  /** JSON: format. */
  private final BaseXCombo format;
  /** JSON: unescape. */
  private final BaseXCheckBox unescape;
  /** JSON: lax name conversion. */
  private final BaseXCheckBox lax;
  /** JSON: merge type information. */
  private final BaseXCheckBox merge;
  /** JSON: include string types. */
  private final BaseXCheckBox strings;
  /** JSON: specification. */
  private final BaseXCombo spec;

  /**
   * Constructor.
   * @param d dialog reference
   * @param opts main options
   */
  DialogJsonParser(final BaseXDialog d, final MainOptions opts) {
    super(d);
    try {
      jopts = new JsonParserOptions(opts.get(MainOptions.JSONPARSER));
    } catch(final IOException ex) { jopts = new JsonParserOptions(); }

    encoding = DialogExport.encoding(d, jopts.get(JsonParserOptions.ENCODING));

    final StringList sl = new StringList();
    final JsonFormat[] formats = JsonFormat.values();
    final int fl = formats.length - 1;
    for(int f = 0; f < fl; f++) sl.add(formats[f].toString());
    format = new BaseXCombo(d, sl.toArray());
    format.setSelectedItem(jopts.get(JsonOptions.FORMAT));

    sl.reset();
    for(final JsonSpec cs : JsonSpec.values()) sl.add(cs.toString());
    spec = new BaseXCombo(d, sl.toArray());
    spec.setSelectedItem(jopts.get(JsonOptions.SPEC));

    unescape = new BaseXCheckBox(UNESCAPE_CHARS, JsonParserOptions.UNESCAPE, jopts, d);
    merge = new BaseXCheckBox(MERGE_TYPES, JsonParserOptions.MERGE, jopts, d);
    strings = new BaseXCheckBox(INCLUDE_STRINGS, JsonParserOptions.STRINGS, jopts, d);
    lax = new BaseXCheckBox(LAX_NAME_CONVERSION, JsonOptions.LAX, jopts, d);

    BaseXBack pp = new BaseXBack(new TableLayout(2, 1, 0, 8));
    BaseXBack p = new BaseXBack(new TableLayout(3, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(encoding);
    p.add(new BaseXLabel(FORMAT + COL, true, true));
    p.add(format);
    p.add(new BaseXLabel(SPECIFICATION + COL, true, true));
    p.add(spec);
    pp.add(p);

    p = new BaseXBack(new TableLayout(4, 1));
    p.add(unescape);
    p.add(merge);
    p.add(strings);
    p.add(lax);
    pp.add(p);
    add(pp, BorderLayout.WEST);

    example = new Editor(false, d);
    add(example, BorderLayout.CENTER);

    action(true);
  }

  @Override
  boolean action(final boolean active) {
    try {
      final boolean jsonml = jopts.format() == JsonFormat.JSONML;
      lax.setEnabled(jopts.format() == JsonFormat.DIRECT);
      merge.setEnabled(!jsonml);
      strings.setEnabled(!jsonml);

      if(active) {
        final String json;
        if(jsonml) {
          json = EXAMPLEML;
        } else if(jopts.spec() == JsonSpec.LIBERAL) {
          json = EXAMPLE.replace("\"Person\"", "Person").replace(" }", ", }");
        } else if(jopts.spec() == JsonSpec.ECMA_262) {
          json = "\"John\\nMiller\"";
        } else {
          json = EXAMPLE;
        }
        final IO io = JsonParser.toXML(new IOContent(json), jopts.toString());
        example.setText(example(DataText.M_JSON.toUpperCase(Locale.ENGLISH), json, io.toString()));
      }
    } catch(final IOException ex) {
      example.setText(error(ex));
    }
    return true;
  }

  @Override
  void update() {
    String enc = encoding.getSelectedItem();
    jopts.set(JsonParserOptions.ENCODING, enc.equals(Token.UTF8) ? null : enc);
    tooltip(jopts, JsonParserOptions.ENCODING, encoding);
    jopts.set(JsonParserOptions.UNESCAPE, unescape.isSelected());
    tooltip(jopts, JsonParserOptions.UNESCAPE, unescape);
    jopts.set(JsonParserOptions.MERGE, merge.isSelected());
    tooltip(jopts, JsonParserOptions.MERGE, merge);
    jopts.set(JsonParserOptions.STRINGS, strings.isSelected());
    tooltip(jopts, JsonParserOptions.STRINGS, strings);
    jopts.set(JsonOptions.FORMAT, format.getSelectedItem());
    tooltip(jopts, JsonOptions.FORMAT, format);
    jopts.set(JsonOptions.SPEC, spec.getSelectedItem());
    tooltip(jopts, JsonOptions.SPEC, spec);
    jopts.set(JsonOptions.LAX, lax.isSelected());
    tooltip(jopts, JsonOptions.LAX, lax);
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.JSONPARSER, jopts.toString());
  }
}
