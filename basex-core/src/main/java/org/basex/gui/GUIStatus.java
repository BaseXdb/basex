package org.basex.gui;

import static org.basex.core.Text.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

import org.basex.core.cmd.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Status bar of the main window, displaying progress information, the runtime of a running
 * command, background jobs, the opened database and the memory status.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GUIStatus extends BaseXPanel {
  /** Status text. */
  private final BaseXLabel label;
  /** Name of the opened database. */
  private final Segment database;
  /** Runtime of the running command. */
  private final Segment runtime;
  /** Number of background jobs. */
  private final Segment jobs;
  /** Timer for refreshing the runtime. */
  private final Timer timer;
  /** Timer for refreshing the number of background jobs. */
  private final Timer jobsTimer;
  /** Start time of the running command. */
  private volatile long start;

  /**
   * Constructor.
   * @param gui reference to the main window
   */
  GUIStatus(final GUI gui) {
    super(gui);
    setPreferredSize(new Dimension(getPreferredSize().width, getFont().getSize() + 10));
    addMouseListener(this);
    addMouseMotionListener(this);

    layout(new BorderLayout());
    label = new BaseXLabel(OK).border(0, 4, 2, 0);
    add(label, BorderLayout.CENTER);

    final AbstractButton stopJobs = BaseXButton.get("c_stop", STOP, false, gui);
    stopJobs.addActionListener(e -> stopJobs());
    jobs = new Segment(() -> gui.execute(new XQuery("job:list-details()[@id != job:current()]")),
        stopButton(stopJobs));
    runtime = new Segment(null, stopButton(BaseXButton.command(GUIMenuCmd.C_STOP, gui)));
    database = new Segment(() -> GUIMenuCmd.C_PROPERTIES.execute(gui), null);
    database.text.setToolTipText(GUIMenuCmd.C_PROPERTIES.shortCut());
    timer = new Timer(1000, e -> {
      runtime.text.setText(elapsed());
      runtime.setVisible(true);
    });
    jobsTimer = new Timer(1000, e -> jobs());

    final BaseXBack east = new BaseXBack(false);
    east.setLayout(new BoxLayout(east, BoxLayout.X_AXIS));
    east.add(jobs);
    east.add(runtime);
    east.add(database);
    final BaseXBack mem = new BaseXBack(false).layout(new BorderLayout()).border(1, 0, 1, 0);
    mem.add(new BaseXMem(gui, true));
    east.add(mem);
    add(east, BorderLayout.EAST);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    jobsTimer.start();
  }

  @Override
  public void removeNotify() {
    jobsTimer.stop();
    timer.stop();
    super.removeNotify();
  }

  /**
   * Sets the status text.
   * @param txt text to be set
   * @param ok success flag
   */
  public void setText(final String txt, final boolean ok) {
    label.setText(txt);
    label.setForeground(ok ? GUIConstants.textColor : GUIConstants.red);
  }

  /**
   * Shows the name of the opened database.
   * @param data opened database (can be {@code null})
   */
  void database(final Data data) {
    database.text.setText(data != null ? data.meta.name : "");
    database.setVisible(data != null);
  }

  /**
   * Starts the runtime display of a command.
   */
  void start() {
    start = System.nanoTime();
    SwingUtilities.invokeLater(timer::restart);
  }

  /**
   * Stops the runtime display of a command.
   */
  void stop() {
    SwingUtilities.invokeLater(() -> {
      timer.stop();
      runtime.setVisible(false);
    });
  }

  /**
   * Returns the formatted runtime of the running command.
   * @return runtime
   */
  private String elapsed() {
    return Performance.formatTime((System.nanoTime() - start) / 1_000_000_000);
  }

  /**
   * Shows the number of background jobs.
   */
  private void jobs() {
    final int size = gui.context.jobs.queryIds().size();
    jobs.text.setText(Util.info(JOBS_X, size));
    jobs.setVisible(size > 0);
  }

  /**
   * Stops all background jobs.
   */
  private void stopJobs() {
    final JobPool pool = gui.context.jobs;
    for(final String id : pool.queryIds()) pool.remove(id);
    jobs();
  }

  /**
   * Wraps a stop button in a toolbar and scales its icon to the height of the status bar.
   * @param button stop button
   * @return toolbar with the button
   */
  private BaseXToolBar stopButton(final AbstractButton button) {
    button.setMargin(new Insets(0, 0, 0, 0));
    final Insets bi = button.getInsets(), si = getInsets();
    final int size = getPreferredSize().height - si.top - si.bottom - bi.top - bi.bottom;
    final Image image = BaseXImages.get("c_stop");
    button.setIcon(new Icon() {
      @Override
      public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        g.drawImage(image, x, y, size, size, c);
      }
      @Override
      public int getIconWidth() {
        return size;
      }
      @Override
      public int getIconHeight() {
        return size;
      }
    });
    final BaseXToolBar toolbar = new BaseXToolBar();
    toolbar.setBorder(BaseXLayout.border(-2, 0, 2, 0));
    toolbar.add(button);
    return toolbar;
  }

  /**
   * Segment of the status bar, which is initially hidden.
   */
  private static final class Segment extends BaseXBack {
    /** Text label. */
    final BaseXLabel text = new BaseXLabel().border(0, 0, 2, 0);

    /**
     * Constructor.
     * @param click action to be performed on a click on the text (can be {@code null})
     * @param button button (can be {@code null})
     */
    Segment(final Runnable click, final JComponent button) {
      super(false);
      layout(new BorderLayout(4, 0));
      setBorder(new CompoundBorder(new MatteBorder(0, 1, 0, 0, GUIConstants.gray),
          BaseXLayout.border(0, 8, 0, 8)));
      add(text, BorderLayout.CENTER);
      if(button != null) add(button, BorderLayout.EAST);
      if(click != null) BaseXLayout.clickable(text, click);
      setVisible(false);
    }
  }
}
