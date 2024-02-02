package me.lethinh.oredrop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class OreState {
    private final String oreName;
    private long lastBroken;
}
