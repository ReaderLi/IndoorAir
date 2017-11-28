package liruide.scu.com.indoorair;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

public class UserAccountActivity extends Activity {

    private static String TAG = "UserAccountActivity";
    private static String UserNameKey= "userName";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        setUserName();
    }

    /**
     从登录界面获取到用户名,并显示在用户中心界面
     */
    public void setUserName(){
        Intent intent = getIntent();
        if(intent != null){
            String userNameValue = intent.getStringExtra(UserNameKey);
            if(userNameValue != null){
                Log.i(TAG,"userName: "+userNameValue);

                TextView userNameTV = (TextView) findViewById(R.id.user_account_username_tv);
                userNameTV.setText(userNameValue);
            }

        }
    }

}
