/*
 * Copyright (c) 2019, tha23rd <https://https://github.com/tha23rd>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.fred.api.lua.library.craftingInterface;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.QueryResults;
import net.runelite.api.queries.WidgetItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

public class CraftingInterfaceQuery extends WidgetItemQuery
{

	private static final Map<Integer, WidgetInfo> widgetIdMap = new HashMap<>();

	@Override
	public QueryResults<WidgetItem> result(Client client)
	{
		Collection<WidgetItem> widgetItems = getOptions(client);
		return new QueryResults<>(widgetItems.stream()
			.filter(Objects::nonNull)
			.filter(predicate)
			.collect(Collectors.toList()));
	}

	private Collection<WidgetItem> getOptions(Client client)
	{
		Collection<WidgetItem> widgetItems = new ArrayList<>();
		Widget dialog = client.getWidget(270, 13);

		if (dialog != null && !dialog.isHidden())
		{
			dialog.revalidate();
			//debugWidget(dialog);
			Widget[] options = dialog.getStaticChildren();
			if (options != null)
			{
				for (int i = 0; i < options.length; i++)
				{
					Widget child = options[i];
					//System.out.println("child: " + (child != null ? child.toString() : "null"));
					if (child != null && child.getName() != null && child.getName().length() > 0)
					{
						// set bounds to same size as default inventory
						widgetItems.add(new WidgetItem(child.getId(), child.getItemQuantity(), i - 1, child.getBounds(), child, false));
					}
				}
			}
		}
		return widgetItems;
	}

	private static WidgetInfo getWidgetInfo(int packedId)
	{
		if (widgetIdMap.isEmpty())
		{
			//Initialize map here so it doesn't create the index
			//until it's actually needed.
			WidgetInfo[] widgets = WidgetInfo.values();
			for (WidgetInfo w : widgets)
			{
				widgetIdMap.put(w.getPackedId(), w);
			}
		}

		return widgetIdMap.get(packedId);
	}


	private String getWidgetName(int packed)
	{
		String str = Integer.toString(packed);
		WidgetInfo info = getWidgetInfo(packed);
		if (info != null)
		{
			str += " [" + info.name() + "]";
		}

		return str;
	}

	private void debugWidget(Widget w)
	{
		if (w == null) return;
		boolean foundChildren = false;
		StringBuilder result = new StringBuilder(String.format("WidgetDebug -> %s {", getWidgetName(w.getId())));
		for (Widget c : w.getStaticChildren())
		{
			if (c == null || (c.getName().equals("") && c.getText().equals(""))) continue;
			result.append(String.format("\n\t(s) %s [%s] <%s> -> %s", getWidgetName(c.getId()), WidgetInfo.TO_CHILD(c.getId()) + "", c.getId() + "", c.getName().equals("") ? c.getText() : c.getName()));
			foundChildren = true;
		}
		Widget[] dChilds = w.getDynamicChildren();
		for (int i = 0; i < dChilds.length; i++)
		{
			if (dChilds[i] == null || (dChilds[i].getName().equals("") && dChilds[i].getText().equals(""))) continue;
			result.append(String.format("\n\t(d) %s[%d] -> %s", getWidgetName(w.getId()), i, (dChilds[i].getName().equals("") ? dChilds[i].getText() : dChilds[i].getName())));
			foundChildren = true;
		}
		result.append("\n}");
		if (foundChildren)
		{
			System.out.println(result.toString());
		}
	}

	public WidgetItemQuery textContains(String... texts)
	{
		predicate = and(widgetItem ->
		{
			for (String text : texts)
			{
				if (widgetItem.getWidget() != null)
				{
					String widgetText = widgetItem.getWidget().getName();
					if (widgetText != null && widgetText.contains(text))
					{
						return true;
					}
				}
			}
			return false;
		});
		return this;
	}
}
