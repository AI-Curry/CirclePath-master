package com.example.circlepath_library;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 自定义圆形菜单
 * Created by LiuLei on 2017/9/12.
 */
public class CircleMenu extends View {

    private static final String TAG = "CricleMenu";

    //动画持续时间
    private static final long ANIMATION_DURATION = 300;
    //默认菜单数字
    private static final int DEFUALT_MENU_NO = 8;

    private int mNumberOfMenu;//Todo
    //贝塞尔曲线常数
    private final float BEZIER_CONSTANT = 0.551915024494f;// pre-calculated value

    private int mFabButtonRadius;
    private int mMenuButtonRadius;
    private int mGab;
    private int mCenterX;
    private int mCenterY;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    private ArrayList<CirclePoint> mMenuPoints = new ArrayList<>();
    private ArrayList<ObjectAnimator> mShowAnimation = new ArrayList<>();
    private ArrayList<ObjectAnimator> mHideAnimation = new ArrayList<>();
    public boolean isMenuVisible = false;
    private Float bezierConstant = BEZIER_CONSTANT;
    public Bitmap mPlusBitmap;
    private String mTitle = "";//自定义
    private float mRotationAngle;
    private CircleMenuLisenter mCircleMenuLisentere;
    private List<Drawable> mDrawableArray;
    public boolean isOpen = false;
    private boolean isMoving;//是否正在被滑动   默认false
    private boolean isClickble = true;//设置是否可点击   默认true

    public static final int[] STATE_ACTIVE =
            {android.R.attr.state_enabled, android.R.attr.state_active};
    public static final int[] STATE_PRESSED =
            {android.R.attr.state_enabled, -android.R.attr.state_active,
                    android.R.attr.state_pressed};


    public CircleMenu(Context context) {
        super(context);
        init(null, context);
    }

    public CircleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }


    public CircleMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }


    private void init(AttributeSet attrs, Context context) {
        if (attrs != null) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.CircleMenu,
                    0, 0);
            try {
                mNumberOfMenu = typedArray.getInt(R.styleable.CircleMenu_no_of_menu, DEFUALT_MENU_NO);
                int center_bg = typedArray.getResourceId(R.styleable.CircleMenu_center_bg, android.R.mipmap.sym_def_app_icon);
                mPlusBitmap = BitmapFactory.decodeResource(getResources(), center_bg);
                mFabButtonRadius = (int) typedArray.getDimension(R.styleable.CircleMenu_fab_radius, getResources().getDimension(R.dimen.big_circle_radius));
                mMenuButtonRadius = (int) typedArray.getDimension(R.styleable.CircleMenu_menu_radius, getResources().getDimension(R.dimen.small_circle_radius));
                mGab = (int) typedArray.getDimension(R.styleable.CircleMenu_gap_between_menu_fab, getResources().getDimensionPixelSize(R.dimen.min_gap));

                TypedValue outValue = new TypedValue();
                // Read array of target drawables
                //加载小球中的图片
                if (typedArray.getValue(R.styleable.CircleMenu_one_menu_drawable, outValue)) {
                    Resources res = getContext().getResources();
                    TypedArray array = res.obtainTypedArray(outValue.resourceId);
                    mDrawableArray = new ArrayList<>(array.length());

                    for (int i = 0; i < array.length(); i++) {
                        TypedValue value = array.peekValue(i);
                        int j;//value != null ? value.resourceId : 0
                        if (value != null) {
                            j = value.resourceId;
                        } else {
                            j = 0;
                        }
                        mDrawableArray.add(getResources().getDrawable(j));
                    }
                    array.recycle();
                }
            } finally {
                typedArray.recycle();
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startHideAnimate();
                }
            }, 200);
        }

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.GRAY);
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);//填充

        mTextPaint = new Paint();
        mTextPaint.setTextSize(40);
        mTextPaint.setColor(Color.BLACK);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth;
        int desiredHeight;
        desiredWidth = getMeasuredWidth();
        desiredHeight = getContext().getResources().getDimensionPixelSize(R.dimen.min_height);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = new Random().nextInt(w - 2 * mFabButtonRadius) + mFabButtonRadius;//TODO 宽高位置
        mCenterY = new Random().nextInt(h - 2 * mFabButtonRadius) + mFabButtonRadius;
        //定义小圆动画集合
        for (int i = 0; i < mNumberOfMenu; i++) {
            CirclePoint circlePoint = new CirclePoint();
            circlePoint.setRadius(mGab);
            Log.e(TAG, "onSizeChanged: " + 2 * Math.PI / mNumberOfMenu * (i));
            circlePoint.setAngle(2 * Math.PI / mNumberOfMenu * (i));//TODO 角度
            mMenuPoints.add(circlePoint);
            ObjectAnimator animShow = ObjectAnimator.ofFloat(mMenuPoints.get(i), "Radius", 0f, mGab);
            animShow.setDuration(ANIMATION_DURATION);
            animShow.setInterpolator(new AnticipateOvershootInterpolator());
            animShow.setStartDelay((ANIMATION_DURATION * (mNumberOfMenu - i)) / 10);
            animShow.addUpdateListener(mUpdateListener);
            mShowAnimation.add(animShow);
            ObjectAnimator animHide = animShow.clone();
            animHide.setFloatValues(mGab, 0f);
            animHide.setStartDelay((ANIMATION_DURATION * i) / 10);
            mHideAnimation.add(animHide);

            if (mDrawableArray != null) {
                for (Drawable drawable : mDrawableArray)
                    drawable.setBounds(-mMenuButtonRadius / 6 * 5, -mMenuButtonRadius / 6 * 5, /*2 * */mMenuButtonRadius * 5 / 3,/* 2 * */mMenuButtonRadius * 5 / 3);
            }
        }
    }

    /**
     * 设置中心小球的图片
     *
     * @param res 资源
     */
    public void setCenterBitmap(int res) {
        mPlusBitmap = BitmapFactory.decodeResource(getResources(), res);
    }

    /**
     * @return  是否可点击
     */
    public boolean isClickble() {
        return isClickble;
    }

    /**
     * 设置菜单是否可点击  可操作
     * @param clickble  是否可点击
     */
    public void setClickble(boolean clickble) {
        isClickble = clickble;
    }

    /**
     * 设置中心圆内的标题
     *
     * @param title 标题
     */
    public void setCentertitle(String title) {
        mTitle = title;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlusBitmap = null;
        mHideAnimation.clear();
        mHideAnimation = null;
        mShowAnimation.clear();
        mHideAnimation = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(TAG, "onDraw: ");
        //定义小圆位置
        for (int i = 0; i < mNumberOfMenu; i++) {
            CirclePoint circlePoint = mMenuPoints.get(i);

            float x = (float) (circlePoint.getRadius() * Math.sin(circlePoint.getAngle()));//TODO 计算五个小球的xy
            float y = (float) (circlePoint.getRadius() * Math.cos(circlePoint.getAngle()));

            //canvas.drawCircle(x + mCenterX, mCenterY - y, mMenuButtonRadius, mCirclePaint);

            canvas.save();
            canvas.translate(x + mCenterX - mMenuButtonRadius, mCenterY - y - mMenuButtonRadius);

            canvas.restore();

            if (i < mDrawableArray.size()) {
                canvas.save();
                canvas.translate(x + mCenterX - mMenuButtonRadius / 2, mCenterY - y - mMenuButtonRadius / 2);

//                canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
//                        | Paint.FILTER_BITMAP_FLAG));

//                mBtnMaskDrawable.setFilterBitmap(true);
//                mBtnMaskDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
//                mBtnMaskDrawable.draw(canvas);

                // mDrawableArray.get(i).setColorFilter(Color.RED, PorterDuff.Mode.DST_ATOP);
                mDrawableArray.get(i).draw(canvas);

                canvas.restore();
            }
        }
        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        Path path = createPath();
        canvas.drawPath(path, mCirclePaint);
        canvas.rotate(mRotationAngle);


        canvas.drawBitmap(mPlusBitmap, -mPlusBitmap.getWidth() / 2, -mPlusBitmap.getHeight() / 2, mCirclePaint);

        canvas.drawText(mTitle, 0, 0, mTextPaint);
        canvas.restore();

    }

    // Use Bezier path to create circle,
    /*    P_0 = (0,1), P_1 = (c,1), P_2 = (1,c), P_3 = (1,0)
        P_0 = (1,0), P_1 = (1,-c), P_2 = (c,-1), P_3 = (0,-1)
        P_0 = (0,-1), P_1 = (-c,-1), P_3 = (-1,-c), P_4 = (-1,0)
        P_0 = (-1,0), P_1 = (-1,c), P_2 = (-c,1), P_3 = (0,1)
        with c = 0.551915024494*/

    private Path createPath() {
        Path path = new Path();
        float c = bezierConstant * mFabButtonRadius;

        path.moveTo(0, mFabButtonRadius);
        path.cubicTo(bezierConstant * mFabButtonRadius, mFabButtonRadius, mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius, mFabButtonRadius, 0);
        path.cubicTo(mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius * (-1), c, (-1) * mFabButtonRadius, 0, (-1) * mFabButtonRadius);
        path.cubicTo((-1) * c, (-1) * mFabButtonRadius, (-1) * mFabButtonRadius, (-1) * BEZIER_CONSTANT * mFabButtonRadius, (-1) * mFabButtonRadius, 0);
        path.cubicTo((-1) * mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius, (-1) * bezierConstant * mFabButtonRadius, mFabButtonRadius, 0, mFabButtonRadius);

        return path;
    }

    long startTime = 0, endTime = 0;
    int i = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isGooeyMenuTouch(event)) {
                    //使当前获得焦点的view对象处于父布局的顶部，防止被遮盖
                    ((View) getParent()).bringToFront();
                    i = 0;
                    startTime = System.currentTimeMillis();
                    return true;
                }

                int menuItem = isMenuItemTouched(event);
                if (isMenuVisible && menuItem > 0) {
                    if (menuItem <= mDrawableArray.size()) {
                        mDrawableArray.get(menuItem - 1).setState(STATE_PRESSED);
                        invalidate();
                    }
                    i = 0;
                    startTime = System.currentTimeMillis();
                    return true;
                }
                if (isMenuVisible && menuItem < 0) {
                    //点击非菜单区域   收缩菜单
                    Log.e(TAG, "开始收缩菜单: ++++++++++");
                    startHideAnimate();
                    if (mCircleMenuLisentere != null) {
                        mCircleMenuLisentere.menuClose();
                    }
                    isMenuVisible = false;
                }
                return false;
            case MotionEvent.ACTION_MOVE:
                Log.e(TAG, "onTouchEvent: 移动了");
                isMoving = true;
                endTime = System.currentTimeMillis();
//                Log.e("MotionEvent", "onTouchEvent: =========="+endTime);
                if (endTime - startTime > 200 && startTime != 0) {
                    if (mCircleMenuLisentere != null) {
                        mCircleMenuLisentere.isMove();
                    }
                    mCenterX = (int) event.getX();
                    mCenterY = (int) event.getY();
                    Log.e("MotionEvent", "mCenterX: ==========" + mCenterX + "mCenterY:==========" + mCenterY);
                    invalidate();//重新绘制图形
                    i = 100;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.e(TAG, "ACTION_UP: " + "i++++=====" + i);
                isMoving = false;
                if (i == 0) {
                    i++;
                    if (isGooeyMenuTouch(event) && isClickble) {
//                    mBezierAnimation.start();
                        cancelAllAnimation();
                        if (isMenuVisible) {
                            startHideAnimate();
                            if (mCircleMenuLisentere != null) {
                                mCircleMenuLisentere.menuClose();
                            }
                        } else {
                            startShowAnimate();
                            if (mCircleMenuLisentere != null) {
                                mCircleMenuLisentere.menuOpen();
                            }
                        }
                        isMenuVisible = !isMenuVisible;
                        isOpen = !isOpen;
                    }

                    if (isMenuVisible) {
                        Log.e("isMenuVisible", "onTouchEvent: " + 22);
                        menuItem = isMenuItemTouched(event);
                        invalidate();
                        if (menuItem > 0) {
                            if (menuItem <= mDrawableArray.size()) {
                                mDrawableArray.get(menuItem - 1).setState(STATE_ACTIVE);
                                postInvalidateDelayed(1000);
                            }
                            if (mCircleMenuLisentere != null) {
                                Log.e("menuItemClicked", "menuItemClicked: " + menuItem);
                                mCircleMenuLisentere.menuItemClicked(menuItem);
                                invalidate();//
                            }
                        }
                    }
                    return false;
                } else if (i == 100) {
//                    Toast.makeText(getContext(), "移动菜单", Toast.LENGTH_SHORT).show();
                    startTime = 0;
                    endTime = 0;
                }
        }
        return true;

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isMoving) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(event);
    }

    private int isMenuItemTouched(MotionEvent event) {
        if (!isMenuVisible) {
            return -1;
        }
        for (int i = 0; i < mMenuPoints.size(); i++) {
            CirclePoint circlePoint = mMenuPoints.get(i);
            float x = (float) (mGab * Math.cos(circlePoint.getAngle())) + mCenterX;
            float y = mCenterY - (float) (mGab * Math.sin(circlePoint.getAngle()));
            if (event.getX() >= x - mMenuButtonRadius && event.getX() <= x + mMenuButtonRadius) {
                if (event.getY() >= y - mMenuButtonRadius && event.getY() <= y + mMenuButtonRadius) {
                    Log.e(TAG, "isMenuItemTouched: " + (mMenuPoints.size() - i));
                    return getPosition(mMenuPoints.size() - i);
                }
            }
        }
        return -1;
    }

    private int getPosition(int position) {
        if (position < 6) {
            return position + 3;
        } else {
            return position - 5;
        }
    }

    public void setOnMenuListener(CircleMenuLisenter onMenuListener) {
        mCircleMenuLisentere = onMenuListener;
    }

    public boolean isGooeyMenuTouch(MotionEvent event) {
        if (event.getX() >= mCenterX - mFabButtonRadius && event.getX() <= mCenterX + mFabButtonRadius) {
            if (event.getY() >= mCenterY - mFabButtonRadius && event.getY() <= mCenterY + mFabButtonRadius) {
                return true;
            }
        }
        return false;
    }

    public void startShowAnimate() {
//        mRotationAnimation.start();
        for (ObjectAnimator objectAnimator : mShowAnimation) {
            objectAnimator.start();
        }
    }

    public void startHideAnimate() {
//        mRotationReverseAnimation.start();
        for (ObjectAnimator objectAnimator : mHideAnimation) {
            objectAnimator.start();
        }
    }

    public void cancelAllAnimation() {
        for (ObjectAnimator objectAnimator : mHideAnimation) {
            objectAnimator.cancel();
        }
        for (ObjectAnimator objectAnimator : mShowAnimation) {
            objectAnimator.cancel();
        }
    }

    ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            invalidate();
        }
    };

    ValueAnimator.AnimatorUpdateListener mBezierUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            bezierConstant = (float) valueAnimator.getAnimatedValue();
            invalidate();
        }
    };
    ValueAnimator.AnimatorUpdateListener mRotationUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mRotationAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        }
    };

    ValueAnimator.AnimatorListener mBezierAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
        }

        @Override
        public void onAnimationEnd(Animator animator) {
//            mBezierEndAnimation.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };


}
