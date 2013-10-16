package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
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
  /** JSON options panel. */
  private final DialogParser[] parsers;

  /** Format label. */
  private final BaseXLabel label;
  /** Tabulators. */
  private final BaseXTabs tabs;
  /** Main window reference. */
  private final GUI gui;

  /** Currently chosen parser. */
  private MainParser parser;
  /** Current parser panel. */
  private BaseXBack panel;

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
        label.setText(Util.info(PARSER_X, parser.name()));
        add(label, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        revalidate();
        repaint();
      }
    });
  }

  /**
   * Sets the chosen parser.
   * @param mp parser
   */
  void setType(final MainParser mp) {
    parser = mp;
    if(mp == MainParser.RAW) {
      tabs.setEnabledAt(1, false);
    } else {
      tabs.setEnabledAt(1, true);
      final MainParser[] mps = MainParser.values();
      for(int t = 0; t < mps.length; t++) if(mps[t] == mp) panel = parsers[t];
    }
  }

  /**
   * Reacts on user input.
   * @return result of check
   */
  boolean action() {
    update();
    boolean ok = true;
    for(final DialogParser dp : parsers) ok &= dp.action(panel == dp);
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
