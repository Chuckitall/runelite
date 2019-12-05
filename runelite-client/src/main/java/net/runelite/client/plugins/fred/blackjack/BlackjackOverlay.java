package net.runelite.client.plugins.fred.blackjack;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

/**
 * Created by npruff on 7/30/2019.
 */
@Singleton
public class BlackjackOverlay extends Overlay
{
	private final FredsBlackjackPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	public BlackjackOverlay(final FredsBlackjackPlugin plugin)
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
			tableComponent.addRow("seqFailed:", Integer.toString(plugin.seqFailed));
			tableComponent.addRow("state:", Integer.toString(plugin.state));
			tableComponent.addRow("target:", Integer.toString(plugin.getTarget() != null ? plugin.getTarget().getIndex() : -1));
			panelComponent.getChildren().add(tableComponent);
		}
		return panelComponent.render(graphics);
	}
}
