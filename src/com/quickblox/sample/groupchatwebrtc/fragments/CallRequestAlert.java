package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.sample.groupchatwebrtc.interfaces.CallRequestAlertInterface;
import com.synsormed.mobile.R;

/**
 * Activities that contain this fragment must implement the
 * {@link CallRequestAlert.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CallRequestAlert#newInstance} factory method to
 * create an instance of this fragment.
 */




public class CallRequestAlert extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public CallRequestAlertInterface listener;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public CallRequestAlert() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CallRequestAlert.
     */
    // TODO: Rename and change types and number of parameters
    public static CallRequestAlert newInstance(String param1, String param2) {
        CallRequestAlert fragment = new CallRequestAlert();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }




/* Have to comment out because Adobe Build doesn't have aPI 23
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
*/
    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View rootView = inflater.inflate(R.layout.fragment_call_request_alert, null);
        builder.setView(rootView);
        // Add action buttons



        Button hangUpButton=(Button) rootView.findViewById(R.id.hang_up_button);
        final AlertDialog.Builder builderFinal = builder;
        hangUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.hangUp();
                CallRequestAlert.this.dismiss();
            }
        });
        return builder.create();
    }



}



