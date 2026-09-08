package com.noapp.accessreader;

import android.app.Activity;
import android.app.Application;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Ajustes globais leves de UX do NÓ.
 *
 * Nesta fase, transforma campos de data da jornada do anunciante em seletores
 * de calendário para evitar erro de digitação e dependência do teclado do aparelho.
 */
public class NoApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private static final String DATE_HINT = "DD/MM/AAAA";
    private static final String DATE_PICKER_TAG = "no_date_picker_attached";
    private final Locale ptBr = new Locale("pt", "BR");

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof AdvertiserActivity) {
            View root = activity.getWindow().getDecorView();
            attachDatePickers(activity, root);
        }
    }

    private void attachDatePickers(Activity activity, View view) {
        if (view instanceof EditText) {
            EditText input = (EditText) view;
            CharSequence hint = input.getHint();
            if (hint != null && DATE_HINT.contentEquals(hint)) {
                configureDateField(activity, input);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                attachDatePickers(activity, group.getChildAt(i));
            }
        }
    }

    private void configureDateField(Activity activity, EditText input) {
        if (DATE_PICKER_TAG.equals(input.getTag())) return;
        input.setTag(DATE_PICKER_TAG);

        input.setFocusable(false);
        input.setFocusableInTouchMode(false);
        input.setCursorVisible(false);
        input.setClickable(true);
        input.setLongClickable(false);
        input.setShowSoftInputOnFocus(false);

        input.setOnClickListener(v -> openDatePicker(activity, input));
    }

    private void openDatePicker(Activity activity, EditText input) {
        Calendar selected = Calendar.getInstance();
        Date parsed = parseDate(input.getText() == null ? "" : input.getText().toString().trim());
        if (parsed != null) {
            selected.setTime(parsed);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                activity,
                (picker, year, month, dayOfMonth) -> input.setText(
                        String.format(ptBr, "%02d/%02d/%04d", dayOfMonth, month + 1, year)
                ),
                selected.get(Calendar.YEAR),
                selected.get(Calendar.MONTH),
                selected.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private Date parseDate(String value) {
        if (TextUtils.isEmpty(value) || !value.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return null;
        }

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", ptBr);
        format.setLenient(false);
        try {
            return format.parse(value);
        } catch (ParseException ignored) {
            return null;
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
    @Override public void onActivityDestroyed(Activity activity) { }
}
