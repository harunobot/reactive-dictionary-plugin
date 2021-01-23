/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.harunobot.plugin.dict;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.harunobot.plugin.HarunoPlugin;
import io.github.harunobot.plugin.PluginHandler;
import io.github.harunobot.plugin.data.PluginDescription;
import io.github.harunobot.plugin.data.PluginHandlerMatcher;
import io.github.harunobot.plugin.data.PluginRegistration;
import io.github.harunobot.pojo.type.Permission;
import io.github.harunobot.plugin.data.type.PluginMatcherType;
import io.github.harunobot.plugin.data.type.PluginReceivedType;
import io.github.harunobot.plugin.data.type.PluginReceivedType;
import io.github.harunobot.plugin.data.type.PluginTextType;
import io.github.harunobot.plugin.dict.configuration.Configuration;
import io.github.harunobot.plugin.dict.configuration.GroupLimitation;
import io.github.harunobot.plugin.dict.data.MessageWrapper;
import io.github.harunobot.plugin.dict.data.DictionaryRecord;
import io.github.harunobot.plugin.dict.data.type.RecordType;
import io.github.harunobot.pojo.BotMessagePojo;
import io.github.harunobot.proto.event.BotEvent;
import io.github.harunobot.proto.event.BotMessage;
import io.github.harunobot.proto.event.type.MessageContentType;
import io.github.harunobot.proto.request.type.RequestType;
import io.github.harunobot.proto.response.BotResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author iTeam_VEP
 */
public class DictionaryPlugin extends HarunoPlugin  {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DictionaryPlugin.class);

    private static final BotMessage NEW_LINE = new BotMessage.Builder().messageType(MessageContentType.TEXT).data("\n").build();
    private static final BotMessage ERROR = new BotMessage.Builder().messageType(MessageContentType.TEXT).data("internal error").build();
    private static final int LIMIT_CACHE_SIZE = 1000;
    private static final String PLUGIN_NAME = "Haruno Reactive Dictionary";
    
    private ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private Map<String, MessageWrapper> caches = new HashMap();
    private Map<String, List<BotMessage[]>> randomCaches = new HashMap();
    private Map<String, Configuration> configurations = new HashMap();
    private Map<String, String> resources = new HashMap();
    private Map<String, String> prefixs = new HashMap();
    private Map<String, GroupLimitation> limitations = new HashMap();
    private Map<String, Cache<Long, Integer>> ratelimit = new ConcurrentHashMap();
    
    private SecureRandom random = new SecureRandom();
    private String path;
    boolean enable = false;
    
    public DictionaryPlugin(){
        super(new PluginDescription.Builder().id("io.github.harunobot.plugin.dict").name(PLUGIN_NAME).version("0.1.0").build());
    }

    @Override
    public PluginRegistration onLoad(String path) throws Exception {
        MDC.put("module", PLUGIN_NAME);
        this.path = path;
        Map<PluginHandlerMatcher, PluginHandler> handlers = new HashMap();
        configure();
        
        Set<Permission> permissions = new HashSet<>(
                Arrays.asList(
                        Permission.TRANSMIT_PUBLIC_MESSAGE
                        , Permission.RECEIVE_PUBLIC_MESSAGE
                        , Permission.MUTE
                )
        );
        prefixs.forEach((name, prefix) -> {
            PluginHandlerMatcher matcher = new PluginHandlerMatcher(
                PluginReceivedType.PUBLIC,
                PluginMatcherType.TEXT,
                PluginTextType.PREFIX,
                prefix
            );
            handlers.put(matcher, (String trait, String message, BotEvent event) -> {
                if(!enable){
                    return null;
                }
                String keyword = event.messages()[0].data().trim();
                if(!caches.containsKey(keyword)){
                    return null;
                }
                MessageWrapper wrapper = caches.get(keyword);
                int alivetime = -1;
                if(wrapper.isLimited() && (alivetime = allow(event, wrapper)) == -1){
                    return null;
                }
//                revokeMuteUser(event);
                if(alivetime > 0){
                    return fastReply(RequestType.MESSAGE_PUBLIC
                            , generateResponse(event, keyword, wrapper.getMessages())
                            , alivetime);
                } else {
                    return fastReply(RequestType.MESSAGE_PUBLIC
                            , generateResponse(event, keyword, wrapper.getMessages()));
                }
            });
        });
        MDC.clear();
        return new PluginRegistration(permissions, handlers, null);
    }
    
    private int allow(BotEvent event, MessageWrapper wrapper){
//        Configuration config = configurations.get(wrapper.getName());
        long groupId = event.groupId();
        String groupKey = generateGroupKey(wrapper.getName(), groupId);
        GroupLimitation limitation = limitations.get(groupKey);
        if(limitation == null){
            return -1;
        }
        if(limitation.getMuteDuration() > 0){
            muteUser(event, limitation.getMuteDuration());
        }
        if(ratelimit.containsKey(groupKey)){
            Integer count = ratelimit.get(groupKey).getIfPresent(event.userId());
            if(count == null){
                count = 0;
            }
            count++;
            if(count > limitation.getFrequency()){
                return -1;
            }
            ratelimit.get(groupKey).put(event.userId(), count);
        }
        return limitation.getAlivetime()>0?limitation.getAlivetime():0;
    }
    
    private BotMessage[] generateResponse(BotEvent event, String key, BotMessage[] cache){
        List<BotMessage> messages = new ArrayList(cache.length*2);
        for(int i=0; i<cache.length; i++){
            if(cache[i] == null)
                System.out.println(cache[i]);
            if(null == cache[i].messageType()){
                messages.add(cache[i]);
            } else switch (cache[i].messageType()) {
                case MENTION:
                    messages.add(
                            new BotMessage.Builder()
                                    .messageType(MessageContentType.MENTION)
                                    .data(String.valueOf(event.userId()))
                                    .build());
                    break;
                case RANDOM:
                    String randomKey = generateRandomKey(key, i);
                    if(!randomCaches.containsKey(randomKey)){
                        messages.add(ERROR);
                        break;
                    }
                    int index = random.nextInt(cache.length);
                    for(BotMessage message:randomCaches.get(randomKey).get(index)){
                        if(message.messageType() == MessageContentType.MENTION){
                            messages.add(
                            new BotMessage.Builder()
                                    .messageType(MessageContentType.MENTION)
                                    .data(String.valueOf(event.userId()))
                                    .build());
                        } else {
                            messages.add(message);
                        }
                    }
                    break;
                default:
                    messages.add(cache[i]);
                    break;
            }
        }
        return messages.toArray(new BotMessage[messages.size()]);
    }
    
    private void configure() throws IOException {
        String file = path+"/config.yml";
        List<Configuration> configs = mapper.readValue(Files.readString(new File(file).toPath(), StandardCharsets.UTF_8), new TypeReference<List<Configuration>>() {});
        for(Configuration config:configs){
            if(configurations.containsKey(config.getName())){
                LOG.warn("dict name duplicated: {}", config.getName());
                continue;
            }
            prefixs.put(config.getName(), config.getPrefix());
            if(config.getResource() == null || config.getResource().isBlank()){
                resources.put(config.getName(), path);
            } else {
                if(config.getResource().endsWith("/") || config.getResource().endsWith("\\\\")){
                    resources.put(config.getName(), config.getResource().substring(0, config.getResource().length()-1));
                } else {
                    resources.put(config.getName(), config.getResource());
                }
            }
            configurations.put(config.getName(), config);
            if(config.getGroups() != null){
                config.getGroups().forEach(group -> {
                    String groupKey = generateGroupKey(config.getName(), group.getGroupId());
                    limitations.put(groupKey, group);
                    if(group.getDuration()>0 && group.getFrequency()>0){
                        ratelimit.put(groupKey, Caffeine.newBuilder()
                                .expireAfterWrite(group.getDuration(), TimeUnit.SECONDS)
                                .maximumSize(LIMIT_CACHE_SIZE)
                                .build());
                    }
                });
            }
            
            readDict(config);
            LOG.info("load dict: {}", config.getName());
        }
    }
    
    private void readDict(Configuration config) throws IOException{
        String file = new StringBuilder()
                .append(path)
                .append("/")
                .append(config.getFile())
                .toString();
        Map<String, DictionaryRecord[]> dict
                = mapper.readValue(Files.readString(new File(file).toPath(), StandardCharsets.UTF_8)
                        , new TypeReference<Map<String, DictionaryRecord[]>>() {});
        for (Map.Entry<String, DictionaryRecord[]> item : dict.entrySet()) {
            BotMessage[] messages = convertBotMessages(config.getName(), item.getValue());
            for(int i=0; i<messages.length;i++){
                if(messages[i].messageType() == MessageContentType.RANDOM){
                    List<List<DictionaryRecord>> entries = item.getValue()[i].getEntry();
                    List<BotMessage[]> randomDict = new ArrayList(entries.size());
                    entries.forEach((records) -> {
                        randomDict.add(convertBotMessages(config.getName()
                                        , records.toArray(new DictionaryRecord[records.size()])));
                    });
                    String randomKeyword = config.getPrefix() + generateRandomKey(item.getKey(), i);
                    if(randomCaches.containsKey(randomKeyword)){
                        LOG.warn("duplicate random key: {}", randomKeyword);
                    }
                    randomCaches.put(randomKeyword, randomDict);
                }
            }
            String keyword = config.getPrefix()+item.getKey();
            caches.put(keyword, new MessageWrapper(config.getName(), messages, (config.getGroups()!=null)));
        }
    }
    
    public String generateRandomKey(String key, int index) {
        return new StringBuilder().append(key).append("-").append(index).toString();
    }
    
    public String generateGroupKey(String key, long groupId) {
        return new StringBuilder().append(key).append("-").append(groupId).toString();
    }
    
    private BotMessage[] convertBotMessages(String name, DictionaryRecord[] records){
        List<BotMessage> messages = new ArrayList(records.length*2);
        for(int i=0; i<records.length; i++){
            messages.add(convertBotMessage(name, records[i]));
//            if(records[i].getRecord() == RecordType.TEXT
//                    || records[i].getRecord() == RecordType.IMAGE
//                    || records[i].getRecord() == RecordType.RECORD
//                    || records[i].getRecord() == RecordType.VIDEO){
//                if(i*2<records.length*2){
//                    messages.add(NEW_LINE);
//                }
//            }
        }
        return messages.toArray(new BotMessage[messages.size()]);
    }
    
    private BotMessage convertBotMessage(String name, DictionaryRecord record){
        BotMessage.Builder messageBuilder = new BotMessage.Builder();
        messageBuilder.messageType(convertContentType(record.getRecord()));
        if(record.getRecord() == RecordType.MENTION){
            return messageBuilder.build();
        }
        if(record.getRecord() == RecordType.IMAGE){
            messageBuilder.file(new StringBuilder()
                    .append("file://")
                    .append(resources.get(name))
                    .append("/image/")
                    .append(record.getFile())
                    .toString());
                //        messageBuilder.file("file:///srv/haruno/plugin/io.github.harunobot.plugin.kancolle.ca/image/"+item.getValue()[i].getFile());
            return messageBuilder.build();
        }
        if(record.getRecord() == RecordType.AUDIO){
            messageBuilder.file(new StringBuilder()
                    .append("file://")
                    .append(resources.get(name))
                    .append("/audio/")
                    .append(record.getFile())
                    .toString());
            return messageBuilder.build();
        }
        if(record.getRecord() == RecordType.TEXT){
//            messageBuilder.data(convertEscape(record.getText()));
            messageBuilder.data(record.getText());
            return messageBuilder.build();
        }
        if(record.getRecord() == RecordType.RANDOM){
            return messageBuilder.build();
        }
        messageBuilder.messageType(MessageContentType.TEXT);
        messageBuilder.data(new StringBuilder()
                .append(record.getRecord())
                .append(" can not be convert yet")
                .toString());
        return messageBuilder.build();
    }
    
    private String convertEscape(String string){
        return string.replaceAll("\\n", "\n");
    }
    
    private MessageContentType convertContentType(RecordType recordType){
        if(recordType == RecordType.IMAGE){
            return MessageContentType.IMAGE;
        }
        if(recordType == RecordType.AUDIO){
            return MessageContentType.AUDIO;
        }
        if(recordType == RecordType.VIDEO){
            return MessageContentType.VIDEO;
        }
        if(recordType == RecordType.MENTION){
            return MessageContentType.MENTION;
        }
        if(recordType == RecordType.REPLY){
            return MessageContentType.REPLY;
        }
        if(recordType == RecordType.RANDOM){
            return MessageContentType.RANDOM;
        }
        return MessageContentType.TEXT;
    }

    @Override
    public boolean onUnload() {
        enable = false;
        caches.clear();
        caches = null;
        randomCaches.clear();
        randomCaches = null;
        configurations.clear();
        configurations = null;
        resources.clear();
        resources = null;
        prefixs.clear();
        prefixs = null;
        limitations.clear();
        limitations = null;
        ratelimit.clear();
        ratelimit = null;
        mapper = null;
        return true;
    }

    @Override
    public boolean onEnable() {
        enable = true;
        return true;
    }
    
}
