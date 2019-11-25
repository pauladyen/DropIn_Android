package com.paul.mydropin;


import android.util.Log;

import com.adyen.checkout.dropin.service.CallResult;
import com.adyen.checkout.dropin.service.DropInService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.paul.mydropin.Network.ApiConfig;
import com.paul.mydropin.Network.AppConfig;
import com.paul.mydropin.Network.EncodingUtil;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Response;

public class YourDropInService extends DropInService {


    public static String paymentData;

    public static String type;

    @Override
    public CallResult makePaymentsCall(JSONObject paymentComponentData) {


        ApiConfig getResponse = AppConfig.getRetrofit().create(ApiConfig.class);

        String data;

        try {
            Log.v("PaymentComponentData", paymentComponentData.getJSONObject("paymentMethod").toString());
            data = EncodingUtil.encodeURIComponent(paymentComponentData.getJSONObject("paymentMethod").toString());
        }catch (JSONException e){
            Log.e("Exception", e.toString());
            data = null;
        }



        Call<JsonObject> call = getResponse.makePayment(data, MainActivity.curr, MainActivity.cc, MainActivity.amo, EncodingUtil.encodeURIComponent("adyencheckout://com.paul.mydropin"), "Android");

        try{
            Response<JsonObject> response = call.execute();

            if (response.isSuccessful() && response.body() != null){

                    String json = new Gson().toJson(response.body());


                    JSONObject paymentsResponse = new JSONObject(json);

                    Log.v("PaymentsResponse", paymentsResponse.toString(4));

                    if(json.contains("action")){

                        paymentData = paymentsResponse.getString("paymentData");

                        type = paymentsResponse.getJSONObject("action").getString("type")
                                + paymentsResponse.getJSONObject("action").getString("paymentMethodType");




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

            if(type.equalsIgnoreCase("redirectscheme")){
                call = getResponse.paymentDetails(
                        type,
                        actionComponentData.getJSONObject("details").getString("MD"),
                        actionComponentData.getJSONObject("details").getString("PaRes"),
                        paymentData
                );
            }else if(type.equalsIgnoreCase("redirectideal")){
                call = getResponse.paymentDetails(
                        type,
                        actionComponentData.getJSONObject("details").getString("payload")
                );
            }else if (type.equalsIgnoreCase("threeDS2Fingerprintscheme")){

                call = getResponse.paymentDetailsFingerPrint(
                        type,
                        actionComponentData.getJSONObject("details").getString("threeds2.fingerprint"),
                        paymentData
                );
            }else if (type.equalsIgnoreCase("threeDS2Challengescheme")){

                call = getResponse.paymentDetailsChallenge(
                        type,
                        actionComponentData.getJSONObject("details").getString("threeds2.challengeResult"),
                        paymentData
                );
            }else if (type.equalsIgnoreCase("redirectklarna_account")  ||  type.equalsIgnoreCase("redirectklarna_paynow")  ||  type.equalsIgnoreCase("redirectklarna")){

                call = getResponse.paymentDetailsKlarna(
                        type,
                        actionComponentData.getJSONObject("details").getString("redirectResult"),
                        paymentData
                );
            }else {
                call = null;
            }


            Response<JsonObject> response = call.execute();

            if (response.isSuccessful() && response.body() != null){


                String json = new Gson().toJson(response.body());


                JSONObject paymentsDetailResponse = new JSONObject(json);

                Log.v("PaymentsDetailsResponse", paymentsDetailResponse.toString(4));


                if(json.contains("action")){

                    paymentData = paymentsDetailResponse.getString("paymentData");

                    type = paymentsDetailResponse.getJSONObject("action").getString("type")
                            + paymentsDetailResponse.getJSONObject("action").getString("paymentMethodType");
                    return new CallResult(CallResult.ResultType.ACTION, paymentsDetailResponse.getJSONObject("action").toString());
                }else{
                    return new CallResult(CallResult.ResultType.FINISHED, json);
                }



            }else {
                Log.e("FailedDetailsResponse", response.message());
                return new CallResult(CallResult.ResultType.FINISHED, response.message());
            }


        }catch (Exception e){
            Log.e("DetailResponseException",e.toString());
            return new CallResult(CallResult.ResultType.FINISHED, e.toString());
        }

    }


    public static String generateString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}






