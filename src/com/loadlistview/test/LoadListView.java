package com.loadlistview.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.loadlistview.test.R;

public class LoadListView extends ListView implements OnScrollListener {
	
	View header;
	View footer;
	int mfirstVisibleItem;
	int mtotalItemCount;
	int mlastVisibleItem;
	boolean isLoading;
	int headerHeight; //顶部布局文件的高度
	
	ILoadListener iLoadListener = null;
	IReflashListener iReflashListener = null;
	
	boolean isRemark; //标记。当前是在listview最顶端按下的
	int startY; //按下时的Y值
	
	int state; //当前的状态
	int mScrollState; //listview的当前滚动状态
	final int STATE_NORMAL = 0; //正常状态
	final int STATE_PULL = 1; //提示下拉的状态
	final int STATE_RELEASE = 2; //提示释放的状态
	final int STATE_REFLASHING = 3; //刷新状态
	
	
	
	
	public LoadListView(Context context) {
		super(context);
		initView(context);
	}

	public LoadListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public LoadListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	
	
	
	
	/**
	 * 
	 */
	private void initView(Context context) {
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		header = inflater.inflate(R.layout.header_layout, null);
		footer = inflater.inflate(R.layout.footer_layout, null);
		
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		topPadding(-headerHeight);
		footer.findViewById(R.id.load_layout).setVisibility(View.GONE);
		
		this.addHeaderView(header);
		this.addFooterView(footer);
		
		this.setOnScrollListener(this);
	}
	
	
	/**
	 * 通知父布局，占有多大地
	 */
	private void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if(p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int height;
		int tempHeight = p.height;
		if(tempHeight > 0) {
			height = MeasureSpec.makeMeasureSpec(tempHeight , MeasureSpec.EXACTLY);
		}
		else {
			height = MeasureSpec.makeMeasureSpec(0 , MeasureSpec.UNSPECIFIED);
		}
		view.measure(width, height);
	}
	
	
	
	
	/**
	 * 设置header的顶部内边距的值
	 */
	private void topPadding(int topPadding) {
		header.setPadding(
				header.getPaddingLeft(), 
				topPadding, 
				header.getPaddingRight(), 
				header.getPaddingBottom());
		header.invalidate(); //重绘view
	}
	
	
	
	
	
	

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		mlastVisibleItem = firstVisibleItem + visibleItemCount;
		mtotalItemCount = totalItemCount;
		mfirstVisibleItem = firstVisibleItem;
	}
	
	
	
	
	

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		//
		if (mtotalItemCount == mlastVisibleItem && scrollState == SCROLL_STATE_IDLE) {
			if (isLoading == false) {
				footer.findViewById(R.id.load_layout).setVisibility(View.VISIBLE);
				iLoadListener.onLoad();
				isLoading = true;
			}
		}	
		
		mScrollState = scrollState;
		
	}
	
	
	/**
	 * 手势事件的监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: //按下
			if(mfirstVisibleItem == 0) { //如果当前是在界面最顶端
				isRemark = true;
				startY = (int) event.getY();
			}//如果当前不是在界面最顶端，显然目的就不是为了刷新
			break;
		case MotionEvent.ACTION_MOVE: //移动
			if(isRemark == true) {
				
				int tempY = (int) event.getY();
				int space = tempY - startY;
				int topPadding = space - headerHeight;
				
				switch (state) {
				case STATE_NORMAL:
					if(space > 0) {
						state = STATE_PULL;
						reflashViewByState();
					}
					break;
				case STATE_PULL:
					topPadding(topPadding);
					if(space > headerHeight + 30 && mScrollState == SCROLL_STATE_TOUCH_SCROLL) {
						state = STATE_RELEASE;
						reflashViewByState();
					}
					break;
				case STATE_RELEASE:
					topPadding(topPadding);
					if(space < headerHeight + 30) {
						state = STATE_PULL;
						reflashViewByState();
					}
					else if(space <= 0) {
						state = STATE_NORMAL;
						isRemark = false;
						reflashViewByState();
					}
					break;
				case STATE_REFLASHING:
					reflashViewByState();
					break;
				}
			}
			break;
		case MotionEvent.ACTION_UP: //抬起
			if(state == STATE_RELEASE) {
				state = STATE_REFLASHING;
				reflashViewByState();
				//加载最新数据，采用接口回调的方式
				iReflashListener.onReflash();
			}
			else if(state == STATE_PULL) {
				state = STATE_NORMAL;
				isRemark = false;
				reflashViewByState();
			}
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	
	public void reflashComplete() {
		state = STATE_NORMAL;
		isRemark = false;
		reflashViewByState();
		TextView lastTextView = (TextView) header.findViewById(R.id.id_header_lastUpdate_time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss"); //设置日期格式
		Date date = new Date(System.currentTimeMillis());
		String time = format.format(date);
		lastTextView.setText(time);
	}
	
	
	/**
	 * 根据state的不同，改变header这个View的显示样式
	 */
	private void reflashViewByState() {
		TextView tip = (TextView) header.findViewById(R.id.id_header_tip);
		ImageView arrow = (ImageView) header.findViewById(R.id.id_header_arrow);
		ProgressBar progressBar = (ProgressBar) header.findViewById(R.id.id_header_progressbar);
		
		RotateAnimation animation1 = new RotateAnimation(
				0, 180, 
				RotateAnimation.RELATIVE_TO_SELF, 0.5f, 
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation1.setDuration(500);
		animation1.setFillAfter(true);
		
		RotateAnimation animation2 = new RotateAnimation(
				180, 0, 
				RotateAnimation.RELATIVE_TO_SELF, 0.5f, 
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation2.setDuration(500);
		animation2.setFillAfter(true);
		
		switch (state) {
		case STATE_NORMAL:
			topPadding(-headerHeight);
			arrow.clearAnimation();
			break;
		case STATE_PULL:
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tip.setText("下拉可以刷新");
			arrow.clearAnimation();
			arrow.setAnimation(animation2);
			break;
		case STATE_RELEASE:
			arrow.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tip.setText("松开可以刷新");
			arrow.clearAnimation();
			arrow.setAnimation(animation1);
			break;
		case STATE_REFLASHING:
			arrow.clearAnimation();
			topPadding(30); //正在刷新的时候有一个正常的高度
			arrow.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
			tip.setText("正在刷新");
			break;
		}
	}
	
	
	
	
	/**
	 * 
	 */
	public void loadComplete(){
		isLoading = false;
		footer.findViewById(R.id.load_layout).setVisibility(View.GONE);
	}
	
	
	
	public void setInterface(ILoadListener iLoadListener , IReflashListener iReflashListener){
		this.iLoadListener = iLoadListener;
		this.iReflashListener = iReflashListener;
	}

	
}
