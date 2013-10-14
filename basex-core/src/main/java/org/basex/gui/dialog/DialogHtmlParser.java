package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * CSV parser panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class DialogHtmlParser extends DialogParser {
  /** Parameters. */
  private final BaseXTextField options;

  /**
   * Constructor.
   * @param d dialog reference
   * @param opts main options
   */
  DialogHtmlParser(final BaseXDialog d, final MainOptions opts) {
    super(d);

    final boolean avl = HtmlParser.available();
    BaseXBack pp  = new BaseXBack(new TableLayout(2, 1, 0, 8));
    pp.add(new BaseXLabel(avl ? H_HTML_PARSER : H_NO_HTML_PARSER));

    options = new BaseXTextField(opts.get(MainOptions.HTMLPARSER), d);
    if(avl) {
      final BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
      p.add(new BaseXLabel(PARAMETERS + COL, true, true));
      p.add(options);
      pp.add(p);
    }

    add(pp, BorderLayout.WEST);
  }

  @Override
  boolean action(final boolean active) {
    return true;
  }

  @Override
  void update() {
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.HTMLPARSER, options.getText());
  }
}
