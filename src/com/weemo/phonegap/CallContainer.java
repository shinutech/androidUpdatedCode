

package com.weemo.phonegap;

import javax.annotation.Nullable;

import java.util.ArrayList;

//import com.weemo.listener.*;
import net.rtccloud.helper.controller.StatusBarController;
import net.rtccloud.helper.fragment.CallFragment;
import net.rtccloud.helper.listener.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;

import com.synsormed.mobile.R;


//import net.rtccloud.helper.fragment.ConferencePanelFragment;
import net.rtccloud.sdk.Call;
import net.rtccloud.sdk.Call.CallStatus;
import net.rtccloud.sdk.Contact;
import net.rtccloud.sdk.Rtcc;
import net.rtccloud.sdk.RtccEngine;
import net.rtccloud.sdk.event.RtccEventListener;
import net.rtccloud.sdk.event.call.StatusEvent;

/***/

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;
import android.app.ActionBar.LayoutParams;
import android.app.FragmentTransaction;
import android.app.FragmentManager;

/**/




@SuppressLint("NewApi") public class CallContainer implements DialogInterface.OnCancelListener, OnCallFragmentListener, OnFullScreenListener
{
    private Activity activity;
    private int passedCallId;
    private Boolean isCallView=false;

    public CallContainer(Activity activity, int callId)
    {
        this.activity=activity;
        this.passedCallId=callId;
        execute();
    }

   /** Mimic the auto-generated file R.java */
    static class R {
        /** integer resources */
        static class integer {
            /** R.integer.camera_correction */
            static int camera_correction;
        }

        /**
         * Initialise the {@link R} class
         * 
         * @param r
         *            The {@link Resources} object
         * @param pn
         *            The packageName as String
         */
        static void init(Resources r, String pn) {
            R.integer.camera_correction = r.getIdentifier("camera_correction", "integer", pn);
        }
    }

    /** The current call */
    protected @Nullable
    Call call;

    /** Tag for call identifier (extra data) */
    private static final String EXTRA_CALLID = "callId";

    private void execute()
    {
        R.init(activity.getResources(), activity.getPackageName());
        // The callId must be provided in the intent that started this activity
        // If it is not, we finish the activity
        // final int callId = getIntent().getIntExtra(EXTRA_CALLID, -1);
        final int callId = passedCallId;
        if (callId == -1) {
            // activity.finish();
            return;
        }

        // Weemo must be initialized before starting this activity
        // If it is not, we finish the activity
        final RtccEngine rtcc = Rtcc.instance();
        if (rtcc == null) {
            // activity.finish();
            return;
        }

        // The call with the given ID must exist before starting this activity
        // If it is not, we finish the activity
        this.call = rtcc.getCall(callId);
        if (this.call == null) {
            // activity.finish();
            return;
        }

        
        ((AudioManager) activity.getSystemService(Context.AUDIO_SERVICE)).setSpeakerphoneOn(true);
        

        //setTitle(this.call.getContactDisplayName());

        // setTitle("TestTitleAmin");
        
        // Add the call window fragment
        /*if (savedInstanceState == null) {*/
            Log.d("AminLog","I am about to call the window fragment");
            
            //RtccCallFragment myFrag = RtccCallFragment.newInstanceCall(this.call);
            CallFragment callFragment=CallFragment.newInstanceCall(this.call);
            callFragment.callContainer=this;
             FragmentTransaction transaction=activity.getFragmentManager()
                    .beginTransaction();
                    transaction.add(android.R.id.content,
                            callFragment, "CALL_FRAGMENT" )
                                    //RtccCallFragment.newInstanceCall(this.call))
                                    //RtccCallFragment.newInstance(callId, com.weemo.phonegap.RtccCallFragment.TouchType.SLIDE_CONTROLS_FULLSCREEN, getResources().getInteger(R.integer.camera_correction), false))
                                    .commit();
            if(RtccAndroidPhonegap.isProvider() && RtccAndroidPhonegap.isDataAvailable())
            {
            	ButtonFragment buttonFragment = new ButtonFragment();
                activity.getFragmentManager()
                        .beginTransaction()
                        .add(android.R.id.content,
                        		buttonFragment, "SWITCH_BUTTON")
                                        //RtccCallFragment.newInstanceCall(this.call))
                                        //RtccCallFragment.newInstance(callId, com.weemo.phonegap.RtccCallFragment.TouchType.SLIDE_CONTROLS_FULLSCREEN, getResources().getInteger(R.integer.camera_correction), false))
                                        .commit();
                
//     		   fm.findFragmentByTag("CALL_FRAGMENT");
                activity.getFragmentManager().beginTransaction()
                .hide(buttonFragment)
                .commit();
            }
           
            
            
        // }
        // Register as event listener
        Rtcc.eventBus().register(this);
    }

   /* @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
    }*/


    public void destroy() {
        // Unregister as event listener

        Rtcc.eventBus().unregister(this);

        // When we leave this activity, we stop the video.
        if (this.call != null) {
            this.call.removeVideoOut();
            //ArrayList<Contact> mycontacts = this.call.getContacts();
            //int contactId = mycontacts.get(0).getId();
            //this.call.removeVideoIn(contactId);
            this.call.removeVideoIn(0);
        }
        removeCallView();

        // super.onDestroy();
    }
/*
    @Override
    public void onBackPressed() {
        // We allow leaving this activity only if specified
        if (getIntent().getBooleanExtra("canComeBack", false)) {
            super.onBackPressed();
        }
    }*/

    /**
     * This listener catches CallStatusChangedEvent 1. It is annotated with @WeemoEventListener
     * 2. It takes one argument which type is CallStatusChangedEvent 3. It's
     * activity object has been registered with
     * Weemo.getEventBus().register(this) in onCreate()
     * 
     * @param event
     *            The event
     */
    @RtccEventListener
    public void onCallStatusChanged(final StatusEvent event) {
        // First, we check that this event concerns the call we are monitoring
        if (event.getCall().getCallId() != this.call.getCallId()) {
            return;
        }

        // If the call has ended, we finish the activity (as this activity is
        // only for an active call)
        if (event.getStatus() == CallStatus.ENDED) {
            destroy();
            // activity.finish();
        }
    }

    private void removeCallView()
    {
        FragmentManager fm = activity.getFragmentManager();
        Fragment callFragment=fm.findFragmentByTag("CALL_FRAGMENT");
        fm.beginTransaction()
                        .remove(callFragment)
                         .commit();
        if(RtccAndroidPhonegap.isProvider() && RtccAndroidPhonegap.isDataAvailable())
        {
        	 Fragment switchButtonFragment=fm.findFragmentByTag("SWITCH_BUTTON");
             fm.beginTransaction()
                             .remove(switchButtonFragment)
                              .commit();
        }
       
    }

    /**
     * This listener catches CallStatusChangedEvent 1. It is annotated with @WeemoEventListener
     * 2. It takes one argument which type is CallStatusChangedEvent 3. It's
     * activity object has been registered with
     * Weemo.getEventBus().register(this) in onCreate()
     * 
     * @param event
     *            The event
     
    @RtccEventListener
    public void onCanCreateCallChanged(final CanCreateCallChangedEvent event) {
        final Error error = event.getError();
        if (error == CanCreateCallChangedEvent.Error.CLOSED) {
            Toast.makeText(activity, error.description(), Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }
*/
    @Override
    public void onCancel(DialogInterface dialog) {
        this.call.hangup();
    }
    
    @Override
    public void onHangup(Call call) {

        Log.d("gunjot", "came in Call Controller onHangup");

        Log.d("gunjot", "confirmed listener working");

        activity.getFragmentManager().popBackStack();


        Log.d("Here it is hanngup","This is me");
        //onStatusUpdate(getString(R.string.app_name), getString(R.string.msg_call_ended, call == null ? "" : DateUtils.formatElapsedTime(call.getCallDuration() / 1000L)), false);
        //getActionBar().show();
    }
    
    @Override
    public void onStatusUpdate(String title, String subtitle, boolean showProgress) {
        //none
    }
    
    public void enableDrawerToggle(boolean enable){
        
    }
    
    public void onFullScreen(boolean enable){
        
    }

    public void onShowStatusBar(String title, StatusBarController.StatusBarAction action, Bundle bundle){

    }

    public class ButtonFragment extends Fragment {

        public Fragment fragment;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            /*EditText v = new EditText(CallContainer.this.activity);
            v.setText("Hello Fragment!");
            return v;*/

            ImageButton tv = new ImageButton(CallContainer.this.activity);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                        Log.d("gunjot", "should be visible ");
                       FragmentManager fm = getActivity().getFragmentManager();
                        fragment=fm.findFragmentByTag("CALL_FRAGMENT");
                        fm.beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .show(fragment)
                         .commit();
                      
                       Fragment switchButtonFragment=ButtonFragment.this;
//                    		   fm.findFragmentByTag("CALL_FRAGMENT");
                        fm.beginTransaction()
                        .hide(switchButtonFragment)
                         .commit();
                        
                        
                        
                }
            });
           tv.setBackgroundResource(com.synsormed.mobile.R.drawable.back_to_call_view);
//           RelativeLayout.LayoutParams parent = new RelativeLayout.LayoutParams(
//                   RelativeLayout.LayoutParams.WRAP_CONTENT,
//                   RelativeLayout.LayoutParams.WRAP_CONTENT);
//           parent.addRule(RelativeLayout.CENTER_IN_PARENT);
//           RelativeLayout relativeLayout= new RelativeLayout(getActivity(), parent); 
//            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
//                    RelativeLayout.LayoutParams.WRAP_CONTENT,
//                    RelativeLayout.LayoutParams.WRAP_CONTENT);
//            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
//            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
//            int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
//            lp.setMargins(margin, topMargin, 0, 0);
////           lp.addRule(LayoutPar)
//            // Setting the parameters on the TextView
//            tv.setLayoutParams(lp);
            
            
//            relativeLayout.addView(tv);


            LinearLayout linearLayout = new LinearLayout(getActivity());
            // Set the layout full width, full height
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(params);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setPadding(10, 10, 0, 0);
            //or VERTICAL
            //For buttons visibility, you must set the layout params in order to give some width and height:
            LayoutParams buttonParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(buttonParams);
            linearLayout.addView(tv);
            return linearLayout;
        }
    }
    
    public void switchToWebView() {
            FragmentManager fm = activity.getFragmentManager();
            Fragment callFragment=fm.findFragmentByTag("CALL_FRAGMENT");
            fm.beginTransaction()
            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
            .hide(callFragment)
             .commit();
          
           Fragment switchButtonFragment=fm.findFragmentByTag("SWITCH_BUTTON");
            fm.beginTransaction()
            .show(switchButtonFragment)
             .commit();
      
        
    }


}