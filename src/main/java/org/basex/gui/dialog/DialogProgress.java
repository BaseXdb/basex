package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import org.basex.core.Command;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Open;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXMem;
import org.basex.gui.layout.TableLayout;
import org.basex.util.Performance;
import org.basex.util.Util;

/**
 * Dialog window for displaying the progress of a command execution.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DialogProgress extends Dialog implements ActionListener {
  /** Refresh action. */
  private final Timer timer = new Timer(100, this);
  /** Information label. */
  private final BaseXLabel info;
  /** Memory usage. */
  private final BaseXMem mem;
  /** Executed command. */
  private final Command command;
  /** Progress bar. */
  private final JProgressBar bar;
  /** Current progress length. */
  private final int ww;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param message waiting message
   * @param cmd progress reference
   */
  DialogProgress(final GUI main, final String message, final Command cmd) {
    super(main, message, false);

    info = new BaseXLabel(" ", true, true);
    set(info, BorderLayout.NORTH);

    if(cmd.supportsProg()) {
      ww = 320;
      bar = new JProgressBar(0, ww);
      bar.setPreferredSize(new Dimension(ww, 16));
      set(bar, BorderLayout.CENTER);
    } else {
      BaseXLayout.setWidth(info, 500);
      bar = null;
      ww = 0;
    }

    final BaseXBack s = new BaseXBack(new BorderLayout()).border(10, 0, 0, 0);
    final BaseXBack m = new BaseXBack(new TableLayout(1, 2, 5, 0));
    mem = new BaseXMem(this, false);
    m.add(new BaseXLabel(MEMUSED));
    m.add(mem);
    s.add(m, BorderLayout.WEST);

    if(cmd.stoppable()) {
      final BaseXButton cancel = new BaseXButton(BUTTONCANCEL, this);
      cancel.setMnemonic();
      s.add(cancel, BorderLayout.EAST);
    }
    set(s, BorderLayout.SOUTH);

    command = cmd;
    timer.start();
    finish(null);
  }

  @Override
  public void cancel() {
    command.stop();
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
    setTitle(command.title());
    final String detail = command.detail();
    info.setText(detail.isEmpty() ? " " : detail);
    mem.repaint();
    if(bar != null) bar.setValue((int) (command.progress() * ww));
  }

  /**
   * Runs the specified commands, decorated by a progress dialog.
   * @param gui reference to the main window
   * @param t dialog title
   * @param cmds commands to be run
   */
  public static void execute(final GUI gui, final String t,
      final Command... cmds) {

    // start database creation thread
    new Thread() {
      @Override
      public void run() {
        exec(gui, t, cmds);
      }
    }.start();
  }

  /**
   * Runs the specified commands, decorated by a progress dialog.
   * @param gui reference to the main window
   * @param t dialog title
   * @param cmds commands to be run
   */
  static void exec(final GUI gui, final String t, final Command... cmds) {
    for(final Command cmd : cmds) {
      if(cmd.newData()) {
        new Close().run(gui.context);
        gui.notify.init();
      }

      // execute command
      final Performance perf = new Performance();
      final DialogProgress wait = new DialogProgress(gui, t, cmd);
      wait.setAlwaysOnTop(true);
      gui.updating = cmd.updating(gui.context);
      final boolean ok = cmd.run(gui.context);
      gui.updating = false;
      final String info = cmd.info();
      wait.dispose();

      final String time = perf.toString();
      gui.info.setInfo(info, cmd, time, ok);
      gui.info.reset();
      gui.status.setText(Util.info(PROCTIME, time));
      if(!ok) Dialog.error(gui, info.equals(PROGERR) ? CANCELCREATE : info);

      // initialize views
      String db = cmd.reOpen();
      if(db != null) new Open(db).run(gui.context);
      if(cmd.newData()) gui.notify.init();
      else if(cmd.updating(gui.context)) gui.notify.update();
    }
  }

  /**
   * Runs the specified commands, decorated by a progress dialog.
   * @param d dialog window
   * @param t dialog title
   * @param cmd commands to be run
   */
  public static void execute(final Dialog d, final String t,
      final Command cmd) {
    // start database creation thread
    new Thread() {
      @Override
      public void run() {
        exec(d.gui, t, cmd);
        d.action(null);
      }
    }.start();
  }
}
