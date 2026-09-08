package com.noapp.accessreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Camada de evolução da Central que adiciona acesso à análise da campanha
 * sem quebrar a lógica existente do MainActivity.
 */
public class MainActivityV2 extends MainActivity {

    private static final String ANALYSIS_TAG = "no_media_analysis_button";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshCampaignActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCampaignActions();
    }

    private void refreshCampaignActions() {
        View root = findViewById(android.R.id.content);
        Button createButton = findButtonByText(root, "Criar campanha", "Criar nova campanha");
        if (createButton == null) return;

        View parent = (View) createButton.getParent();
        if (!(parent instanceof LinearLayout)) return;
        LinearLayout card = (LinearLayout) parent;

        SharedPreferences prefs = getSharedPreferences("no_media", MODE_PRIVATE);
        String requestId = prefs.getString("last_request_id", "");
        boolean hasCampaign = !TextUtils.isEmpty(requestId);

        Button analysisButton = findTaggedButton(card);

        if (!hasCampaign) {
            if (analysisButton != null) card.removeView(analysisButton);
            createButton.setText("Criar campanha");
            createButton.setTextColor(getColor(R.color.no_navy_deep));
            createButton.setBackgroundResource(R.drawable.bg_no_button_primary);
            return;
        }

        createButton.setText("Criar nova campanha");
        createButton.setTextColor(getColor(R.color.no_white));
        createButton.setBackgroundResource(R.drawable.bg_no_button_secondary);

        if (analysisButton == null) {
            analysisButton = new Button(this);
            analysisButton.setTag(ANALYSIS_TAG);
            analysisButton.setText("Ver campanha e análise");
            analysisButton.setAllCaps(false);
            analysisButton.setTextSize(13);
            analysisButton.setTextColor(getColor(R.color.no_navy_deep));
            analysisButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            analysisButton.setBackgroundResource(R.drawable.bg_no_button_primary);
            analysisButton.setStateListAnimator(null);
            analysisButton.setOnClickListener(v ->
                    startActivity(new Intent(this, CampaignAnalysisActivity.class))
            );

            int createIndex = card.indexOfChild(createButton);
            LinearLayout.LayoutParams analysisLp = new LinearLayout.LayoutParams(-1, dp(44));
            analysisLp.setMargins(0, dp(12), 0, 0);
            card.addView(analysisButton, Math.max(0, createIndex), analysisLp);

            LinearLayout.LayoutParams createLp = (LinearLayout.LayoutParams) createButton.getLayoutParams();
            createLp.setMargins(0, dp(8), 0, 0);
            createButton.setLayoutParams(createLp);
        }
    }

    private Button findTaggedButton(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof Button && ANALYSIS_TAG.equals(child.getTag())) {
                return (Button) child;
            }
        }
        return null;
    }

    private Button findButtonByText(View view, String... labels) {
        if (view instanceof Button) {
            String text = ((Button) view).getText().toString();
            for (String label : labels) {
                if (label.equals(text)) return (Button) view;
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button found = findButtonByText(group.getChildAt(i), labels);
                if (found != null) return found;
            }
        }
        return null;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
