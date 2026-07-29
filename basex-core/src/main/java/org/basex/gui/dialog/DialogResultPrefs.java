package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Visualization preferences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogResultPrefs extends BaseXBack {
  /** Value of {@link GUIOptions#MAXRESULTS}. */
  private static final int[] MAXRESULTS = {
    50000, 100000, 250000, 500000, 1000000, 2500000, Integer.MAX_VALUE
  };
  /** Value of {@link GUIOptions#MAXTEXT}. */
  private static final int[] MAXTEXT = {
    1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25, Integer.MAX_VALUE
  };

  /** GUI reference. */
  private final GUI gui;

  /** Serialization parameters. */
  private final BaseXSerial serial;

  /** Number of hits. */
  private final BaseXSlider resultsMax;
  /** Result cache. */
  private final BaseXSlider textMax;
  /** Label for number of hits. */
  private final BaseXLabel resultsLabel;
  /** Label for text size. */
  private final BaseXLabel textLabel;

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogResultPrefs(final BaseXDialog dialog) {
    border(8).setLayout(new RowLayout(0));
    gui = dialog.gui();

    int val = BaseXSlider.index(gui.gopts.get(GUIOptions.MAXRESULTS), MAXRESULTS);
    resultsMax = new BaseXSlider(dialog, 0, MAXRESULTS.length - 1, val);
    resultsMax.addActionListener(e -> action());
    resultsLabel = new BaseXLabel(" ");

    val = BaseXSlider.index(gui.gopts.get(GUIOptions.MAXTEXT), MAXTEXT);
    textMax = new BaseXSlider(dialog, 0, MAXTEXT.length - 1, val);
    textMax.addActionListener(e -> action());
    textLabel = new BaseXLabel(" ");

    serial = new BaseXSerial(dialog, gui.context.options.get(MainOptions.SERIALIZER));

    BaseXBack p, pp;
    p = new BaseXBack(new RowLayout());
    pp = new BaseXBack(new ColumnLayout(12));
    pp.add(resultsMax);
    pp.add(resultsLabel);
    p.add(new BaseXLabel(MAX_NO_OF_HITS + COL));
    p.add(pp);
    pp = new BaseXBack(new ColumnLayout(12));
    pp.add(textMax);
    pp.add(textLabel);
    p.add(new BaseXLabel(SIZE_TEXT_RESULTS + COL));
    p.add(pp);

    add(new BaseXLabel(LIMITS + COL, true, true));
    add(p);
    add(serial.border(16, 0, 0, 0));
  }

  /**
   * Reacts on user input.
   * @return success flag
   */
  boolean action() {
    final int mt = MAXTEXT[textMax.getValue()], mr = MAXRESULTS[resultsMax.getValue()];
    final GUIOptions gopts = gui.gopts;
    gopts.set(GUIOptions.MAXTEXT, mt);
    gopts.set(GUIOptions.MAXRESULTS, mr);

    resultsLabel.setText(mr == Integer.MAX_VALUE ? ALL : BaseXLayout.format(mr));
    textLabel.setText(mt == Integer.MAX_VALUE ? ALL : Performance.formatHuman(mt));
    return true;
  }

  /**
   * Initializes the panel.
   */
  void init() {
    serial.init(gui.context.options.get(MainOptions.SERIALIZER));
  }

  /**
   * Closes the panel.
   */
  void cancel() {
    gui.set(MainOptions.SERIALIZER, serial.options());
  }
}
