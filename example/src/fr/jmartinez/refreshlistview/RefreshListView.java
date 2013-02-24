package fr.jmartinez.refreshlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class RefreshListView extends ListView {

	private static final int RESISTANCE = 4;
	private static final int HEADER_HEIGHT = 60;
	private static final int DURATION = 300;

	private OnRefreshListener refreshListener;
	private View container;
	private RelativeLayout header;
	private ProgressBar progress;
	private TextView comment;
	private LayoutInflater inflater;

	private boolean isRefreshing;
	private boolean isAfterRefreshLimit;
	private float currentY;

	private int headerHeight;

	public RefreshListView(Context context) {
		super(context);
		init(context);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		isRefreshing = false;
		inflater = LayoutInflater.from(context);
		container = inflater.inflate(R.layout.layout_refreshlistview_header, null);
		header = (RelativeLayout) container.findViewById(R.id.header);
		progress = (ProgressBar) container.findViewById(R.id.progress);
		comment = (TextView) container.findViewById(R.id.comment);

		container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		header.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		addHeaderView(container);

		headerHeight = (int) (HEADER_HEIGHT * getContext().getResources().getDisplayMetrics().density);
		changeHeaderHeight(0);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			currentY = ev.getY();
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			if (!isRefreshing) {
				if (isAfterRefreshLimit) {
					startRefreshing();
					header.startAnimation(new ResizeHeaderAnimation(container.getHeight(), headerHeight));
				} else {
					header.startAnimation(new ResizeHeaderAnimation(container.getHeight(), 0));
				}
			} else {
				header.startAnimation(new ResizeHeaderAnimation(container.getHeight(), headerHeight));
			}
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean dispatchTouchEvent(final MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_MOVE && getFirstVisiblePosition() == 0) {
			if (isAllowedToShowHeader(ev.getY())) {
				changeHeaderHeight(getHeightWithScrollResistance(ev.getY()));
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	private boolean isAllowedToShowHeader(float newY) {
		return isScrollingEnough(newY) && (!isRefreshing || (isRefreshing && (newY - currentY) > 0));
	}

	private boolean isScrollingEnough(float newY) {
		float deltaY = Math.abs(currentY - newY);
		ViewConfiguration config = ViewConfiguration.get(getContext());
		return deltaY > config.getScaledTouchSlop();
	}

	private int getHeightWithScrollResistance(float newY) {
		return Math.max((int) (newY - currentY) / RESISTANCE, 0);
	}

	@Override
	public boolean performItemClick(View view, int position, long id) {
		if (position == 0) {
			return true;
		} else {
			return super.performItemClick(view, position - 1, id);
		}
	}

	private void changeHeaderHeight(int height) {
		hideOrShowHeader(height);

		LayoutParams layoutParams = (LayoutParams) container.getLayoutParams();
		layoutParams.height = height;
		container.setLayoutParams(layoutParams);

		LinearLayout.LayoutParams headerLayoutParams = (LinearLayout.LayoutParams) header.getLayoutParams();
		headerLayoutParams.topMargin = height - headerHeight;
		header.setLayoutParams(headerLayoutParams);

		if (!isRefreshing) {
			if (height > headerHeight) {
				comment.setText("Release to refresh...");
				isAfterRefreshLimit = true;
			} else if (height < headerHeight) {
				comment.setText("Pull down to refresh...");
				isAfterRefreshLimit = false;
			}
		}
	}

	private void hideOrShowHeader(int height) {
		if (height <= 0) {
			header.setVisibility(View.GONE);
		} else {
			header.setVisibility(View.VISIBLE);
		}
	}

	private void startRefreshing() {
		progress.setVisibility(View.VISIBLE);
		comment.setText("Updating...");
		isRefreshing = true;

		if (refreshListener != null) {
			refreshListener.onRefresh(this);
		}
	}

	public void finishRefreshing() {
		progress.setVisibility(View.INVISIBLE);
		header.startAnimation(new ResizeHeaderAnimation(container.getHeight(), 0));
		isRefreshing = false;
		invalidate();
	}

	public OnRefreshListener getRefreshListener() {
		return refreshListener;
	}

	public void setRefreshListener(OnRefreshListener listener) {
		this.refreshListener = listener;
	}

	public interface OnRefreshListener {
		public void onRefresh(RefreshListView listView);
	}

	public class ResizeHeaderAnimation extends Animation {
		private int toHeight;
		private int fromHeight;

		public ResizeHeaderAnimation(int fromHeight, int toHeight) {
			this.toHeight = toHeight;
			this.fromHeight = fromHeight;
			setDuration(DURATION);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			float height = (toHeight - fromHeight) * interpolatedTime + fromHeight;
			LayoutParams lp = (LayoutParams) container.getLayoutParams();
			LinearLayout.LayoutParams headerlp = (LinearLayout.LayoutParams) header.getLayoutParams();
			headerlp.topMargin = (int) height - headerHeight;
			lp.height = (int) height;
			lp.width = (int) container.getWidth();
			container.requestLayout();
		}

		@Override
		public boolean getTransformation(long currentTime, Transformation outTransformation) {
			hideOrShowHeader(toHeight);
			return super.getTransformation(currentTime, outTransformation);
		}
	}

}
