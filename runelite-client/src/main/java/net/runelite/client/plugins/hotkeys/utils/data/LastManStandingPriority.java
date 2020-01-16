package net.runelite.client.plugins.hotkeys.utils.data;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LastManStandingPriority
{
	private List<Integer> headSlotPriorityList;
	private List<Integer> capeSlotPriorityList;
	private List<Integer> neckSlotPriorityList;
	private List<Integer> ammoSlotPriorityList;
	private List<Integer> weaponSlotPriorityList;
	private List<Integer> bodySlotPriorityList;
	private List<Integer> shieldSlotPriorityList;
	private List<Integer> legsSlotPriorityList;
	private List<Integer> glovesSlotPriorityList;
	private List<Integer> bootsSlotPriorityList;
	private List<Integer> ringSlotPriorityList;
}
