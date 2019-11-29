package net.runelite.client.plugins.fred.npctalker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by npruff on 9/2/2019.
 */

@Singleton
public class TalkerOverlay extends Overlay
{
	private final TalkerPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();


	@Inject
	private TalkerOverlay(final TalkerPlugin plugin)
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
			.text("Freds Talker")
			.color(Color.WHITE)
			.build());

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		tableComponent.addRow(ColorUtil.prependColorTag("Name", Color.WHITE), ColorUtil.prependColorTag((plugin.getInteractingWith() != null) ? plugin.getInteractingWith().getName() : "null", (plugin.getInteractingWith() == null) ? Color.RED : Color.GREEN));
		tableComponent.addRow(ColorUtil.prependColorTag("ID", Color.WHITE), ColorUtil.prependColorTag((plugin.getInteractingWith() != null) ? plugin.getInteractingWith().getId() + "" : "-1", (plugin.getInteractingWith() == null) ? Color.RED : Color.GREEN));
		panelComponent.getChildren().add(tableComponent);

		return panelComponent.render(graphics);
	}
}
