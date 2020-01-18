package net.runelite.client.plugins.groovy.debugger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.DynamicGridLayout;

@Slf4j
public class DebuggerWindow extends JFrame
{

	@Getter(AccessLevel.PUBLIC)
	public enum LogLevel
	{
		ERROR("Error", Color.RED),
		WARN("Warning", Color.ORANGE),
		INFO("Info", Color.BLUE),
		DEBUG("Debug", Color.CYAN),
		TRACE("Trace", Color.GRAY);

		private final String name;
		private final JCheckBox checkBox;
		private final Color color;

		LogLevel(String name, Color bg)
		{
			this.name = name;
			checkBox = new JCheckBox(name, true);
			this.color = new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 200);
		}
	}

	private final static int MAX_LOG_ENTRIES = 10000;

	private final Client client;
	private final EventBus eventBus;

	private final JPanel tracker = new JPanel();

	private int lastTick = 0;

	public DebuggerWindow(Client client, EventBus eventBus)
	{
		this.client = client;
		this.eventBus = eventBus;

		setTitle("Groovy Debugger");
		setIconImage(ClientUI.ICON);

		setLayout(new BorderLayout());

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				eventBus.unregister(this);
				close();
				//set button disabled if needed.
			}
		});

		tracker.setLayout(new DynamicGridLayout(0, 1, 0, 3));

		final JPanel trackerWrapper = new JPanel();
		trackerWrapper.setLayout(new BorderLayout());
		trackerWrapper.add(tracker, BorderLayout.NORTH);

		final JScrollPane trackerScroller = new JScrollPane(trackerWrapper);
		trackerScroller.setPreferredSize(new Dimension(400, 400));

		final JScrollBar vertical = trackerScroller.getVerticalScrollBar();
		vertical.addAdjustmentListener(new AdjustmentListener()
		{
			int lastMaximum = actualMax();

			private int actualMax()
			{
				return vertical.getMaximum() - vertical.getModel().getExtent();
			}

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				if (vertical.getValue() >= lastMaximum)
				{
					vertical.setValue(actualMax());
				}
				lastMaximum = actualMax();
			}
		});

		add(trackerScroller, BorderLayout.CENTER);

		final JPanel trackerOpts = new JPanel();
		trackerOpts.setLayout(new FlowLayout());
		for (LogLevel lvl : LogLevel.values())
		{
			trackerOpts.add(lvl.getCheckBox());
		}
		trackerOpts.setLayout(new FlowLayout());

		final JButton clearBtn = new JButton("Clear");
		clearBtn.addActionListener(e ->
		{
			tracker.removeAll();
			tracker.revalidate();
		});
		trackerOpts.add(clearBtn);

		add(trackerOpts, BorderLayout.SOUTH);
		this.setAlwaysOnTop(true);
		pack();
	}

	private void onGroovyLogEvent(GroovyLogEvent e)
	{
		if (!e.getLogLevel().getCheckBox().isSelected())
		{
			return;
		}
		SwingUtilities.invokeLater(() ->
		{

			JLabel line = new JLabel(String.format("%s: %s", e.getName(), e.getMessage()));
			line.setBackground(e.getLogLevel().getColor());
			tracker.add(line);

			// Cull very old stuff
			for (; tracker.getComponentCount() > MAX_LOG_ENTRIES; )
			{
				tracker.remove(0);
			}

			tracker.revalidate();
		});
	}

	public void open()
	{
		eventBus.subscribe(GroovyLogEvent.class, this, this::onGroovyLogEvent);
//		eventBus.subscribe(VarClientIntChanged.class, this, this::onVarClientIntChanged);
//		eventBus.subscribe(VarClientStrChanged.class, this, this::onVarClientStrChanged);

		setVisible(true);
		toFront();
		repaint();
	}

	public void close()
	{
		tracker.removeAll();
		eventBus.unregister(this);
		setVisible(false);
	}
}
