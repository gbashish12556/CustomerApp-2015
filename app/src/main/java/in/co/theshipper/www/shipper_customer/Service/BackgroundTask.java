package in.co.theshipper.www.shipper_customer.Service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import in.co.theshipper.www.shipper_customer.Constants;
import in.co.theshipper.www.shipper_customer.Activities.EditProfile;
import in.co.theshipper.www.shipper_customer.Activities.CompleteActivity;
import in.co.theshipper.www.shipper_customer.Helper;
import in.co.theshipper.www.shipper_customer.Activities.OtpVerification;

/**
 * Created by GB on 3/15/2016.
 */
public class BackgroundTask extends AsyncTask<String,Void, String> {

    private String method = "None";
    private String return_param = "Failed";
    private String JSON_STRING = "";
    private Context ctx;
    BackgroundTask(Context ctx){
        this.ctx = ctx;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public String doInBackground(String... params) {
        method = params[0];
        String user_token = Helper.getPreference(ctx,"user_token");

        if(method.equals("register"))
        {

            String reg_url = Constants.Config.ROOT_PATH+"customer_registration";
            String mobile_no = params[1];
            Random ran = new Random();
            int otp= (100000 + ran.nextInt(900000));
            Helper.putPreference(ctx,"OTP",String.valueOf(otp));
            Helper.putPreference(ctx,"mobile_no",String.valueOf(mobile_no));

            try {

                String data = URLEncoder.encode("mobile_no", "UTF-8")+"="+URLEncoder.encode(mobile_no,"UTF-8")+"&"+
                        URLEncoder.encode("OTP","UTF-8")+"="+URLEncoder.encode(String.valueOf(otp),"UTF-8");
                JSON_STRING = Helper.AndroidToServer(ctx, reg_url,data);

            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();

            }

        }
        else if(method.equals("get_customer_info"))
        {

            String mobile_no = "";
            String get_user_info_url = Constants.Config.ROOT_PATH+"get_customer_info";
            mobile_no =  Helper.getPreference(ctx,"mobile_no");

            try {

                String data = URLEncoder.encode("mobile_no","UTF-8")+"="+URLEncoder.encode(mobile_no, "UTF-8");
                JSON_STRING =  Helper.AndroidToServer(ctx,get_user_info_url,data);

            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();

            }

        }
        else if(method.equals("edit_customer_profile"))
        {

            String edit_customer_profile_url = Constants.Config.ROOT_PATH+"edit_customer_profile";
            String name = params[1];
            String email = params[2];
            String postal_address = params[3];
            Helper.putPreference(ctx,"name",name);

            try {

                String data = URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8")+"&"+
                        URLEncoder.encode("email","UTF-8")+"="+URLEncoder.encode(email,"UTF-8")+"&"+
                        URLEncoder.encode("postal_address","UTF-8")+"="+URLEncoder.encode(postal_address,"UTF-8")+"&"+
                        URLEncoder.encode("user_token","UTF-8")+"="+URLEncoder.encode(user_token,"UTF-8");
                JSON_STRING = Helper.AndroidToServer(ctx,edit_customer_profile_url,data);

            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();

            }

        }
        else if(method.equals("update_customer_location"))
        {

            String update_customer_location_url = Constants.Config.ROOT_PATH+"update_customer_location";
            String lattitude = params[1];
            String longitude = params[2];

            try {

                String data = URLEncoder.encode("lattitude","UTF-8")+"="+URLEncoder.encode(lattitude,"UTF-8")+"&"+
                        URLEncoder.encode("longitude","UTF-8")+"="+URLEncoder.encode(longitude,"UTF-8")+"&"+
                        URLEncoder.encode("user_token","UTF-8")+"="+URLEncoder.encode(user_token,"UTF-8");
                JSON_STRING = Helper.AndroidToServer(ctx,update_customer_location_url,data);

            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();

            }

        }
        else if(method.equals("book_now"))
        {

            String book_now_url = Constants.Config.ROOT_PATH+"book_now";
            String vehicle_type = params[1];
            String datetime = Helper.getDateTimeNow();

            try {

                String data = URLEncoder.encode("user_token","UTF-8")+"="+URLEncoder.encode(user_token,"UTF-8")+"&"+
                        URLEncoder.encode("vehicle_type","UTF-8")+"="+URLEncoder.encode(vehicle_type,"UTF-8")+"&"+
                        URLEncoder.encode("datetime","UTF-8")+"="+URLEncoder.encode(datetime,"UTF-8");
                JSON_STRING = Helper.AndroidToServer(ctx,book_now_url,data);

            } catch (UnsupportedEncodingException e) {

                e.printStackTrace();

            }

        }
        return JSON_STRING;

    }

    @Override
    public void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    public void onPostExecute(String result) {
        super.onPostExecute(result);

        if (method.equals("register")) {

            Intent intent = new Intent(ctx, OtpVerification.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ctx.startActivity(intent);

        }
        if(!Helper.CheckJsonError(result))
        {

            if (method.equals("get_customer_info")) {

                Intent intent = new Intent(ctx, EditProfile.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("JSON_STRING", result);
                ctx.startActivity(intent);

            } else if (method.equals("edit_customer_profile")) {

                Intent intent = new Intent(ctx, CompleteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ctx.startActivity(intent);

            }

        }
        else{

            Helper.Toast(ctx,"Error While Connecting to Server");

        }

    }

    public String getCrnNo(String JsonString){

        String errFlag;
        String received_crn_no = "DefaultIsNothing";
        JSONObject jsonObject;
        JSONArray jsonArray;

        try {

            jsonObject = new JSONObject(JsonString);
            errFlag = jsonObject.getString("errFlag");
            if(errFlag.equals("0"))
            {

                if(jsonObject.has("likes")) {

                    jsonArray = jsonObject.getJSONArray("likes");
                    int count = 0;

                    while (count < jsonArray.length()) {

                        Helper.logD("likes_entered", "likes_entered");
                        JSONObject JO = jsonArray.getJSONObject(count);
                        received_crn_no = JO.getString("crn_no");
                        count++;

                    }

                }

            }

        } catch (JSONException e) {

            e.printStackTrace();

        }

         return received_crn_no;
    }

}