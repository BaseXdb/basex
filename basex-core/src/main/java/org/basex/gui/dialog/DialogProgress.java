package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Dialog window for displaying the progress of a command execution.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogProgress extends BaseXDialog implements ActionListener {
  /** Maximum value of progress bar. */
  private static final int MAX = 600;
  /** Refresh action. */
  private final Timer timer = new Timer(100, this);
   /** Information label. */
  private BaseXLabel info;
  /** Cancel button. */
  private BaseXButton cancel;
  /** Memory usage. */
  private BaseXMem mem;
  /** Executed command. */
  private Command command;
  /** Progress bar. */
  private JProgressBar bar;

  /**
   * Default constructor.
   * @param gui main window
   * @param cmd progress reference
   */
  private DialogProgress(final GUI gui, final Command cmd) {
    super(gui, "");
    init(gui, cmd);
  }

  /**
   * Default constructor.
   * @param dialog dialog window
   * @param cmd progress reference
   */
  private DialogProgress(final BaseXDialog dialog, final Command cmd) {
    super(dialog, "");
    init(dialog, cmd);
  }

  /**
   * Initializes all components.
   * @param win window
   * @param cmd progress reference
   */
  private void init(final BaseXWindow win, final Command cmd) {
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
    final BaseXBack m = new BaseXBack(new ColumnLayout(5));
    mem = new BaseXMem(this, false);
    m.add(new BaseXLabel(MEMUSED_C));
    m.add(mem);
    s.add(m, BorderLayout.WEST);

    if(cmd.stoppable()) {
      cancel = new BaseXButton(this, B_CANCEL);
      s.add(cancel, BorderLayout.EAST);
    }
    set(s, BorderLayout.SOUTH);

    command = cmd;
    timer.start();
    pack();
    setLocationRelativeTo(win.component());
  }

  @Override
  public void cancel() {
    if(cancel != null) cancel.setEnabled(false);
    command.stop();
  }

  @Override
  public void close() {
    dispose();
  }

  @Override
  public void dispose() {
    timer.stop();
    command = null;
    super.dispose();
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final Job job = command.active();
    setTitle(job.shortInfo());
    final String detail = job.detailedInfo();
    info.setText(detail.isEmpty() ? " " : detail);
    mem.repaint();
    if(bar != null) bar.setValue((int) (job.progressInfo() * MAX));
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link BaseXDialog#action} if the dialog is closed.
   * @param dialog reference to the dialog window
   * @param cmds commands to be run
   */
  static void execute(final BaseXDialog dialog, final Command... cmds) {
    execute(dialog, null, cmds);
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link BaseXDialog#action} if the dialog is closed.
   * @param gui reference to the main window
   * @param cmds commands to be run
   */
  public static void execute(final GUI gui, final Command... cmds) {
    execute(gui, null, null, cmds);
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and
   * calls {@link BaseXDialog#action} if the dialog is closed.
   * @param dialog reference to the calling dialog window
   * @param post post-processing step
   * @param cmds commands to be run
   */
  static void execute(final BaseXDialog dialog, final Runnable post, final Command... cmds) {
    execute(dialog.gui, dialog, post, cmds);
  }

  /**
   * Runs the specified commands, decorated by a progress dialog, and calls
   * {@link BaseXDialog#action} if the dialog is closed.
   * @param gui reference to the main window
   * @param dialog reference to the dialog window (may be {@code null})
   * @param post post-processing step (may be {@code null})
   * @param cmds commands to be run
   */
  private static void execute(final GUI gui, final BaseXDialog dialog, final Runnable post,
      final Command... cmds) {

    for(final Command cmd : cmds) {
      // reset views
      final boolean newData = cmd.newData(gui.context);
      if(newData) gui.notify.init();

      // create wait dialog
      final DialogProgress wait = dialog != null ? new DialogProgress(dialog, cmd) :
        new DialogProgress(gui, cmd);

      // start command thread
      new Thread(() -> {
        // execute command
        final Performance perf = new Performance();
        gui.updating = cmd.updating(gui.context);
        boolean ok = true;
        String info;
        try {
          cmd.execute(gui.context);
          info = cmd.info();
        } catch(final BaseXException ex) {
          ok = false;
          info = Util.message(ex);
        } finally {
          gui.updating = false;
        }

        // return status information
        final String time = perf.toString();
        gui.info.setInfo(info, cmd, time, ok, true);
        gui.status.setText(cmd + ": " + time);

        // close progress window and show error if command failed
        wait.dispose();
        if(!ok) BaseXDialog.error(gui, info.equals(INTERRUPTED) ? COMMAND_CANCELED : info);
      }).start();

      // show progress windows until being disposed
      wait.setVisible(true);

      // initialize views if database was closed before
      if(newData) gui.notify.init();
      else if(cmd.updating(gui.context)) gui.notify.update();
      gui.editor.refreshContextLabel();
    }
    if(dialog != null && dialog.isVisible()) dialog.action(dialog);
    if(post != null) SwingUtilities.invokeLater(post);
  }
}
