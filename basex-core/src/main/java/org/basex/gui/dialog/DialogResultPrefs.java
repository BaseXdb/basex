package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.text.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Visualization preferences.
 *
 * @author BaseX Team 2005-21, BSD License
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
  private final BaseXSlider maxResults;
  /** Result cache. */
  private final BaseXSlider maxText;
  /** Label for number of hits. */
  private final BaseXLabel labelResults;
  /** Label for text size. */
  private final BaseXLabel labelText;

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogResultPrefs(final BaseXDialog dialog) {
    border(8).setLayout(new ColumnLayout(40));
    gui = dialog.gui;

    int val = sliderIndex(gui.gopts.get(GUIOptions.MAXRESULTS), MAXRESULTS);
    maxResults = new BaseXSlider(dialog, 0, MAXRESULTS.length - 1, val);
    maxResults.addActionListener(e -> action());
    labelResults = new BaseXLabel(" ");

    val = sliderIndex(gui.gopts.get(GUIOptions.MAXTEXT), MAXTEXT);
    maxText = new BaseXSlider(dialog, 0, MAXTEXT.length - 1, val);
    maxText.addActionListener(e -> action());
    labelText = new BaseXLabel(" ");

    serial = new BaseXSerial(dialog, gui.context.options.get(MainOptions.SERIALIZER));

    final BaseXBack p = new BaseXBack().layout(new RowLayout());
    BaseXBack pp, ppp;
    p.add(new BaseXLabel(LIMITS + COL, true, true));

    pp = new BaseXBack(new RowLayout());
    ppp = new BaseXBack(new ColumnLayout(12));
    ppp.add(maxResults);
    ppp.add(labelResults);
    pp.add(new BaseXLabel(MAX_NO_OF_HITS + COL));
    pp.add(ppp);
    ppp = new BaseXBack(new ColumnLayout(12));
    ppp.add(maxText);
    ppp.add(labelText);
    pp.add(new BaseXLabel(SIZE_TEXT_RESULTS + COL));
    pp.add(ppp);
    p.add(pp);
    add(p);

    add(serial);
  }

  /**
   * Reacts on user input.
   * @return success flag
   */
  boolean action() {
    gui.gopts.set(GUIOptions.MAXTEXT, MAXTEXT[maxText.getValue()]);
    gui.gopts.set(GUIOptions.MAXRESULTS, MAXRESULTS[maxResults.getValue()]);

    final int mr = MAXRESULTS[maxResults.getValue()];
    labelResults.setText(mr == Integer.MAX_VALUE ? ALL : new DecimalFormat("#,###,###").format(mr));
    final int mt = MAXTEXT[maxText.getValue()];
    labelText.setText(mt == Integer.MAX_VALUE ? ALL : Performance.format(mt));
    return true;
  }

  /**
   * Updates the panel.
   */
  void update() {
    serial.init(gui.context.options.get(MainOptions.SERIALIZER));
  }

  /**
   * Closes the panel.
   */
  void cancel() {
    gui.set(MainOptions.SERIALIZER, serial.options());
  }

  /**
   * Returns the selected maximum number of hits as slider value.
   * @param value value to be found
   * @param values allowed values
   * @return index
   */
  private static int sliderIndex(final int value, final int[] values) {
    final int hl = values.length - 1;
    int i = -1;
    while(++i < hl && values[i] < value);
    return i;
  }
}
