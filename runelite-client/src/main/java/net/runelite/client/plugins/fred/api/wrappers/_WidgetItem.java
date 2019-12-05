package net.runelite.client.plugins.fred.api.wrappers;


import java.awt.Rectangle;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.plugins.fred.api.interfaces.I_Item;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "from")
@ToString
@EqualsAndHashCode
public class _WidgetItem implements I_Item
{
	private WidgetItem wrapped;

	@Override
	public int getId()
	{
		return wrapped.getId();
	}

	@Override
	public int getQty()
	{
		return wrapped.getQuantity();
	}

	@Override
	public int getIdx()
	{
		return wrapped.getIndex();
	}

	public Rectangle getCanvasBounds(boolean recompute)
	{
		if (recompute)
		{
			wrapped.getWidget().revalidate();
		}
		return wrapped.getCanvasBounds();
	}
}