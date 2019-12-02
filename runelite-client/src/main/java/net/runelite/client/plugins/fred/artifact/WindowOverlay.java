package net.runelite.client.plugins.fred.artifact;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Varbits;
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
@Slf4j
public class WindowOverlay extends Overlay
{
	private final ArtifactPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();

	@Inject
	public WindowOverlay(final ArtifactPlugin plugin)
	{
		super(plugin);
		setPosition(OverlayPosition.TOP_LEFT);
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!plugin.shouldRunPlugin())
		{
			return null;
		}
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Steal Artifact")
			.color(Color.MAGENTA)
			.build());
		TableComponent tableComponent = new TableComponent();
		tableComponent.setColumnAlignments(TableAlignment.LEFT, TableAlignment.RIGHT);

		tableComponent.addRow("State", plugin.getState().getId() + "");
		tableComponent.addRow("State", plugin.getState().getText());
		tableComponent.addRow("varbit", plugin.getClient().getVar(Varbits.ARTIFACT_STATE) + "");

		panelComponent.getChildren().add(tableComponent);
		return panelComponent.render(graphics);
	}
}
