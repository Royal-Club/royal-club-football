package com.bjit.royalclub.royalclubfootball.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A message being posted into a team room.
 *
 * <p>Neither field is individually required: a member may send words, a file, or both. "At least
 * one of the two" is checked in the service, where the message can say which, rather than through a
 * class-level constraint that would only report the object as invalid.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamChatMessageRequest {

    @Size(max = 4000, message = "A message can be at most 4000 characters")
    private String body;

    @Valid
    @Size(max = 5, message = "At most 5 files can be attached to one message")
    private List<TeamChatAttachmentRequest> attachments;
}
