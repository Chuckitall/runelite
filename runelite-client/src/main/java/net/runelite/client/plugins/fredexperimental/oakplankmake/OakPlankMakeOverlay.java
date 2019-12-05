package net.runelite.client.plugins.fredexperimental.oakplankmake;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

public class OakPlankMakeOverlay extends Overlay
{
	private final OakPlankMakePlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@javax.inject.Inject
	public OakPlankMakeOverlay(final OakPlankMakePlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Oak Plank Make")
			.color(Color.MAGENTA)
			.build());
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableComponent.addRow("ClickTarget:", plugin.getClickTarget());
		tableComponent.addRow("Animation:", plugin.getAnimation() + "");
		tableComponent.addRow("ClickFlag", plugin.isClick() + "");
		panelComponent.getChildren().add(tableComponent);
		return panelComponent.render(graphics);
	}
}