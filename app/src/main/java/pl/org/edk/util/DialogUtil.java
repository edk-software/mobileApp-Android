package pl.org.edk.util;

import pl.org.edk.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class DialogUtil {
	private DialogUtil() {
	}

	public static void showWarningDialog(String message, final Activity activity) {
		activity.setContentView(R.layout.black_background);
		AlertDialog.Builder builder = new Builder(activity);
		builder.setTitle(R.string.warning_dialog_title);
		builder.setCancelable(false);
		builder.setMessage(message);
		builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.finish();
			}
		});

		AlertDialog dialog = builder.show();

		addRedTitleDivider(activity, dialog);

	}

	public static void addRedTitleDivider(final Context context, AlertDialog dialog) {
		int titleDividerId = context.getResources().getIdentifier("titleDivider", "id", "android");
		View titleDivider = dialog.findViewById(titleDividerId);
		if (titleDivider != null) {
			titleDivider.setBackgroundColor(context.getResources().getColor(R.color.red));
		}
	}

}
