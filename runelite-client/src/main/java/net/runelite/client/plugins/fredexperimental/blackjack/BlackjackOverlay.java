package net.runelite.client.plugins.fredexperimental.blackjack;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

/**
 * Created by npruff on 7/30/2019.
 */
@Singleton
public class BlackjackOverlay extends Overlay
{
	private final FredsBlackjackPlugin2 plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	public BlackjackOverlay(final FredsBlackjackPlugin2 plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		if (plugin.shouldRunPlugin())
		{
			panelComponent.getChildren().add(TitleComponent.builder()
					.text("Blackjacking")
					.color(Color.MAGENTA)
					.build());
			TableComponent tableComponent = new TableComponent();
			tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);
			tableComponent.addRow("Mouse move:", plugin.isMoveMouse() + "");
			tableComponent.addRow("Mouse click:", plugin.isClickMouse() + "");
			tableComponent.addRow("state:", Integer.toString(plugin.state));
			tableComponent.addRow("mode:", Integer.toString(plugin.mode));
			tableComponent.addRow("target:", Integer.toString(plugin.getTarget() != null ? plugin.getTarget().getIndex() : -1));
			panelComponent.getChildren().add(tableComponent);
		}
		return panelComponent.render(graphics);
	}
}
