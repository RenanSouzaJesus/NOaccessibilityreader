package com.noapp.accessreader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class PremiumUi {
    static final int BG = Color.rgb(247, 248, 250);
    static final int SURFACE = Color.WHITE;
    static final int NAVY = Color.rgb(8, 31, 53);
    static final int NAVY_SOFT = Color.rgb(18, 56, 84);
    static final int CYAN = Color.rgb(28, 194, 222);
    static final int CYAN_SOFT = Color.rgb(230, 249, 252);
    static final int ORANGE = Color.rgb(255, 155, 34);
    static final int TEXT = Color.rgb(18, 24, 33);
    static final int MUTED = Color.rgb(102, 112, 133);
    static final int BORDER = Color.rgb(228, 231, 236);
    static final int GREEN = Color.rgb(18, 183, 106);
    static final int RED = Color.rgb(217, 45, 32);
    static final int SOFT = Color.rgb(241, 244, 247);

    private PremiumUi() {}

    static void lightSystemBars(Activity activity) {
        Window w = activity.getWindow();
        w.setStatusBarColor(BG);
        w.setNavigationBarColor(SURFACE);
        if (Build.VERSION.SDK_INT >= 23) {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            );
        }
    }

    static int dp(Context c, int value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    static TextView text(Context c, String value, int sp, int color, boolean bold) {
        TextView tv = new TextView(c);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setIncludeFontPadding(false);
        if (bold) tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return tv;
    }

    static GradientDrawable shape(int fill, int radiusPx, int strokeColor, int strokePx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(radiusPx);
        if (strokePx > 0) d.setStroke(strokePx, strokeColor);
        return d;
    }

    static LinearLayout card(Context c) {
        LinearLayout card = new LinearLayout(c);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(c, 16), dp(c, 16), dp(c, 16), dp(c, 16));
        card.setBackground(shape(SURFACE, dp(c, 20), BORDER, dp(c, 1)));
        card.setElevation(dp(c, 1));
        return card;
    }

    static Button primaryButton(Context c, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(14);
        b.setTextColor(NAVY);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(CYAN, dp(c, 16), CYAN, 0));
        b.setStateListAnimator(null);
        return b;
    }

    static Button secondaryButton(Context c, String label) {
        Button b = new Button(c);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setTextColor(NAVY);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(shape(SURFACE, dp(c, 16), BORDER, dp(c, 1)));
        b.setStateListAnimator(null);
        return b;
    }

    static TextView chip(Context c, String label, int fg, int bg) {
        TextView chip = text(c, label, 10, fg, true);
        chip.setPadding(dp(c, 10), dp(c, 6), dp(c, 10), dp(c, 6));
        chip.setBackground(shape(bg, dp(c, 999), bg, 0));
        return chip;
    }
}
