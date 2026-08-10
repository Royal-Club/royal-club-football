package com.bjit.royalclub.royalclubfootball.service;

import com.bjit.royalclub.royalclubfootball.model.RsvpVoteResponse;

public interface RsvpVoteService {

    /**
     * Describe what a link would do, without recording anything.
     * <p>
     * This is what makes the flow safe against mail scanners and link prefetchers: loading the page
     * has no side effect, so only a real click on the confirm button can cast a vote.
     */
    RsvpVoteResponse preview(String token);

    /**
     * Record the answer carried by the token. Re-usable until kickoff, so a member can change their mind.
     */
    RsvpVoteResponse vote(String token);
}
