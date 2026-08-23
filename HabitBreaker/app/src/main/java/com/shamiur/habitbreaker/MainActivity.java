package com.shamiur.habitbreaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String PREFS = "habit_breaker_prefs";
    private static final String KEY_STAGE = "stage";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_START = "start";
    private static final String KEY_WINS = "wins";
    private static final String KEY_SLIPS = "slips";

    private static final long HOUR = 60L * 60L * 1000L;
    private static final long DAY = 24L * HOUR;

    private static final long[] DURATIONS = {
            5L * HOUR,
            8L * HOUR,
            12L * HOUR,
            18L * HOUR,
            24L * HOUR,
            48L * HOUR,
            72L * HOUR,
            7L * DAY,
            14L * DAY,
            30L * DAY,
            60L * DAY,
            90L * DAY
    };

    private static final String[] LABELS = {
            "5 hours", "8 hours", "12 hours", "18 hours", "24 hours", "48 hours", "72 hours",
            "7 days", "14 days", "30 days", "60 days", "90 days"
    };

    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private TextView stageText;
    private TextView countdownText;
    private TextView statusText;
    private TextView statsText;
    private TextView nextText;
    private ProgressBar challengeProgress;
    private ProgressBar journeyProgress;
    private Button startButton;
    private Button completeButton;
    private Button slipButton;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            render();
            handler.postDelayed(this, 1000L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        setContentView(buildUi());
        render();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(ticker);
    }

    private View buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(246, 248, 252));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView eyebrow = text("SMOKE-FREE CHALLENGE", 13, Color.rgb(70, 97, 214), true);
        eyebrow.setLetterSpacing(0.08f);
        root.addView(eyebrow);

        TextView title = text("Habit Breaker", 32, Color.rgb(24, 31, 50), true);
        title.setPadding(0, dp(5), 0, 0);
        root.addView(title);

        TextView subtitle = text("One challenge at a time. The goal is to keep extending the smoke-free gap.", 16,
                Color.rgb(91, 101, 122), false);
        subtitle.setPadding(0, dp(6), 0, dp(20));
        root.addView(subtitle);

        LinearLayout challengeCard = card();
        challengeCard.setPadding(dp(22), dp(22), dp(22), dp(22));
        root.addView(challengeCard, marginBottom(16));

        stageText = text("", 15, Color.rgb(70, 97, 214), true);
        challengeCard.addView(stageText);

        countdownText = text("", 38, Color.rgb(24, 31, 50), true);
        countdownText.setGravity(Gravity.CENTER_HORIZONTAL);
        countdownText.setPadding(0, dp(16), 0, dp(8));
        challengeCard.addView(countdownText);

        statusText = text("", 15, Color.rgb(91, 101, 122), false);
        statusText.setGravity(Gravity.CENTER_HORIZONTAL);
        challengeCard.addView(statusText);

        challengeProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        challengeProgress.setMax(1000);
        LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(10));
        progressLp.setMargins(0, dp(20), 0, dp(14));
        challengeCard.addView(challengeProgress, progressLp);

        startButton = primaryButton("Start this challenge");
        startButton.setOnClickListener(v -> startChallenge());
        challengeCard.addView(startButton, matchWidth(52));

        completeButton = primaryButton("I stayed smoke-free ✓");
        completeButton.setOnClickListener(v -> completeChallenge());
        challengeCard.addView(completeButton, topMargin(matchWidth(52), 10));

        slipButton = secondaryButton("I slipped — restart this stage");
        slipButton.setOnClickListener(v -> confirmSlip());
        challengeCard.addView(slipButton, topMargin(matchWidth(52), 10));

        LinearLayout journeyCard = card();
        journeyCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        root.addView(journeyCard, marginBottom(16));

        TextView journeyTitle = text("Your journey", 20, Color.rgb(24, 31, 50), true);
        journeyCard.addView(journeyTitle);

        nextText = text("", 15, Color.rgb(91, 101, 122), false);
        nextText.setPadding(0, dp(8), 0, dp(12));
        journeyCard.addView(nextText);

        journeyProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        journeyProgress.setMax(DURATIONS.length);
        journeyCard.addView(journeyProgress, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(10)));

        statsText = text("", 14, Color.rgb(91, 101, 122), false);
        statsText.setPadding(0, dp(14), 0, 0);
        journeyCard.addView(statsText);

        LinearLayout tipCard = card();
        tipCard.setPadding(dp(20), dp(20), dp(20), dp(20));
        root.addView(tipCard, marginBottom(16));

        TextView tipTitle = text("When a craving hits", 19, Color.rgb(24, 31, 50), true);
        tipCard.addView(tipTitle);

        TextView tip = text("Delay for 10 minutes, drink water, change rooms, take a short walk, or do slow breathing. You only need to get through the next few minutes.",
                15, Color.rgb(91, 101, 122), false);
        tip.setPadding(0, dp(8), 0, 0);
        tipCard.addView(tip);

        Button reset = secondaryButton("Reset all progress");
        reset.setOnClickListener(v -> confirmReset());
        root.addView(reset, matchWidth(48));

        TextView footer = text("This app is a self-tracking tool, not medical care. If quitting feels difficult, talking with a trusted adult or healthcare professional can help.",
                12, Color.rgb(126, 134, 151), false);
        footer.setPadding(0, dp(16), 0, 0);
        root.addView(footer);

        return scroll;
    }

    private void startChallenge() {
        int stage = getStage();
        if (stage >= DURATIONS.length) {
            return;
        }
        prefs.edit()
                .putBoolean(KEY_ACTIVE, true)
                .putLong(KEY_START, System.currentTimeMillis())
                .apply();
        render();
    }

    private void completeChallenge() {
        int stage = getStage();
        if (stage >= DURATIONS.length || !prefs.getBoolean(KEY_ACTIVE, false)) {
            return;
        }
        long elapsed = System.currentTimeMillis() - prefs.getLong(KEY_START, 0L);
        if (elapsed < DURATIONS[stage]) {
            return;
        }

        int wins = prefs.getInt(KEY_WINS, 0) + 1;
        int nextStage = Math.min(stage + 1, DURATIONS.length);
        prefs.edit()
                .putInt(KEY_WINS, wins)
                .putInt(KEY_STAGE, nextStage)
                .putBoolean(KEY_ACTIVE, false)
                .remove(KEY_START)
                .apply();

        if (nextStage < DURATIONS.length) {
            new AlertDialog.Builder(this)
                    .setTitle("Challenge complete")
                    .setMessage("Nice work. Your next smoke-free challenge is " + LABELS[nextStage] + ".")
                    .setPositiveButton("Continue", null)
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("90-day path complete")
                    .setMessage("You completed every challenge in this plan. Keep protecting the progress you built.")
                    .setPositiveButton("Done", null)
                    .show();
        }
        render();
    }

    private void confirmSlip() {
        new AlertDialog.Builder(this)
                .setTitle("Restart this challenge?")
                .setMessage("A slip does not erase your earlier wins. This will restart only the current timer.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Restart", (dialog, which) -> {
                    int slips = prefs.getInt(KEY_SLIPS, 0) + 1;
                    prefs.edit()
                            .putInt(KEY_SLIPS, slips)
                            .putBoolean(KEY_ACTIVE, true)
                            .putLong(KEY_START, System.currentTimeMillis())
                            .apply();
                    render();
                })
                .show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Reset all progress?")
                .setMessage("This clears completed stages, wins, slips, and the current timer on this device.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    render();
                })
                .show();
    }

    private void render() {
        int stage = getStage();
        int wins = prefs.getInt(KEY_WINS, 0);
        int slips = prefs.getInt(KEY_SLIPS, 0);
        boolean active = prefs.getBoolean(KEY_ACTIVE, false);

        if (stage >= DURATIONS.length) {
            stageText.setText("ALL STAGES COMPLETE");
            countdownText.setText("90 days ✓");
            statusText.setText("Keep going — your progress continues beyond the challenge list.");
            challengeProgress.setProgress(1000);
            startButton.setVisibility(View.GONE);
            completeButton.setVisibility(View.GONE);
            slipButton.setVisibility(View.GONE);
            nextText.setText("You completed the full progression from 5 hours to 90 days.");
            journeyProgress.setProgress(DURATIONS.length);
            statsText.setText(String.format(Locale.getDefault(), "Completed: %d   •   Restarts: %d", wins, slips));
            return;
        }

        long duration = DURATIONS[stage];
        stageText.setText("STAGE " + (stage + 1) + " OF " + DURATIONS.length + "  •  " + LABELS[stage]);
        journeyProgress.setProgress(stage);

        if (stage + 1 < DURATIONS.length) {
            nextText.setText("Complete " + LABELS[stage] + " to unlock " + LABELS[stage + 1] + ".");
        } else {
            nextText.setText("This is the final challenge in the current plan.");
        }
        statsText.setText(String.format(Locale.getDefault(), "Completed: %d   •   Restarts: %d", wins, slips));

        if (!active) {
            countdownText.setText(formatDuration(duration));
            statusText.setText("Ready when you are. Start the timer after your last cigarette.");
            challengeProgress.setProgress(0);
            startButton.setVisibility(View.VISIBLE);
            completeButton.setVisibility(View.GONE);
            slipButton.setVisibility(View.GONE);
            return;
        }

        long start = prefs.getLong(KEY_START, System.currentTimeMillis());
        long elapsed = Math.max(0L, System.currentTimeMillis() - start);
        long remaining = Math.max(0L, duration - elapsed);
        int progress = (int) Math.min(1000L, (elapsed * 1000L) / Math.max(1L, duration));
        challengeProgress.setProgress(progress);

        startButton.setVisibility(View.GONE);
        slipButton.setVisibility(View.VISIBLE);

        if (remaining > 0L) {
            countdownText.setText(formatDuration(remaining));
            statusText.setText("Stay smoke-free until the timer reaches zero.");
            completeButton.setVisibility(View.GONE);
        } else {
            countdownText.setText("00:00:00");
            statusText.setText("Challenge reached. Mark it complete to unlock the next stage.");
            completeButton.setVisibility(View.VISIBLE);
            completeButton.setEnabled(true);
        }
    }

    private int getStage() {
        return Math.max(0, Math.min(prefs.getInt(KEY_STAGE, 0), DURATIONS.length));
    }

    private String formatDuration(long millis) {
        long totalSeconds = Math.max(0L, millis / 1000L);
        long days = totalSeconds / 86400L;
        long hours = (totalSeconds % 86400L) / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (days > 0) {
            return String.format(Locale.getDefault(), "%dd %02dh %02dm", days, hours, minutes);
        }
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(0f, 1.18f);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private LinearLayout card() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp(22));
        bg.setStroke(dp(1), Color.rgb(228, 233, 242));
        layout.setBackground(bg);
        layout.setElevation(dp(2));
        return layout;
    }

    private Button primaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextAllCaps(false);
        button.setTextSize(15);
        button.setTextColor(Color.WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(70, 97, 214));
        bg.setCornerRadius(dp(14));
        button.setBackground(bg);
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextAllCaps(false);
        button.setTextSize(14);
        button.setTextColor(Color.rgb(50, 62, 87));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(242, 245, 250));
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1), Color.rgb(220, 226, 236));
        button.setBackground(bg);
        return button;
    }

    private LinearLayout.LayoutParams matchWidth(int heightDp) {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(heightDp));
    }

    private LinearLayout.LayoutParams topMargin(LinearLayout.LayoutParams lp, int topDp) {
        lp.setMargins(0, dp(topDp), 0, 0);
        return lp;
    }

    private LinearLayout.LayoutParams marginBottom(int bottomDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(bottomDp));
        return lp;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
