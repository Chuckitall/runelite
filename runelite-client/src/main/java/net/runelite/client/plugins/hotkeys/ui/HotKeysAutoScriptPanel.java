package net.runelite.client.plugins.hotkeys.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.api.Skill;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.hotkeys.HotKeysPlugin;
import net.runelite.client.plugins.hotkeys.script.AutoScriptTypes;
import net.runelite.client.plugins.hotkeys.script.HotKeysAutoScript;
import net.runelite.client.plugins.hotkeys.script.SourceTypes;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.FlatTextField;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ObjectUtils;

public class HotKeysAutoScriptPanel extends JPanel
{

	private static final Border NAME_BOTTOM_BORDER = new CompoundBorder(
		BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
		BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));

	private static final ImageIcon EDIT_ICON;
	private static final ImageIcon EDIT_HOVER_ICON;

	private static final ImageIcon DELETE_ICON;
	private static final ImageIcon DELETE_HOVER_ICON;

	private final HotKeysPlugin plugin;

	@Getter
	private final HotKeysAutoScript autoScript;

	private final JLabel editLabel = new JLabel();
	private final JLabel deleteLabel = new JLabel();
	private final JButton hotkeyButton = new JButton();
	private final FlatTextField nameInput = new FlatTextField();
	private final JLabel save = new JLabel("Save");
	private final JLabel cancel = new JLabel("Cancel");
	private final JLabel rename = new JLabel("Rename");

	static
	{
		final BufferedImage editImg = ImageUtil.getResourceStreamFromClass(HotKeysPlugin.class, "edit_icon.png");
		final BufferedImage editImgHover = ImageUtil.luminanceOffset(editImg, -150);
		EDIT_ICON = new ImageIcon(editImg);
		EDIT_HOVER_ICON = new ImageIcon(editImgHover);

		final BufferedImage deleteImg = ImageUtil.getResourceStreamFromClass(HotKeysPlugin.class, "delete_icon.png");
		DELETE_ICON = new ImageIcon(deleteImg);
		DELETE_HOVER_ICON = new ImageIcon(ImageUtil.alphaOffset(deleteImg, -100));
	}

	HotKeysAutoScriptPanel(final HotKeysPlugin plugin, final HotKeysAutoScript autoScript)
	{
		this.plugin = plugin;
		this.autoScript = autoScript;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(2, 2, 2, 2));

		JPanel nameWrapper = new JPanel(new BorderLayout());
		nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameWrapper.setBorder(NAME_BOTTOM_BORDER);

		JPanel nameActions = new JPanel(new BorderLayout(3, 0));
		nameActions.setBorder(new EmptyBorder(0, 0, 0, 8));
		nameActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		save.setVisible(false);
		save.setFont(FontManager.getRunescapeSmallFont());
		save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		save.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				String input = nameInput.getText();
				if (input.isEmpty())
				{
					JOptionPane.showMessageDialog(HotKeysAutoScriptPanel.this, "Script name cannot be empty", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (!plugin.renameAutoScript(nameInput.getText(), autoScript))
				{
					JOptionPane.showMessageDialog(HotKeysAutoScriptPanel.this, "An auto script with that name already exists", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				nameInput.setEditable(false);
				updateNameActions(false);
				requestFocusInWindow();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				save.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}

		});

		cancel.setVisible(false);
		cancel.setFont(FontManager.getRunescapeSmallFont());
		cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
		cancel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				nameInput.setEditable(false);
				nameInput.setText(autoScript.getName());
				updateNameActions(false);
				requestFocusInWindow();
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR.darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				cancel.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			}
		});

		rename.setFont(FontManager.getRunescapeSmallFont());
		rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
		rename.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				nameInput.setEditable(true);
				updateNameActions(true);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker().darker());
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				rename.setForeground(ColorScheme.LIGHT_GRAY_COLOR.darker());
			}
		});
		nameActions.add(save, BorderLayout.EAST);
		nameActions.add(cancel, BorderLayout.WEST);
		nameActions.add(rename, BorderLayout.CENTER);

		nameInput.setText(autoScript.getName());
		nameInput.setBorder(null);
		nameInput.setEditable(false);
		nameInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		nameInput.setPreferredSize(new Dimension(0, 20));
		nameInput.getTextField().setForeground(Color.WHITE);
		nameInput.getTextField().setBorder(new EmptyBorder(0, 8, 0, 0));

		nameWrapper.add(nameInput, BorderLayout.CENTER);
		nameWrapper.add(nameActions, BorderLayout.EAST);
//
		JPanel infoPanel = new JPanel(new BorderLayout(0, 0));
		JComboBox<Object> skillComboBox = new JComboBox<>(Skill.values());
		if (autoScript.getSkill() != null)
		{
			skillComboBox.setSelectedItem(autoScript.getSkill());
		}
		skillComboBox.addActionListener(e ->
		{
			JComboBox comboBox = (JComboBox) e.getSource();
			Skill value = (Skill) comboBox.getSelectedItem();
			autoScript.setSkill(value);
			plugin.updateConfig();
		});
		skillComboBox.setPreferredSize(new Dimension(75, 20));
		JSpinner firstSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		firstSpinner.setPreferredSize(new Dimension(55, 20));
		firstSpinner.addChangeListener(e ->
		{
			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			autoScript.setArgs(new int[]{value, autoScript.getArgs()[1]});
			plugin.updateConfig();
		});
		firstSpinner.setValue(autoScript.getArgs()[0]);
		JSpinner secondSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		secondSpinner.setPreferredSize(new Dimension(55, 20));
		secondSpinner.addChangeListener(e ->
		{
			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			autoScript.setArgs(new int[]{autoScript.getArgs()[0], value});
			plugin.updateConfig();
		});
		secondSpinner.setValue(autoScript.getArgs()[1]);
		JComboBox<Object> sourceTypeComboBox = new JComboBox<>(SourceTypes.values());
		sourceTypeComboBox.setSelectedItem(autoScript.getSourceType());
		sourceTypeComboBox.setPreferredSize(new Dimension(85, 20));
		sourceTypeComboBox.addActionListener(e ->
		{
			JComboBox comboBox = (JComboBox) e.getSource();
			SourceTypes value = (SourceTypes) comboBox.getSelectedItem();
			autoScript.setSourceType(value);
			plugin.updateConfig();
		});
		JComboBox<Object> sourceTypeComboBox2 = new JComboBox<>(SourceTypes.values());
		sourceTypeComboBox2.setSelectedItem(autoScript.getSourceType());
		sourceTypeComboBox2.setPreferredSize(new Dimension(85, 20));
		sourceTypeComboBox2.addActionListener(e ->
		{
			JComboBox comboBox = (JComboBox) e.getSource();
			SourceTypes value = (SourceTypes) comboBox.getSelectedItem();
			autoScript.setSourceType(value);
			plugin.updateConfig();
		});

		JSpinner firstSpinner2 = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		firstSpinner2.setPreferredSize(new Dimension(100, 20));
		firstSpinner2.addChangeListener(e ->
		{
			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			autoScript.setArgs(new int[]{value, autoScript.getArgs()[1]});
			plugin.updateConfig();
		});
		firstSpinner2.setValue(autoScript.getArgs()[0]);
		JSpinner firstSpinner3 = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
		firstSpinner3.setPreferredSize(new Dimension(100, 20));
		firstSpinner3.addChangeListener(e ->
		{
			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			autoScript.setArgs(new int[]{value, autoScript.getArgs()[1]});
			plugin.updateConfig();
		});
		firstSpinner3.setValue(autoScript.getArgs()[0]);

		JPanel cards = new JPanel(new CardLayout());

		JPanel notSetPanel = new JPanel();
		notSetPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		cards.add(notSetPanel, AutoScriptTypes.NOT_SET.toString());
		JPanel xpDropPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		xpDropPanel.add(skillComboBox);
		xpDropPanel.add(firstSpinner);
		xpDropPanel.add(secondSpinner);
		cards.add(xpDropPanel, AutoScriptTypes.XP_DROP.toString());
		JPanel gameTickPanel = new JPanel();
		gameTickPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		cards.add(gameTickPanel, AutoScriptTypes.GAME_TICK.toString());
		JPanel inventoryChangedPanel = new JPanel();
		cards.add(inventoryChangedPanel, AutoScriptTypes.INVENTORY_CHANGED.toString());
		inventoryChangedPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		JPanel animationChangedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
		animationChangedPanel.add(firstSpinner2);
		animationChangedPanel.add(sourceTypeComboBox);
		cards.add(animationChangedPanel, AutoScriptTypes.ANIMATION_PLAYED.toString());
		JPanel soundEffectPlayedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
		soundEffectPlayedPanel.add(firstSpinner3);
		soundEffectPlayedPanel.add(sourceTypeComboBox2);
		cards.add(soundEffectPlayedPanel, AutoScriptTypes.SOUND_EFFECT_PLAYED.toString());
		JPanel specChangedPanel = new JPanel();
		specChangedPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		cards.add(specChangedPanel, AutoScriptTypes.SPEC_CHANGED.toString());
		JComboBox<Object> actionTypeCombo = new JComboBox<>(AutoScriptTypes.values());
		actionTypeCombo.addActionListener(e ->
		{
			JComboBox comboBox = (JComboBox) e.getSource();
			AutoScriptTypes value = (AutoScriptTypes) ObjectUtils.defaultIfNull(comboBox.getSelectedItem(), AutoScriptTypes.NOT_SET);
			autoScript.setAutoScriptType(value);
			plugin.updateConfig();
			CardLayout c1 = (CardLayout)(cards.getLayout());
			c1.show(cards, value.toString());
		});
		actionTypeCombo.setSelectedItem(autoScript.getAutoScriptType());
		infoPanel.add(actionTypeCombo, BorderLayout.NORTH);
		infoPanel.add(cards, BorderLayout.SOUTH);
//
		JPanel bottomContainer = new JPanel(new BorderLayout());
		bottomContainer.setBorder(new EmptyBorder(4, 0, 4, 0));
		bottomContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		leftActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		hotkeyButton.setText(autoScript.getHotkey().toString());
		hotkeyButton.addActionListener(e -> {
			autoScript.setHotkey(Keybind.NOT_SET);
			hotkeyButton.setText(autoScript.getHotkey().toString());
			plugin.updateConfig();
		});
		hotkeyButton.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				autoScript.setHotkey(new Keybind(e));
				hotkeyButton.setText(autoScript.getHotkey().toString());
				plugin.updateConfig();
			}

		});
		hotkeyButton.setFocusTraversalKeysEnabled(false);
		leftActions.add(hotkeyButton);

		JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
		rightActions.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		editLabel.setIcon(EDIT_ICON);
		editLabel.setToolTipText("Edit script");
		editLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				plugin.openAutoScriptEditor(autoScript);
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				editLabel.setIcon(EDIT_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				editLabel.setIcon(EDIT_ICON);
			}
		});

		deleteLabel.setIcon(DELETE_ICON);
		deleteLabel.setToolTipText("Delete script");
		deleteLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				int confirm = JOptionPane.showConfirmDialog(HotKeysAutoScriptPanel.this,
					"Are you sure you want to delete this script? It will be permanently lost if it is not saved in any presets.",
					"Warning", JOptionPane.OK_CANCEL_OPTION);

				if (confirm == 0)
				{
					plugin.deleteScript(autoScript);
				}
			}

			@Override
			public void mouseEntered(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_HOVER_ICON);
			}

			@Override
			public void mouseExited(MouseEvent mouseEvent)
			{
				deleteLabel.setIcon(DELETE_ICON);
			}
		});

		rightActions.add(editLabel);
		rightActions.add(deleteLabel);

		bottomContainer.add(leftActions, BorderLayout.WEST);
		bottomContainer.add(rightActions, BorderLayout.EAST);

		add(nameWrapper, BorderLayout.NORTH);
		add(infoPanel, BorderLayout.CENTER);
		add(bottomContainer, BorderLayout.SOUTH);

		setFocusTraversalKeysEnabled(false);
	}

	private void updateNameActions(boolean saveAndCancel)
	{
		save.setVisible(saveAndCancel);
		cancel.setVisible(saveAndCancel);
		rename.setVisible(!saveAndCancel);

		if (saveAndCancel)
		{
			nameInput.getTextField().requestFocusInWindow();
			nameInput.getTextField().selectAll();
		}
	}
}