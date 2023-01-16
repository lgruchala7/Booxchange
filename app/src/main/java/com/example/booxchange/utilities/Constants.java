package com.example.booxchange.utilities;

import java.util.HashMap;

public class Constants {
    public static int MENU_HOME = 0;
    public static int MENU_MAP = 1;
    public static int MENU_ACCOUNT = 2;
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "booxchangePreference";
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
    public static final String KEY_TITLE = "title";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_GENRE = "genre";
    public static final String KEY_CONDITION = "condition";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_COLLECTION_ADS = "ads";
    public static final String KEY_CITY = "city";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_FAVORITES = "favorites";
    public static final String KEY_USER_IMAGE = "userImage";
    public static final String KEY_FRIENDS = "friends";
    public static final String KEY_AD = "ad";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAA7PxYXB4:APA91bF8CGqEXO_5VlaTTrQ1su6dTb7ymAsRcbVrAMSKig2q4VGpFLqjtBF5-yYcB-h3SZ9ZYhEZfMW_g1ql-R0rFmCvAMuhPI2rdzUhoJJrlOsDW2IADdiF3-Fc2T1lDPtVnzVsLSTZ"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    };

}
