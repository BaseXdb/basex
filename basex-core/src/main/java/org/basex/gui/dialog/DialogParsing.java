package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Parsing options dialog.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class DialogParsing extends BaseXBack {
  /** Parser types. */
  private static final String[] TYPES = {
    DataText.M_XML, DataText.M_HTML, DataText.M_JSON, DataText.M_CSV, DataText.M_TEXT
  };
  /** JSON options panel. */
  private final DialogParser[] parsers;

  /** Format label. */
  private final BaseXLabel label;
  /** Tabulators. */
  private final BaseXTabs tabs;

  /** Main panel. */
  private final BaseXBack main;
  /** Main window reference. */
  private final GUI gui;

  /** Current parser. */
  private BaseXBack parser;

  /**
   * Default constructor.
   * @param d dialog reference
   * @param t tabs
   */
  public DialogParsing(final BaseXDialog d, final BaseXTabs t) {
    main = new BaseXBack(new BorderLayout()).border(8);
    gui = d.gui;
    tabs = t;
    label = new BaseXLabel(" ").border(0, 0, 12, 0).large();

    final MainOptions opts = gui.context.options;
    parsers = new DialogParser[] { new DialogXmlParser(d, opts), new DialogHtmlParser(d, opts),
      new DialogJsonParser(d, opts), new DialogCsvParser(d, opts), new DialogTextParser(d, opts)
    };

    setLayout(new BorderLayout());
    add(main, BorderLayout.CENTER);
  }

  /**
   * Updates the options, depending on the specific type.
   * @param type parsing type
   */
  void updateType(final String type) {
    label.setText(Util.info(PARSER_X, type.toUpperCase(Locale.ENGLISH)));

    final int tl = TYPES.length;
    for(int t = 0; t < tl; t++) if(type.equals(TYPES[t])) parser = parsers[t];

    main.removeAll();
    main.add(label, BorderLayout.NORTH);
    if(parser != null) main.add(parser, BorderLayout.CENTER);
    main.revalidate();
    tabs.setEnabledAt(1, !type.equals(DataText.M_RAW));
  }

  /**
   * Reacts on user input.
   * @return result of check
   */
  boolean action() {
    update();
    boolean ok = true;
    for(final DialogParser dp : parsers) ok &= dp.action(parser == dp);
    return ok;
  }

  /**
   * Sets the parsing options.
   */
  public void update() {
    for(final DialogParser dp : parsers) dp.update();
  }

  /**
   * Sets the parsing options.
   */
  public void setOptions() {
    update();
    for(final DialogParser dp : parsers) dp.setOptions(gui);
  }
}
