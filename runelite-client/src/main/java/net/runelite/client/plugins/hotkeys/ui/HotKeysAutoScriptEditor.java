package net.runelite.client.plugins.hotkeys.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.hotkeys.HotKeysPlugin;
import net.runelite.client.plugins.hotkeys.script.HotKeysAutoScript;
import net.runelite.client.plugins.hotkeys.utils.ExtUtils;
import net.runelite.client.ui.ColorScheme;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public class HotKeysAutoScriptEditor extends JFrame
{
	private final HotKeysPlugin plugin;

	@Getter
	private final HotKeysAutoScript autoScript;

	public HotKeysAutoScriptEditor(HotKeysPlugin plugin, HotKeysAutoScript autoScript) throws HeadlessException
	{
		this.plugin = plugin;
		this.autoScript = autoScript;
		JPanel panel = new JPanel(new BorderLayout());
		JTextArea textArea = new JTextArea(20, 60);
		DocumentListener documentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent documentEvent) {
				autoScript.setStatementsFromText(textArea.getText());
			}
			public void insertUpdate(DocumentEvent documentEvent) {
				autoScript.setStatementsFromText(textArea.getText());
			}
			public void removeUpdate(DocumentEvent documentEvent) {
				autoScript.setStatementsFromText(textArea.getText());
			}
		};
		textArea.getDocument().addDocumentListener(documentListener);
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
		toolbarPanel.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		JButton copyButton = new JButton(); //start copy button
		copyButton.setFocusable(false);

		copyButton.setText("Copy Gear IDs");
		copyButton.setPreferredSize(new Dimension(110, 25));
		copyButton.addActionListener(e -> {
			ExtUtils.copyGear(plugin.getClient());
			plugin.updateConfig();
		}); //end copy button
		toolbarPanel.add(copyButton);
		toolbarPanel.add(Box.createRigidArea(new Dimension(30, 0)));

		List<String> actionTypes = ExtUtils.actionTypes;
		JComboBox actionTypeCombo = new JComboBox<>(actionTypes.toArray());
		actionTypeCombo.setPreferredSize(new Dimension(115, 25));
		toolbarPanel.add(actionTypeCombo);
		toolbarPanel.add(Box.createRigidArea(new Dimension(5, 0)));

		JTextField actionArgsInput = new JTextField(12);
		actionArgsInput.setPreferredSize(new Dimension(75, 25));
		toolbarPanel.add(actionArgsInput);
		toolbarPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		textArea.setBackground(ColorScheme.DARK_GRAY_COLOR);

		textArea.setTabSize(2);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		textArea.setOpaque(false);

		textArea.setText(autoScript.getStatementsAsText());
		textArea.setEditable(true);

		JButton addCommandButton = new JButton(); //start add command button
		addCommandButton.setPreferredSize(new Dimension(110, 25));
		addCommandButton.setFocusable(false);
		addCommandButton.setText("Add Command");
		addCommandButton.addActionListener(e -> {
			if (((String) ObjectUtils.defaultIfNull(actionTypeCombo.getSelectedItem(), "")).equals("") || actionArgsInput.getText().isEmpty())
			{
				return;
			}
			String text = textArea.getText();
			text += "\n[" + (String) actionTypeCombo.getSelectedItem() + ":" + actionArgsInput.getText() + "]";
			autoScript.setStatementsFromText(text);
			textArea.setText(autoScript.getStatementsAsText());
			actionArgsInput.setText("");
		}); //end add command button
		toolbarPanel.add(addCommandButton);

		panel.add(textArea, BorderLayout.SOUTH);
		panel.add(toolbarPanel, BorderLayout.NORTH);
		setContentPane(panel);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				log.info(textArea.getText());
				autoScript.setStatementsFromText(textArea.getText());
				textArea.setText(autoScript.getStatementsAsText());
				close();
			}

			@Override
			public void windowOpened(WindowEvent e)
			{
				log.info("window open");
				textArea.setText(autoScript.getStatementsAsText());
			}
		});
		pack();
	}

	public void open()
	{
		plugin.updateConfig();
		setTitle("Auto Script Editor - " + autoScript.getName());
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public void close()
	{
		setAlwaysOnTop(false);
		setVisible(false);
	}
}
