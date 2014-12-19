package ray.nm_final_test2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.app.DialogFragment;

import java.util.Vector;

/**
 * Created by Ray on 2014/12/9.
 */
public class DeployChoiceDialog extends DialogFragment {

    public static DeployChoiceDialog newInstance(Vector<CharSequence> r){
        DeployChoiceDialog d = new DeployChoiceDialog();

        CharSequence[] repoNames = new CharSequence[r.size()];
        r.toArray(repoNames);

        Bundle args = new Bundle();
        args.putCharSequenceArray("repoNames", repoNames);
        d.setArguments(args);

        return d;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View main = inflater.inflate(R.layout.deploy_choice_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(main)
                .setTitle(getString(R.string.deploy_text))
                .setMessage("Choose which repository to deploy!")
                .setItems(savedInstanceState.getCharSequenceArray("repoNames"),
                                      new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                })
                .setPositiveButton(getString(R.string.button_Ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { /* do nothing ! */ }
                });

        return builder.create();
    }

}
