package net.runelite.client.plugins.fred.oneclick.matchers.twisted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.MenuOpcode;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "from")
@ToString
@EqualsAndHashCode
class _ItemObjEntry
{
	@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
	@RequiredArgsConstructor(staticName = "create")
	@ToString
	@EqualsAndHashCode
	static class _ItemObjEntryBuilder
	{
		private final List<Integer> obj_ids = new ArrayList<>();
		private final List<Integer> npc_ids = new ArrayList<>();
		private final List<Integer> items_ids = new ArrayList<>();
		private final String name;
		private final int opCode;

		public _ItemObjEntry build()
		{
			final int[] objs = obj_ids.stream().mapToInt(f -> f).toArray();
			final int[] npcs = npc_ids.stream().mapToInt(f -> f).toArray();
			final int[] items = items_ids.stream().mapToInt(f -> f).toArray();
			return _ItemObjEntry.from(name, opCode, objs, npcs, items);
		}

		public _ItemObjEntryBuilder withObject(int id)
		{
			if (!this.obj_ids.contains(id))
			{
				this.obj_ids.add(id);
			}
			return this;
		}

		public _ItemObjEntryBuilder withItem(int id)
		{
			if (!this.items_ids.contains(id))
			{
				this.items_ids.add(id);
			}
			return this;
		}

		public _ItemObjEntryBuilder withNPC(int id)
		{
			if (!this.npc_ids.contains(id))
			{
				this.npc_ids.add(id);
			}
			return this;
		}

		public _ItemObjEntryBuilder withObjects(int ... ids)
		{
			for (int id : ids)
			{
				if (!this.obj_ids.contains(id))
				{
					this.obj_ids.add(id);
				}
			}
			return this;
		}

		public _ItemObjEntryBuilder withItems(int ... ids)
		{
			for (int id : ids)
			{
				if (!this.items_ids.contains(id))
				{
					this.items_ids.add(id);
				}
			}
			return this;
		}

		public _ItemObjEntryBuilder withNPCs(int ... ids)
		{
			for (int id : ids)
			{
				if (!this.npc_ids.contains(id))
				{
					this.npc_ids.add(id);
				}
			}
			return this;
		}
	}

	@Getter
	private final String name;
	@Getter
	private final int opCode;
	@Getter
	private final int[] objIds;
	@Getter
	private final int[] npcIds;
	@Getter
	private final int[] itemIds;

	public static _ItemObjEntryBuilder builder(String name, MenuOpcode opcode)
	{
		return _ItemObjEntryBuilder.create(name, opcode.getId());
	}

	public boolean matches(MenuEntry entry, Client client)
	{
		if(opCode != entry.getOpcode())
		{
			return false;
		}
		else if (opCode == MenuOpcode.GAME_OBJECT_FIRST_OPTION.getId())
		{
			return Arrays.stream(objIds).anyMatch(j -> j == entry.getIdentifier());
		}
		else if (opCode == MenuOpcode.NPC_FIRST_OPTION.getId())
		{
			return Arrays.stream(npcIds).anyMatch(j -> j == client.getCachedNPCs()[entry.getIdentifier()].getId());
		}
		else
		{
			return false;
		}
	}
}
