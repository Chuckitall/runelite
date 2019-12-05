package net.runelite.client.plugins.fred.fighter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

@Singleton
@Slf4j
public class FighterOverlayDebug extends Overlay
{
	private final PanelComponent panelComponent = new PanelComponent();
	private FighterPlugin plugin;

	@Inject
	FighterOverlayDebug(FighterPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.BOTTOM_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(380, 0));
		panelComponent.getChildren().add(TitleComponent.builder()
				.text("Camera Debug")
				.color(Color.WHITE)
				.build());

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableComponent.addRow("Target:", plugin.getTarget() == null ? "null" : (plugin.getTarget().getName() + " - " + plugin.getTarget().getCombatLevel()));
		tableComponent.addRow("Camera Pitch: {}", "" + plugin.getCamera().getPitch());
		tableComponent.addRow("Camera Yaw: {}", "" + plugin.getCamera().getYaw());
		tableComponent.addRow("Target Yaw: {}", "" + plugin.getCamera().getAngleOfTarget(plugin.getTarget()));
		tableComponent.addRow("Delta  Yaw: {}", "" + plugin.getCamera().getAngleToTarget(plugin.getTarget()));

		panelComponent.getChildren().add(tableComponent);
		return panelComponent.render(graphics);
	}
}
