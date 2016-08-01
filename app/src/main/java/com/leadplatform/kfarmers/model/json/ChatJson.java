package com.leadplatform.kfarmers.model.json;

import com.fasterxml.jackson.databind.JsonNode;

public class ChatJson<T>
{
    public String idx;
    public String chat_idx;
    public String type;
    public String member;
    public String nick;
    public String profile_image;
    public String message;
    public String message_type;
    public String read;
    public String use_flag;
    public String datetime;

    public JsonNode info;

    public T jsonData;


}
