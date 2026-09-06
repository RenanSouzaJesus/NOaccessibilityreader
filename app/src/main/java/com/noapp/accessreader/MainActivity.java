package com.noapp.accessreader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView statusText;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        resultText = findViewById(R.id.resultText);

        Button accessButton = findViewById(R.id.accessButton);
        Button refreshButton = findViewById(R.id.refreshButton);

        accessButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        );

        refreshButton.setOnClickListener(v -> refreshData());
        refreshData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        statusText.setText(isAccessibilityEnabled()
                ? "Status: acessibilidade ATIVA"
                : "Status: acessibilidade DESATIVADA");

        SharedPreferences prefs =
                getSharedPreferences("no_accessibility", MODE_PRIVATE);

        String pkg = prefs.getString("package", "");
        String content = prefs.getString("content", "");
        long time = prefs.getLong("time", 0L);

        if (TextUtils.isEmpty(pkg) && TextUtils.isEmpty(content)) {
            resultText.setText("Nenhuma leitura capturada ainda.");
            return;
        }

        resultText.setText(
                "Pacote ativo: " + pkg +
                "\n\nConteúdo acessível:\n" + content +
                "\n\nTimestamp: " + time
        );
    }

    private boolean isAccessibilityEnabled() {
        String expected = new ComponentName(
                this,
                ScreenAccessibilityService.class
        ).flattenToString();

        String enabled = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        if (enabled == null) return false;

        TextUtils.SimpleStringSplitter splitter =
                new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabled);

        while (splitter.hasNext()) {
            if (expected.equalsIgnoreCase(splitter.next())) {
                return true;
            }
        }

        return false;
    }
}
