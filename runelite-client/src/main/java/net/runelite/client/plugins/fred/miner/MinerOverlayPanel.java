package net.runelite.client.plugins.fred.miner;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.plugins.fred.util.WidgetUtils;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MinerOverlayPanel extends Overlay
{
	private final MinerPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();


	@Inject
	private MinerOverlayPanel(final MinerPlugin plugin)
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
				.text("Freds Mining")
				.color(Color.WHITE)
				.build());

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		tableComponent.addRow(ColorUtil.prependColorTag("Status:", Color.WHITE), ColorUtil.prependColorTag(plugin.getCurrentActivity().getActionString(), plugin.getCurrentActivity() == MinerActivity.IDLE ? Color.RED : Color.GREEN));
		tableComponent.addRow(ColorUtil.prependColorTag("Ore:", Color.WHITE), ColorUtil.prependColorTag(Integer.toString(plugin.getOresCount()), Color.BLUE));
		tableComponent.addRow(ColorUtil.prependColorTag("Empty:", Color.WHITE), ColorUtil.prependColorTag(Integer.toString(WidgetUtils.getFreeInventorySpaces(plugin.getClient())), Color.BLUE));
		tableComponent.addRow(ColorUtil.prependColorTag("Region:", Color.WHITE), ColorUtil.prependColorTag(Integer.toString(plugin.getRegionID()), Color.BLUE));
		tableComponent.addRow(ColorUtil.prependColorTag("Pickaxe:", Color.WHITE), ColorUtil.prependColorTag(Boolean.toString(plugin.isHasPickaxe()), Color.BLUE));
		tableComponent.addRow(ColorUtil.prependColorTag("Cloak:", Color.WHITE), ColorUtil.prependColorTag(Boolean.toString(plugin.isHasCloak()), Color.BLUE));
		tableComponent.addRow(ColorUtil.prependColorTag("Jewelery:", Color.WHITE), ColorUtil.prependColorTag(Boolean.toString(plugin.isHasTele()), Color.BLUE));

		panelComponent.getChildren().add(tableComponent);

		return panelComponent.render(graphics);
	}
}
