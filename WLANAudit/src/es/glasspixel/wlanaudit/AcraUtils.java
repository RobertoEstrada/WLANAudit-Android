package es.glasspixel.wlanaudit;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "dHA5WlE3S0VqT2tWM18tVm1Vdm1BZmc6MQ", mailTo = "manzanocaminojesus@gmail.com", forceCloseDialogAfterToast = true, mode = ReportingInteractionMode.TOAST, resToastText = R.string.send_report)
public class AcraUtils extends Application {
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		super.onCreate();
	}
}
