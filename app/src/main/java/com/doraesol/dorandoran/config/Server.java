package com.doraesol.dorandoran.config;

/**
 * Created by YOONGOO on 2017-04-01.
 */

public class Server {
    public static final String SERVER_CONNECTION = "http://115.68.20.194/";
    public static final String JOIN                 = SERVER_CONNECTION + "join.php";
    public static final String USER_JOIN                 = SERVER_CONNECTION + "user_join.php";
    public static final String USER_INSERT_PATH_NAME = SERVER_CONNECTION + "user_insert_path_name.php";
    public static final String REGISTER_FCM_TOKEN   = SERVER_CONNECTION + "register_fcm.php";
    public static final String GET_USER             = SERVER_CONNECTION + "get_user.php";
    public static final String SEND_MESSAGE_TO_USER = SERVER_CONNECTION + "send_message.php";
    public static final String UPLOAD_PROFILE_IMAGE = SERVER_CONNECTION + "upload_profile_img.php";
    public static final String CONNETION_FAMILYTREE_UI = SERVER_CONNECTION + "familytree/index.html";
    public static final String ROUTE_SAVE = SERVER_CONNECTION + "route_save.php";
    public static final String ROUTE_LOAD = SERVER_CONNECTION + "route_load.php";
    public static final String USER_LOGIN = SERVER_CONNECTION + "user_login.php";


    // declare server request code..
    public static final String REQUEST_USER_FAMILYTREE = "1000";
    public static final String RESPONSE_USER_FAMILYTREE = "1001";
}
