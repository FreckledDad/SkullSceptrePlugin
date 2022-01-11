package com.SkullSceptre;


import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
enum ItemChargeType
{
    INVOKE(SkullSceptreConfig::showSkullSceptreCount);



    private final Predicate<SkullSceptreConfig> enabled;
}