/*
 * Copyright (C) 2015 Bruno Bierbaumer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


package org.shadowice.flocke.andotp;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v7.widget.ActionBarContextView;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ViewAsserts;
import android.view.View;
import android.widget.ListView;


import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base32;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mActivity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mActivity = getActivity();
    }

    public void testStart(){
        ViewAsserts.assertOnScreen(mActivity.getWindow().getDecorView(), mActivity.findViewById(R.id.cardList));
    }


    //TODO. switch to toolbar
    public void test000About() throws InterruptedException {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getContext());

        onView(allOf(withText("About"), isDisplayed())).perform(click());

        Thread.sleep(1000);
        onView(withId(R.id.about_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.about_layout)).perform(pressBack());

        onView(withId(R.id.about_layout)).check(doesNotExist());

    }

    public void test000EmptyStart() throws InterruptedException {

        onView(withText("No account has been added yet")).check(matches(isDisplayed()));
        onView(withText("Add")).check(matches(isDisplayed()));

        Intents.init();

        String qr = "XXX" ;

        // Build a result to return from the ZXING app
        Intent resultData = new Intent();
        resultData.putExtra(com.google.zxing.client.android.Intents.Scan.RESULT, qr);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Stub out the Camera. When an intent is sent to the Camera, this tells Espresso to respond
        // with the ActivityResult we just created
        intending(hasAction("com.google.zxing.client.android.SCAN")).respondWith(result);


        onView(withText("Add")).check(matches(isDisplayed()));

        onView(withText("Add")).perform(click());

        // We can also validate that an intent resolving to the "camera" activity has been sent out by our app
        intended(hasAction("com.google.zxing.client.android.SCAN"));


        Intents.release();
    }

    public void test001InvalidQRCode() throws InterruptedException {
        Intents.init();

        String qr ="invalid qr code";

        // Build a result to return from the ZXING app
        Intent resultData = new Intent();
        resultData.putExtra(com.google.zxing.client.android.Intents.Scan.RESULT, qr);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Stub out the Camera. When an intent is sent to the Camera, this tells Espresso to respond
        // with the ActivityResult we just created
        intending(hasAction("com.google.zxing.client.android.SCAN")).respondWith(result);

        // Now that we have the stub in place, click on the button in our app that launches into the Camera
        onView(withId(R.id.action_scan)).perform(click());

        // We can also validate that an intent resolving to the "camera" activity has been sent out by our app
        intended(hasAction("com.google.zxing.client.android.SCAN"));

        onView(withText("Invalid QR Code")).check(matches(isDisplayed()));


        Thread.sleep(5000);
        onView(withText("No account has been added yet")).check(matches(isDisplayed()));

        Intents.release();
    }

    public void test002NocodeScanned() throws InterruptedException {
        Intents.init();

        // Build a result to return from the ZXING app
        Intent resultData = new Intent();

        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData);

        // Stub out the Camera. When an intent is sent to the Camera, this tells Espresso to respond
        // with the ActivityResult we just created
        intending(hasAction("com.google.zxing.client.android.SCAN")).respondWith(result);

        // Now that we have the stub in place, click on the button in our app that launches into the Camera
        onView(withId(R.id.action_scan)).perform(click());

        // We can also validate that an intent resolving to the "camera" activity has been sent out by our app
        intended(hasAction("com.google.zxing.client.android.SCAN"));

        onView(withText("No account has been added yet")).check(matches(isDisplayed()));

        Intents.release();
    }


    String[][] codes = new String [][]{
            new String[]{"WOW", "Sicherheit00000"},
            new String[]{"SUCH", "Sicherheit00001"},
            new String[]{"APP", "Sicherheit00002"},
    };


    public  void test003AddCodes() throws InterruptedException {
        for(String[] code: codes){
            Intents.init();

            String qr = "otpauth://totp/"+code[0] +"?secret="+new String(new Base32().encode(code[1].getBytes())) ;

            // Build a result to return from the ZXING app
            Intent resultData = new Intent();
            resultData.putExtra(com.google.zxing.client.android.Intents.Scan.RESULT, qr);
            Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

            // Stub out the Camera. When an intent is sent to the Camera, this tells Espresso to respond
            // with the ActivityResult we just created
            intending(hasAction("com.google.zxing.client.android.SCAN")).respondWith(result);

            // Now that we have the stub in place, click on the button in our app that launches into the Camera
            onView(withId(R.id.action_scan)).perform(click());

            // We can also validate that an intent resolving to the "camera" activity has been sent out by our app
            intended(hasAction("com.google.zxing.client.android.SCAN"));

            onView(withText("Account added")).check(matches(isDisplayed()));

            Intents.release();
        }

        Thread.sleep(500);


        for(int i = 0; i < codes.length; i++){
            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewLabel))
                    .check(matches(withText(codes[i][0])));

            String otp = TOTPHelper.generate(codes[i][1].getBytes());

            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewOTP))
                    .check(matches(withText(otp)));
        }
    }

    public  void test003CodesChange() throws InterruptedException {
        ArrayList<String> oldCodes = new ArrayList<>();

        for(int i = 0; i < codes.length; i++){
            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewLabel))
                    .check(matches(withText(codes[i][0])));

            String otp = TOTPHelper.generate(codes[i][1].getBytes());
            oldCodes.add(otp);

            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewOTP))
                    .check(matches(withText(otp)));
        }

        Thread.sleep(30*1000);

        for(int i = 0; i < codes.length; i++){
            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewLabel))
                    .check(matches(withText(codes[i][0])));

            String otp = TOTPHelper.generate(codes[i][1].getBytes());
            assertTrue(!oldCodes.get(i).equals(otp));

            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewOTP))
                    .check(matches(withText(otp)));
        }


    }

    public void test004Rearrange() throws InterruptedException, UiObjectNotFoundException {
        UiDevice mDevice = UiDevice.getInstance(getInstrumentation());


        UiObject start =  mDevice.findObject(new UiSelector().textContains(codes[0][0]));
        UiObject end =  mDevice.findObject(new UiSelector().textContains(codes[1][0]));

        start.dragTo(start, 10);
        start.dragTo(end, 10);

        mDevice.pressBack();

        Thread.sleep(2000);

        String t = codes[0][0];
        codes[0][0] = codes[1][0];
        codes[1][0] = t;

        for(int i = 0; i < codes.length; i++){
            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewLabel))
                    .check(matches(withText(codes[i][0])));
        }

        start =  mDevice.findObject(new UiSelector().textContains(codes[0][0]));
        end =  mDevice.findObject(new UiSelector().textContains(codes[1][0]));

        start.dragTo(start, 10);
        start.dragTo(end, 10);

        mDevice.pressBack();

        Thread.sleep(2000);

        t = codes[0][0];
        codes[0][0] = codes[1][0];
        codes[1][0] = t;

        for(int i = 0; i < codes.length; i++){
            onData(anything()).inAdapterView(withId(R.id.cardList))
                    .atPosition(i)
                    .onChildView(withId(R.id.textViewLabel))
                    .check(matches(withText(codes[i][0])));
        }
    }

    /*
    public void test005EditMode() throws InterruptedException {

        onView(withId(R.id.action_edit)).check(doesNotExist());

        onData(anything()).inAdapterView(withId(R.id.listView))
                .atPosition(0)
                .perform(longClick());

        onView(withId(R.id.action_edit)).check(matches(isDisplayed()));
        onView(withId(R.id.action_delete)).check(matches(isDisplayed()));
        ActionBarContextView.class.getCanonicalName();
        onView(allOf(isDescendantOfA(withClassName(Matchers.containsString("ActionBarContextView"))), withText(codes[0][0]))).check(matches(isDisplayed()));


        onData(anything()).inAdapterView(withId(R.id.listView))
                .atPosition(1)
                .perform(longClick());



        onView(withId(R.id.action_edit)).check(matches(isDisplayed()));
        onView(withId(R.id.action_delete)).check(matches(isDisplayed()));
        onView(allOf(isDescendantOfA(withClassName(Matchers.containsString("ActionBarContextView"))), withText(codes[1][0]))).check(matches(isDisplayed()));


        onView(withId(R.id.listView)).perform(pressBack());

        onView(withId(R.id.action_edit)).check(doesNotExist());
    }

    public void test005RenameCancel(){

        onData(anything()).inAdapterView(withId(R.id.listView))
                .atPosition(1)
                .perform(longClick());

        onView(withId(R.id.action_edit)).check(matches(isDisplayed()));

        onView(withId(R.id.action_edit)).perform(click());

        onView(withText(codes[1][0])).perform(click()).perform(typeText(" VERY TEST"));

        onView(withText("Cancel")).perform(click());

        onData(anything()).inAdapterView(withId(R.id.listView))
                .atPosition(1)
                .onChildView(withId(R.id.textViewLabel))
                .check(matches(withText(codes[1][0])));

    }

    public void test006Rename(){

        onData(anything()).inAdapterView(withId(R.id.listView))
                .atPosition(1)
                .perform(longClick());

        onView(withId(R.id.action_edit)).check(matches(isDisplayed()));

        onView(withId(R.id.action_edit)).perform(click());

        onView(withText(codes[1][0])).perform(click()).perform(typeText(" VERY TEST"));

        onView(withText("Save")).perform(click());

        onData(anything()).inAdapterView(withId(R.id.listView))
                .atPosition(1)
                .onChildView(withId(R.id.textViewLabel))
                .check(matches(withText(codes[1][0] + " VERY TEST")));

    }


    public void test007DeleteCancel() throws InterruptedException, EncoderException {


        onData(anything()).inAdapterView(withId(R.id.listView))
                .atPosition(0)
                .perform(longClick());

        onView(withId(R.id.action_delete)).check(matches(isDisplayed()));
        onView(withId(R.id.action_delete)).perform(click());

        onView(withText("Remove")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));

        onView(withText("Cancel")).perform(click());

        onView(withText("Remove")).check(doesNotExist());

        onView(withId(R.id.listView)).check(matches(withListSize(codes.length)));

    }

    public void test008Delete() throws InterruptedException, EncoderException {




        // remove test
        for(int i = codes.length; i > 0; i--){

            onData(anything()).inAdapterView(withId(R.id.listView))
                    .atPosition(0)
                    .perform(longClick());

            onView(withId(R.id.action_delete)).check(matches(isDisplayed()));
            onView(withId(R.id.action_delete)).perform(click());

            onView(withText("Remove")).check(matches(isDisplayed()));



            onView(withText("Remove")).perform(click());
            onView(withId(R.id.listView)).check(matches(withListSize(i - 1)));

            if(i > 1){
                onView(withText("Account removed")).check(matches(isDisplayed()));
            }
            else {
                onView(withText(R.string.no_accounts)).check(matches(isDisplayed()));
            }

        }


    }
    */

    public static Matcher<View> withListSize (final int size) {
        return new TypeSafeMatcher<View> () {
            @Override public boolean matchesSafely (final View view) {
                return ((ListView) view).getChildCount () == size;
            }

            @Override public void describeTo (final Description description) {
                description.appendText ("ListView should have " + size + " items");
            }
        };
    }


    public static Matcher<View> withResourceName(String resourceName) {
        return withResourceName(is(resourceName));
    }

    public static Matcher<View> withResourceName(final Matcher<String> resourceNameMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with resource name: ");
                resourceNameMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                int id = view.getId();
                return id != View.NO_ID && id != 0 && view.getResources() != null
                        && resourceNameMatcher.matches(view.getResources().getResourceName(id));
            }
        };
    }



}
