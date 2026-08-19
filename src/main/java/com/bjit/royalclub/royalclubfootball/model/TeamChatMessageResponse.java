package com.bjit.royalclub.royalclubfootball.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** One message as rendered in the room, and as broadcast over the socket. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamChatMessageResponse {

    private Long id;
    private Long teamId;
    private Long senderId;
    private String senderName;
    private String senderPhotoUrl;
    private String body;
    private LocalDateTime sentAt;
    private List<TeamChatAttachmentResponse> attachments;
}
