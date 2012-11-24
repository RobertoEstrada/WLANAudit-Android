/*
 * Copyright (C) 2012 Roberto Estrada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package es.glasspixel.wlanaudit;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(formKey = "dHA5WlE3S0VqT2tWM18tVm1Vdm1BZmc6MQ", mailTo = "manzanocaminojesus@gmail.com", forceCloseDialogAfterToast = true, mode = ReportingInteractionMode.TOAST, resToastText = R.string.send_report)
public class WLANAuditApplication extends Application {
    /**
     * App package name
     */
    public static final String PACKAGE_NAME = "es.glasspixel.wlanaudit";
    /**
     * Unique action name for the locatio update action
     */
    public static final String LOCATION_UPDATE_ACTION = "es.glasspixel.wlanaudit.action.ACTION_FRESH_LOCATION";
    
    @Override
    public void onCreate() {        
        super.onCreate();
        //ACRA.init(this);
    }
}
