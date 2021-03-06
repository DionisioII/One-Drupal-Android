package com.technikh.onedrupal.activities;

/*
 * Copyright (c) 2019. Nikhil Dubbaka from TechNikh.com under GNU AFFERO GENERAL PUBLIC LICENSE
 * Copyright and license notices must be preserved.
 * When a modified version is used to provide a service over a network, the complete source code of the modified version must be made available.
 */

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.technikh.onedrupal.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import com.technikh.onedrupal.adapter.SiteAdapter;
import com.technikh.onedrupal.models.Site;

public class FeaturedSitesActivity extends AppCompatActivity {

    private SiteAdapter adapter;
    private RecyclerView recyclerView;
    private String TAG = "FeaturedSitesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured_sites);

        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        toolbar.setTitle("Featured Sites");

        ArrayList<Site> empDataList = new ArrayList<Site>();
        empDataList.add(new Site("http://", "one-drupal-demo.technikh.com", "Demo Site - Search & Filters"));
        empDataList.add(new Site("https://", "content.dubbaka.com", "Nikhil Dubbaka Blog"));
        empDataList.add(new Site("http://", "app.eschool2go.org", "Demo Site - Image gallery"));
        generateEmployeeList(empDataList);
/*
        //Create handle for the RetrofitOneDrupalInstance interface
        GetSiteDataService service = RetrofitOneDrupalInstance.getRetrofitInstance().create(GetSiteDataService.class);

        //Call the method with parameter in the interface to get the employee data
        Call<SiteList> call = service.getEmployeeData(100);

        //Log the URL called
        Log.wtf("URL Called", call.request().url() + "");

        call.enqueue(new Callback<SiteList>() {
            @Override
            public void onResponse(Call<SiteList> call, Response<SiteList> response) {
                Log.d(TAG, "onResponse: "+response.toString());
                Log.d(TAG, "onResponse: body "+response.body().toString());
                ArrayList<Site> empDataList = response.body().getEmployeeArrayList();
                if(empDataList != null) {
                    generateEmployeeList(empDataList);
                }
            }

            @Override
            public void onFailure(Call<SiteList> call, Throwable t) {
                Toast.makeText(FeaturedSitesActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    /*Method to generate List of employees using RecyclerView with custom adapter*/
    private void generateEmployeeList(ArrayList<Site> empDataList) {
        Log.d(TAG, "generateEmployeeList: "+empDataList.size());
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_employee_list);

        adapter = new SiteAdapter(empDataList, FeaturedSitesActivity.this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(FeaturedSitesActivity.this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
    }
}
