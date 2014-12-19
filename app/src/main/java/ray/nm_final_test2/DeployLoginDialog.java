package ray.nm_final_test2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jcabi.github.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.jcabi.http.response.JsonResponse;
import javax.json.JsonObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by Ray on 2014/12/9.
 */
public class DeployLoginDialog extends DialogFragment {

    OnDeployLoginFinishedListener mCallBack;

    public interface OnDeployLoginFinishedListener{
        public void onLoginFinished(Github github, Vector<CharSequence> repoNames);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mCallBack = (OnDeployLoginFinishedListener) activity;
        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString() + " must implement OnDeployFinishedListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View main = inflater.inflate(R.layout.deploy_login_dialog, null);
        final TextView usr_text = (TextView) main.findViewById(R.id.deploy_user_name);
        final TextView pas_text = (TextView) main.findViewById(R.id.deploy_password);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setView(main)
                    .setTitle(getString(R.string.deploy_text))
                    .setMessage("Few steps, and deploy your website on GitHub!")
                    .setPositiveButton(getString(R.string.button_Ok), null)
                    .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) { /* do nothing ! */ }
                    })
                    .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button ok_btn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                ok_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Validate user input data.
                        if(usr_text.getText().toString().equals("")){
                            Toast.makeText(getDialog().getContext(), getString(R.string.usr_empty_warning), Toast.LENGTH_SHORT)
                                 .show();
                        }else{
                            // Send the username & password to DeployTask and execute for logging in GitHub.
                            DeployTask login = new DeployTask(usr_text.getText().toString(), pas_text.getText().toString());
                            login.execute();
                            dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    private class DeployTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog _mProgressDialog = null;
        private String username, password;
        private boolean fin = false;
        private Github gh = null;
        private Vector<CharSequence> repoNames = new Vector<CharSequence>();

        DeployTask(String u, String p){
            username = u;
            password = p;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _mProgressDialog = new ProgressDialog(getActivity());
            _mProgressDialog.setTitle("Checking your GitHub repositories");
            _mProgressDialog.setMessage("Loading...");
            _mProgressDialog.setIndeterminate(false);
            _mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                Log.v("deployAsync", username+" "+password);
                Github github = new RtGithub(username, password);
                final JsonResponse resp = gh.entry()
                                            .uri().path("/users/"+username+"/repos").back()
                                            .fetch()
                                            .as(JsonResponse.class);
                final List<JsonObject> repos = resp.json().readArray()
                                                          .getValuesAs(JsonObject.class);
                for (final JsonObject repo : repos)
                    repoNames.add(repo.get("name").toString().replaceAll("\"",""));
                fin = true;
                gh = github;
            }
            catch (java.net.UnknownHostException uhe){
                Toast.makeText(getDialog().getContext(), getString(R.string.bad_internet_warning), Toast.LENGTH_SHORT)
                     .show();}
            catch (IOException e){ e.printStackTrace(); }
            catch (javax.json.JsonException jsonE){ jsonE.printStackTrace(); }
            catch (ExceptionInInitializerError iAE){ Log.v("deployAsync", "bad wifi"); }

            //try {
            //    TimeUnit.MILLISECONDS.sleep(2000);
            //}catch (InterruptedException e){}
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            _mProgressDialog.dismiss();
            if(fin)
                mCallBack.onLoginFinished(gh, repoNames);
        }
    }
}
