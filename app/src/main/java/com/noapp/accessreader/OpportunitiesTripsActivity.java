package com.noapp.accessreader;

import android.os.Bundle;

/** Oportunidades com navegação independente para Viagens e Campanhas. */
public class OpportunitiesTripsActivity extends MainActivityPremium {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumNavUpgrade.install(this, "Oportunidades");
    }

    @Override
    protected void onResume() {
        super.onResume();
        PremiumNavUpgrade.install(this, "Oportunidades");
    }
}
