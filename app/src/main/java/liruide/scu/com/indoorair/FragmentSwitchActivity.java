package liruide.scu.com.indoorair;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;

/**
 * Created by LRD on 2017/11/27.
 */

import android.app.ActionBar;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.test.suitebuilder.TestMethod;
import android.text.Layout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import org.w3c.dom.Text;

import liruide.scu.com.indoorair.WebService.LoginWebService;


public class FragmentSwitchActivity extends FragmentActivity implements GestureDetector.OnGestureListener {

    public static Fragment[] fragments;
    public static LinearLayout[] linearLayouts;
    public static TextView[] textViews;
    /**定义手势检测实例*/
    public static GestureDetector detector;
    /**做标签，记录当前是哪个fragment*/
    public int MARK=0;
    /**定义手势两点之间的最小距离*/
    final int DISTANT=50;
    private ActionBar actionBar;

    private String UNameKey = "userName";

    //tag等于false，即代表底部按钮的背景图片是黑色
    boolean tagFragment0 = false;
    boolean tagFragment1 = false;
    boolean tagFragment2 = false;

    private static String TAG="FragmentSwitchActivity";

    private String airData;
    private static Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);//设置窗口无标题栏
        setContentView(R.layout.activity_fragment_switch);

        Log.i(TAG,"OnCreate");
        //获取用户名
        setUserName();
        //设置滑动按钮
        switchButton();

        //分别实例化和初始化fragement、lineatlayout、textview
        setfragment();
        setlinearLayouts();
        settextview();

        //创建手势检测器
        detector=new GestureDetector(this);

        //点击"修改密码"按钮，跳转
        TextView modifyPwdTV = (TextView) findViewById(R.id.user_center_account_tv);
        modifyPwdTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){

                TextView tv = (TextView) findViewById(R.id.user_center_userName_tv);
                String username = tv.getText().toString();

                Intent intent = new Intent(FragmentSwitchActivity.this,UserAccountActivity.class);
                intent.putExtra(UNameKey,username);
                startActivity(intent);

            }

        });

        attemptConn();


    }

    /**
     * 连接服务器获取气体数据
     */
    private void attemptConn(){
        new Thread(new MyThread()).start();
    }

    /**
     * 开启线程连接服务器
     */
    public class MyThread implements Runnable {


        @Override
        public void run() {
            airData = LoginWebService.executeGasDataHttpGet();
            Log.i(TAG,"gasData is:"+airData);

            //handler.post(r)其实这样并不会新起线程，只是执行的runnable里的run()方法，
            // 却没有执行start()方法，所以runnable走的还是UI线程。
            handler.post(new Runnable() {
                @Override
                public void run() {

                    //打印当前线程,结果是main
                    Log.i(TAG,"mu current thread is："+Thread.currentThread().getName());
                    //获取气体数据
                    getAirData(airData);



                }
            });
        }
    }

    /**
     * 获取服务端气体数据和传感器状态
     * @param Data
     */
    public void getAirData(String Data){

        String start = "<body>";
        String end="</body>";
        String CoTag="CO";
        String GasTag="Gas";
        String HchoTag="HCHO";

        String CoData;
        String GasData;
        String HchoData;

        String CoState;
        String GasState;
        String HchoState;

        Resources resources=getBaseContext().getResources();
        Drawable smallDrawable;

        //从获取到的服务端的网页截取body节点中的内容
        int s = Data.indexOf(start)+start.length();
        int e = Data.indexOf(end);
        String totalData = Data.substring(s,e);
        totalData = totalData.replace("\n","");
        totalData = totalData.replace(" ","");
        Log.i(TAG,"totalData is: "+totalData);
        //totalData is: CO581Gas578HCHO23COFalseGasFalseHCHOFalse

        //从totalData中截取传感器数据
        //int indexOf(int ch,int fromIndex)函数：就是字符ch在字串fromindex位后出现的第一个位置.没有找到返加-1
        s = totalData.indexOf(CoTag,0);
        e = totalData.indexOf(CoTag,2);
        String sensorData = totalData.substring(s,e);
        Log.i(TAG,"sensor data is: "+sensorData);
        CoData = sensorData.substring(sensorData.indexOf(CoTag)+CoTag.length(),sensorData.indexOf(GasTag));
        GasData = sensorData.substring(sensorData.indexOf(GasTag)+GasTag.length(),sensorData.indexOf(HchoTag));
        HchoData = sensorData.substring(sensorData.indexOf(HchoTag)+HchoTag.length());
        Log.i(TAG,"CoData is: "+CoData+",GasData is: "+GasData+",HchoData is: "+HchoData);

        //从totalData中截取传感器连接状态
        s = e;
        String sensorState = totalData.substring(s);
        Log.i(TAG,"sensor state is: "+sensorState);
        CoState = sensorState.substring(sensorState.indexOf(CoTag)+CoTag.length(),sensorState.indexOf(GasTag));
        GasState = sensorState.substring(sensorState.indexOf(GasTag)+GasTag.length(),sensorState.indexOf(HchoTag));
        HchoState = sensorState.substring(sensorState.indexOf(HchoTag)+HchoTag.length());
        Log.i(TAG,"CoState is: "+CoState+",GasState is: "+GasState+",HchoState is: "+HchoState);

        //设置CO的气体数据
        TextView Co_data__tv= (TextView) findViewById(R.id.weatherCO_data);
        Co_data__tv.setText(CoData);
        TextView CO_title_tv = (TextView) findViewById(R.id.weatherCO_describe_title);
        TextView CO_icon_tv = (TextView) findViewById(R.id.weatherCO_describe_icon);
        setDataIcon(CoData,CO_title_tv,CO_icon_tv);

        //设置CO传感器连接状态
        TextView Co_state_tv = (TextView) findViewById(R.id.sensorCO_describe_title);
        TextView Co_sensor_state_tv = (TextView) findViewById(R.id.sensorCO_describe_icon);
        if(CoState.equals("False")){
            Co_state_tv.setText("连接断开");
            smallDrawable=resources.getDrawable(R.drawable.sensor_break_state_icon);
            Co_sensor_state_tv.setBackground(smallDrawable);

        }else{
            Co_state_tv.setText("连接正常");
            smallDrawable=resources.getDrawable(R.drawable.sensor_connect_state_icon);
            Co_sensor_state_tv.setBackground(smallDrawable);
        }


        TextView Gas_data_tv = (TextView) findViewById(R.id.weatherGas_data);
        Gas_data_tv.setText(GasData);
        TextView Gas_title_tv = (TextView) findViewById(R.id.weatherGas_describe_title);
        TextView Gas_icon_tv = (TextView) findViewById(R.id.weatherGas_describe_icon);
        setDataIcon(GasData,Gas_title_tv,Gas_icon_tv);

        TextView Gas_state_tv = (TextView) findViewById(R.id.sensorCO2_describe_title);
        TextView Gas_sensor_state_tv = (TextView) findViewById(R.id.sensorCO2_describe_icon);
        if(CoState.equals("False")){
            Gas_state_tv.setText("连接断开");
            smallDrawable=resources.getDrawable(R.drawable.sensor_break_state_icon);
            Gas_sensor_state_tv.setBackground(smallDrawable);
        }else{
            Gas_state_tv.setText("连接正常");
            smallDrawable=resources.getDrawable(R.drawable.sensor_connect_state_icon);
            Gas_sensor_state_tv.setBackground(smallDrawable);
        }

        TextView C2H2O_data_tv = (TextView) findViewById(R.id.weatherC2H2O_data);
        C2H2O_data_tv.setText(HchoData);
        TextView C2H2O_title_tv = (TextView) findViewById(R.id.weatherC2H2O_describe_title);
        TextView C2H2O_icon_tv = (TextView) findViewById(R.id.weatherC2H2O_describe_icon);
        setDataIcon(HchoData,C2H2O_title_tv,C2H2O_icon_tv);

        TextView C2H2O_state_tv = (TextView) findViewById(R.id.sensorC2H2O_describe_title);
        TextView C2H2O_sensor_state_tv = (TextView) findViewById(R.id.sensorC2H2O_describe_icon);
        if(CoState.equals("False")){
            C2H2O_state_tv.setText("连接断开");
            smallDrawable=resources.getDrawable(R.drawable.sensor_break_state_icon);
            C2H2O_sensor_state_tv.setBackground(smallDrawable);
        }else{
            C2H2O_state_tv.setText("连接正常");
            smallDrawable=resources.getDrawable(R.drawable.sensor_connect_state_icon);
            C2H2O_sensor_state_tv.setBackground(smallDrawable);
        }


    }

    public void setDataIcon(String myData,TextView title,TextView icon){
        //传感器数据区间为： 低： 0-20  中：21-80  高：81-300 极高:301-600
        double num = Double.valueOf(myData);
        Resources resources=getBaseContext().getResources();

        if(num <= 20.0){
            title.setText(R.string.weather_describe1);
            Drawable smallDrawable=resources.getDrawable(R.drawable.small_data);
            icon.setBackground(smallDrawable);
        }else if(num >20.0 && num <= 80.0){
            title.setText(R.string.weather_describe2);
            Drawable middleDrawable = resources.getDrawable(R.drawable.middle_data);
            icon.setBackground(middleDrawable);
        }else{
            title.setText(R.string.weather_describe3);
            Drawable heightDrawable = resources.getDrawable(R.drawable.height_data);
            icon.setBackground(heightDrawable);
        }
    }

    /**
     * 设置是否接受消息
     */
    public void switchButton(){
        com.suke.widget.SwitchButton switchButton = (com.suke.widget.SwitchButton)
                findViewById(R.id.switch_button);

        switchButton.setChecked(true);
        switchButton.isChecked();
        //switchButton.toggle();     //switch state
        switchButton.toggle(true);//switch with animation
        switchButton.setShadowEffect(false);//disable shadow effect
        switchButton.setEnabled(true);//enable button
        switchButton.setEnableEffect(true);//enable the switch animation
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                //TODO do your job
                //when the color become green,isChecked value is true,otherwise,false.
                Log.i(TAG,"switch button's status:"+isChecked);
            }
        });
    }
    /**
    从登录界面获取到用户名,并显示在用户中心界面
     */
    public void setUserName(){
        Intent intent = getIntent();
        if(intent != null){
            String userNameValue = intent.getStringExtra(UNameKey);
            if(userNameValue != null){
                Log.i(TAG,"userName: "+userNameValue);

                //设置字体
                Typeface vibeFont = Typeface.createFromAsset(getAssets(), "fonts/GreatVibes-Regular.otf");
                TextView userNameTV = (TextView) findViewById(R.id.user_center_userName_tv);
                userNameTV.setTypeface(vibeFont);
                userNameTV.setText(userNameValue);
            }

        }
    }

    /**初始化fragment*/
    public void setfragment()
    {
        Log.i(TAG,"setFragment");

        fragments=new Fragment[3];
        fragments[0]=getSupportFragmentManager().findFragmentById(R.id.fragment1);
        fragments[1]=getSupportFragmentManager().findFragmentById(R.id.fragment2);
        fragments[2]=getSupportFragmentManager().findFragmentById(R.id.fragment3);
        getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                .show(fragments[0]).commit();

    }
    /**初始化linerlayout*/
    public void setlinearLayouts()
    {
        Log.i(TAG,"setlinearLayouts");
        linearLayouts=new LinearLayout[3];
        linearLayouts[0]=(LinearLayout)findViewById(R.id.lay1);
        linearLayouts[1]=(LinearLayout)findViewById(R.id.lay2);
        linearLayouts[2]=(LinearLayout)findViewById(R.id.lay3);
        //linearLayouts[0].setBackgroundResource(R.color.lightseagreen);
    }

    /**初始化textview*/
    public void settextview()
    {
        Log.i(TAG,"settextview");
        textViews=new TextView[3];
        textViews[0]=(TextView)findViewById(R.id.fratext1);
        textViews[1]=(TextView)findViewById(R.id.fratext2);
        textViews[2]=(TextView)findViewById(R.id.fratext3);
        //textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
        textViews[0].setBackground(getDrawable(R.drawable.button_gas_blue));
        tagFragment0 = !tagFragment0;
        setTitle(R.string.weatherPage_title);
    }
    /**点击底部linerlayout实现切换fragment的效果*/
    public void LayoutOnclick(View v)
    {

        Log.i(TAG,"LayoutOnclick");
        //获取数据更新
        attemptConn();

        //resetlaybg();//每次点击都重置linearLayouts的背景、textViews字体颜色
        switch (v.getId()) {
            case R.id.lay1:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                        .show(fragments[0]).commit();
                //linearLayouts[0].setBackgroundResource(R.drawable.background);
                // textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));
                //textViews[0].setBackground(getDrawable(R.drawable.button_gas_blue));
                //设置底部按钮的颜色变换，因为当前是fragment0，所以gas button图片背景如果是黑色，则置换为蓝色
                if(tagFragment0 == false){
                    textViews[0].setBackground(getDrawable(R.drawable.button_gas_blue));
                    tagFragment0 = !tagFragment0;

                }

                //若其余两个按钮为蓝色，则置为黑色
                if(tagFragment2 == true){
                    textViews[2].setBackground(getDrawable(R.drawable.button_user_center_black));
                    tagFragment2 = !tagFragment2;

                }
                if(tagFragment1 == true){
                    textViews[1].setBackground(getDrawable(R.drawable.button_sensor_black));
                    tagFragment1 = !tagFragment1;

                }

                setTitle(R.string.weatherPage_title);

                MARK=0;
                break;

            case R.id.lay2:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                        .show(fragments[1]).commit();
                //linearLayouts[1].setBackgroundResource(R.drawable.background);
                //textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
                // textViews[1].setBackground(getDrawable(R.drawable.button_sensor_blue));

                //设置底部按钮的颜色变换，因为当前是fragment1，所以sensor button图片背景如果是黑色，则置换为蓝色
                if(tagFragment1 == false){
                    textViews[1].setBackground(getDrawable(R.drawable.button_sensor_blue));
                    tagFragment1 = !tagFragment1;

                }

                //若其余两个按钮为蓝色，则置为黑色
                if(tagFragment0 == true){
                    textViews[0].setBackground(getDrawable(R.drawable.button_gas_black));
                    tagFragment0 = !tagFragment0;

                }
                if(tagFragment2 == true){
                    textViews[2].setBackground(getDrawable(R.drawable.button_user_center_black));
                    tagFragment2 = !tagFragment2;

                }

                setTitle(R.string.sensorPage_title);
                MARK=1;
                break;
            case R.id.lay3:
                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                        .show(fragments[2]).commit();
                //linearLayouts[2].setBackgroundResource(R.drawable.background);
                //textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));
                //textViews[2].setBackground(getDrawable(R.drawable.button_user_center_blue));

                //设置底部按钮的颜色变换，因为当前是fragment2，所以user center图片背景如果是黑色，则置换为蓝色
                if(tagFragment2 == false){
                    textViews[2].setBackground(getDrawable(R.drawable.button_user_center_blue));
                    tagFragment2 = !tagFragment2;

                }

                //若其余两个按钮为蓝色，则置为黑色
                if(tagFragment0 == true){
                    textViews[0].setBackground(getDrawable(R.drawable.button_gas_black));
                    tagFragment0 = !tagFragment0;

                }
                if(tagFragment1 == true){
                    textViews[1].setBackground(getDrawable(R.drawable.button_sensor_black));
                    tagFragment1 = !tagFragment1;

                }
                setTitle(R.string.user_center_title);
                MARK=2;
                break;
            default:
                break;
        }
    }
    /**重置linearLayouts、textViews*/
    public void resetlaybg()
    {
        Log.i(TAG,"resetlaybg");
        for(int i=0;i<3;i++)
        {
            linearLayouts[i].setBackgroundResource(R.color.lightseagreen);
            textViews[i].setTextColor(getResources().getColor(R.color.black));
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        //将该Activity上触碰事件交给GestureDetector处理
        return detector.onTouchEvent(event);
    }
    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }
    /**滑动切换效果的实现*/
    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
                           float arg3) {
        // TODO Auto-generated method stub
        Log.i(TAG,"onFling");
        //resetlaybg();
        //当是Fragment0的时候
        if(MARK==0)
        {
            if(arg1.getX()>arg0.getX()+DISTANT)
            {
                //获取数据更新
                attemptConn();

                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                        .show(fragments[1]).commit();
                //linearLayouts[1].setBackgroundResource(R.color.lightseagreen);
                //textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));
                //设置底部按钮的颜色变换，因为当前是fragment1，所以sensor button图片背景如果是黑色，则置换为蓝色
                if(tagFragment1 == false){
                    textViews[1].setBackground(getDrawable(R.drawable.button_sensor_blue));
                    tagFragment1 = !tagFragment1;
                }

                //若其余两个按钮为蓝色，则置为黑色
                if(tagFragment0 == true){
                    textViews[0].setBackground(getDrawable(R.drawable.button_gas_black));
                    tagFragment0 = !tagFragment0;
                }
                if(tagFragment2 == true){
                    textViews[2].setBackground(getDrawable(R.drawable.button_user_center_black));
                    tagFragment2 = !tagFragment2;
                }

                setTitle(R.string.sensorPage_title);

                MARK=1;
            }

        }
        //当是Fragment1的时候
        else if (MARK==1)
        {
            if(arg1.getX()>arg0.getX()+DISTANT)
            {
                //获取数据更新
                attemptConn();

                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                        .show(fragments[2]).commit();
                //linearLayouts[2].setBackgroundResource(R.color.lightseagreen);
                //textViews[2].setTextColor(getResources().getColor(R.color.lightseagreen));

                //设置底部按钮的颜色变换，因为当前是fragment2，所以user center图片背景如果是黑色，则置换为蓝色
                if(tagFragment2 == false){
                    textViews[2].setBackground(getDrawable(R.drawable.button_user_center_blue));
                    tagFragment2 = !tagFragment2;
                }

                //若其余两个按钮为蓝色，则置为黑色
                if(tagFragment0 == true){
                    textViews[0].setBackground(getDrawable(R.drawable.button_gas_black));
                    tagFragment0 = !tagFragment0;
                }
                if(tagFragment1 == true){
                    textViews[1].setBackground(getDrawable(R.drawable.button_sensor_black));
                    tagFragment1 = !tagFragment1;
                }

                setTitle(R.string.user_center_title);
                MARK=2;
            }
            else if(arg0.getX()>arg1.getX()+DISTANT)
            {
                //获取数据更新
                attemptConn();

                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                        .show(fragments[0]).commit();
                //linearLayouts[0].setBackgroundResource(R.color.lightseagreen);
                //textViews[0].setTextColor(getResources().getColor(R.color.lightseagreen));

                //设置底部按钮的颜色变换，因为当前是fragment0，所以gas button图片背景如果是黑色，则置换为蓝色
                if(tagFragment0 == false){
                    textViews[0].setBackground(getDrawable(R.drawable.button_gas_blue));
                    tagFragment0 = !tagFragment0;
                }

                //若其余两个按钮为蓝色，则置为黑色
                if(tagFragment2 == true){
                    textViews[2].setBackground(getDrawable(R.drawable.button_user_center_black));
                    tagFragment2 = !tagFragment2;
                }
                if(tagFragment1 == true){
                    textViews[1].setBackground(getDrawable(R.drawable.button_sensor_black));
                    tagFragment1 = !tagFragment1;
                }


                setTitle(R.string.weatherPage_title);
                MARK=0;
            }

        }
        //当是Fragment2的时候
        else if(MARK==2)
        {
            if(arg0.getX()>arg1.getX()+DISTANT)
            {
                //获取数据更新
                attemptConn();

                getSupportFragmentManager().beginTransaction().hide(fragments[0]).hide(fragments[1]).hide(fragments[2])
                        .show(fragments[1]).commit();
                //linearLayouts[1].setBackgroundResource(R.color.black);
                //textViews[1].setTextColor(getResources().getColor(R.color.lightseagreen));

                //设置底部按钮的颜色变换，因为当前是fragment1，所以sensor button图片背景如果是黑色，则置换为蓝色
                if(tagFragment1 == false){
                    textViews[1].setBackground(getDrawable(R.drawable.button_sensor_blue));
                    tagFragment1 = !tagFragment1;
                }

                //若其余两个按钮为蓝色，则置为黑色
                if(tagFragment0 == true){
                    textViews[0].setBackground(getDrawable(R.drawable.button_gas_black));
                    tagFragment0 = !tagFragment0;
                }
                if(tagFragment2 == true){
                    textViews[2].setBackground(getDrawable(R.drawable.button_user_center_black));
                    tagFragment2 = !tagFragment2;
                }
                setTitle(R.string.sensorPage_title);
                MARK=1;
            }

        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                            float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }


}
