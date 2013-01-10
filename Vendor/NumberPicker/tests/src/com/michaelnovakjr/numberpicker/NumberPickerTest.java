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
package com.michaelnovakjr.numberpicker;

import android.test.AndroidTestCase;
import android.util.Log;

public class NumberPickerTest extends AndroidTestCase {
    private static final String TAG = NumberPickerTest.class.getSimpleName();

    NumberPicker mNumberPicker;

    @Override
    protected void setUp() throws Exception {
        mNumberPicker = new NumberPicker(getContext());
        assertNotNull(mNumberPicker);
    }

    public void testSetRange() {
        mNumberPicker.setRange(2, 10);
        assertEquals(mNumberPicker.mStart, 2);
        assertEquals(mNumberPicker.mEnd, 10);
    }

    public void testSetCurrent() {
        mNumberPicker.setCurrent(20);
        assertEquals(mNumberPicker.getCurrent(), 20);
    }

    public void testRangeBounds() {
        mNumberPicker.setRange(1, 4);
        
        try {
            mNumberPicker.setCurrent(5);
            fail("Did not throw illegal argument exception for current greater than range end.");
        } catch (Exception e) {
            Log.d(TAG, "Exception thrown: " + e.toString());
        }
    }

    public void testChangeCurrent() {
        int current = mNumberPicker.getCurrent();

        mNumberPicker.changeCurrent(current + 2);
        assertEquals(mNumberPicker.getCurrent(), current + 2);
    }
    
    public void testNumberFormatter() {
        mNumberPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);

        String formattedNumber = mNumberPicker.formatNumber(2);
        assertEquals(formattedNumber, "02");
    }
}
