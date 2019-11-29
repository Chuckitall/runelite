package net.runelite.client.plugins.fred.fighter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by npruff on 8/24/2019.
 */

@AllArgsConstructor
@Getter(AccessLevel.PACKAGE)
public enum TaskEnum
{
	INVALID(),
	NONE(),
	DELAY_SHORT(),
	DELAY_LONG(),
	OPEN_INV(),
	EAT_FOOD(),
	DRINK_ANTIPOISON(),
	TELEPORT_INV(),
	OPEN_EQUIP(),
	TELEPORT_EQUIP(),
	GET_TARGET(),
	WALK_CLOSER_TO_TARGET(),
	TURN_CAMERA_TO_TARGET(),
	ATTACK_TARGET()
}
