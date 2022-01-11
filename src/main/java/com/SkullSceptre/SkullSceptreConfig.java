/*
 * Copyright (c) 2017, Devin French <https://github.com/devinfrench>
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

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(SkullSceptreConfig.GROUP)
public interface SkullSceptreConfig extends Config
{
	String GROUP = "itemCharge";
	String KEY_SKULL_SCEPTRE_I = "skullSceptre(i)";

	@ConfigSection(
			name = "Charge Settings",
			description = "Configuration for which charges should be displayed",
			position = 98
	)
	String chargesSection = "charges";

	@ConfigSection(
			name = "Notification Settings",
			description = "Configuration for notifications",
			position = 99
	)
	String notificationSection = "notifications";

	@ConfigItem(
			keyName = "veryLowWarningColor",
			name = "Very Low Warning",
			description = "The color of the overlay when charges are very low",
			position = 1
	)
	default Color veryLowWarningColor()
	{
		return Color.RED;
	}

	@ConfigItem(
			keyName = "lowWarningColor",
			name = "Low Warning",
			description = "The color of the overlay when charges are low",
			position = 2
	)
	default Color lowWarningolor()
	{
		return Color.YELLOW;
	}

	@ConfigItem(
			keyName = "veryLowWarning",
			name = "Very Low Warning",
			description = "The charge count for the very low warning color",
			position = 3
	)
	default int veryLowWarning()
	{
		return 1;
	}

	@ConfigItem(
			keyName = "lowWarning",
			name = "Low Warning",
			description = "The charge count for the low warning color",
			position = 4
	)
	default int lowWarning()
	{
		return 2;
	}

	@ConfigItem(
			keyName = "showInfoboxes",
			name = "Infoboxes",
			description = "Show an infobox with remaining charges for equipped items",
			position = 24
	)
	default boolean showInfoboxes()
	{
		return false;
	}

	@ConfigItem(
			keyName = "showTeleportCharges",
			name = "Teleport Charges",
			description = "Show teleport item charge counts",
			position = 5,
			section = chargesSection
	)
	default boolean showTeleportCharges()
	{
		return true;
	}



	@ConfigItem(
			keyName = "showSceptreCount",
			name = "Skull sceptre Count",
			description = "Show Skull sceptre(i) charges",
			position = 30,
			section = chargesSection
	)
	default boolean showSkullSceptreCount()
	{
		return true;
	}

	@ConfigItem(
			keyName = "sceptreNotification",
			name = "Skull sceptre Notification",
			description = "Send a notification when a Skull sceptre is low",
			position = 31,
			section = notificationSection
	)
	default boolean skullSceptreNotification()
	{
		return true;
	}
}
