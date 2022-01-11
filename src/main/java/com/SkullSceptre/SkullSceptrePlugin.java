/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2018, Hydrox6 <ikada@protonmail.ch>
 * Copyright (c) 2019, Aleios <https://github.com/aleios>
 * Copyright (c) 2020, Unmoon <https://github.com/unmoon>
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
package com.SkullSceptre;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
		name = "Skull Sceptre",
		description = "Show number of item charges remaining",
		tags = {"skull sceptre"}
)
@Slf4j
public class SkullSceptrePlugin extends Plugin
{
	private static final Pattern SKULL_SCEPTRE_CHECK_PATTERN = Pattern.compile(
			"Your Skull Sceptre has (\\d+) charges? left\\.");
	private static final Pattern SKULL_SCEPTRE_EMPTY_PATTERN = Pattern.compile(
			"Your imbued Skull Sceptre has run out of charges. You must use some more sceptre pieces or fragments on it to recharge it.");
	private static final int MIN_SKULL_SCEPTRE_CHARGES = 0;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemChargeOverlay overlay;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private Notifier notifier;

	@Inject
	private SkullSceptreConfig config;

	// Limits destroy callback to once per tick
	private int lastCheckTick;
	private final Map<EquipmentInventorySlot, ItemChargeInfobox> infoboxes = new EnumMap<>(EquipmentInventorySlot.class);

	@Provides
	SkullSceptreConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SkullSceptreConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		infoBoxManager.removeIf(ItemChargeInfobox.class::isInstance);
		infoboxes.clear();
		lastCheckTick = -1;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(SkullSceptreConfig.GROUP))
		{
			return;
		}

		clientThread.invoke(this::updateInfoboxes);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() == ChatMessageType.GAMEMESSAGE || event.getType() == ChatMessageType.SPAM)
		{
			String message = Text.removeTags(event.getMessage());

			Matcher skullSceptreCheckMatcher = SKULL_SCEPTRE_CHECK_PATTERN.matcher(message);
			Matcher skullSceptreEmptyMatcher = SKULL_SCEPTRE_EMPTY_PATTERN.matcher(message);

			if (skullSceptreEmptyMatcher.find())
			{
				if (config.skullSceptreNotification())
				{
					notifier.notify("Your skull sceptre is empty.");
				}

				updateSkullSceptreCharges(MIN_SKULL_SCEPTRE_CHARGES);
			}

			else if (skullSceptreCheckMatcher.find())
			{
				updateSkullSceptreCharges(Integer.parseInt(skullSceptreCheckMatcher.group(1)));
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.EQUIPMENT.getId())
		{
			return;
		}

		updateInfoboxes();
	}

	@Subscribe
	private void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (!"destroyOnOpKey".equals(event.getEventName()))
		{
			return;
		}

		final int yesOption = client.getIntStack()[client.getIntStackSize() - 1];
		if (yesOption == 1)
		{
			checkDestroyWidget();
		}
	}

	private void updateSkullSceptreCharges(final int value)
	{
		setItemCharges(SkullSceptreConfig.KEY_SKULL_SCEPTRE_I, value);
		updateInfoboxes();
	}



	private void checkDestroyWidget()
	{
		final int currentTick = client.getTickCount();
		if (lastCheckTick == currentTick)
		{
			return;
		}
		lastCheckTick = currentTick;

		final Widget widgetDestroyItemName = client.getWidget(WidgetInfo.DESTROY_ITEM_NAME);
		if (widgetDestroyItemName == null)
		{
			return;
		}
	}

	private void updateInfoboxes()
	{
		final ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);

		if (itemContainer == null)
		{
			return;
		}

		final Item[] items = itemContainer.getItems();
		boolean showInfoboxes = config.showInfoboxes();
		for (EquipmentInventorySlot slot : EquipmentInventorySlot.values())
		{
			if (slot.getSlotIdx() >= items.length)
			{
				break;
			}

			Item i = items[slot.getSlotIdx()];
			int id = i.getId();
			ItemChargeType type = null;
			int charges = -1;

			final ItemWithCharge itemWithCharge = ItemWithCharge.findItem(id);
			if (itemWithCharge != null)
			{
				type = itemWithCharge.getType();
				charges = itemWithCharge.getCharges();
			}
			else
			{
				final SkullSceptreEnum itemWithConfig = SkullSceptreEnum.findItem(id);
				if (itemWithConfig != null)
				{
					type = itemWithConfig.getType();
					charges = getItemCharges(itemWithConfig.getConfigKey());
				}
			}

			boolean enabled = type != null && type.getEnabled().test(config);

			if (showInfoboxes && enabled && charges > 0)
			{
				ItemChargeInfobox infobox = infoboxes.get(slot);
				if (infobox != null)
				{
					if (infobox.getItem() == id)
					{
						if (infobox.getCount() == charges)
						{
							continue;
						}

						log.debug("Updating infobox count for {}", infobox);
						infobox.setCount(charges);
						continue;
					}

					log.debug("Rebuilding infobox {}", infobox);
					infoBoxManager.removeInfoBox(infobox);
					infoboxes.remove(slot);
				}

				final String name = itemManager.getItemComposition(id).getName();
				final BufferedImage image = itemManager.getImage(id);
				infobox = new ItemChargeInfobox(this, image, name, charges, id, slot);
				infoBoxManager.addInfoBox(infobox);
				infoboxes.put(slot, infobox);
			}
			else
			{
				ItemChargeInfobox infobox = infoboxes.remove(slot);
				if (infobox != null)
				{
					log.debug("Removing infobox {}", infobox);
					infoBoxManager.removeInfoBox(infobox);
				}
			}
		}
	}

	int getItemCharges(String key)
	{
		// Migrate old non-profile configurations
		Integer i = configManager.getConfiguration(SkullSceptreConfig.GROUP, key, Integer.class);
		if (i != null)
		{
			configManager.unsetConfiguration(SkullSceptreConfig.GROUP, key);
			configManager.setRSProfileConfiguration(SkullSceptreConfig.GROUP, key, i);
			return i;
		}

		i = configManager.getRSProfileConfiguration(SkullSceptreConfig.GROUP, key, Integer.class);
		return i == null ? -1 : i;
	}

	private void setItemCharges(String key, int value)
	{
		configManager.setRSProfileConfiguration(SkullSceptreConfig.GROUP, key, value);
	}

	Color getColor(int charges)
	{
		Color color = Color.WHITE;
		if (charges <= config.veryLowWarning())
		{
			color = config.veryLowWarningColor();
		}
		else if (charges <= config.lowWarning())
		{
			color = config.lowWarningolor();
		}
		return color;
	}
}
