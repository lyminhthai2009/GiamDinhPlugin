package com.yogurt.giamdinh.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LevelPerk {
    private final int maxBatchSize;
    private final List<String> permissions;
    private final double rareFindChanceModifier;
    private final double feeReductionModifier;
    private final double xpBoostModifier;

    public static LevelPerk getDefault() {
        return new LevelPerk(1, List.of(), 1.0, 1.0, 1.0);
    }
}
