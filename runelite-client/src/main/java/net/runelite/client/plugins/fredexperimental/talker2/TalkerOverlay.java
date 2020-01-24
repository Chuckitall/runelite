package net.runelite.client.plugins.fredexperimental.talker2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by npruff on 9/2/2019.
 */

@Singleton
public class TalkerOverlay extends Overlay
{
	private final TalkerCore plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	private TalkerOverlay(final TalkerCore plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.BOTTOM_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(180, 0));

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Freds Talker 2")
			.color(Color.WHITE)
			.build());

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		panelComponent.getChildren().add(tableComponent);

		return panelComponent.render(graphics);
	}
}