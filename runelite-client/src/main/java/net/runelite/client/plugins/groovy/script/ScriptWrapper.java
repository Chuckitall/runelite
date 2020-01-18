package net.runelite.client.plugins.groovy.script;


import com.google.inject.Inject;
import groovy.lang.GroovyClassLoader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.groovy.GroovyCore;
import net.runelite.client.plugins.groovy.GroovyPanel;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayManager;

import static net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptCommand.DISABLE;
import static net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptCommand.ENABLE;
import static net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptCommand.LOAD;
import static net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptCommand.RELOAD;
import static net.runelite.client.plugins.groovy.script.ScriptWrapper.ScriptState.*;
import static net.runelite.client.plugins.groovy.GroovyPanel.CONTENT_WIDTH;
import static net.runelite.client.plugins.groovy.GroovyPanel.OFF_SWITCHER;
import static net.runelite.client.plugins.groovy.GroovyPanel.ON_SWITCHER;
import static net.runelite.client.plugins.groovy.GroovyPanel.TIME_WIDTH;

//This is the object that is held by the ScriptLoader.
//We use this object to enable or disable the plugin.
//It is in charge of holding a jPanel representing the runtime plugin
//It is in charge of holding a state (UNLOADED, LOADED, ENABLED, DISABLED, ERROR)
@Data
@Slf4j
public class ScriptWrapper
{
	@Inject
	private Client client;
	@Inject
	private EventBus eventBus;
	@Inject
	private MenuManager menuManager;
	@Inject
	private OverlayManager overlayManager;

	public enum ScriptCommand
	{
		LOAD(), ENABLE(), DISABLE(), RELOAD()
	}

	public enum ScriptState
	{
		UNLOADED(LOAD),
		DISABLED(ENABLE, RELOAD),
		ENABLED(DISABLE, RELOAD),
		ERROR_COMPILE(RELOAD),
		ERROR_RUNTIME(RELOAD),
		ERROR_FILE_NOT_FOUND(RELOAD);

		@Getter(AccessLevel.PUBLIC)
		private List<ScriptCommand> acceptedCommands;

		ScriptState()
		{
			this.acceptedCommands = Collections.emptyList();
		}

		ScriptState(ScriptCommand command)
		{
			this.acceptedCommands = List.of(command);
		}

		ScriptState(ScriptCommand command1, ScriptCommand command2)
		{
			this.acceptedCommands = List.of(command1, command2);
		}

		ScriptState(ScriptCommand command1, ScriptCommand command2, ScriptCommand command3)
		{
			this.acceptedCommands = List.of(command1, command2, command3);
		}
	}

	public class ScriptPanel extends JPanel
	{
		Color UNLOADED_BACKGROUND_COLOR = new Color(0, 125, 114);
		Color ENABLED_BACKGROUND_COLOR = new Color(30, 125, 30);
		Color DISABLED_BACKGROUND_COLOR = new Color(125, 124, 0);
		Color ERROR_BACKGROUND_COLOR = new Color(125, 30, 30);

		private Color color = DISABLED_BACKGROUND_COLOR;

		public JLabel avatar; //on off toggle
		public JLabel gear; //configure

		public JLabel titleLabel;
		private JPanel upAndContent;
		private JPanel buttonsContent;

		public String getName()
		{
			return name;
		}

		public ScriptPanel()
		{
			super(new BorderLayout());
			this.setPreferredSize(new Dimension(0, 56));

			buttonsContent = new JPanel();
			buttonsContent.setLayout(new BorderLayout());
			buttonsContent.setBorder(new EmptyBorder(4, 8, 4, 4));
			buttonsContent.setBackground(null);

			avatar = new JLabel();
			// width = 48+4 to compensate for the border
			avatar.setPreferredSize(new Dimension(29, 15));
			avatar.setBorder(new EmptyBorder(0, 4, 0, 0));

			gear = new JLabel();
			// width = 48+4 to compensate for the border
			gear.setPreferredSize(new Dimension(29, 15));
			gear.setBorder(new EmptyBorder(0, 4, 0, 0));

			buttonsContent.add(avatar, BorderLayout.WEST);
			buttonsContent.add(gear, BorderLayout.EAST);


			upAndContent = new JPanel();
			upAndContent.setLayout(new BoxLayout(upAndContent, BoxLayout.Y_AXIS));
			upAndContent.setBorder(new EmptyBorder(4, 8, 4, 4));
			upAndContent.setBackground(null);

			Color darkerForeground = UIManager.getColor("Label.foreground").darker();

			titleLabel = new JLabel(name);
			titleLabel.setFont(FontManager.getRunescapeSmallFont());
			titleLabel.setBackground(null);
			titleLabel.setForeground(darkerForeground);
			titleLabel.setPreferredSize(new Dimension(CONTENT_WIDTH - TIME_WIDTH, 0));

			upAndContent.add(titleLabel);
			upAndContent.add(new Box.Filler(new Dimension(0, 0),
				new Dimension(0, Short.MAX_VALUE),
				new Dimension(0, Short.MAX_VALUE)));
			upAndContent.add(buttonsContent);

			this.add(upAndContent, BorderLayout.CENTER);
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e)
				{
					setBackground(color.brighter().brighter());
					super.mouseEntered(e);
				}

				@Override
				public void mouseExited(MouseEvent e)
				{
					setBackground(color);
					super.mouseExited(e);
				}

				@Override
				public void mousePressed(MouseEvent e)
				{
					setBackground(color.brighter().brighter().brighter());
					super.mousePressed(e);
				}

				@Override
				public void mouseReleased(MouseEvent e)
				{
					setBackground(color.brighter().brighter());
					super.mouseReleased(e);
				}
			});
		}

		public void updateSelf()
		{
			if (state == UNLOADED)
			{
				this.color = UNLOADED_BACKGROUND_COLOR;
				avatar.setIcon(ON_SWITCHER);
			}
			else if (state == ENABLED)
			{
				this.color = ENABLED_BACKGROUND_COLOR;
				avatar.setIcon(ON_SWITCHER);
			}
			else if (state == DISABLED)
			{
				this.color = DISABLED_BACKGROUND_COLOR;
				avatar.setIcon(OFF_SWITCHER);
			}
			else if(state == ERROR_COMPILE || state == ERROR_RUNTIME || state == ERROR_FILE_NOT_FOUND)
			{
				this.color = ERROR_BACKGROUND_COLOR;
				avatar.setIcon(OFF_SWITCHER);
			}
			setBackground(color);
		}
	}

	private ScriptPanel buildJPanel()
	{
		ScriptPanel toRet = new ScriptPanel();
		toRet.updateSelf();
		toRet.gear.setIcon(GroovyPanel.GEAR_ICON);
		toRet.avatar.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mousePressed(e));
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseReleased(e));
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseEntered(e));
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseExited(e));
				super.mouseExited(e);
			}
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (state == ENABLED)
				{
					doCommand(DISABLE);
				}
				else if (state == DISABLED)
				{
					doCommand(ENABLE);
				}
				else if(state == UNLOADED)
				{
					doCommand(LOAD);
				}
				else
				{
					doCommand(RELOAD);
				}
				super.mouseClicked(e);
			}
		});
		toRet.titleLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mousePressed(e));
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseReleased(e));
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseEntered(e));
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseExited(e));
				super.mouseExited(e);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (state.getAcceptedCommands().contains(RELOAD))
				{
					doCommand(RELOAD);
				}
			}
		});
		toRet.gear.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mousePressed(e));
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseReleased(e));
				super.mouseReleased(e);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseEntered(e));
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				Arrays.stream(toRet.getMouseListeners()).forEach(f -> f.mouseExited(e));
				super.mouseExited(e);
			}

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (state == ENABLED)
				{
					log.debug("Cant open Config window for {} while enabled", name);
				}
				else
				{
					log.debug("Open Config window for script: {}", name);
				}
			}
		});
		return toRet;
	}

	private final String name;

	private ScriptedPlugin script = null;
	private ScriptContext ctx = null;
	private ScriptPanel panel;
	private ScriptState state = UNLOADED;

	public ScriptWrapper(String name, ScriptContext context)
	{
		this.name = name;
		this.ctx = context;
		this.panel = this.buildJPanel();
	}

	public void doCommand(ScriptCommand command)
	{
		if (!this.state.getAcceptedCommands().contains(command))
		{
			log.error("Can not process command {} while in state {}. Acceptable commands are {}", command, state, state.getAcceptedCommands());
			return;
		}
		else
		{
			log.debug("Calling command {} on {}", command, name);
		}

		log.debug("state: {}", this.state);
		ScriptState oldState = this.state;
		switch (this.state)
		{
			case UNLOADED:
				if (command.equals(LOAD))
				{
					log.debug("loading plugin {} from {}", name, GroovyCore.getGroovyRoot());
					String fileName = name + ".groovy";
					this.script = null;
					GroovyClassLoader gcl = new GroovyClassLoader();
					try
					{
						Class clazz = gcl.parseClass(new File(GroovyCore.getGroovyRoot(), fileName)) ;
						Object inst = clazz.getDeclaredConstructor(ScriptContext.class).newInstance(ctx);
						if (inst instanceof ScriptedPlugin)
						{
							log.debug("we got here? (inside the inst instanceof BaseScript");
							this.script = (ScriptedPlugin) inst;
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
						this.state = ERROR_FILE_NOT_FOUND;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					if (this.script != null)
					{
						this.state = DISABLED;
					}
					else if (state == UNLOADED)
					{
						this.state = ERROR_COMPILE;
					}
				}
				break;
			case DISABLED:
				if (command.equals(ENABLE))
				{
					this.script.startup();
					this.state = ENABLED;
				}
				if (command.equals(RELOAD))
				{
					this.script = null;
					this.state = UNLOADED;
					doCommand(LOAD);
				}
				break;
			case ENABLED:
				if (command.equals(DISABLE))
				{
					this.script.shutdown();
					this.state = DISABLED;
				}
				if (command.equals(RELOAD))
				{
					doCommand(DISABLE);
					doCommand(RELOAD);
					doCommand(ENABLE);
				}
				break;
			case ERROR_RUNTIME:
			case ERROR_COMPILE:
			case ERROR_FILE_NOT_FOUND:
				if (command.equals(RELOAD))
				{
					this.script = null;
					this.state = UNLOADED;
					this.doCommand(LOAD);
				}
				break;
//			case ERROR_FILE_NOT_FOUND:
//				if (command.equals(RELOAD))
//				{
//					this.script = null;
//					this.state = UNLOADED;
//					this.doCommand(LOAD);
//				}
//				break;
		}
		if (this.state != oldState)
		{
			panel.updateSelf();
		}
	}
}
