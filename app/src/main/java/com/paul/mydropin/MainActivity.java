package com.paul.mydropin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import com.adyen.checkout.base.model.PaymentMethodsApiResponse;
import com.adyen.checkout.card.CardConfiguration;
import com.adyen.checkout.dropin.DropIn;
import com.adyen.checkout.dropin.DropInConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paul.mydropin.Network.ApiConfig;
import com.paul.mydropin.Network.AppConfig;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText currency,country,amount;
    Button dropInPay;

    public static String curr;
    public static String amo;
    Intent resultsIntent;

    Boolean dropIn;

    LinearLayoutCompat layout;


    JSONObject jsonPayMethodsResponse;

    CardConfiguration cardConfiguration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);

        layout = findViewById(R.id.layout);

        dropIn = false;

        currency = findViewById(R.id.currency);
        country = findViewById(R.id.countryCode);
        amount = findViewById(R.id.amount);
        dropInPay = findViewById(R.id.makePayment);


        cardConfiguration = new CardConfiguration.Builder(MainActivity.this, "10001|BD67BF5FDCD84F2CD99DD452A76AFC999CA060A187A9A527BA8F72743BAC53BA0362EA7A01AF4612A8FE7B736D9F5B02C65414707A4344D9A666AFCFF33DAB7C387B997D4FE1FABA3502BD604E8793F5CD657C19D822F133E09C48C360E302E5865641FB70DA304E4C067C95361F4181095E61DDF943518FD4811C85C81D7424AF5A0CB66C25E8F1285075F92C2EA4C143E8B1DF6DB2F1CF0B992B5DFBF7FD7419FCBA1421E87A245F42A52DE253E85FB2BE446911214A02A9B40168F354BAC7D742C77B3F99B093FDDD576896B53B32B35419AE9BF2F795D2A3CF2B27C8F466D8D77998B526842FFAF9F188DB5471079EC222590BA298E992C315AEEA44BCCF")
                .build();

        resultsIntent = new Intent(MainActivity.this, ResultsPage.class);

        Random rand = new Random();

        amount.setText(String.valueOf(1000 + rand.nextInt(9999)));



        dropInPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                curr = currency.getText().toString().toUpperCase();
                amo = amount.getText().toString();

//                GetPaymentMethods get = new GetPaymentMethods();
//                get.execute(curr,
//                        country.getText().toString().toUpperCase(),
//                        amount.getText().toString());

                getPaymentMethods(country.getText().toString().toUpperCase(),
                        curr,
                        amount.getText().toString());



            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    private class idealPay extends AsyncTask<String, PaymentMethodsResponse, PaymentMethodsResponse> {
//
//        protected PaymentMethodsResponse doInBackground(String... params) {
//
//        }
//
//        protected void onPostExecute(PaymentMethodsResponse response) {
//
//
//
//        }
//    }


    private void getPaymentMethods(String country, String currency, String amount){
        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);

        Call<JsonObject> call = getResponse.getPaymentMethods(country, currency, amount, "Android");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful()){
                    if (response.body() != null){
                        //String serverResponse = response.body();

                        Gson gson = new Gson();
                        String json = gson.toJson(response.body());

                        try {
                            jsonPayMethodsResponse = new JSONObject(json);

                            Log.v("PaymentMethodsResponse", response.body().toString());

                            PaymentMethodsApiResponse paymentMethodsApiResponse = PaymentMethodsApiResponse.SERIALIZER.deserialize(jsonPayMethodsResponse);


                            DropInConfiguration dropInConfiguration = new DropInConfiguration.Builder(MainActivity.this, YourDropInService.class)
                                    .addCardConfiguration(cardConfiguration)
                                    .build();




                            dropInConfiguration.setResultHandlerIntent(resultsIntent);

                            DropIn.startPayment(MainActivity.this, paymentMethodsApiResponse, dropInConfiguration);

                        }catch (JSONException err){
                            Log.d("Error", err.toString());
                        }


                    }
                }else {
                    Log.e("Unsuccessful", response.message());

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("Failure",t.toString());

            }
        });
    }


}
