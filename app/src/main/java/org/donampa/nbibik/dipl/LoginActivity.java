package org.donampa.nbibik.dipl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        /*if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            // TODO: alert the user with a Snackbar/AlertDialog giving them the permission rationale
            // To use the Snackbar from the design support library, ensure that the activity extends
            // AppCompatActivity and uses the Theme.AppCompat theme.
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }*/
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
//            }
//        }
//    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if(TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(this, email, password, true);
            mAuthTask.execute((Void) null);
        }
    }

//    private enum emailValidSts {
//        localPartFirst,
//        localPart,
//        sucsess,
//        err
//    }

    private boolean isEmailValid(String email) {
//        final int emailLength = email.length();
//        if (emailLength < 7)
//            return false;
//
//        // Ограничения длины по спецификации RFC 5321
//        if (emailLength > 254)
//            return false;
//
//        emailValidSts state = emailValidSts.localPartFirst;
//        int index = 1;
//        char curChar = email.charAt(index);
//        int mark = 0;
//        String local = null;
//        ArrayList<String> domain = new ArrayList<>();
//
//        try {
//            while (index < emailLength && state != emailValidSts.err) {
//
//                switch (state) {
//
//                    case localPartFirst: {
//                        // Первый символ {atext} -- текстовой части локального имени
//                        if (checkChar(curChar)) {
//                            state = emailValidSts.localPart;
//                            break;
//                        }
//                        // Если встретили неправильный символ -> отмечаемся в state об ошибке
//                        state = emailValidSts.err;
//                        break;
//                    }
//
//                    case localPart: {
//                        // Остальные символы {atext} -- текстовой части локального имени
//                        if (checkChar(curChar)) {
//                            break;
//                        }
//                        if (curChar == '.') {
//                            state = 2;
//                            break;
//                        }
//                        if (curChar == '@') { // Конец локальной части
//                            local = email.substring(0, index - mark);
//                            mark = index + 1;
//                            state = 3;
//                            break;
//                        }
//                        // Если встретили неправильный символ -> отмечаемся в state об ошибке
//                        state = emailValidSts.err;
//                        break;
//                    }
//
//                    case 2: {
//                        // Переход к {atext} (текстовой части) после точки
//                        if (checkChar(curChar)) {
//                            state = emailValidSts.localPart;
//                            break;
//                        }
//                        // Если встретили неправильный символ -> отмечаемся в state об ошибке
//                        state = emailValidSts.err;
//                        break;
//                    }
//
//                    case 3: {
//                        // Переходим {alnum} (домену), проверяем первый символ
//                        if (checkChar(curChar, false)) {
//                            state = 4;
//                            break;
//                        }
//                        // Если встретили неправильный символ -> отмечаемся в state об ошибке
//                        state = emailValidSts.err;
//                        break;
//                    }
//
//                    case 4: {
//                        // Собираем {alnum} --- домен
//                        if (checkChar(curChar, false)) {
//                            break;
//                        }
//                        if (curChar == '-') {
//                            state = 5;
//                            break;
//                        }
//                        if (curChar == '.') {
//                            domain.add(email.substring(mark, index - mark));
//                            mark = index + 1;
//                            state = 5;
//                            break;
//                        }
//                        // Проверка на конец строки
//                        if (index == emailLength - 1) {
//                            domain.add(email.substring(mark, index - mark));
//                            state = emailValidSts.sucsess;
//                            break; // Дошли до конца строки -> заканчиваем работу
//                        }
//                        // Если встретили неправильный символ -> отмечаемся в state об ошибке
//                        state = emailValidSts.err;
//                        break;
//                    }
//
//                    case 5: {
//                        if (checkChar(curChar, false)) {
//                            state = 4;
//                            break;
//                        }
//                        if (curChar == '-') {
//                            break;
//                        }
//                        // Если встретили неправильный символ -> отмечаемся в state об ошибке
//                        state = emailValidSts.err;
//                        break;
//                    }
//
//                    case sucsess: {
//                        // Успех! (На самом деле, мы сюда никогда не попадём)
//                        break;
//                    }
//                }
//                index++;
//                curChar = email.charAt(index);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        // Остальные проверки
//
//        // Не прошли проверку выше? Возвращаем false!
//        if (state != 6)
//            return false;
//
//        // Нам нужен домен как минимум второго уровня
//        if (domain.size() < 2)
//            return false;
//
//        // Ограничения длины по спецификации RFC 5321
//        if (local.length() > 64)
//            return false;
//
//        // Домен верхнего уровня должен состоять только из букв и быть не короче двух символов
//        index = emailLength - 1;
//        while (index > 0) {
//            curChar = email.charAt(index);
//            if (curChar == '.' && emailLength - index > 2) {
//                return true;
//            }
//            if (!((curChar >= 'a' && curChar <= 'z') || (curChar >= 'A' && curChar <= 'Z'))) {
//                return false;
//            }
//            index--;
//        }
        return true;
//        return Pattern.matches(
//                "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$",
//                email);
    }

    private boolean isPasswordValid(String pwd) {
        return (pwd.length() > 7);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
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

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public static class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;

        Exception exception;

        private Activity context;
        private LoginActivity lacontext;

        UserLoginTask(Activity context, String email, String password, boolean f) {
            mEmail = email;
            mPassword = password;
            if(f) {
                this.lacontext = (LoginActivity) context;
                this.context =null;
            } else {
                this.context = context;
                this.lacontext = null;
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            // - https://habrahabr.ru/post/13726/
            // TODO: attempt authentication against a network service.

            try {
                String link =
                        "https://mirgar.ga/userCheck.php/";
                String data = "?login=" + mEmail
                            + "&pass="  + mPassword;

                URL url = new URL(link + data);
                URLConnection conn = url.openConnection();

//                conn.setDoOutput(true);
//                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
//
//                writer.write(data);
//                writer.flush();

                BufferedReader bufReader = new BufferedReader(
                        new InputStreamReader(
                                conn.getInputStream())
                );
                StringBuilder strBuilder = new StringBuilder();
                String line = null;

                while((line = bufReader.readLine()) != null) {
                    strBuilder.append(line);
                }
                String res = strBuilder.toString();
                if (res.equals("1"))
                    return "";
                if (res.equals("0"))
                    return "[EMAIL_DOES_NOT_EXISTS]";
                return res;

            } catch (MalformedURLException e) {
                exception = e;
                return "[MALFORMED_URL]";

            } catch (IOException e) {
                exception = e;
                return "[IO_ERROR]";
            }
        }

        @Override
        protected void onPostExecute(final String res) {
            if(lacontext!=null) {
                lacontext.mAuthTask = null;
                lacontext.showProgress(false);

                switch (Util.GetErr(res)) {
                    case NoErr:
                        Intent ansIntent = new Intent();
                        if (res.equals("1")) {
                            ansIntent.putExtra(Util.FULLUSERNAME_FIELD, mEmail + ':' + mPassword);
                            lacontext.setResult(RESULT_OK, ansIntent);
                            lacontext.finish();
                        } else if (res.equals("2")) {
                            hideProgress();
                            lacontext.mEmailView.setError(context.getString(R.string.error_user_banned));
                            break;
                        }
                    case PwdIncorrect:
                        hideProgress();
                        lacontext.mPasswordView.setError(context.getString(R.string.error_incorrect_password));
                        break;
                    case EmailNExist:
                        hideProgress();
                        lacontext.mEmailView.setError(context.getString(R.string.error_not_exist_emailpwd_pair));
                        break;
                    case IO:
                        exception.printStackTrace();
                        break;
                }
            } else {
                switch (Util.GetErr(res)) {
                    case NoErr:
                        Intent ansIntent = new Intent();
                        context.setResult(RESULT_OK, ansIntent);
                        ansIntent.putExtra(Util.FULLUSERNAME_FIELD, mEmail + ':' + mPassword);
                        break;
                    case IO:
                        exception.printStackTrace();
                        break;
                    case MalformedUrl:
                        exception.printStackTrace();
                        break;
                    default:
                        ansIntent = new Intent();
                        context.setResult(RESULT_CANCELED, ansIntent);
                }
            }
        }

        private void hideProgress() {
            if (lacontext != null && lacontext.mProgressView.getVisibility() != View.GONE) {
                //final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
                lacontext.mProgressView.setVisibility(View.GONE);
                //mProgressView.startAnimation(fadeOut);
            }
        }

        @Override
        protected void onCancelled() {
            if(lacontext!=null) {
                lacontext.mAuthTask = null;
                lacontext.showProgress(false);
            }
        }
    }
}

