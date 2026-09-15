package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.build.json.*;
import org.basex.build.json.JsonParser;
import org.basex.build.json.JsonOptions.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.io.parse.json.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * JSON parser panel.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogJsonParser extends DialogParser {
  /** Example string for JSON conversion. */
  private static final String EXAMPLE = "{ \"Person\": \"John\\nAdam\",\n" +
      "  \"Born\"  : 1984,\n  \"X?_\"  : [ true, null ] }";
  /** Example string for JSONML conversion. */
  private static final String EXAMPLEML = "[ \"Person\",\n" +
      "  { \"born\": \"1984\" },\n  \"John\\nAdam\"\n]";
  /** Example string for the w3-mapping conversion. */
  private static final String EXAMPLEPLAN = "{ \"Person\": {\n  \"Name\": \"John\",\n" +
      "  \"Born\": 1984,\n  \"Langs\": [ \"en\", \"de\" ] } }";

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
  /** JSON: JSON Lines. */
  private final BaseXCheckBox lines;
  /** JSON: escape. */
  private final BaseXCheckBox escape;
  /** JSON: lax name conversion. */
  private final BaseXCheckBox lax;
  /** JSON: merge type information. */
  private final BaseXCheckBox merge;
  /** JSON: include string types. */
  private final BaseXCheckBox strings;
  /** JSON: mapping file. */
  private final BaseXTextField mapping;
  /** JSON: browse mapping file. */
  private final BaseXButton browse;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param opts main options
   */
  DialogJsonParser(final BaseXDialog dialog, final MainOptions opts) {
    jopts = new JsonParserOptions(opts.get(MainOptions.JSONPARSER));

    encoding = encoding(dialog, jopts.get(JsonParserOptions.ENCODING));

    final String[] formats = Arrays.stream(new JsonFormat[] {
      JsonFormat.DIRECT, JsonFormat.ATTRIBUTES, JsonFormat.JSONML, JsonFormat.W3_XML,
      JsonFormat.W3_MAPPING
    }).map(JsonFormat::toString).toArray(String[]::new);
    format = new BaseXCombo(dialog, formats);
    format.setSelectedItem(jopts.get(JsonOptions.FORMAT));

    // file with the options of the w3-mapping format
    final Value path = jopts.get(JsonOptions.MAPPING);
    mapping = new BaseXTextField(dialog, path instanceof final Str str ?
      Token.string(str.string()) : "");
    browse = new BaseXButton(dialog, BROWSE_D);
    browse.addActionListener(e -> {
      final GUIOptions gopts = dialog.gui().gopts;
      final BaseXFileChooser fc = new BaseXFileChooser(dialog, FILE_OR_DIR,
          gopts.get(GUIOptions.INPUTPATH)).filter(JSON_DOCUMENTS, true, IO.JSONSUFFIX);
      final IO file = fc.select(Mode.FDOPEN);
      if(file != null) mapping.setText(file.path());
    });

    liberal = new BaseXCheckBox(dialog, LIBERAL_PARSING, JsonParserOptions.LIBERAL, jopts);
    lines = new BaseXCheckBox(dialog, "JSON Lines", JsonParserOptions.JSON_LINES, jopts);
    escape = new BaseXCheckBox(dialog, ESCAPE_CHARS, JsonParserOptions.ESCAPE, jopts);
    merge = new BaseXCheckBox(dialog, MERGE_TYPES, JsonOptions.MERGE, jopts);
    strings = new BaseXCheckBox(dialog, INCLUDE_STRINGS, JsonOptions.STRINGS, jopts);
    lax = new BaseXCheckBox(dialog, LAX_NAME_CONVERSION, JsonOptions.LAX, jopts);
    example = new TextPanel(dialog, false);

    final BaseXBack pp = new BaseXBack(new RowLayout(8));
    BaseXBack p = new BaseXBack(new TableLayout(2, 2, 8, 4));
    p.add(new BaseXLabel(ENCODING + COL, true, true));
    p.add(encoding);
    p.add(new BaseXLabel(FORMAT + COL, true, true));
    p.add(format);
    pp.add(p);
    p = new BaseXBack(new RowLayout());
    p.add(liberal);
    p.add(lines);
    p.add(escape);
    p.add(merge);
    p.add(strings);
    p.add(lax);
    pp.add(p);
    add(pp, BorderLayout.WEST);
    add(example, BorderLayout.CENTER);

    // mapping file, spanning the full width below the options and the example
    final BaseXBack mp = new BaseXBack(new BorderLayout(8, 4)).border(8, 0, 0, 0);
    mp.add(new BaseXLabel("Mapping" + COL, true, true), BorderLayout.NORTH);
    mp.add(mapping, BorderLayout.CENTER);
    mp.add(browse, BorderLayout.EAST);
    add(mp, BorderLayout.SOUTH);

    action(true);
  }

  @Override
  boolean action(final boolean active) {
    try {
      final boolean jl = jopts.get(JsonParserOptions.LIBERAL);
      final JsonFormat jf = jopts.get(JsonOptions.FORMAT);
      final boolean plan = jf == JsonFormat.W3_MAPPING;
      mapping.setEnabled(plan);
      browse.setEnabled(plan);
      // type information is only available in the direct and attributes formats
      final boolean types = jf == JsonFormat.DIRECT || jf == JsonFormat.ATTRIBUTES;
      merge.setEnabled(types);
      strings.setEnabled(types);
      lax.setEnabled(jf == JsonFormat.DIRECT);
      if(active) {
        String json;
        if(jf == JsonFormat.JSONML) {
          json = EXAMPLEML;
        } else if(plan) {
          json = EXAMPLEPLAN;
        } else if(jl) {
          json = EXAMPLE.replace("\"Person\"", "Person").replace(" }", ", }");
        } else {
          json = EXAMPLE;
        }
        final Value value;
        final boolean lns = jopts.get(JsonParserOptions.JSON_LINES);
        if(lns || plan) {
          // converted as on import; JSON Lines: two single-line copies of the example
          if(lns) {
            final String line = json.replaceAll("\n *", " ");
            json = line + '\n' + line;
          }
          final MainOptions mopts = new MainOptions();
          mopts.set(MainOptions.JSONPARSER, jopts);
          value = new DBNode(MemBuilder.build(new JsonParser(new IOContent(json), mopts, jopts)));
        } else {
          value = JsonConverter.get(jopts).convert(new IOContent(json));
        }
        example.setText(example(MainParser.JSON.name(), json, value));
      }
    } catch(final QueryException | IOException ex) {
      example.setText(error(ex));
    }
    return true;
  }

  @Override
  void update() {
    final String enc = encoding.getSelectedItem();
    jopts.set(JsonParserOptions.ENCODING, enc.equals(Strings.UTF8) ? null : enc);
    jopts.set(JsonParserOptions.LIBERAL, liberal.isSelected());
    jopts.set(JsonParserOptions.JSON_LINES, lines.isSelected());
    jopts.set(JsonParserOptions.ESCAPE, escape.isSelected());
    jopts.set(JsonOptions.MERGE, merge.isSelected());
    jopts.set(JsonOptions.STRINGS, strings.isSelected());
    jopts.set(JsonOptions.FORMAT, format.getSelectedItem());
    jopts.set(JsonOptions.LAX, lax.isSelected());
    // the mapping is only supported by the w3-mapping format
    final String path = mapping.getText().trim();
    final boolean plan = jopts.get(JsonOptions.FORMAT) == JsonFormat.W3_MAPPING;
    jopts.set(JsonOptions.MAPPING, plan && !path.isEmpty() ? Str.get(path) : Empty.VALUE);
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.JSONPARSER, jopts);
  }
}
