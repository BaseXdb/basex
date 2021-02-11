package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.JsonFormat;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.io.parse.json.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * JSON parser panel.
 *
 * @author BaseX Team 2005-21, BSD License
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
  private final JsonParserOptions jopts;
  /** JSON example. */
  private final TextPanel example;
  /** JSON: encoding. */
  private final BaseXCombo encoding;
  /** JSON: format. */
  private final BaseXCombo format;
  /** JSON: liberal. */
  private final BaseXCheckBox liberal;
  /** JSON: escape. */
  private final BaseXCheckBox escape;
  /** JSON: lax name conversion. */
  private final BaseXCheckBox lax;
  /** JSON: merge type information. */
  private final BaseXCheckBox merge;
  /** JSON: include string types. */
  private final BaseXCheckBox strings;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param opts main options
   */
  DialogJsonParser(final BaseXDialog dialog, final MainOptions opts) {
    jopts = new JsonParserOptions(opts.get(MainOptions.JSONPARSER));

    encoding = encoding(dialog, jopts.get(JsonParserOptions.ENCODING));

    final JsonFormat[] formats = JsonFormat.values();
    final int fl = formats.length - 1;
    final StringList frmts = new StringList(fl);
    for(int f = 0; f < fl; f++) frmts.add(formats[f].toString());
    format = new BaseXCombo(dialog, frmts.finish());
    format.setSelectedItem(jopts.get(JsonOptions.FORMAT));

    liberal = new BaseXCheckBox(dialog, LIBERAL_PARSING, JsonParserOptions.LIBERAL, jopts);
    escape = new BaseXCheckBox(dialog, ESCAPE_CHARS, JsonParserOptions.ESCAPE, jopts);
    merge = new BaseXCheckBox(dialog, MERGE_TYPES, JsonOptions.MERGE, jopts);
    strings = new BaseXCheckBox(dialog, INCLUDE_STRINGS, JsonOptions.STRINGS, jopts);
    lax = new BaseXCheckBox(dialog, LAX_NAME_CONVERSION, JsonOptions.LAX, jopts);

    final BaseXBack pp = new BaseXBack(new RowLayout(8));
    BaseXBack p = new BaseXBack(new TableLayout(2, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(encoding);
    p.add(new BaseXLabel(FORMAT + COL, true, true));
    p.add(format);
    pp.add(p);

    p = new BaseXBack(new RowLayout());
    p.add(liberal);
    p.add(escape);
    p.add(merge);
    p.add(strings);
    p.add(lax);
    pp.add(p);
    add(pp, BorderLayout.WEST);

    example = new TextPanel(dialog, false);
    add(example, BorderLayout.CENTER);

    action(true);
  }

  @Override
  boolean action(final boolean active) {
    try {
      final boolean jl = jopts.get(JsonParserOptions.LIBERAL);
      final JsonFormat jf = jopts.get(JsonOptions.FORMAT);
      lax.setEnabled(jf == JsonFormat.DIRECT);
      merge.setEnabled(jf != JsonFormat.JSONML);
      strings.setEnabled(jf != JsonFormat.JSONML);

      if(active) {
        final String json;
        if(jf == JsonFormat.JSONML) {
          json = EXAMPLEML;
        } else if(jl) {
          json = EXAMPLE.replace("\"Person\"", "Person").replace(" }", ", }");
        } else {
          json = EXAMPLE;
        }
        example.setText(example(MainParser.JSON.name(), json,
            JsonConverter.get(jopts).convert(new IOContent(json)).serialize().toString()));
      }
    } catch(final IOException ex) {
      example.setText(error(ex));
    }
    return true;
  }

  @Override
  void update() {
    final String enc = encoding.getSelectedItem();
    jopts.set(JsonParserOptions.ENCODING, enc.equals(Strings.UTF8) ? null : enc);
    jopts.set(JsonParserOptions.LIBERAL, liberal.isSelected());
    jopts.set(JsonParserOptions.ESCAPE, escape.isSelected());
    jopts.set(JsonOptions.MERGE, merge.isSelected());
    jopts.set(JsonOptions.STRINGS, strings.isSelected());
    jopts.set(JsonOptions.FORMAT, format.getSelectedItem());
    jopts.set(JsonOptions.LAX, lax.isSelected());
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.JSONPARSER, jopts);
  }
}
