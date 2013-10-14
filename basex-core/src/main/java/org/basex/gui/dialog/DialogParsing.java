package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;
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
  /** Main window reference. */
  private final GUI gui;

  /** Current parser. */
  private BaseXBack parser;
  /** Current parser type. */
  private String type;

  /**
   * Default constructor.
   * @param d dialog reference
   * @param t tabs
   */
  DialogParsing(final BaseXDialog d, final BaseXTabs t) {
    border(8);
    gui = d.gui;
    tabs = t;
    label = new BaseXLabel().border(0, 0, 12, 0).large();

    final MainOptions opts = gui.context.options;
    parsers = new DialogParser[] { new DialogXmlParser(d, opts), new DialogHtmlParser(d, opts),
      new DialogJsonParser(d, opts), new DialogCsvParser(d, opts), new DialogTextParser(d, opts)
    };

    setLayout(new BorderLayout());
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(final ComponentEvent e) {
        removeAll();
        label.setText(Util.info(PARSER_X, type.toUpperCase(Locale.ENGLISH)));
        add(label, BorderLayout.NORTH);
        add(parser, BorderLayout.CENTER);
        revalidate();
        repaint();
      }
    });
  }

  /**
   * Sets the correct input type.
   * @param tp type
   */
  void setType(final String tp) {
    type = tp;
    tabs.setEnabledAt(1, !tp.equals(DataText.M_RAW));
    final int tl = TYPES.length;
    for(int t = 0; t < tl; t++) if(tp.equals(TYPES[t])) parser = parsers[t];
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
