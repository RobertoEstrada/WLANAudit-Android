/*
 * Copyright 2011-2012 Michael Novak <michael.novakjr@gmail.com>.
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
package com.michaelnovakjr.sample.numberpicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.michaelnovakjr.numberpicker.NumberPickerDialog;

public class NumberPickerSample extends Activity implements NumberPickerDialog.OnNumberSetListener {
    private static final String TAG = NumberPickerSample.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sample_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_dialog_item) {
            NumberPickerDialog dialog = new NumberPickerDialog(this, -1, 5);
            dialog.setTitle(getString(R.string.dialog_picker_title));
            dialog.setOnNumberSetListener(this);
            dialog.show();

            return true;
        } else if (item.getItemId() == R.id.menu_preferences_item) {
            startActivity(new Intent(this, NumberPickerPreferenceActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onNumberSet(int number) {
        Log.d(TAG, "Number selected: " + number);
    }
}
