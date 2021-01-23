/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.dict.data;

import io.github.harunobot.plugin.dict.data.type.RecordType;
import java.util.List;

/**
 *
 * @author iTeam_VEP
 */
public class DictionaryRecord {
    private RecordType record;
    private String text;
    private String file;
    private String url;
    private List<List<DictionaryRecord>> entry;

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the entry
     */
    public List<List<DictionaryRecord>> getEntry() {
        return entry;
    }

    /**
     * @param entry the entry to set
     */
    public void setEntry(List<List<DictionaryRecord>> entry) {
        this.entry = entry;
    }

    /**
     * @return the record
     */
    public RecordType getRecord() {
        return record;
    }

    /**
     * @param record the record to set
     */
    public void setRecord(RecordType record) {
        this.record = record;
    }

}
