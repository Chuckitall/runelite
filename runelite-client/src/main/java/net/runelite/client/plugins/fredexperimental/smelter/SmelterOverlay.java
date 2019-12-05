package net.runelite.client.plugins.fredexperimental.smelter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

public class SmelterOverlay extends Overlay
{
	private final SmelterPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	public SmelterOverlay(final SmelterPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (plugin.getLocation() == null)
		{
			return null;
		}
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Smelting Bot")
			.color(Color.MAGENTA)
			.build());
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableComponent.addRow("Enabled:", plugin.isEnabled() + "");
		tableComponent.addRow("Location:", plugin.getLocation().name());
		tableComponent.addRow("Producing:", plugin.getLocation().name());
		panelComponent.getChildren().add(tableComponent);
		return panelComponent.render(graphics);
	}
}