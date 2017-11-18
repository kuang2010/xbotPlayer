package cn.iscas.xlab.xbotplayer;

/**
 * Created by lisongting on 2017/10/9.
 */

public class Constant {

    public static final String KEY_BROADCAST_ROS_CONN = "ros_conn_status";

    public static final String SUBSCRIBE_TOPIC_MAP = "/base64_img/map_img";

    public static final String PUBLISH_TOPIC_CONTROL_COMMAND = "/cmd_vel_mux/input/teleop";

    public static final String PUBLISH_TOPIC_CMD_LIFT = "/mobile_base/commands/lift";

    public static final String PUBLISH_TOPIC_CMD_CLOUD_CAMERA = "/mobile_base/commands/cloud_camera";

    public static final String SUBSCRIBE_TOPIC_RGB_IMAGE = "/base64_img/camera_rgb";

    public static final String SUBSCRIBE_TOPIC_DEPTH_IMAGE = "/base64_img/camera_depth";

    public static final String SUBSCRIBE_TOPIC_ROBOT_STATE = "/mobile_base/xbot/state";



    //用来表示Ros服务器的连接状态
    public static final int CONN_ROS_SERVER_SUCCESS = 0x11;

    public static final int CONN_ROS_SERVER_ERROR = 0x12;

    //广播的Intentfilter
    public static final String ROS_RECEIVER_INTENTFILTER = "xbotplayer.rosconnection.receiver";
}
