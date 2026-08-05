package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamFormationResponse {

    private Long id;
    private Long teamId;
    private String teamName;
    private String teamLogoUrl;

    /** Null on the team's default formation. */
    private Long matchId;
    private Boolean isDefault;

    private String presetName;
    private Integer teamSize;
    private String notes;

    /**
     * Whether the caller may save changes to this formation. Lets the client
     * show the editor without a second round trip to work out permissions.
     */
    private Boolean editable;

    /** Why editing is closed — the match is over, caller is not the captain, etc. */
    private String lockedReason;

    /**
     * False when nothing has been saved yet and these slots are the preset the
     * client is being offered as a starting point.
     */
    private Boolean saved;

    /** Preset names valid for this squad size, so the client need not guess. */
    private List<String> availablePresets;

    private List<TeamFormationSlotResponse> slots;

    /**
     * Everyone signed to the team, whether or not they are in the line-up. The
     * editor picks from this, so it needs no second call to build a sheet.
     */
    private List<TeamPlayerResponse> squad;
}
