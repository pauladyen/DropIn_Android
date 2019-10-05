package com.paul.mydropin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.adyen.checkout.dropin.DropIn;

public class ResultsPage extends AppCompatActivity {


    TextView results;
    Button newPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultspage);

        results = findViewById(R.id.results);
        newPayment = findViewById(R.id.newPayment);


        Intent intent = getIntent();

        results.setText(intent.getStringExtra(DropIn.RESULT_KEY));

        newPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restart();
            }
        });
    }


    public void restart(){
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finishAffinity();
    }

}
