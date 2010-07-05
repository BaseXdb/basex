package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import org.basex.core.Progress;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXMem;
import org.basex.gui.layout.TableLayout;
import org.basex.util.Performance;

/**
 * Dialog window for displaying the progress of a command execution.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class DialogProgress extends Dialog implements ActionListener {
  /** Refresh action. */
  private final Timer timer = new Timer(100, this);
  /** Information label. */
  private final BaseXLabel info;
  /** Memory usage. */
  private final BaseXMem mem;
  /** Progress reference. */
  private final Progress prog;
  /** Progress bar. */
  private final JProgressBar bar;
  /** Current progress length. */
  private final int ww;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param msg waiting message
   * @param pb showing a progress bar
   * @param cnc cancel flag
   * @param prg progress reference
   */
  public DialogProgress(final GUI main, final String msg, final boolean pb,
      final boolean cnc, final Progress prg) {

    super(main, msg, false);

    info = new BaseXLabel(" ", true, true);
    set(info, BorderLayout.NORTH);

    if(!pb) {
      BaseXLayout.setWidth(info, 500);
      bar = null;
      ww = 0;
    } else {
      ww = 320;
      bar = new JProgressBar(0, ww);
      bar.setPreferredSize(new Dimension(ww, 16));
      set(bar, BorderLayout.CENTER);
    }

    final BaseXBack south = new BaseXBack();
    south.setLayout(new BorderLayout());
    south.setBorder(10, 0, 0, 0);

    final BaseXBack m = new BaseXBack();
    m.setLayout(new TableLayout(1, 2, 5, 0));
    m.setBorder(0, 0, 0, 0);
    mem = new BaseXMem(this, false);
    m.add(new BaseXLabel(MEMUSED));
    m.add(mem);
    south.add(m, BorderLayout.WEST);

    if(cnc) south.add(new BaseXButton(BUTTONCANCEL, this), BorderLayout.EAST);
    set(south, BorderLayout.SOUTH);
    finish(null);

    prog = prg;
    Performance.gc(1);
    timer.start();
  }

  @Override
  public void cancel() {
    prog.stop();
    close();
  }

  @Override
  public void close() {
    dispose();
  }

  @Override
  public void dispose() {
    timer.stop();
    super.dispose();
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    setTitle(prog.title());
    info.setText(prog.detail());
    mem.repaint();
    //mem.setText("Memory: " + Performance.getMem());
    if(bar != null) bar.setValue((int) (prog.progress() * ww));
  }
}
