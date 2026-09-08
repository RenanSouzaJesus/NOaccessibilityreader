package com.noapp.accessreader;

import android.os.Bundle;

/** Campanhas isoladas da área de viagens e oportunidades. */
public class CampaignsTripsActivity extends CampaignsActivityPremium {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumNavUpgrade.install(this, "Campanhas");
    }

    @Override
    protected void onResume() {
        super.onResume();
        PremiumNavUpgrade.install(this, "Campanhas");
    }
}
