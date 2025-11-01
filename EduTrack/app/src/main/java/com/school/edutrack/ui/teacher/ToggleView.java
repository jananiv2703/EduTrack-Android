package com.school.edutrack.ui.teacher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CompoundButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.school.edutrack.R;

public class ToggleView extends ConstraintLayout implements CompoundButton.OnCheckedChangeListener {

    private static final int CIRCLE_DIM = 56; // 1.4em at 17px font size (approx 40dp)
    private static final int WIDTH = 140; // 3.5em at 17px
    private static final int HEIGHT = 80; // 2em at 17px

    private View slider;
    private View sliderCard;
    private View frontFace;
    private View backFace;
    private boolean isChecked = false;
    private Paint paint = new Paint();

    public ToggleView(Context context) {
        this(context, null);
    }

    public ToggleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_toggle, this);

        slider = findViewById(R.id.slider);
        sliderCard = findViewById(R.id.slider_card);
        frontFace = findViewById(R.id.slider_card_front);
        backFace = findViewById(R.id.slider_card_back);

        setWillNotDraw(false);
        updateState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(dpToPx(WIDTH), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dpToPx(HEIGHT), MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Custom drawing can be added here if needed
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    public void setChecked(boolean checked) {
        if (isChecked != checked) {
            isChecked = checked;
            updateState();
        }
    }

    public boolean isChecked() {
        return isChecked;
    }

    private void updateState() {
        if (isChecked) {
            slider.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.switch_track_tint_checked));
            sliderCard.animate()
                    .translationX(dpToPx(60)) // 1.5em translation
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            frontFace.animate()
                    .rotationY(-180f)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            backFace.animate()
                    .rotationY(0f)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } else {
            slider.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.switch_track_tint_unchecked));
            sliderCard.animate()
                    .translationX(0)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            frontFace.animate()
                    .rotationY(0f)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            backFace.animate()
                    .rotationY(180f)
                    .setDuration(400)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setChecked(isChecked);
    }
}