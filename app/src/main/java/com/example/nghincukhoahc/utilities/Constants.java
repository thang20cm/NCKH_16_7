package com.example.nghincukhoahc.utilities;

import java.util.HashMap;

public class Constants {
    public static final int PICK_IMAGE_REQUEST_CODE = 1;
    public static final String KEY_FROM_MAIN_ACTIVITY = "from_main_activity";
    public static final String KEY_FROM_USER_ACTIVITY = "from_user_activity";

    public static final String KEY_REMEMBER_ME = "remember_me";
    public static final String KEY_STATUS = "QuyenTruyCap";
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_COLLECTION_ADMIN = "adminclass";
    public static final String KEY_SUBCOLLECTION_ADMIN = "adminstatus";
    public static final String KEY_REMEMBER_LOGIN = "remember_login";
    public static final String KEY_USER_TYPE = "usertype";

    public static final String KEY_COLLECTION_SUPER_ADMIN = "superadmin";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_CLASS = "class";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String,String> remoteMsgHeaders = null;
    public static HashMap<String,String> getRemoteMsgHeaders(){
        if(remoteMsgHeaders == null){
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAcAtqgEQ:APA91bHKP7-gJQvMINaDSbgcwBPCiJ4FZMgdLXnh24A4YG871TgFqQvKTW1YY5a5Hk5A5FuDXoL9v5qvFB8XgCFlA1c7Z1Yh4Q45YfQXXEneyRciSbqOpJtmHU-OH4X7dtHb-7fkzV3u"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }
}

