package net.runelite.client.plugins.fred.wintertodt;

import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.components.table.TableAlignment;
import net.runelite.client.ui.overlay.components.table.TableComponent;
import net.runelite.client.util.ColorUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

import static net.runelite.client.plugins.fred.wintertodt.FredWinterPlugin.WINTERTODT_KINDLING_MULTIPLIER;
import static net.runelite.client.plugins.fred.wintertodt.FredWinterPlugin.WINTERTODT_ROOTS_MULTIPLIER;

@Singleton
public class FredWinterOverlay extends Overlay
{
	@Inject
	private Client client;
	private static float[] green = Color.GREEN.getRGBColorComponents(null);
	private static float[] red = Color.RED.getRGBColorComponents(null);
//	private static final Color[] colors = {Color.GREEN, Color.RED};
	private static Color getPercentColor(int i)
	{
		float p1 = (float) i / 100.0f;
		return new Color(green[0] * p1 + red[0] * (1.0f - p1), green[1] * p1 + red[1] * (1.0f - p1), green[2] * p1 + red[2] * (1.0f - p1));
	}

	private final FredWinterPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	private FredWinterOverlay(final FredWinterPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		setPosition(OverlayPosition.BOTTOM_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.isInWintertodt())
		{
			return null;
		}

		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(180, 0));

		panelComponent.getChildren().add(TitleComponent.builder()
				.text("Points in inventory")
				.color(Color.WHITE)
				.build());

		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		tableComponent.addRow(ColorUtil.prependColorTag("Status:", Color.WHITE), ColorUtil.prependColorTag(plugin.getCurrentActivity().getActionString(), plugin.getCurrentActivity() == FredWinterActivity.IDLE ? Color.RED : Color.GREEN));
		tableComponent.addRow(ColorUtil.prependColorTag("Health:", Color.WHITE), ColorUtil.prependColorTag(Integer.toString(plugin.getPercent_left()), getPercentColor(plugin.getPercent_left())));
		tableComponent.addRow(ColorUtil.prependColorTag("Points:", Color.WHITE), ColorUtil.prependColorTag(Integer.toString(plugin.getPoints()), plugin.getPoints() < 500 ? Color.RED : Color.GREEN));

		int firemakingLvl = client.getRealSkillLevel(Skill.FIREMAKING);

		int rootsScore = (plugin.getNumRoots() * WINTERTODT_ROOTS_MULTIPLIER);
		int rootsXp = plugin.getNumRoots() * Math.round(2 + (3 * firemakingLvl));

		tableComponent.addRow(ColorUtil.prependColorTag("Roots:", Color.WHITE), ColorUtil.prependColorTag(rootsScore + " pts (" + rootsXp + " xp)", plugin.getNumRoots() > 0 ? Color.GREEN : Color.RED));

		int kindlingScore = (plugin.getNumKindling() * WINTERTODT_KINDLING_MULTIPLIER);
		long kindlingXp = plugin.getNumKindling() * Math.round(3.8 * firemakingLvl);

		tableComponent.addRow(ColorUtil.prependColorTag("Kindling:", Color.WHITE), ColorUtil.prependColorTag(kindlingScore + " pts (" + kindlingXp + " xp)", plugin.getNumKindling() > 0 ? Color.GREEN : Color.RED));
		tableComponent.addRow(ColorUtil.prependColorTag("Total:", Color.WHITE), ColorUtil.prependColorTag((rootsScore + kindlingScore) + " pts (" + (rootsXp + kindlingXp) + " xp)", (rootsScore + kindlingScore > 0) ? Color.GREEN : Color.RED));

		panelComponent.getChildren().add(tableComponent);

		return panelComponent.render(graphics);
	}
}
