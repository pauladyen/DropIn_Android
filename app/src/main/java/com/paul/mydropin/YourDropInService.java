package com.paul.mydropin;


import android.util.Log;
import com.adyen.checkout.dropin.service.CallResult;
import com.adyen.checkout.dropin.service.DropInService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paul.mydropin.Network.ApiConfig;
import com.paul.mydropin.Network.AppConfig;
import com.paul.mydropin.Network.EncodingUtil;
import org.json.JSONObject;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Response;

public class YourDropInService extends DropInService {


    public static String paymentData;

    public static String paymentMethodType;

    @Override
    public CallResult makePaymentsCall(JSONObject paymentComponentData) {


        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);

        String data = EncodingUtil.encodeURIComponent(paymentComponentData.toString());

        Log.v("PaymentComponentData", data);

        Call<JsonObject> call = getResponse.makePayment(data, MainActivity.curr, MainActivity.amo, EncodingUtil.encodeURIComponent("adyencheckout://com.paul.mydropin"));

        try{
            Response<JsonObject> response = call.execute();

            if (response.isSuccessful() && response.body() != null){

                    String json = new Gson().toJson(response.body());


                    JSONObject paymentsResponse = new JSONObject(json);

                    Log.v("PaymentsResponse", paymentsResponse.toString(4));

                    if(json.contains("action")){

                        paymentData = paymentsResponse.getString("paymentData");

                        paymentMethodType = paymentsResponse.getJSONObject("action").getString("paymentMethodType");

                        return new CallResult(CallResult.ResultType.ACTION, paymentsResponse.getJSONObject("action").toString());
                    }else{
                        return new CallResult(CallResult.ResultType.FINISHED, json);
                    }


            }else {
                Log.e("PaymentsResponse", response.message());
                return new CallResult(CallResult.ResultType.FINISHED, response.message());
            }



        }catch (Exception e){
            Log.e("PaymentsResponse",e.toString());
            return new CallResult(CallResult.ResultType.FINISHED, e.toString());
        }
    }

    @Override
    public CallResult makeDetailsCall(JSONObject actionComponentData) {

        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);

        try{

            Log.v("actionData",actionComponentData.toString(4));
        Call<JsonObject> call;

        if(paymentMethodType.equalsIgnoreCase("scheme")){

            call = getResponse.paymentDetails(
                    paymentMethodType,
                    actionComponentData.getJSONObject("details").getString("MD"),
                    actionComponentData.getJSONObject("details").getString("PaRes"),
                    paymentData
            );

        }else{
            call = getResponse.paymentDetails(
                    paymentMethodType,
                    actionComponentData.getJSONObject("details").getString("payload")
            );
        }

        //String data = EncodingUtil.encodeURIComponent(paymentComponentData.toString());

            Response<JsonObject> response = call.execute();

            if (response.isSuccessful() && response.body() != null){

                String json = new Gson().toJson(response.body());

                JSONObject paymentsResponse = new JSONObject(json);

                Log.v("PaymentsDetailsResponse", paymentsResponse.toString(4));


                return new CallResult(CallResult.ResultType.FINISHED, json);


            }else {
                Log.e("PaymentsDetailsResponse", response.message());
                return new CallResult(CallResult.ResultType.FINISHED, response.message());
            }



        }catch (Exception e){
            Log.e("PaymentsDetailsResponse",e.toString());
            return new CallResult(CallResult.ResultType.FINISHED, e.toString());
        }

    }


    public static String generateString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}






