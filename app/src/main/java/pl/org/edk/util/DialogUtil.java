package pl.org.edk.util;

import pl.org.edk.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class DialogUtil {
	// ---------------------------------------
	// Subclasses
	// ---------------------------------------
	public interface OnCloseEventListener{
		void onClose();
	}

	public interface OnSelectedEventListener{
		void onAccepted();
		void onRejected();
	}

	// ---------------------------------------
	// Members
	// ---------------------------------------
	private static AlertDialog mDialog = null;

	// ---------------------------------------
	// Constructors
	// ---------------------------------------
	private DialogUtil() {}

	// ---------------------------------------
	// Public methods
	// ---------------------------------------
	public static void showDialog(String title, String message, Activity activity,
								  boolean closeEnabled, final OnCloseEventListener listener) {
		AlertDialog.Builder builder = new Builder(activity);
		builder.setTitle(title);
		builder.setCancelable(false);
		builder.setMessage(message);
		if(closeEnabled) {
			builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (listener != null) {
						listener.onClose();
					}
				}
			});
		}

		mDialog = builder.show();
		addRedTitleDivider(activity, mDialog);
	}

	/**
	 * Displays a custom warning dialog
	 * @param messageResId string id of the message to show
	 * @param activity Activity that calls the pop-up
	 * @param finishActivity Should the current activity be finished after the pop-up is closed?
	 */
	public static void showWarningDialog(int messageResId, final Activity activity, boolean finishActivity) {
		showWarningDialog(activity.getString(messageResId), activity, finishActivity);
	}

	/**
	 * Displays a custom warning dialog
	 * @param message Information in the content of the pop-up
	 * @param activity Activity that calls the pop-up
	 * @param finishActivity Should the current activity be finished after the pop-up is closed?
	 */
	public static void showWarningDialog(String message, final Activity activity, boolean finishActivity) {
		String title = activity.getApplicationContext().getString(R.string.warning_dialog_title);
		if(finishActivity){
			showDialog(title, message, activity, true, new OnCloseEventListener() {
				@Override
				public void onClose() {
					activity.finish();
				}
			});
		}
		else {
			showDialog(title, message, activity, true, null);
		}
	}

	/**
	 * Display custom pop-up informing that the application is busy
	 * @param messageResId Information in the content of the pop-up
	 * @param activity The activity that calls the pop-up
	 */
	public static void showBusyDialog(int messageResId, final Activity activity){
		String title = activity.getApplicationContext().getString(R.string.busy_dialog_title);
		showDialog(title, activity.getString(messageResId), activity, false, null);
	}

	/**
	 * Hide the busy pop-up, if it's visible
	 */
	public static void closeBusyDialog(){
		if(mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public static void showYesNoDialog(String title, String message, final Activity activity, final OnSelectedEventListener listener){
		AlertDialog.Builder builder = new Builder(activity);
		builder.setTitle(title);
		builder.setCancelable(false);
		builder.setMessage(message);
		builder.setPositiveButton(activity.getApplicationContext().getString(R.string.ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onAccepted();
				}
			}
		});
		builder.setNegativeButton(activity.getApplicationContext().getString(R.string.cancel), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (listener != null) {
					listener.onRejected();
				}
			}
		});

		mDialog = builder.show();
		addRedTitleDivider(activity, mDialog);
	}

	public static void showYesNoDialog(int titleResId, int messageResId, final Activity activity, final OnSelectedEventListener listener){
		String title = activity.getString(titleResId);
		String message = activity.getString(messageResId);
		showYesNoDialog(title, message, activity, listener);
	}

	public static void addRedTitleDivider(final Context context, AlertDialog dialog) {
		int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
		View titleDivider = dialog.findViewById(titleDividerId);
		if (titleDivider != null) {
			titleDivider.setBackgroundColor(context.getResources().getColor(R.color.red));
		}
	}
}
