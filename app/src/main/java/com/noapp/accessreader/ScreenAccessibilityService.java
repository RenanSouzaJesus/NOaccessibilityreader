package com.noapp.accessreader;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedHashSet;
import java.util.Set;

public class ScreenAccessibilityService extends AccessibilityService {

    private long lastRead = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        CharSequence packageName = event.getPackageName();
        String currentPackage = packageName == null ? "" : packageName.toString();

        // Não sobrescreve a última captura quando o usuário volta ao próprio app NO.
        if (getPackageName().equals(currentPackage)) return;

        long now = SystemClock.elapsedRealtime();
        if (now - lastRead < 500) return;
        lastRead = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        Set<String> lines = new LinkedHashSet<>();
        collectNodeData(root, lines);

        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            if (out.length() > 0) out.append("\n");
            out.append(line);
        }

        SharedPreferences prefs =
                getSharedPreferences("no_accessibility", MODE_PRIVATE);

        prefs.edit()
                .putString("package", currentPackage)
                .putString("content", out.toString())
                .putLong("time", System.currentTimeMillis())
                .apply();

        root.recycle();
    }

    private void collectNodeData(
            AccessibilityNodeInfo node,
            Set<String> lines
    ) {
        if (node == null) return;

        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        String viewId = node.getViewIdResourceName();

        if (text != null && text.length() > 0) {
            lines.add("TEXT: " + text);
        }

        if (desc != null && desc.length() > 0) {
            lines.add("DESC: " + desc);
        }

        if (viewId != null && !viewId.isEmpty()) {
            lines.add("VIEW_ID: " + viewId);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectNodeData(child, lines);
                child.recycle();
            }
        }
    }

    @Override
    public void onInterrupt() {
    }
}
