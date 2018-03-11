package org.shadowice.flocke.andotp.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import org.shadowice.flocke.andotp.R;

public class WhatsNewActivity extends ThemedActivity
    implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setTitle(R.string.auth_activity_title);
        setContentView(R.layout.activity_container);

        /*Toolbar toolbar = findViewById(R.id.container_toolbar);
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);*/

        ViewStub stub = findViewById(R.id.container_stub);
        stub.setLayoutResource(R.layout.content_whatsnew);
        View v = stub.inflate();

        //Intent callingIntent = getIntent();

        Button buttonGo = v.findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
    }

    // Go back to the main activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
