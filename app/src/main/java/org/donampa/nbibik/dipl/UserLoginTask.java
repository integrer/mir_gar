package org.donampa.nbibik.dipl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;

import org.donampa.nbibik.dipl.util.Logger;
import org.donampa.nbibik.dipl.util.StringTable;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Errs> {

    private final String mLogin;
    private final String mPassword;

    private Exception exception;

    private final MainActivity maContext;
    private final LoginActivity laContext;
    private OnLoginTaskFinal onFinishListener;

    UserLoginTask(MainActivity context, String login, String password) {
        mLogin = login;
        mPassword = password;
        maContext = context;
        laContext = null;
    }

    UserLoginTask(LoginActivity context, String login, String password) {
        mLogin = login;
        mPassword = password;
        laContext = context;
        maContext = null;
    }

    @Contract(pure = true)
    private boolean inLoginActivityContext() {
        return laContext != null;
    }

    @Override
    protected Errs doInBackground(Void... params) {
        // - https://habrahabr.ru/post/13726/
        // TODO: attempt authentication against a network service.

        try {
            String link =
                    "https://mirgar.ga/userCheck.php/";
            String data = "?login=" + mLogin
                    + "&pass=" + mPassword;

            URL url = new URL(link + data);
            URLConnection conn = url.openConnection();

            BufferedReader bufReader = new BufferedReader(
                    new InputStreamReader(
                            conn.getInputStream())
            );

            String res = bufReader.readLine();

            if (res.equals("1"))
                return Errs.NoErr;
            if (res.equals("2"))
                return Errs.UserBanned;
            if (res.equals("0"))
                return Errs.EmailPwdIncorrect;

            return Errs.IO;

        } catch (MalformedURLException e) {
            exception = e;
            return Errs.MalformedUrl;

        } catch (IOException e) {
            exception = e;
            return Errs.IO;
        }
    }

    @Override
    protected void onPostExecute(final Errs res) {
        if (inLoginActivityContext()) {
            laContext.showProgress(false);

            switch (res) {
                case NoErr:
                    Intent ansIntent = new Intent();
                    ansIntent.putExtra(StringTable.IntentDataCuriosity, mLogin + ':' + mPassword);
                    laContext.setResult(Activity.RESULT_OK, ansIntent);
                    laContext.finish();
                    break;
                case EmailPwdIncorrect:
                    hideProgress();
                    laContext.mLoginView.setError(laContext.getString(R.string.error_not_exist_emailpwd_pair));
                    break;
                case UserBanned:
                    hideProgress();
                    laContext.mLoginView.setError(laContext.getString(R.string.error_user_banned));
                    break;
                case IO:
                    Logger.e(getClass(), exception);
                    AlertDialog.Builder builder = new AlertDialog.Builder(laContext);
                    builder.setTitle("Внимание!")
                            .setMessage("Возможно, отсутствует подключение интернет! Проверьте ваше подключение к сети.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton("Ок", (DialogInterface dialog, int id) -> dialog.cancel())
                            .show();
                    break;
                case MalformedUrl:
                    Logger.e(getClass(), exception.getLocalizedMessage());
                    exception.printStackTrace();
                    break;
            }
            laContext.mAuthTask = null;
        } else {
            if (onFinishListener != null)
                onFinishListener.onLoginTaskFinal(res);
        }
    }

    private void hideProgress() {
        if (laContext != null && laContext.mProgressView.getVisibility() != View.GONE) {
            //final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            laContext.mProgressView.setVisibility(View.GONE);
            //mProgressView.startAnimation(fadeOut);
        }
    }

    @Override
    protected void onCancelled() {
        if (laContext != null) {
            laContext.mAuthTask = null;
            laContext.showProgress(false);
        }
    }

    public void setOnFinishListener(OnLoginTaskFinal onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public interface OnLoginTaskFinal {
        public void onLoginTaskFinal(Errs res);
    }
}
