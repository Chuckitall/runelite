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
public class FighterOverlayPanel extends Overlay
{
	private final PanelComponent panelComponent = new PanelComponent();
	private FighterPlugin plugin;

	@Inject
	FighterOverlayPanel(FighterPlugin plugin)
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
				.text("Freds Fighter")
				.color(Color.WHITE)
				.build());

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
		tableComponent.addRow("Enabled:", plugin.isScriptEnabled() + "");
		tableComponent.addRow("Paused", plugin.isScriptPaused() + "");
		tableComponent.addRow("Interacting:", plugin.getFightingWith() == null ? "null" : (plugin.getFightingWith().hashCode() + "->[" + plugin.getFightingWith().getName() + " - " + plugin.getFightingWith().getCombatLevel() + "]"));
		tableComponent.addRow("Target:", plugin.getTarget() == null ? "null" : (plugin.getTarget().hashCode() + "->[" + plugin.getTarget().getName() + " - " + plugin.getTarget().getCombatLevel() + "]"));
		tableComponent.addRow("Angle:", "" + plugin.getCamera().getAngleToTarget(plugin.getTarget()));
		tableComponent.addRow("Tick:", "" + plugin.getTickFightingWith());
		tableComponent.addRow("Action:", "" + plugin.getTask().name());

		panelComponent.getChildren().add(tableComponent);
		return panelComponent.render(graphics);
	}
}
