package liruide.scu.com.indoorair;
import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.app.LoaderManager.LoaderCallbacks;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import liruide.scu.com.indoorair.WebService.LoginWebService;


public class MainActivity extends Activity implements LoaderCallbacks<Cursor> {

    private Context context;

    private String TAG="LoginActivity";

    private String info;
    // private TextView infotv;
    private static Handler handler = new Handler();

    // UI references.
    private EditText mNameET;
    private EditText mPasswordET;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置隐藏ActionBar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        Log.i(TAG,"oncreate()");

        createLogo();

        //set up login form
        mNameET = (EditText) findViewById(R.id.etName);
        //  infotv = (TextView) findViewById(R.id.tvInfo);
        mPasswordET = (EditText) findViewById(R.id.etPassword);


        mPasswordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    //attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mLoginButton = (Button) findViewById(R.id.subBtn);
        if (mLoginButton != null) {
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    //提交输入的账户名和密码，验证登录
                    attemptLogin();

                    Log.i(TAG,"onclick!");
                    //为了便于测试，设置为不验证就直接跳转
                    Intent intent = new Intent(MainActivity.this,FragmentSwitchActivity.class);
                    startActivity(intent);



                }
            });
        }




    }

    private void attemptLogin() {

        new Thread(new MyThread()).start();
    }

    /*
    只有是拥有主线程looper的handler才可以操作ui。
    而在主线程操作ui可以在handler的handlerMessage()方法中操作Ui,
    也可以在handler的post(r)的run方法里操作Ui.
     */
    public class MyThread implements Runnable {

        @Override
        public void run() {
            info = LoginWebService.executeHttpGet("LogLet",mNameET.getText().toString(), mPasswordET.getText().toString());
            Log.i("LoginActivity","my return info is :"+info);

            //handler.post(r)其实这样并不会新起线程，只是执行的runnable里的run()方法，
            // 却没有执行start()方法，所以runnable走的还是UI线程。
            handler.post(new Runnable() {
                @Override
                public void run() {

                    //打印当前线程,结果是main

                    Log.i("LoginActivity","mu current thread is："+Thread.currentThread().getName());
                    String value="\n登陆成功";
                    if(value.equals(info))
                    {
                        Toast.makeText(getApplicationContext(), "验证成功",
                                Toast.LENGTH_SHORT).show();
                        //验证成功，跳转到相应界面
                        Intent intent = new Intent(MainActivity.this,FragmentSwitchActivity.class);
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(getApplicationContext(),"密码或账户名出错，请重新输入 "+info,
                                Toast.LENGTH_SHORT).show();

                    // infotv.setText(info);
                }
            });
        }
    }

    //设置登录界面的TextView内容的字体
    private void createLogo(){
        Typeface vibeFont = Typeface.createFromAsset(getAssets(), "fonts/GreatVibes-Regular.otf");
        TextView tvLogo = (TextView) findViewById(R.id.tvLogo);
        tvLogo.setTypeface(vibeFont);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }



    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }




}
