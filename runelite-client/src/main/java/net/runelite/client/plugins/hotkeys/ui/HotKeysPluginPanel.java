package net.runelite.client.plugins.hotkeys.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.hotkeys.HotKeysPlugin;
import net.runelite.client.plugins.hotkeys.script.HotKeysAutoScript;
import net.runelite.client.plugins.hotkeys.script.HotKeysScript;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.CustomScrollBarUI;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
@Singleton
public class HotKeysPluginPanel extends PluginPanel
{
	private static final ImageIcon ADD_ICON;
	private static final ImageIcon ADD_HOVER_ICON;
	private static final ImageIcon SAVE_ICON;
	private static final ImageIcon SAVE_HOVER_ICON;
	private static final ImageIcon LOAD_ICON;
	private static final ImageIcon LOAD_HOVER_ICON;
	private static final ImageIcon DELETE_PRESET_ICON;
	private static final ImageIcon DELETE_PRESET_HOVER_ICON;
	private static final ImageIcon LOGO_ICON;

	private final Client client;

	private final HotKeysPlugin plugin;

	@Getter
	private final List<HotKeysScriptPanel> scriptPanels;

	@Getter
	private final List<HotKeysAutoScriptPanel> autoScriptPanels;

	static
	{
		LOGO_ICON = new ImageIcon(ImageUtil.getResourceStreamFromClass(HotKeysPlugin.class, "logo.png"));
		final BufferedImage addIcon = ImageUtil.getResourceStreamFromClass(HotKeysPlugin.class, "add_icon.png");
		ADD_ICON = new ImageIcon(addIcon);
		ADD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(addIcon, 0.53f));
		final BufferedImage saveIcon = ImageUtil.getResourceStreamFromClass(HotKeysPlugin.class, "save_icon.png");
		SAVE_ICON = new ImageIcon(saveIcon);
		SAVE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(saveIcon, 0.53f));
		final BufferedImage loadIcon = ImageUtil.getResourceStreamFromClass(HotKeysPlugin.class, "load_icon.png");
		LOAD_ICON = new ImageIcon(loadIcon);
		LOAD_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(loadIcon, 0.53f));
		final BufferedImage deletePresetIcon = ImageUtil.getResourceStreamFromClass(HotKeysPlugin.class, "delete_icon.png");
		DELETE_PRESET_ICON = new ImageIcon(deletePresetIcon);
		DELETE_PRESET_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deletePresetIcon, 0.53f));
	}

	@Inject
	HotKeysPluginPanel(Client client, HotKeysPlugin plugin)
	{
		super();
		this.client = client;
		this.plugin = plugin;
		this.scriptPanels = new CopyOnWriteArrayList<>();
		this.autoScriptPanels = new CopyOnWriteArrayList<>();

		rebuildPanel();
	}

	private void init()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.getScrollPane().getVerticalScrollBar().setUI(new CustomScrollBarUI());
		this.getScrollPane().getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
		this.getScrollPane().getVerticalScrollBar().setUnitIncrement(16);

		//region create titlePanel
		JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JLabel leftTitleIcon = new JLabel(LOGO_ICON);
		JLabel rightTitleIcon = new JLabel(LOGO_ICON);
		JLabel titleText = new JLabel("Tom C's HotKeys");
		titleText.setFont(FontManager.getRunescapeBoldFont());
		titleText.setForeground(ColorScheme.BRAND_BLUE);
		titlePanel.add(leftTitleIcon);
		titlePanel.add(titleText);
		titlePanel.add(rightTitleIcon);
		//endregion


		//region create settings panel
		JPanel settingsPanel = new JPanel(new GridLayout(6, 0));
		settingsPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1), new EmptyBorder(4, 4, 4, 4)));
		JLabel settingsPanelLabel = new JLabel("  Settings");
		settingsPanelLabel.setForeground(ColorScheme.BRAND_BLUE);
		settingsPanelLabel.setFont(FontManager.getRunescapeBoldFont());
		settingsPanel.add(settingsPanelLabel, BorderLayout.NORTH);
		JPanel delaysPanel = new JPanel(new GridLayout(0, 2, 5, 5)); //make labels/spinners for delays
		JPanel spinnersPanel = new JPanel(new GridLayout(0, 2, 0, 5));
		JLabel delayLabel = new JLabel("Min/Max Delay:");
		JSpinner minDelaySpinner = new JSpinner(new SpinnerNumberModel(plugin.getMinDelay(), 0, 500, 1));
		minDelaySpinner.setPreferredSize(new Dimension(50, 20));
		minDelaySpinner.addChangeListener(e ->
		{
			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			plugin.setMinDelay(value);
			plugin.updateConfig();

		});
		JSpinner maxDelaySpinner = new JSpinner(new SpinnerNumberModel(plugin.getMaxDelay(), 0, 500, 1));
		maxDelaySpinner.setPreferredSize(new Dimension(50, 25));
		maxDelaySpinner.addChangeListener(e ->
		{
			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			plugin.setMaxDelay(value);
			plugin.updateConfig();

		});
		delaysPanel.add(delayLabel);
		spinnersPanel.add(minDelaySpinner);
		spinnersPanel.add(maxDelaySpinner);
		delaysPanel.add(spinnersPanel); //finish making delays
		settingsPanel.add(delaysPanel);
		//
		//
		JCheckBox mainOverlayCheckbox = new JCheckBox("Main Overlay", plugin.isMainOverlayOn());
		mainOverlayCheckbox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				plugin.setMainOverlayOn(false);
			}
			else
			{
				plugin.setMainOverlayOn(true);
			}
			plugin.updateConfig();
		});

		settingsPanel.add(mainOverlayCheckbox);
		//
		//
		JCheckBox autoOverlayCheckbox = new JCheckBox("Auto Scripts Overlay", plugin.isAutoOverlayOn());
		autoOverlayCheckbox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				plugin.setAutoOverlayOn(false);
			}
			else
			{
				plugin.setAutoOverlayOn(true);
			}
			plugin.updateConfig();
		});

		settingsPanel.add(autoOverlayCheckbox);
		//
		JCheckBox salveCheckbox = new JCheckBox("Keep Salve On", plugin.isKeepSalveOn());
		salveCheckbox.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.DESELECTED)
			{
				plugin.setKeepSalveOn(false);
			}
			else
			{
				plugin.setKeepSalveOn(true);
			}
			plugin.updateConfig();
		});

		settingsPanel.add(salveCheckbox);
		//
		JButton tutorialButton = new JButton();
		tutorialButton.setFocusable(false);

		tutorialButton.setText("Help");
		tutorialButton.setToolTipText("View a window explaining what commands are available and what they do.");
		tutorialButton.addActionListener(e -> {
			JDialog.setDefaultLookAndFeelDecorated(true);
			int response = JOptionPane.showConfirmDialog(null, "Insert instructions here", "Title", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);
		});
		settingsPanel.add(tutorialButton);
		//
		//endregion

		//region create presetManagerPanel
		JPanel presetContainerPanel = new JPanel(new GridLayout(2, 0));
		presetContainerPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1), new EmptyBorder(4, 4, 4, 4)));
		JLabel presetManagerLabel = new JLabel("  Manage Presets");
		presetManagerLabel.setForeground(ColorScheme.BRAND_BLUE);
		presetManagerLabel.setFont(FontManager.getRunescapeBoldFont());
		presetContainerPanel.add(presetManagerLabel, BorderLayout.NORTH);
		JPanel presetManagerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		List<String> presetNames = plugin.getPresetNames();
		presetNames.add(0, "");
		JComboBox presetCombo = new JComboBox<>(presetNames.toArray());
		presetCombo.setEditable(true);
		presetCombo.setPreferredSize(new Dimension(125, 20));
		presetManagerPanel.add(presetCombo);

		JLabel savePreset = new JLabel(SAVE_ICON);
		savePreset.setToolTipText("Save preset");
		savePreset.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.savePreset((String) ObjectUtils.defaultIfNull(presetCombo.getSelectedItem(), ""));
				rebuild();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				savePreset.requestFocus();
				savePreset.setIcon(SAVE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				savePreset.setIcon(SAVE_ICON);
			}
		});
		presetManagerPanel.add(savePreset);

		JLabel loadPreset = new JLabel(LOAD_ICON);
		loadPreset.setToolTipText("Load preset");
		loadPreset.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.loadPreset((String) presetCombo.getSelectedItem());
				rebuild();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				loadPreset.setIcon(LOAD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				loadPreset.setIcon(LOAD_ICON);
			}
		});
		presetManagerPanel.add(loadPreset);

		JLabel deletePreset = new JLabel(DELETE_PRESET_ICON);
		deletePreset.setToolTipText("Delete preset");
		deletePreset.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.deletePreset((String) presetCombo.getSelectedItem());
				rebuild();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				deletePreset.setIcon(DELETE_PRESET_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deletePreset.setIcon(DELETE_PRESET_ICON);
			}
		});
		presetManagerPanel.add(deletePreset);
		presetContainerPanel.add(presetManagerPanel, BorderLayout.SOUTH);
		//endregion
		//region keybindsPanel
		JPanel keybindsPanel = new JPanel(new GridLayout(4, 0, 5, 5));
		keybindsPanel.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1), new EmptyBorder(4, 4, 4, 4)));
		JLabel keybindsPanelLabel = new JLabel("  Key Bindings");
		keybindsPanelLabel.setForeground(ColorScheme.BRAND_BLUE);
		keybindsPanelLabel.setFont(FontManager.getRunescapeBoldFont());
		keybindsPanel.add(keybindsPanelLabel);
		JPanel toggleHotkeyPanel = new JPanel(new GridLayout(0, 2, 5, 5)); //make label/button for toggle hotkey
		JLabel toggleHotkeyLabel = new JLabel("Toggle Hotkey:");
		JButton toggleHotkeyButton = new JButton();
		toggleHotkeyButton.setText(plugin.getToggleKeybind().toString());
		toggleHotkeyButton.addActionListener(e -> {
			plugin.setToggleKeybind(Keybind.NOT_SET);
			toggleHotkeyButton.setText(plugin.getToggleKeybind().toString());
			plugin.updateConfig();
		});
		toggleHotkeyButton.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				plugin.setToggleKeybind(new Keybind(e));
				toggleHotkeyButton.setText(plugin.getToggleKeybind().toString());
				plugin.updateConfig();
			}

		});
		toggleHotkeyPanel.add(toggleHotkeyLabel);
		toggleHotkeyPanel.add(toggleHotkeyButton); //finish toggle hotkey
		keybindsPanel.add(toggleHotkeyPanel);
		JPanel prayFlickPanel = new JPanel(new GridLayout(0, 2, 5, 5)); //make label/button for pray flick hotkey
		JLabel prayFlickLabel = new JLabel("Pray Flick Hotkey:");
		JButton prayFlickButton = new JButton();
		prayFlickButton.setText(plugin.getPrayFlickKeybind().toString());
		prayFlickButton.addActionListener(e -> {
			plugin.setPrayFlickKeybind(Keybind.NOT_SET);
			prayFlickButton.setText(plugin.getPrayFlickKeybind().toString());
			plugin.updateConfig();
		});
		prayFlickButton.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				plugin.setPrayFlickKeybind(new Keybind(e));
				prayFlickButton.setText(plugin.getPrayFlickKeybind().toString());
				plugin.updateConfig();
			}

		});
		prayFlickPanel.add(prayFlickLabel);
		prayFlickPanel.add(prayFlickButton); //finish pray flick hotkey
		keybindsPanel.add(prayFlickPanel);
		JPanel togglePrayFlickPanel = new JPanel(new GridLayout(0, 2, 5, 5)); //make label/button for pray flick toggle hotkey
		JLabel togglePrayFlickLabel = new JLabel("Pray Flick Toggle:");
		JButton togglePrayFlickButton = new JButton();
		togglePrayFlickButton.setText(plugin.getTogglePrayFlickKeybind().toString());
		togglePrayFlickButton.addActionListener(e -> {
			plugin.setTogglePrayFlickKeybind(Keybind.NOT_SET);
			togglePrayFlickButton.setText(plugin.getTogglePrayFlickKeybind().toString());
			plugin.updateConfig();
		});
		togglePrayFlickButton.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				plugin.setTogglePrayFlickKeybind(new Keybind(e));
				togglePrayFlickButton.setText(plugin.getTogglePrayFlickKeybind().toString());
				plugin.updateConfig();
			}

		});
		togglePrayFlickPanel.add(togglePrayFlickLabel);
		togglePrayFlickPanel.add(togglePrayFlickButton); //finish pray flick toggle hotkey
		keybindsPanel.add(togglePrayFlickPanel);
		//endregion

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(titlePanel);
		topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		topPanel.add(presetContainerPanel);
		topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		topPanel.add(settingsPanel);
		topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		topPanel.add(keybindsPanel);

		JPanel scriptsContainer = new JPanel();
		scriptsContainer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1), new EmptyBorder(4, 4, 4, 4)));
		scriptsContainer.setLayout(new BoxLayout(scriptsContainer, BoxLayout.Y_AXIS));

		JPanel scriptHeaderPanel = new JPanel();
		scriptHeaderPanel.setLayout(new BorderLayout());
		scriptHeaderPanel.setBorder(new EmptyBorder(5, 5, 5, 11));
		JLabel addScript = new JLabel(ADD_ICON);
		JLabel scriptsTitle = new JLabel();
		scriptsTitle.setText("  Scripts");
		scriptsTitle.setFont(FontManager.getRunescapeBoldFont());
		scriptsTitle.setForeground(ColorScheme.BRAND_BLUE);
		addScript.setToolTipText("Add new script");
		addScript.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				String name = (String) JOptionPane.showInputDialog(HotKeysPluginPanel.this, null,
					"Choose a script name", JOptionPane.PLAIN_MESSAGE, null,
					null, "Script");
				if (name == null)
				{
					return;
				}
				if (plugin.createNewScript(name, Keybind.NOT_SET))
				{
					rebuild();
				}
				else
				{
					JOptionPane.showMessageDialog(HotKeysPluginPanel.this, "A script with that name already exists", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addScript.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addScript.setIcon(ADD_ICON);
			}
		});
		scriptHeaderPanel.add(scriptsTitle, BorderLayout.WEST);
		scriptHeaderPanel.add(addScript, BorderLayout.EAST);
		//scriptHeaderPanel.add(Box.createRigidArea(new Dimension(0, 5)), BorderLayout.SOUTH);
		JPanel scriptsView = new JPanel(new GridBagLayout());
		scriptsView.setBackground(ColorScheme.DARK_GRAY_COLOR);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		scriptPanels.clear();
		for (final HotKeysScript script : plugin.getScripts())
		{
			HotKeysScriptPanel scriptPanel = new HotKeysScriptPanel(plugin, script);
			scriptPanels.add(scriptPanel);
			scriptsView.add(scriptPanel, constraints);
			constraints.gridy++;

			scriptsView.add(Box.createRigidArea(new Dimension(0, 5)), constraints);
			constraints.gridy++;
		}

		JButton clearButton = new JButton();
		clearButton.setFocusable(false);

		clearButton.setText("Delete All Scripts");
		clearButton.setToolTipText("Delete all scripts. This will not delete scripts saved in presets.");
		clearButton.setFont(FontManager.getRunescapeBoldFont());
		clearButton.setForeground(Color.RED);
		clearButton.addActionListener(e -> {
			JDialog.setDefaultLookAndFeelDecorated(true);
			int response = JOptionPane.showConfirmDialog(null, "Do you really want to delete all scripts? If they are not saved in any presets, they will be permanently lost.", "Delete All Scripts?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION)
			{

			}
			else if (response == JOptionPane.YES_OPTION)
			{
				plugin.setScripts(new ArrayList<>());
				plugin.updateConfig();
				rebuild();
			}
			else if (response == JOptionPane.CLOSED_OPTION)
			{

			}

		});
		scriptsView.add(clearButton, constraints);
		constraints.gridy++;

		scriptsContainer.add(scriptHeaderPanel);
		scriptsContainer.add(scriptsView);

		JPanel autoScriptsContainer = new JPanel();
		autoScriptsContainer.setBorder(BorderFactory.createCompoundBorder(new LineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1), new EmptyBorder(4, 4, 4, 4)));
		autoScriptsContainer.setLayout(new BoxLayout(autoScriptsContainer, BoxLayout.Y_AXIS));

		JPanel autoScriptHeaderPanel = new JPanel();
		autoScriptHeaderPanel.setLayout(new BorderLayout());
		autoScriptHeaderPanel.setBorder(new EmptyBorder(5, 5, 5, 11));
		JLabel addAutoScript = new JLabel(ADD_ICON);
		JLabel autoScriptsTitle = new JLabel();
		autoScriptsTitle.setText(  "Auto Scripts");
		autoScriptsTitle.setFont(FontManager.getRunescapeBoldFont());
		autoScriptsTitle.setForeground(ColorScheme.BRAND_BLUE);
		addAutoScript.setToolTipText("Add new auto script");
		addAutoScript.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				String name = (String) JOptionPane.showInputDialog(HotKeysPluginPanel.this, null,
					"Choose a script name", JOptionPane.PLAIN_MESSAGE, null,
					null, "Auto Script");
				if (name == null)
				{
					return;
				}
				if (plugin.createNewAutoScript(name))
				{
					rebuild();
				}
				else
				{
					JOptionPane.showMessageDialog(HotKeysPluginPanel.this, "An auto script with that name already exists", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				addAutoScript.setIcon(ADD_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				addAutoScript.setIcon(ADD_ICON);
			}
		});
		autoScriptHeaderPanel.add(autoScriptsTitle, BorderLayout.WEST);
		autoScriptHeaderPanel.add(addAutoScript, BorderLayout.EAST);
		autoScriptsContainer.add(autoScriptHeaderPanel);

		JPanel autoScriptsView = new JPanel(new GridBagLayout());////////////
		autoScriptsView.setBackground(ColorScheme.DARK_GRAY_COLOR);
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		autoScriptPanels.clear();
		for (final HotKeysAutoScript autoScript : plugin.getAutoScripts())
		{
			HotKeysAutoScriptPanel autoScriptPanel = new HotKeysAutoScriptPanel(plugin, autoScript);
			autoScriptPanels.add(autoScriptPanel);
			autoScriptsView.add(autoScriptPanel, constraints);
			constraints.gridy++;
			autoScriptsView.add(Box.createRigidArea(new Dimension(0, 5)), constraints);
			constraints.gridy++;
		}

		JButton clearAutoButton = new JButton();
		clearAutoButton.setFocusable(false);

		clearAutoButton.setText("Delete All Scripts");
		clearAutoButton.setToolTipText("Delete all auto scripts. This will not delete scripts saved in presets.");
		clearAutoButton.setFont(FontManager.getRunescapeBoldFont());
		clearAutoButton.setForeground(Color.RED);
		clearAutoButton.addActionListener(e -> {
			JDialog.setDefaultLookAndFeelDecorated(true);
			int response = JOptionPane.showConfirmDialog(null, "Do you really want to delete all auto scripts? If they are not saved in any presets, they will be permanently lost.", "Delete All Auto Scripts?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.NO_OPTION)
			{

			}
			else if (response == JOptionPane.YES_OPTION)
			{
				plugin.setAutoScripts(new ArrayList<>());
				plugin.updateConfig();
				rebuild();
			}
			else if (response == JOptionPane.CLOSED_OPTION)
			{

			}

		});
		constraints.gridy++;
		autoScriptsView.add(clearAutoButton, constraints);
		autoScriptsContainer.add(autoScriptsView, BorderLayout.SOUTH);

		add(Box.createRigidArea(new Dimension(0, 5)));
		add(topPanel);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(scriptsContainer);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(autoScriptsContainer);
	}

	public void rebuild()
	{
		SwingUtil.setupRuneLiteLookAndFeel();
		removeAll();
		repaint();
		revalidate();
		init();
	}

	public void rebuildPanel()
	{
		SwingUtilities.invokeLater(this::rebuild);
	}
}
