package com.noapp.accessreader;

import android.os.Bundle;

/** Dashboard de viagens com navegação premium completa. */
public class TripsActivityV2 extends TripsActivityPremium {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PremiumNavUpgrade.install(this, "Viagens");
    }

    @Override
    protected void onResume() {
        super.onResume();
        PremiumNavUpgrade.install(this, "Viagens");
    }
}
