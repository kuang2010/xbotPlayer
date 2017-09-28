package cn.iscas.xlab.xbotplayer.mvp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cn.iscas.xlab.xbotplayer.Config;
import cn.iscas.xlab.xbotplayer.R;
import cn.iscas.xlab.xbotplayer.RosConnectionService;
import cn.iscas.xlab.xbotplayer.Twist;

import static cn.iscas.xlab.xbotplayer.mvp.ControlContract.CONN_ROS_SERVER_ERROR;
import static cn.iscas.xlab.xbotplayer.mvp.ControlContract.CONN_ROS_SERVER_SUCCESS;
import static cn.iscas.xlab.xbotplayer.mvp.ControlContract.ROS_RECEIVER_INTENTFILTER;

/**
 * Created by lisongting on 2017/9/27.
 */

public class ControlFragment extends Fragment implements ControlContract.View{



    private RosConnectionService.ServiceBinder serviceProxy;

    private RosConnectionReceiver receiver;
    private ControlContract.Presenter presenter;

    private TextView connectionState;
    private EditText ipEditText;
    private EditText speedEditText;
    private float speed ;
    private boolean isRosServerConnected = false;
    private Timer timer;
    private ImageButton bt_up;
    private ImageButton bt_down;
    private ImageButton bt_rotate_left;
    private ImageButton bt_rotate_right;
    private ImageButton bt_stop;
    private volatile boolean isControling = false;

    public ControlFragment() {

    }

    @Override
    public void initView() {
        Config.ROS_SERVER_IP = ipEditText.getEditableText().toString();

        ipEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Config.ROS_SERVER_IP = s.toString();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        ipEditText = (EditText) view.findViewById(R.id.et_ros_ip);
        speedEditText = (EditText) view.findViewById(R.id.et_speed);
        connectionState = (TextView) view.findViewById(R.id.et_state);

        bt_up = (ImageButton) view.findViewById(R.id.image_button_up);
        bt_down = (ImageButton) view.findViewById(R.id.image_button_down);
        bt_stop= (ImageButton) view.findViewById(R.id.image_button_stop);
        bt_rotate_left= (ImageButton) view.findViewById(R.id.image_button_rotate_left);
        bt_rotate_right = (ImageButton) view.findViewById(R.id.image_button_rotate_right);

        bt_up.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speed = Float.parseFloat(speedEditText.getEditableText().toString());
                        Twist twist = new Twist(speed, 0F, 0F, 0F, 0F, 0F);
//                        presenter.publishCommand(twist);
                        startTimerTask(twist,200);
                        isControling = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        cancelTimerTask();
                        isControling = false;
                        break;
                }
                return true;
            }
        });

        bt_down.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speed = Float.parseFloat(speedEditText.getEditableText().toString());
                        Twist twist = new Twist(-speed, 0F, 0F, 0F, 0F, 0F);
//                        presenter.publishCommand(twist);
                        startTimerTask(twist,200);
                        isControling = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        cancelTimerTask();
                        isControling = false;
                        break;
                }
                return true;
            }
        });


        bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed = Float.parseFloat(speedEditText.getEditableText().toString());
                Twist twist = new Twist(0F, 0F, 0F, 0F, 0F, 0F);
                presenter.publishCommand(twist);
            }
        });

       bt_rotate_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speed = Float.parseFloat(speedEditText.getEditableText().toString());
                        Twist twist = new Twist(0F, 0F, 0F, 0F, 0F, speed*3F);
//                        presenter.publishCommand(twist);
                        startTimerTask(twist,600);
                        isControling = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        cancelTimerTask();
                        isControling = false;
                        break;
                }
                return true;
            }
        });
        bt_rotate_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        speed = Float.parseFloat(speedEditText.getEditableText().toString());
                        Twist twist = new Twist(0F, 0F, 0F, 0F, 0F, -speed*3F);
//                        presenter.publishCommand(twist);
                        startTimerTask(twist,600);
                        isControling = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        cancelTimerTask();
                        isControling = false;
                        break;
                }
                return true;
            }
        });

        initView();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter = new ControlPresenter(getContext(),this);
        presenter.start();

        initBroadcastReceiver();
        
    }

    private void initBroadcastReceiver() {
        receiver = new RosConnectionReceiver(new RosConnectionReceiver.RosCallback() {
            @Override
            public void onSuccess() {
                if (!isRosServerConnected) {
                    connectionState.setTextColor(Color.GREEN);
                    connectionState.setText("连接成功");
                    Toast.makeText(getContext(), "Ros服务端连接成功", Toast.LENGTH_SHORT).show();
                    isRosServerConnected = true;
                }
            }

            @Override
            public void onFailure() {
                if (isRosServerConnected) {
                    connectionState.setTextColor(Color.RED);
                    connectionState.setText("未连接");
                    isRosServerConnected = false;
                }
            }
        });

        IntentFilter filter = new IntentFilter(ROS_RECEIVER_INTENTFILTER);
        getActivity().registerReceiver(receiver,filter);

    }

    public void setServicePresenter(RosConnectionService.ServiceBinder service) {
        presenter.setServiceProxy(service);
    }

    @Override
    public void setPresenter(ControlContract.Presenter presenter) {
        this.presenter = presenter;
    }


    public synchronized  void  startTimerTask(final Twist twist,int frequency) {
        if (isControling) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v("ControlFragment", "Publish Task is running ");
                presenter.publishCommand(twist);
            }
        },0,frequency);
    }

    public synchronized void cancelTimerTask() {
        timer.cancel();
        Log.v("ControlFragment", "TimerTask is killed ");
    }

    public static class RosConnectionReceiver extends BroadcastReceiver {

        RosCallback rosCallback;

        interface RosCallback {
            void onSuccess();

            void onFailure();
        }

        public RosConnectionReceiver(RosCallback callback) {
            this.rosCallback = callback;
        }

        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            switch (data.getInt("ros_conn_status")) {
                case CONN_ROS_SERVER_SUCCESS:
                    rosCallback.onSuccess();
                    break;
                case CONN_ROS_SERVER_ERROR:
                    rosCallback.onFailure();
                    break;
                default:
                    break;
            }
        }
    }


}
