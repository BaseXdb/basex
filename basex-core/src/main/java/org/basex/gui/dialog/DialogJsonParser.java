package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.JsonFormat;
import org.basex.build.JsonOptions.JsonSpec;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * JSON parser panel.
 *
 * @author BaseX Team 2005-14, BSD License
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
    jopts = new JsonParserOptions(opts.get(MainOptions.JSONPARSER));

    encoding = DialogExport.encoding(d, jopts.get(JsonParserOptions.ENCODING));

    final JsonFormat[] formats = JsonFormat.values();
    final int fl = formats.length - 1;
    final StringList frmts = new StringList(fl);
    for(int f = 0; f < fl; f++) frmts.add(formats[f].toString());
    format = new BaseXCombo(d, frmts.finish());
    format.setSelectedItem(jopts.get(JsonOptions.FORMAT));

    final StringList spcs = new StringList();
    for(final JsonSpec cs : JsonSpec.values()) spcs.add(cs.toString());
    spec = new BaseXCombo(d, spcs.finish());
    spec.setSelectedItem(jopts.get(JsonOptions.SPEC));

    unescape = new BaseXCheckBox(UNESCAPE_CHARS, JsonParserOptions.UNESCAPE, jopts, d);
    merge = new BaseXCheckBox(MERGE_TYPES, JsonOptions.MERGE, jopts, d);
    strings = new BaseXCheckBox(INCLUDE_STRINGS, JsonOptions.STRINGS, jopts, d);
    lax = new BaseXCheckBox(LAX_NAME_CONVERSION, JsonOptions.LAX, jopts, d);

    final BaseXBack pp = new BaseXBack(new TableLayout(2, 1, 0, 8));
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

    example = new TextPanel(false, d);
    add(example, BorderLayout.CENTER);

    action(true);
  }

  @Override
  boolean action(final boolean active) {
    try {
      final JsonSpec js = jopts.get(JsonOptions.SPEC);
      final JsonFormat jf = jopts.get(JsonOptions.FORMAT);
      lax.setEnabled(jf == JsonFormat.DIRECT);
      merge.setEnabled(jf != JsonFormat.JSONML);
      strings.setEnabled(jf != JsonFormat.JSONML);

      if(active) {
        final String json;
        if(jf == JsonFormat.JSONML) {
          json = EXAMPLEML;
        } else if(js == JsonSpec.LIBERAL) {
          json = EXAMPLE.replace("\"Person\"", "Person").replace(" }", ", }");
        } else if(js == JsonSpec.ECMA_262) {
          json = "\"John\\nMiller\"";
        } else {
          json = EXAMPLE;
        }
        final DBNode node = new DBNode(JsonParser.toXML(new IOContent(json), jopts));
        example.setText(example(MainParser.JSON.name(), json, node.serialize().toString()));
      }
    } catch(final IOException ex) {
      example.setText(error(ex));
    }
    return true;
  }

  @Override
  void update() {
    final String enc = encoding.getSelectedItem();
    jopts.set(JsonParserOptions.ENCODING, enc.equals(Token.UTF8) ? null : enc);
    jopts.set(JsonParserOptions.UNESCAPE, unescape.isSelected());
    jopts.set(JsonOptions.MERGE, merge.isSelected());
    jopts.set(JsonOptions.STRINGS, strings.isSelected());
    jopts.set(JsonOptions.FORMAT, format.getSelectedItem());
    jopts.set(JsonOptions.SPEC, spec.getSelectedItem());
    jopts.set(JsonOptions.LAX, lax.isSelected());
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.JSONPARSER, jopts);
  }
}
