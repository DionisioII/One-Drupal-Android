package com.technikh.onedrupal.activities;

/*
 * Copyright (c) 2019. Nikhil Dubbaka from TechNikh.com under GNU AFFERO GENERAL PUBLIC LICENSE
 * Copyright and license notices must be preserved.
 * When a modified version is used to provide a service over a network, the complete source code of the modified version must be made available.
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.technikh.onedrupal.BuildConfig;
import com.technikh.onedrupal.R;
import com.technikh.onedrupal.app.MyApplication;
import com.technikh.onedrupal.authenticator.AuthPreferences;
import com.technikh.onedrupal.helpers.PDRestClient;
import com.technikh.onedrupal.helpers.PDUtils;
import com.technikh.onedrupal.models.ModelFanPosts;
import com.technikh.onedrupal.models.ModelNodeType;
import com.technikh.onedrupal.models.SettingsType;
import com.technikh.onedrupal.models.nodeData;
import com.technikh.onedrupal.models.nodeDeserializer;
import com.technikh.onedrupal.network.AddCookiesInterceptor;
import com.technikh.onedrupal.network.GetDrupalNodeDataService;
import com.technikh.onedrupal.network.GetSiteDataService;
import com.technikh.onedrupal.network.RetrofitDrupalNodeInstance;
import com.technikh.onedrupal.network.RetrofitSiteInstance;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityFanPostDetails extends ActivityBase {

    private static String TAG = "ActivityFanPostDetails";
    Toolbar toolbar;
    ImageView iv_a_fan_post_details_user_image, iv_a_fan_post_details_image;
    Button btnPostActionPublish, btnPostActionPromote;
    private boolean isPublished, isPromoted;
    TextView tv_a_fan_post_details_title, tv_a_fan_post_details_body, tv_a_fan_post_details_category, no_connection_text;
    Button tv_row_read_more;
    LinearLayout no_connection_ll;
    ScrollView sv_main;
    String response_error = "";
    String nid = "";
    private String nImageURL = "";
    private String nID = "";
    private String nodeType = "";


    private AuthPreferences mAuthPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_posts_details);

        mAuthPreferences = new AuthPreferences(this);

        nid = getIntent().getStringExtra("nid");
        init();
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Post Detail");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        iv_a_fan_post_details_user_image = findViewById(R.id.iv_a_fan_post_details_user_image);
        iv_a_fan_post_details_image = findViewById(R.id.iv_a_fan_post_details_image);
        tv_a_fan_post_details_title = findViewById(R.id.tv_a_fan_post_details_title);
        tv_row_read_more = findViewById(R.id.tv_row_read_more);
        tv_a_fan_post_details_body = findViewById(R.id.tv_a_fan_post_details_body);
        tv_a_fan_post_details_category = findViewById(R.id.tv_a_fan_post_details_category);
        no_connection_ll = findViewById(R.id.no_connection_ll);
        no_connection_text = findViewById(R.id.no_connection_text);
        sv_main = findViewById(R.id.sv_main);
        btnPostActionPublish = findViewById(R.id.btnPostActionPublish);
        btnPostActionPromote = findViewById(R.id.btnPostActionPromote);

        String site_domain = getIntent().getStringExtra("SiteDomain");
        String site_protocol = getIntent().getStringExtra("SiteProtocol");
        requestNewsList(nid, site_protocol, site_domain);
    }

    private void drupalNodeEditProperty(String nid, boolean status, String field) {
        _progressDialogAsync.show();
        Log.d(TAG, "drupalNodeEditProperty: " + nid);
        GetSiteDataService service = RetrofitSiteInstance.getRetrofitSiteInstance(mAuthPreferences.getPrimarySiteProtocol(), mAuthPreferences.getPrimarySiteUrl(), null).create(GetSiteDataService.class);
        //Call the method with parameter in the interface to get the employee data
        ModelNodeType nodeObj = new ModelNodeType();
        if (field.equals("status")) {
            nodeObj.setPublished(status);
        } else if (field.equals("promote")) {
            nodeObj.setPromoted(status);
        }
        nodeObj.setNodeType(nodeType);
        retrofit2.Call<ModelNodeType> call = service.postNodeEdit(nid, nodeObj);

        Log.d("URL Called", call.request().url() + "");
        call.enqueue(new retrofit2.Callback<ModelNodeType>() {
            @Override
            public void onResponse(retrofit2.Call<ModelNodeType> call, retrofit2.Response<ModelNodeType> response) {
                if (response.isSuccessful()) {
                    _progressDialogAsync.cancel();
                    String success_message = "";
                    if (field.equals("status")) {
                        success_message = "Published";
                        if (!status) {
                            success_message = "Unpublished";
                        }
                    } else if (field.equals("promote")) {
                        success_message = "Promoted";
                        if (!status) {
                            success_message = "Demoted";
                        }
                    }
                    Log.d(TAG, "onResponse: isSuccessful " + response.body().toString());
                    new AlertDialog.Builder(ActivityFanPostDetails.this)
                            .setTitle(getString(R.string.dialog_api_success_title))
                            .setMessage(getString(R.string.dialog_api_success_message_prefix) + " " + success_message)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                } else {
                    _progressDialogAsync.cancel();
                    Log.d(TAG, "onResponse: " + response.toString());
                    Log.d(TAG, "onResponse: call body" + call.request().body().toString());
                    new AlertDialog.Builder(ActivityFanPostDetails.this)
                            .setTitle(getString(R.string.dialog_api_failed_title))
                            .setMessage(getString(R.string.dialog_api_failed_message_prefix) + response.toString())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ModelNodeType> call, Throwable t) {
                _progressDialogAsync.cancel();
                Log.d(TAG, "onFailure: " + t.getMessage());
                new AlertDialog.Builder(ActivityFanPostDetails.this)
                        .setTitle(getString(R.string.dialog_api_failed_title))
                        .setMessage(getString(R.string.dialog_api_failed_message_prefix) + t.getMessage())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                //Toast.makeText(MyApplication.getAppContext(), "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });
        _progressDialogAsync.cancel();
    }

    public void onClickBtnNodeActionPublish(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String publishActionButtonLabel = getString(R.string.drupal_publish);
        if (isPublished) {
            publishActionButtonLabel = getString(R.string.drupal_unpublish);
        }
        builder.setMessage("Are you sure you want to " + publishActionButtonLabel + " the content " + tv_a_fan_post_details_title.getText() + "?").setPositiveButton("Yes", actionPublishDialogClickListener)
                .setNegativeButton("No", actionPublishDialogClickListener).show();
    }

    public void onClickBtnNodeActionPromote(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String publishActionButtonLabel = getString(R.string.drupal_promote);
        if (isPromoted) {
            publishActionButtonLabel = getString(R.string.drupal_demote);
        }
        builder.setMessage("Are you sure you want to " + publishActionButtonLabel + " the content " + tv_a_fan_post_details_title.getText() + "?").setPositiveButton("Yes", actionPromoteDialogClickListener)
                .setNegativeButton("No", actionPromoteDialogClickListener).show();
    }

    DialogInterface.OnClickListener actionPublishDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    drupalNodeEditProperty(nid, !isPublished, "status");
                    //finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    dialog.dismiss();
                    break;
            }
        }
    };

    DialogInterface.OnClickListener actionPromoteDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    drupalNodeEditProperty(nid, !isPromoted, "promote");
                    //finish();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    dialog.dismiss();
                    break;
            }
        }
    };

    public void onClickBtnNodeEdit(View v) {
        Bundle b = new Bundle();
        b.putBoolean("editMode", true);
        b.putString("nTitle", tv_a_fan_post_details_title.getText().toString());
        b.putString("nBody", tv_a_fan_post_details_body.getText().toString());
        b.putString("nImage", nImageURL);
        b.putString("nID", nID);
        b.putString("nodeType", nodeType);

        Intent intent = new Intent(context, ActivityPost.class);
        intent.putExtras(b);
        startActivity(intent);
    }

    private void requestNewsList(String nodeId, String site_protocol, String site_domain) {
        if (site_domain == null || site_domain.isEmpty()) {
            Log.d(TAG, "requestNewsList: site_domain empty");
            site_domain = mAuthPreferences.getPrimarySiteUrl();
            Log.d(TAG, "requestNewsList: site_domain " + site_domain);
            site_protocol = mAuthPreferences.getPrimarySiteProtocol();
        }
        _progressDialogAsync.show();
        //String url = site_protocol+site_domain+"/node/" + nodeId + "?_format=json";
        //Log.d(TAG, "requestNewsList: "+url);
/*
        Gson gson =
                new GsonBuilder()
                        .registerTypeAdapter(nodeData.class, new nodeDeserializer())
                        .create();
        Log.d(TAG, "requestNewsList: gson"+gson.toString());
        GetDrupalNodeDataService service = RetrofitDrupalNodeInstance.getRetrofitDrupalNodeInstance(site_protocol, site_domain).create(GetDrupalNodeDataService.class);
        //Call the method with parameter in the interface to get the employee data
        retrofit2.Call<ModelNodeType> call = service.getNode(nodeId);
        Log.d("URL Called", call.request().url() + "");

        call.enqueue(new retrofit2.Callback<ModelNodeType>() {
            @Override
            public void onResponse(retrofit2.Call<ModelNodeType> call, retrofit2.Response<ModelNodeType> response) {
                if(response.isSuccessful()) {
                    try {
                        ModelNodeType nodeObj = response.body();
                        if (nodeObj.getInt(nodeObj.nid) <= 0) {
                            response_error = "No response from server.";
                        } else {
                            //ModelFanPosts modelFanPosts = new ModelFanPosts(result);
                            if(nodeObj.isPublished()) {
                                isPublished = true;
                                btnPostActionPublish.setText("Unpublish");
                            }
                            if(nodeObj.isPromoted()) {
                                isPromoted = true;
                                btnPostActionPromote.setText("Demote");
                            }
                            nodeType = nodeObj.getTargetId(nodeObj.nodeType);
                            nID = nodeObj.getInt(nodeObj.nid);
                            SettingsType NodeTypeObj = MyApplication.gblGetNodeTypeObj(nodeType);
                            String fieldNameBody = NodeTypeObj.getFieldsList().getFieldBody();
                            Log.d(TAG, "onResponse: fieldNameBody "+fieldNameBody);
                            if(!fieldNameBody.isEmpty()) {

                            }

                            tv_a_fan_post_details_title.setText(nodeObj.getString(nodeObj.title));
                            tv_a_fan_post_details_body.setText(Html.fromHtml(modelFanPosts.getBody()));
                            //tv_a_fan_post_details_category.setText(modelFanPosts.getField_text_category());
                            nImageURL = modelFanPosts.getField_image();
                            //nID = modelFanPosts.getNid();

                            Glide.with(context)
                                    .load(modelFanPosts.getField_image())
                                    .into(iv_a_fan_post_details_image);
                            String video_url = modelFanPosts.field_video;
                            if(!video_url.isEmpty()) {
                                tv_row_read_more.setVisibility(View.VISIBLE);
                                tv_row_read_more.setText("Watch Video...");
                                tv_row_read_more.setOnClickListener(new View.OnClickListener() {

                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(video_url));
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                });
                            }
                            String remote_page_url = modelFanPosts.field_remote_page;
                            if(!remote_page_url.isEmpty()) {
                                tv_row_read_more.setVisibility(View.VISIBLE);
                                tv_row_read_more.setText("Read More...");
                                tv_row_read_more.setOnClickListener(new View.OnClickListener() {

                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(remote_page_url));
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        response_error = getString(R.string.unable_to_connect);
                        e.printStackTrace();
                    }
                }else{
                    //Log.d(TAG, "onResponse: is not Successful");
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle(getString(R.string.dialog_api_failed_title))
                            .setMessage(getString(R.string.dialog_api_failed_message_prefix)+response.message()+"Code: "+response.code())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    // TODO: throw alert dialog as the user doesn't have access to settings API REST resource permission
                }
                _progressDialogAsync.cancel();
            }

            @Override
            public void onFailure(retrofit2.Call<ModelNodeType> call, Throwable t) {
                _progressDialogAsync.cancel();
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle(getString(R.string.dialog_api_failed_title))
                        .setMessage(getString(R.string.dialog_api_failed_message_prefix)+t.getMessage())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                //Toast.makeText(MyApplication.getAppContext(), "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });

*/
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(new AddCookiesInterceptor())
                //.addInterceptor(new ReceivedCookiesInterceptor())
                .build();
        String url = site_protocol + site_domain + "/node/" + nodeId + "?_format=json";
        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                // .addHeader("X-CSRF-Token", xCSRFToken)
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _progressDialogAsync.cancel();
                        new AlertDialog.Builder(ActivityFanPostDetails.this)
                                .setTitle("Error")
                                .setMessage("API returned error: " + e.toString())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String respoStr = response.body().string();
                Log.d("GET Content", "onResponse: "+respoStr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _progressDialogAsync.cancel();
                        try {
                            //Log.d(TAG, "run: "+res);
                            JSONObject responseBodyObj = new JSONObject(respoStr);
                            ModelFanPosts modelFanPosts = new ModelFanPosts(responseBodyObj);
                            if (modelFanPosts.isPublished()) {
                                isPublished = true;
                                btnPostActionPublish.setText("Unpublish");
                            }
                            if (modelFanPosts.isPromoted()) {
                                isPromoted = true;
                                btnPostActionPromote.setText("Demote");
                            }

                            tv_a_fan_post_details_title.setText(modelFanPosts.getTitle());
                            tv_a_fan_post_details_body.setText(Html.fromHtml(modelFanPosts.getBody()));
                            tv_a_fan_post_details_category.setText(modelFanPosts.getField_text_category());
                            nImageURL = modelFanPosts.getField_image();
                            nID = modelFanPosts.getNid();
                            nodeType = modelFanPosts.getNodeType();
                            Glide.with(context)
                                    .load(modelFanPosts.getField_image())
                                    .into(iv_a_fan_post_details_image);
                            String video_url = modelFanPosts.field_video;
                            if (!video_url.isEmpty()) {
                                tv_row_read_more.setVisibility(View.VISIBLE);
                                tv_row_read_more.setText("Watch Video...");
                                tv_row_read_more.setOnClickListener(new View.OnClickListener() {

                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(video_url));
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                });
                            }
                            String remote_page_url = modelFanPosts.field_remote_page;
                            if (!remote_page_url.isEmpty()) {
                                tv_row_read_more.setVisibility(View.VISIBLE);
                                tv_row_read_more.setText("Read More...");
                                tv_row_read_more.setOnClickListener(new View.OnClickListener() {

                                    public void onClick(View v) {
                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(remote_page_url));
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}