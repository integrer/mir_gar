package org.donampa.nbibik.dipl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    public int LogIntID = 0;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.textView);
        Util.Init(getApplicationContext());
        loadUsernameFromPref();
    }

    private void loadUsernameFromPref() {
        String login = Util.GetPref(Util.Prefs.LOGIN, "");
        String username=login.split(":")[0];
        String pwd=login.split(":")[1];
        if(!login.isEmpty()) {
            Util.Logger.i("Logged in as \"" + username + "\".");
            LoginActivity.UserLoginTask loginTask = new LoginActivity.UserLoginTask(this, username, pwd, false);
            loginTask.execute((Void) null);
            tv.setText("Hello Mr/s " + username + '!');
            return;
        }
        Util.Logger.w("Information about authorisation does not found!");
        Intent loginFormIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginFormIntent, LogIntID);
        tv.setText(login.split(":")[0]);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LogIntID) {
            if (resultCode == RESULT_OK) {
                String fullUserName = data.getStringExtra(Util.FULLUSERNAME_FIELD);
                Util.SetPref(Util.Prefs.LOGIN, fullUserName);
            }
        }
    }

}
