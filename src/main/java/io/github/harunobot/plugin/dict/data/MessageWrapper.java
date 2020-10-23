/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.dict.data;

import io.github.harunobot.proto.event.BotMessage;

/**
 *
 * @author iTeam_VEP
 */
public class MessageWrapper {
    private BotMessage[] messages;
    private String name;
    private boolean limited;
    
    public MessageWrapper(){}
    
    public MessageWrapper(String name, BotMessage[] messages, boolean limited){
        this.name = name;
        this.messages = messages;
        this.limited = limited;
    }

    /**
     * @return the messages
     */
    public BotMessage[] getMessages() {
        return messages;
    }

    /**
     * @param messages the messages to set
     */
    public void setMessages(BotMessage[] messages) {
        this.messages = messages;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the limited
     */
    public boolean isLimited() {
        return limited;
    }

    /**
     * @param limited the limited to set
     */
    public void setLimited(boolean limited) {
        this.limited = limited;
    }

}
