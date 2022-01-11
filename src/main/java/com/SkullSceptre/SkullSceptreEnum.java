package com.SkullSceptre;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
enum SkullSceptreEnum
{

    SKULL_SCEPTRE_I(ItemID.SKULL_SCEPTRE_I, SkullSceptreConfig.KEY_SKULL_SCEPTRE_I, ItemChargeType.INVOKE);

    private final int itemId;
    private final String configKey;
    private final ItemChargeType type;

    private static final Map<Integer, SkullSceptreEnum> ID_MAP;

    static
    {
        ImmutableMap.Builder<Integer, SkullSceptreEnum> builder = new ImmutableMap.Builder<>();

        for (SkullSceptreEnum item : values())
        {
            builder.put(item.getItemId(), item);
        }

        ID_MAP = builder.build();
    }

    @Nullable
    static SkullSceptreEnum findItem(int itemId)
    {
        return ID_MAP.get(itemId);
    }
}
