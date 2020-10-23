/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.dict.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author iTeam_VEP
 */
public class GroupLimitation {
    @JsonProperty(value="group")
    private long groupId;
    private int duration;
    private int frequency;
    private int alivetime;
    @JsonProperty(value="mute-duration")
    private int muteDuration;

    /**
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * @return the frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /**
     * @return the alivetime
     */
    public int getAlivetime() {
        return alivetime;
    }

    /**
     * @param alivetime the alivetime to set
     */
    public void setAlivetime(int alivetime) {
        this.alivetime = alivetime;
    }

    /**
     * @return the muteDuration
     */
    public int getMuteDuration() {
        return muteDuration;
    }

    /**
     * @param muteDuration the muteDuration to set
     */
    public void setMuteDuration(int muteDuration) {
        this.muteDuration = muteDuration;
    }

    /**
     * @return the groupId
     */
    public long getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
    
}
