package org.basex.gui.dialog;

import org.basex.core.Command;
import static org.basex.core.Text.*;
import org.basex.gui.GUI;
import org.basex.gui.layout.*;
import org.basex.util.Performance;
import org.basex.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog window for displaying the progress of a command execution.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogProgress extends Dialog implements ActionListener {
  /** Maximum value of progress bar. */
  private static final int MAX = 500;
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

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param title dialog title
   * @param cmd progress reference
   */
  DialogProgress(final GUI main, final String title, final Command cmd) {
    super(main, title, false);

    info = new BaseXLabel(" ", true, true);
    set(info, BorderLayout.NORTH);

    if(cmd.supportsProg()) {
      bar = new JProgressBar(0, MAX);
      set(bar, BorderLayout.CENTER);
    } else {
      bar = null;
    }
    BaseXLayout.setWidth(info, MAX);

    final BaseXBack s = new BaseXBack(new BorderLayout()).border(10, 0, 0, 0);
    final BaseXBack m = new BaseXBack(new TableLayout(1, 2, 5, 0));
    mem = new BaseXMem(this, false);
    m.add(new BaseXLabel(MEMUSED_C));
    m.add(mem);
    s.add(m, BorderLayout.WEST);

    if(cmd.stoppable()) {
      final BaseXButton cancel = new BaseXButton(B_CANCEL, this);
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
    if(bar != null) bar.setValue((int) (command.progress() * MAX));
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link Dialog#action} if the dialog is closed.
   * @param d reference to the dialog window
   * @param t dialog title (may be an empty string)
   * @param cmds commands to be run
   */
  public static void execute(final Dialog d, final String t,
      final Command... cmds) {
    execute(d, t, null, cmds);
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link Dialog#action} if the dialog is closed.
   * @param d reference to the dialog window
   * @param t dialog title (may be an empty string)
   * @param post post-processing step
   * @param cmds commands to be run
   */
  public static void execute(final Dialog d, final String t,
      final Runnable post, final Command... cmds) {

    // start database creation thread
    new Thread() {
      @Override
      public void run() {
        d.setEnabled(false);

        final GUI gui = d.gui;
        for(final Command cmd : cmds) {
          // reset views
          final boolean newData = cmd.newData(gui.context);
          if(newData) gui.notify.init();

          // open wait dialog
          final DialogProgress wait = new DialogProgress(gui, t, cmd);

          // execute command
          final Performance perf = new Performance();
          gui.updating = cmd.updating(gui.context);
          final boolean ok = cmd.run(gui.context);
          gui.updating = false;
          final String info = cmd.info();

          // return status information
          final String time = perf.toString();
          gui.info.setInfo(info, cmd, time, ok);
          gui.info.reset();
          gui.status.setText(Util.info(TIME_NEEDED_X, time));

          // close progress window and show error if command failed
          wait.dispose();
          if(!ok) Dialog.error(gui, info.equals(INTERRUPTED) ?
              CREATION_CANCELED : info);

          // initialize views if database was closed before
          if(newData) gui.notify.init();
          else if(cmd.updating(gui.context)) gui.notify.update();
        }

        d.setEnabled(true);
        d.action(null);
        if(post != null) post.run();
      }
    }.start();
  }
}
