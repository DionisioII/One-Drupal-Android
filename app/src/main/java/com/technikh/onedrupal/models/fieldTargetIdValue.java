package com.technikh.onedrupal.models;

/*
 * Copyright (c) 2019. Nikhil Dubbaka from TechNikh.com under GNU AFFERO GENERAL PUBLIC LICENSE
 * Copyright and license notices must be preserved.
 * When a modified version is used to provide a service over a network, the complete source code of the modified version must be made available.
 */

/*
	"type": [{
		"target_id": "article"
	}],
 */
import com.google.gson.annotations.SerializedName;

public class fieldTargetIdValue {
    @SerializedName("target_id")
    private String mValue;

    public String getValue() {
        return mValue;
    }

    public void setValue(String val) {
        mValue = val;
    }
}
