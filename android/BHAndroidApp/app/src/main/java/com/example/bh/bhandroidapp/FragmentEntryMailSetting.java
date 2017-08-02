package com.example.bh.bhandroidapp;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentEntryMailSetting extends Fragment {

    private EditText editMailId;
    private EditText editMailHost;
    private String resultMail[] = null;
    public static final FragmentEntryMailSetting newInstance(String res){
        FragmentEntryMailSetting ret = new FragmentEntryMailSetting();
        ret.resultMail = res.split("@");
        return ret;
    }
    public FragmentEntryMailSetting() {
        // Required empty public constructor

    }

    public String getMailIdToString(){
        return editMailId.getText().toString().trim();
    }
    public String getMailHostToString(){
        return editMailHost.getText().toString().trim();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View retFragment = inflater.inflate(R.layout.frag_entry_mail_settings,container,false);
        editMailId = (EditText)retFragment.findViewById(R.id.edit_mail_id);
        editMailHost = (EditText)retFragment.findViewById(R.id.edit_mail_host);
        if(resultMail != null){
            editMailId.setText(resultMail[0]);
            editMailHost.setText(resultMail[1]);
        }

        return retFragment;
    }
    public String getFullMailEntry(){
        return String.format("%s@%s",editMailId.getText().toString(),editMailHost.getText().toString());
    }

}
