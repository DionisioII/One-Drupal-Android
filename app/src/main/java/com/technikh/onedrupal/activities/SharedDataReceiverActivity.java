package com.technikh.onedrupal.activities;

/*
 * Copyright (c) 2019. Nikhil Dubbaka from TechNikh.com under GNU AFFERO GENERAL PUBLIC LICENSE
 * Copyright and license notices must be preserved.
 * When a modified version is used to provide a service over a network, the complete source code of the modified version must be made available.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;

import com.technikh.onedrupal.R;
import com.technikh.onedrupal.app.MyApplication;
import com.technikh.onedrupal.models.SettingsType;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SharedDataReceiverActivity extends AppCompatActivity {

    private static String TAG = "SharedDataReceiverActivity";
    private String nodeType = "";
    private boolean isRedirected;
    private String nodeTitle = "";
    private String nodeBody = "";
    private String nRemotePage = "";
    private String nRemoteImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_data_receiver);

        List<String> postTypeList = new ArrayList<String>();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
        for (int j = 0; j < MyApplication.gblNodeTypeSettings.size(); j++) {
            SettingsType modelNodeType = MyApplication.gblNodeTypeSettings.get(j);
            Log.d(TAG, "onClick: "+modelNodeType.getNodeType());
            if(modelNodeType.userHasAccesstoCreate()){
                postTypeList.add(modelNodeType.getNodeType());
                arrayAdapter.add(modelNodeType.getNodeType());
            }
        }
        Log.d(TAG, "onClick: size "+postTypeList.size());
        if(postTypeList.size() == 1) {
            nodeType = postTypeList.get(0);
            processNodeType();
        }else if(postTypeList.size() > 1) {
            Log.d(TAG, "onCreate: AlertDialog");
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setIcon(R.mipmap.ic_launcher);
            builderSingle.setTitle("Select A content type to POST:-");

            builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    nodeType = arrayAdapter.getItem(which);
                    processNodeType();
                }
            });
            builderSingle.show();
        }
    }

    private void processNodeType(){
        Log.d(TAG, "onClick: "+nodeType);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        Log.d(TAG, "onClick: action "+action+" type "+type+" getComponent "+intent.getComponent()+" getData "+intent.getData()+" getPackage "+intent.getPackage());
        if (!Intent.ACTION_SEND.equals(action) || type == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        Log.d(TAG, "onClick: extras "+extras.toString());
        String linkAndText ="";
        for (String key : extras.keySet()) {
            try {
                Object extraValue = extras.get(key);
                Log.d(TAG, "extras key " + key + " value: " + extraValue);
                if (extraValue instanceof String) {
                    if (extraValue != null && extraValue.toString().contains("http")) {
                        linkAndText = extraValue.toString();
                    } else {
                        if (key.equals("android.intent.extra.SUBJECT")) {
                            nodeTitle = extraValue.toString();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }


                    /*
                    link Ars Technica: Many websites threatened by highly critical code-execution bug in Drupal.
    https://arstechnica.com/information-technology/2019/02/millions-of-websites-threatened-by-highly-critical-code-execution-bug-in-drupal/
                     */
        // if link doesn't start with http and contains http then link has title + URL
        Log.d(TAG, "onClick: linkAndText "+linkAndText);
        String link="";
        if (!linkAndText.startsWith("http") && linkAndText.contains("http")){
            int i = linkAndText.indexOf("http");
            link = linkAndText.substring(i);
            Log.d(TAG, "onClick: link "+link);
            if(nodeTitle == null || nodeTitle.isEmpty()) {
                nodeTitle = linkAndText.substring(0, i);
            }
        }else if (linkAndText.startsWith("http")) {
            link = linkAndText;
        }
        if(nodeTitle == null || nodeTitle.isEmpty()) {
            nodeTitle = extras.getString(Intent.EXTRA_SUBJECT);
            Log.d(TAG, "processNodeType: nodeTitle "+nodeTitle);
        }
        if(link != null && !link.isEmpty()){
            ProgressDialog pd = new ProgressDialog(SharedDataReceiverActivity.this);
            pd.setMessage("Intelligently fetching meta data like title, description, image... for the post!");
            pd.show();
            Log.d(TAG, "onClick: postTitle "+nodeTitle);
            nRemotePage = link;
            WebView webView = findViewById(R.id.shared_intent_webview);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setLoadsImagesAutomatically(false);
            webView.getSettings().setBlockNetworkImage(true);

            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);
            webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            webView.setScrollbarFadingEnabled(false);
            String ua = webView.getSettings().getUserAgentString();
            String androidOSString = webView.getSettings().getUserAgentString().substring(ua.indexOf("("), ua.indexOf(")") + 1);
            String newUserAgent = webView.getSettings().getUserAgentString().replace(androidOSString, "(X11; Linux x86_64)");
            //String newUA= "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
            webView.getSettings().setUserAgentString(newUserAgent);

            //webView.getSettings().setBlockNetworkLoads (true);
            Log.d(TAG, "111 processNodeType: webView.loadUrl(link) "+link);
            Log.d(TAG, "111 processNodeType: Uri.parse (link) "+Uri.parse(link));
            webView.loadUrl(link);

            //webView.addJavascriptInterface(new JsInterface(), "CC_FUND");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {


                    isRedirected = false;
                    super.onPageStarted(view, url, favicon);
                }/*
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url){
                    // do your handling codes here, which url is the requested url
                    // probably you need to open that url rather than redirect:
                    Log.d(TAG, "111 shouldOverrideUrlLoading: url "+url);
                    view.loadUrl(url);
                    isRedirected = true;
                    return false; // then it is not handled by default action
                }*/
                @Override
                public void onPageFinished(WebView view, String url) {
                    Log.d(TAG, "111 onPageFinished: url "+url);
                    if (url.startsWith("intent")) {
// https://www.zidsworld.com/android-development/how-to-fix-unknown-url-scheme-in-android-webview/

                        try {
                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

                            String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                            Log.d(TAG, "111 onPageFinished: fallbackUrl "+fallbackUrl);
                            if (fallbackUrl != null) {
                                view.loadUrl(fallbackUrl);
                                return;
                            }
                        } catch (URISyntaxException e) {
                            //not an intent uri
                            Log.d(TAG, "onPageFinished: URISyntaxException "+e.getMessage());
                        }
                    }
                    if (!isRedirected) {
                        Log.d(TAG, "111 onPageFinished: not isRedirected "+url);
                    }
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        Log.d(TAG, "onClick: evaluateJavascript");
                        String javaScriptCode = "(function() { return (document.querySelector('meta[property~=\"og:description\"]').content); })();";
                        Log.d(TAG, "onPageFinished: url "+url);
                        if(url.contains("https://m.youtube.com") || url.contains("youtube.com")){
                            javaScriptCode = "(function() { return (document.getElementsByClassName(\"slim-video-metadata-description\").item(0).innerText); })();";
                        }
                        Log.d(TAG, "onPageFinished: javaScriptCode "+javaScriptCode);
                        view.evaluateJavascript(
                                javaScriptCode,
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(final String html) {
                                        Log.d(TAG, "onReceiveValue: "+html);
                                        if(html != null && !html.isEmpty() && html.length() >= 5) {
                                            nodeBody = html;
                                        }

                                    }
                                });
                        // <meta content="http://www.quickmeme.com/img/80/8084330f93a02be5098d2c6d1c5021914b2be71d71325a2c12b0e3af1c9d48df.jpg" itemprop="image">
                        // itemprop="image"
                        javaScriptCode = "(function() { return (document.querySelector('meta[property~=\"og:image\"]').content); })();";
                        Log.d(TAG, "og:image onPageFinished: "+javaScriptCode);
                        view.evaluateJavascript(
                                javaScriptCode,
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(final String html) {
                                        Log.d(TAG, "og:image onReceiveValue: "+html);
                                        if(html != null && !html.isEmpty() && html.length() >= 5) {
                                            nRemoteImage = html.replaceAll("^\"|\"$", "");
                                        }
                                        // TODO: if null, take page screenshots -> https://stackoverflow.com/questions/9745988/how-can-i-programmatically-take-a-screenshot-of-a-webview-capturing-the-full-pa
                                    }
                                });

                        if(nodeTitle == null || nodeTitle.isEmpty()) {
                            javaScriptCode = "(function() { return (document.querySelector('meta[property~=\"og:title\"]').content); })();";
                            Log.d(TAG, "og:title onPageFinished: "+javaScriptCode);
                            view.evaluateJavascript(
                                    javaScriptCode,
                                    new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(final String html) {
                                            Log.d(TAG, "og:title onReceiveValue: "+html);
                                            if(html != null && !html.isEmpty() && html.length() >= 5) {
                                                nodeTitle = html.replaceAll("^\"|\"$", "");
                                            }
                                        }
                                    });
                        }

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    handleSendLink(intent);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                pd.dismiss();
                            }
                        }, 2000);


                    }
                    super.onPageFinished(view, url);

                }
                @Override
                public void onReceivedError(WebView view, int errorCode,
                                            String description, String failingUrl) {
                    Log.d(TAG, "onReceivedError: "+errorCode+description);
                }
            });

            Set<String> categories = intent.getCategories();
            if(categories != null) {
                Log.d(TAG, "onClick: categories " + categories.toString());
            }
        }



        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                //handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                //handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    private class JsInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String content) {
            Log.d(TAG, "processHTML: "+content);
            //handle content
            AlertDialog alertDialog = new AlertDialog.Builder(SharedDataReceiverActivity.this).create();
            alertDialog.setTitle("link title");
            alertDialog.setMessage(content);
            alertDialog.setCancelable(true);
            alertDialog.show();
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            // Update UI to reflect image being shared
            Bundle b = new Bundle();
            b.putString("nodeType", nodeType);
            b.putString("nImage", imageUri.toString());
            Intent intent1 = new Intent(this, ActivityPost.class);
            intent1.putExtras(b);
            startActivity(intent1);
        }
    }

    void handleSendLink(Intent intent) {
        Log.d(TAG, "handleSendLink: ");
        Bundle b = new Bundle();
        b.putString("nodeType", nodeType);

        SettingsType NodeTypeObj = MyApplication.gblGetNodeTypeObj(nodeType);
        String remoteVideoField = NodeTypeObj.getFieldsList().remote_video;
        if(remoteVideoField != null && !remoteVideoField.isEmpty() && nRemotePage != null && !nRemotePage.isEmpty()){
            b.putString("nRemoteVideo", nRemotePage);
        }

        if(nodeBody != null && !nodeBody.isEmpty()){
            Log.d(TAG, "handleSendLink: setting body to "+ nodeBody);
            b.putString("nBody", nodeBody);
        }
        if(nodeTitle != null && !nodeTitle.isEmpty()){
            b.putString("nTitle", nodeTitle);
        }
        if(nRemoteImage != null && !nRemoteImage.isEmpty()){
            b.putString("nRemoteImage", nRemoteImage);
        }
        Log.d(TAG, "handleSendLink: nRemotePage "+nRemotePage);
        if(nRemotePage != null && !nRemotePage.isEmpty()){
            b.putString("nRemotePage", nRemotePage);
        }
        try {
            Intent intent1 = new Intent(SharedDataReceiverActivity.this, ActivityPost.class);
            intent1.putExtras(b);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
