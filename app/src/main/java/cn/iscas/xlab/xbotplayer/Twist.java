package cn.iscas.xlab.xbotplayer;

/**
 * Created by lisongting on 2017/9/27.
 */

public class Twist {

    //Vector3  linear
    private float linear_x;
    private float linear_y;
    private float linear_z;


    //Vector3   angular
    private float angular_x;
    private float angular_y;
    private float angular_z;

    public Twist(float linear_x, float linear_y, float linear_z, float angular_x, float angular_y, float angular_z) {
        this.linear_x = linear_x;
        this.linear_y = linear_y;
        this.linear_z = linear_z;
        this.angular_x = angular_x;
        this.angular_y = angular_y;
        this.angular_z = angular_z;
    }

    public float getLinear_x() {
        return linear_x;
    }

    public void setLinear_x(float linear_x) {
        this.linear_x = linear_x;
    }

    public float getLinear_y() {
        return linear_y;
    }

    public void setLinear_y(float linear_y) {
        this.linear_y = linear_y;
    }

    public float getLinear_z() {
        return linear_z;
    }

    public void setLinear_z(float linear_z) {
        this.linear_z = linear_z;
    }

    public float getAngular_x() {
        return angular_x;
    }

    public void setAngular_x(float angular_x) {
        this.angular_x = angular_x;
    }

    public float getAngular_y() {
        return angular_y;
    }

    public void setAngular_y(float angular_y) {
        this.angular_y = angular_y;
    }

    public float getAngular_z() {
        return angular_z;
    }

    public void setAngular_z(float angular_z) {
        this.angular_z = angular_z;
    }

    @Override
    public String toString() {
        return "Twist{" +
                "linear_x=" + linear_x +
                ", linear_y=" + linear_y +
                ", linear_z=" + linear_z +
                ", angular_x=" + angular_x +
                ", angular_y=" + angular_y +
                ", angular_z=" + angular_z +
                '}';
    }
}
