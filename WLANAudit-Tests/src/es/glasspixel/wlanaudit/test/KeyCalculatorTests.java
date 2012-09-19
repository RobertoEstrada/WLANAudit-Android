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
package es.glasspixel.wlanaudit.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite to test every key calculator on the
 * application. New calculators should include its own test case
 * and must be added to this test suite.
 * 
 * @author Roberto Estrada
 */
public class KeyCalculatorTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(KeyCalculatorTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(DiscusKeyCalculatorTest.class);
        suite.addTestSuite(DLinkKeyCalculatorTest.class);
        suite.addTestSuite(HWeiKeyCalculatorTest.class);
        suite.addTestSuite(WiFi6XKeyCalculatorTest.class);
        suite.addTestSuite(WLAN4XKeyCalculatorTest.class);
        //$JUnit-END$
        return suite;
    }

}
