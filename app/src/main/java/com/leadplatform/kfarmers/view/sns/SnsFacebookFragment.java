package com.leadplatform.kfarmers.view.sns;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.view.base.BaseFragment;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SnsFacebookFragment extends BaseFragment
{
	public static final String TAG = "SnsFacebookFragment";

	CallbackManager callbackManager;
    
	private LoginButton loginBtn;
	private TextView userName;

	private boolean isPublishCheck = false;
	private boolean isReadCheck = false;

	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final List<String> PERMISSIONS2 = Arrays.asList("user_photos","user_posts");

	//"user_status"

    public static SnsFacebookFragment newInstance()
    {
        final SnsFacebookFragment f = new SnsFacebookFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_sns_facebook, container, false);

		callbackManager = CallbackManager.Factory.create();

		userName = (TextView)v.findViewById(R.id.user_name);
		loginBtn = (LoginButton)v.findViewById(R.id.login_button);
		loginBtn.setReadPermissions(PERMISSIONS2);
		//loginBtn.setPublishPermissions(PERMISSIONS);
		loginBtn.setFragment(this);

		loginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {

				Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();
				if (AccessToken.getCurrentAccessToken() == null  || !checkPermissions(permissions,PERMISSIONS2)) {
					LoginManager.getInstance().logInWithReadPermissions(SnsFacebookFragment.this, PERMISSIONS2);
					return;
				}

				if(AccessToken.getCurrentAccessToken() == null  || !checkPermissions(permissions,PERMISSIONS)) {
					LoginManager.getInstance()
							.setDefaultAudience(DefaultAudience.EVERYONE)
							.logInWithPublishPermissions(SnsFacebookFragment.this, PERMISSIONS);
					return;
				}
				onSnsFacebookBtnClicked(loginResult.getAccessToken().getToken());
			}

			@Override
			public void onCancel() {
				Toast.makeText(getActivity(), "Login canceled", Toast.LENGTH_SHORT).show();
				if (AccessToken.getCurrentAccessToken() != null) {
					LoginManager.getInstance().logOut();
				}
			}

			@Override
			public void onError(FacebookException exception) {
				Toast.makeText(getActivity(), "Login error", Toast.LENGTH_SHORT).show();
			}
		});

		/*loginBtn.setUserInfoChangedCallback(new UserInfoChangedCallback() {
			@Override
			public void onUserInfoFetched(GraphUser user) {
				if (user != null) {
					userName.setText("Hello, " + user.getName());
				} else {
					userName.setText("");
				}
			}
		});
*/
        return v;
    }

	public boolean checkPermissions(Set<String> permissions, List<String> checkPermissions) {
		boolean isCheck = false;
		for(int i = 0; i< checkPermissions.size();i++)
		{
			if(permissions.contains(checkPermissions.get(i))) {
				isCheck = true;
			}
			else
			{
				isCheck = false;
				break;
			}
		}
		return isCheck;
	}
    
    /*private Session.StatusCallback statusCallback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			if (state.isOpened()) {

				if(checkPermissions())
				{
					onSnsFacebookBtnClicked(session.getAccessToken());
				}
				else
				{
					if(isReadCheck && !checkReadPermissions())
					{
						Toast.makeText(getActivity(), "권한을 승인해 주세요.", Toast.LENGTH_SHORT).show();	
						isReadCheck = false;
						isPublishCheck = false;

						 Session.getActiveSession().closeAndClearTokenInformation();
						 Session.getActiveSession().close();
						 Session.setActiveSession(null);
					}
					else if(!isReadCheck && !checkReadPermissions())
					{
						requestPermissions(true);
					}
					
					else if(isPublishCheck && !checkPublishPermissions())
					{
						Toast.makeText(getActivity(), "권한을 승인해 주세요.", Toast.LENGTH_SHORT).show();
						isReadCheck = false;
						isPublishCheck = false;
						
						Session.getActiveSession().closeAndClearTokenInformation();
						Session.getActiveSession().close();
						Session.setActiveSession(null);
					}
					else if(checkReadPermissions())
					{
						if(!isPublishCheck)
						{
							if(!checkPublishPermissions())
							{
								requestPermissions(false);
							}			
						}
					}
				}				
			} else if (state.isClosed()) {
				
			}
		}
	};*/
	
	/*public boolean checkReadPermissions() {
		Session s = Session.getActiveSession();
		if (s != null) {
			
			boolean isCheck = false;
			for(int i = 0; i< PERMISSIONS2.size();i++)
			{
				if(s.getPermissions().contains(PERMISSIONS2.get(i)))
				{
					isCheck = true;
				}
				else
				{
					isCheck = false;
					break;
				}
			}
			
			if(isCheck) 
			{
				isReadCheck = true;
			}
			return isCheck;
		} else
			return false;
	}
	
	public boolean checkPublishPermissions() {
		Session s = Session.getActiveSession();
		if (s != null) {
			
			boolean isCheck = false;
			for(int i = 0; i< PERMISSIONS.size();i++)
			{
				if(s.getPermissions().contains(PERMISSIONS.get(i)))
				{
					isCheck = true;
				}
				else
				{
					isCheck = false;
					break;
				}
			}
			return isCheck;
		} else
			return false;
	}
	
	public boolean checkPermissions() {
		Session s = Session.getActiveSession();
		if (s != null) {
			boolean isCheck = false;
			for(int i = 0; i< CHECK_PERMISSIONS.size();i++)
			{
				if(s.getPermissions().contains(CHECK_PERMISSIONS.get(i)))
				{
					isCheck = true;
				}
				else
				{
					isCheck = false;
					break;
				}
			}
			return isCheck;
		} else
			return false;
	}
	
	

	public void requestPermissions(Boolean isCheck) {
		Session s = Session.getActiveSession();
		if (s != null)
		{
			if(!isCheck)
			{
				s.requestNewPublishPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS));
				isPublishCheck = true;
			}
			else
			{
				s.requestNewReadPermissions(new Session.NewPermissionsRequest(this, PERMISSIONS2));
				isReadCheck = true;
			}	
		}
	}*/


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onSnsFacebookBtnClicked(final String accessToken)
    {
        UiController.hideSoftKeyboard(getSherlockActivity());

		DbController.updateFaceBookSession(getSherlockActivity(), accessToken);
		((SnsActivity) getSherlockActivity()).displaySuccessConnectSNS();
        /*CenterController.snsFaceBook(accessToken, new CenterResponseListener(getSherlockActivity())
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            DbController.updateFaceBookSession(getSherlockActivity(), accessToken);
                            ((SnsActivity) getSherlockActivity()).displaySuccessConnectSNS();
                            break;

                        case 1001:
                            ((SnsActivity) getSherlockActivity()).displayUnknownAccountSNS();
                            break;

                        default:
                            ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                            break;
                    }
                }
                catch (Exception e)
                {
                    ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                }
            }
        });*/
    }

	
	
}