package org.basex.gui.dialog;

import static org.basex.core.Text.*;

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
import org.basex.util.list.*;

/**
 * JSON parser panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class DialogJsonParser extends DialogParser {
  /** JSON example string. */
  private static final String EXAMPLE = "{ \"Person\": \"John\\nAdam\",\n" +
      "  \"Born\"  : 1984,\n  \"_X ?\"  : null }";
  /** JSONML example string. */
  private static final String EXAMPLEML = "[ \"Person\",\n" +
      "  { \"born\": \"1984\" },\n  \"John\\nAdam\"\n]";
  /** JSON examples. */
  private static final String[] EXAMPLES = { EXAMPLE, EXAMPLE, EXAMPLEML };

  /** Options. */
  private JsonOptions jopts;
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
  /** JSON: store types in root element. */
  private final BaseXCheckBox root;
  /** JSON: specification. */
  private final BaseXCombo spec;

  /**
   * Constructor.
   * @param d dialog reference
   * @param opts main options
   */
  DialogJsonParser(final BaseXDialog d, final MainOptions opts) {
    try {
      jopts = new JsonOptions(opts.get(MainOptions.JSONPARSER));
    } catch(final IOException ex) { jopts = new JsonOptions(); }

    encoding = DialogExport.encoding(d, jopts.get(JsonOptions.ENCODING));

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

    unescape = new BaseXCheckBox(UNESCAPE_CHARS, jopts.get(JsonOptions.UNESCAPE), 0, d);
    lax = new BaseXCheckBox(LAX_NAME_CONVERSION, jopts.get(JsonOptions.LAX), 0, d);
    root = new BaseXCheckBox(STORE_TYPES_ROOT, jopts.get(JsonOptions.ROOT_TYPES), 0, d);

    BaseXBack pp = new BaseXBack(new TableLayout(2, 1, 0, 8));
    BaseXBack p = new BaseXBack(new TableLayout(3, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(encoding);
    p.add(new BaseXLabel(FORMAT + COL, true, true));
    p.add(format);
    p.add(new BaseXLabel(SPECIFICATION + COL, true, true));
    p.add(spec);
    pp.add(p);

    p = new BaseXBack(new TableLayout(3, 1));
    p.add(unescape);
    p.add(lax);
    p.add(root);
    pp.add(p);

    add(pp, BorderLayout.WEST);

    example = new Editor(false, d);
    BaseXLayout.setWidth(example, 300);

    add(example, BorderLayout.CENTER);
    action(true);
  }

  @Override
  boolean action(final boolean active) {
    try {
      final boolean direct = jopts.format() == JsonFormat.DIRECT;
      lax.setEnabled(direct);
      root.setEnabled(direct);

      if(active) {
        String ex = EXAMPLES[format.getSelectedIndex()];
        if(jopts.format() != JsonFormat.JSONML) {
          if(jopts.spec() == JsonSpec.LIBERAL) {
            ex = ex.replace("\"Person\"", "Person").replace(" }", ", }");
          } else if(jopts.spec() == JsonSpec.ECMA_262) {
            ex = "\"John\\nMiller\"";
          }
        }
        final IO io = JsonParser.toXML(new IOContent(ex), jopts.toString());
        example.setText(example(DataText.M_JSON.toUpperCase(Locale.ENGLISH), ex, io.toString()));
      }
    } catch(final IOException ex) {
      example.setText(error(ex));
    }
    return true;
  }

  @Override
  void update() {
    jopts.set(JsonOptions.ENCODING, encoding.getSelectedItem());
    jopts.set(JsonOptions.FORMAT, format.getSelectedItem());
    jopts.set(JsonOptions.SPEC, spec.getSelectedItem());
    jopts.set(JsonOptions.UNESCAPE, unescape.isSelected());
    jopts.set(JsonOptions.LAX, lax.isSelected());
    jopts.set(JsonOptions.ROOT_TYPES, root.isSelected());
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.JSONPARSER, jopts.toString());
  }
}
