package com.SkullSceptre;


import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.ui.overlay.components.TextComponent;

import javax.inject.Inject;
import java.awt.*;

class ItemChargeOverlay extends WidgetItemOverlay
{
    private final SkullSceptrePlugin SkullSceptrePlugin;
    private final SkullSceptreConfig config;

    @Inject
    ItemChargeOverlay(SkullSceptrePlugin SkullSceptrePlugin, SkullSceptreConfig config)
    {
        this.SkullSceptrePlugin = SkullSceptrePlugin;
        this.config = config;
        showOnInventory();
        showOnEquipment();
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        int charges;
        SkullSceptreEnum itemWithConfig = SkullSceptreEnum.findItem(itemId);
        if (itemWithConfig != null)
        {
            if (!itemWithConfig.getType().getEnabled().test(config))
            {
                return;
            }

            charges = SkullSceptrePlugin.getItemCharges(itemWithConfig.getConfigKey());
        }
        else
        {
            ItemWithCharge chargeItem = ItemWithCharge.findItem(itemId);
            if (chargeItem == null)
            {
                return;
            }

            ItemChargeType type = chargeItem.getType();
            if (!type.getEnabled().test((SkullSceptreConfig) config))
            {
                return;
            }

            charges = chargeItem.getCharges();
        }

        graphics.setFont(FontManager.getRunescapeSmallFont());

        final Rectangle bounds = widgetItem.getCanvasBounds();
        final TextComponent textComponent = new TextComponent();
        textComponent.setPosition(new Point(bounds.x - 1, bounds.y + 15));
        textComponent.setText(charges < 0 ? "?" : String.valueOf(charges));
        textComponent.setColor(SkullSceptrePlugin.getColor(charges));
        textComponent.render(graphics);
    }
}
