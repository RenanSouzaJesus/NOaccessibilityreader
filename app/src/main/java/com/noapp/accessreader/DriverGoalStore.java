package com.noapp.accessreader;

import android.content.Context;
import android.content.SharedPreferences;

/** Preferências simples de jornada do motorista. */
public final class DriverGoalStore {

    private static final String PREFS = "no_driver_goal";
    private static final String KEY_DAILY_NET_GOAL = "daily_net_goal";
    private static final String KEY_MAX_HOURS = "max_hours";

    private DriverGoalStore() {}

    public static DriverGoal load(Context context) {
        if (context == null) return new DriverGoal(0, 0);
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return new DriverGoal(
                prefs.getFloat(KEY_DAILY_NET_GOAL, 0f),
                prefs.getFloat(KEY_MAX_HOURS, 0f)
        );
    }

    public static void save(Context context, DriverGoal goal) {
        if (context == null || goal == null) return;
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putFloat(KEY_DAILY_NET_GOAL, (float) Math.max(0, goal.dailyNetGoal))
                .putFloat(KEY_MAX_HOURS, (float) Math.max(0, goal.maxHours))
                .apply();
    }

    public static final class DriverGoal {
        public final double dailyNetGoal;
        public final double maxHours;

        public DriverGoal(double dailyNetGoal, double maxHours) {
            this.dailyNetGoal = Math.max(0, dailyNetGoal);
            this.maxHours = Math.max(0, maxHours);
        }

        public boolean hasDailyGoal() {
            return dailyNetGoal > 0;
        }

        public boolean hasHourLimit() {
            return maxHours > 0;
        }
    }
}
