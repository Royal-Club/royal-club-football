package com.bjit.royalclub.royalclubfootball.util;

import com.bjit.royalclub.royalclubfootball.enums.PositionGroup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The formation shapes a captain can pick from, keyed by players per side.
 *
 * <p>A preset is stored only as its outfield line-up — "2-2-1" means two at the
 * back, two in midfield, one up top, plus the keeper. Slot co-ordinates are
 * derived from that, so adding a shape is a one-line change and the numbers
 * cannot drift out of step with the name.
 *
 * <p>The same derivation is mirrored in the front-end (`formationPresets.ts`);
 * keep the two in step if the geometry changes.
 */
public final class FormationPresets {

    /** Pitch co-ordinates, in percent, matching the front-end pitch box. */
    private static final BigDecimal GK_X = bd(50);
    private static final BigDecimal GK_Y = bd(90);
    /** Backmost outfield line sits here; the front line at {@link #FRONT_LINE_Y}. */
    private static final double BACK_LINE_Y = 74;
    private static final double FRONT_LINE_Y = 18;
    private static final double SINGLE_LINE_Y = 48;
    /** Widest gap between two neighbours in a line, and the usable width. */
    private static final double MAX_SPACING = 30;
    private static final double USABLE_WIDTH = 76;

    private FormationPresets() {
    }

    /** Outfield lines, back to front, for every supported squad size. */
    private static final Map<Integer, List<int[]>> SHAPES_BY_TEAM_SIZE = new LinkedHashMap<>();

    static {
        SHAPES_BY_TEAM_SIZE.put(4, List.of(new int[]{1, 1, 1}, new int[]{2, 1}, new int[]{1, 2}));
        SHAPES_BY_TEAM_SIZE.put(5, List.of(new int[]{1, 2, 1}, new int[]{2, 1, 1}, new int[]{2, 2}));
        SHAPES_BY_TEAM_SIZE.put(6, List.of(new int[]{2, 2, 1}, new int[]{2, 1, 2}, new int[]{1, 2, 2},
                new int[]{1, 3, 1}, new int[]{3, 1, 1}));
        SHAPES_BY_TEAM_SIZE.put(7, List.of(new int[]{2, 3, 1}, new int[]{3, 2, 1}, new int[]{2, 2, 2}));
        SHAPES_BY_TEAM_SIZE.put(8, List.of(new int[]{3, 3, 1}, new int[]{3, 2, 2}, new int[]{2, 3, 2},
                new int[]{1, 3, 3}));
        SHAPES_BY_TEAM_SIZE.put(9, List.of(new int[]{3, 3, 2}, new int[]{4, 3, 1}, new int[]{3, 4, 1}));
        SHAPES_BY_TEAM_SIZE.put(10, List.of(new int[]{4, 3, 2}, new int[]{3, 4, 2}, new int[]{4, 4, 1}));
        SHAPES_BY_TEAM_SIZE.put(11, List.of(new int[]{4, 3, 3}, new int[]{4, 4, 2}, new int[]{3, 5, 2}));
    }

    /**
     * A single pitch slot in a preset. Not an entity — the service copies these
     * onto {@code TeamFormationSlot} rows when it seeds a formation.
     */
    @Getter
    @RequiredArgsConstructor
    public static class PresetSlot {
        private final int slotIndex;
        private final PositionGroup positionGroup;
        private final String slotLabel;
        private final BigDecimal x;
        private final BigDecimal y;
    }

    public static boolean isSupportedTeamSize(int teamSize) {
        return SHAPES_BY_TEAM_SIZE.containsKey(teamSize);
    }

    /** Preset names for a squad size, e.g. {@code ["2-2-1", "2-1-2", "1-2-2"]}. */
    public static List<String> presetNames(int teamSize) {
        List<int[]> shapes = SHAPES_BY_TEAM_SIZE.get(teamSize);
        if (shapes == null) {
            return List.of();
        }
        return shapes.stream().map(FormationPresets::shapeName).toList();
    }

    /** The first preset for a size — what a brand-new formation starts as. */
    public static String defaultPresetName(int teamSize) {
        List<String> names = presetNames(teamSize);
        return names.isEmpty() ? null : names.get(0);
    }

    /**
     * Pitch slots for a named preset, goalkeeper first then back to front.
     *
     * @return an empty list when the name is not a preset for that squad size
     */
    public static List<PresetSlot> slotsFor(int teamSize, String presetName) {
        List<int[]> shapes = SHAPES_BY_TEAM_SIZE.get(teamSize);
        if (shapes == null || presetName == null) {
            return List.of();
        }
        return shapes.stream()
                .filter(shape -> shapeName(shape).equals(presetName.trim()))
                .findFirst()
                .map(FormationPresets::buildSlots)
                .orElse(List.of());
    }

    private static String shapeName(int[] shape) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < shape.length; i++) {
            if (i > 0) {
                name.append('-');
            }
            name.append(shape[i]);
        }
        return name.toString();
    }

    private static List<PresetSlot> buildSlots(int[] shape) {
        List<PresetSlot> slots = new ArrayList<>();
        slots.add(new PresetSlot(0, PositionGroup.GK, "GK", GK_X, GK_Y));

        int lineCount = shape.length;
        int slotIndex = 1;
        for (int line = 0; line < lineCount; line++) {
            PositionGroup group = groupForLine(line, lineCount);
            double y = lineY(line, lineCount);
            int inLine = shape[line];
            for (int seat = 0; seat < inLine; seat++) {
                String label = inLine > 1 ? group.name() + " " + (seat + 1) : group.name();
                slots.add(new PresetSlot(slotIndex++, group, label, bd(seatX(seat, inLine)), bd(y)));
            }
        }
        return slots;
    }

    private static PositionGroup groupForLine(int line, int lineCount) {
        if (lineCount == 1) {
            return PositionGroup.MID;
        }
        if (line == 0) {
            return PositionGroup.DEF;
        }
        return line == lineCount - 1 ? PositionGroup.FWD : PositionGroup.MID;
    }

    private static double lineY(int line, int lineCount) {
        if (lineCount == 1) {
            return SINGLE_LINE_Y;
        }
        double step = (BACK_LINE_Y - FRONT_LINE_Y) / (lineCount - 1);
        return BACK_LINE_Y - (line * step);
    }

    /** Spreads a line evenly around the centre, never wider than the pitch box. */
    private static double seatX(int seat, int inLine) {
        if (inLine <= 1) {
            return 50;
        }
        double spacing = Math.min(MAX_SPACING, USABLE_WIDTH / inLine);
        return 50 + ((seat - ((inLine - 1) / 2.0)) * spacing);
    }

    private static BigDecimal bd(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
