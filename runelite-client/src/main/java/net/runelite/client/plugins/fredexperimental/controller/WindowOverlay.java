package net.runelite.client.plugins.fredexperimental.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

public class WindowOverlay extends Overlay
{
	private final ControllerPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	public WindowOverlay(final ControllerPlugin _plugin)
	{
		super(_plugin);
		plugin = _plugin;
		setPreferredSize(new Dimension(120, 0));
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Controller")
			.color(Color.MAGENTA)
			.build());
		LineComponent scriptName = LineComponent.builder().left("Running").leftColor(Color.YELLOW).right(plugin.getScript().getClass().getSimpleName()).rightColor(Color.CYAN).build();
		panelComponent.getChildren().add(scriptName);
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableComponent.addRow("Enabled:", plugin.getScript().isEnabled() + "");
		tableComponent.addRow("Task:", plugin.getCurrentTask());
		panelComponent.getChildren().add(tableComponent);
		if (plugin.getScript().isEnabled())
		{
			panelComponent.getChildren().add(plugin.getScript().getWindowInfo());
		}
		return panelComponent.render(graphics);
	}
}