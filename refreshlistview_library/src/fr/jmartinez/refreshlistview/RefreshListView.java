/**
 * Copyright 2012-2013 Jeremie Martinez (jeremiemartinez@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.jmartinez.refreshlistview;

import java.text.DateFormat;
import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author jmartinez
 * 
 *         Simple Android ListView that enables pull to refresh as Twitter or Facebook apps
 *         ListView. Developers must implement OnRefreshListener interface and set it to the list.
 *         They also have to call finishRefreshing when their task is done. See 
 *         <a href="https://github.com/jeremiemartinez/RefreshListView">Project Site</a> for more
 *         information.
 * 
 */
public class RefreshListView extends ListView {

	private static final int RESISTANCE = 3;
	private static final int HEADER_HEIGHT = 60;
	private static final int DURATION = 300;
	private static final String DEFAULT_UPDATING = "Updating...";
	private static final String DEFAULT_RELEASE = "Release to refresh...";
	private static final String DEFAULT_PULLDOWN = "Pull down to refresh...";
	private static final String NO_UPDATE = "No past update";

	private OnRefreshListener refreshListener;
	private View container;
	private RelativeLayout header;
	private ProgressBar progress;
	private TextView comment;
	private ImageView arrow;
	private TextView date;
	private LayoutInflater inflater;

	private float currentY;

	private int headerHeight;

	private boolean enabledDate;
	private Date lastUpdateDate;
	private DateFormat formatter;

	private State currentState;

	private enum State {
		PULLDOWN, RELEASE, UPDATING;
	}

	private enum Rotation {
		CLOCKWISE, ANTICLOCKWISE;
	}

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

	/**
	 * initializing method. Call in constructors to set up the headers.
	 * 
	 * @param context
	 *            activity context, got by constructors
	 */
	private void init(Context context) {
		formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		inflater = LayoutInflater.from(context);
		container = inflater.inflate(R.layout.layout_refreshlistview_header, null);
		header = (RelativeLayout) container.findViewById(R.id.header);
		arrow = (ImageView) container.findViewById(R.id.arrow);
		progress = (ProgressBar) container.findViewById(R.id.progress);
		date = (TextView) container.findViewById(R.id.date);
		comment = (TextView) container.findViewById(R.id.comment);

		container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		header.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		addHeaderView(container);

		headerHeight = (int) (HEADER_HEIGHT * getContext().getResources().getDisplayMetrics().density);
		changeHeaderHeight(0);

		comment.setText(getResourceString(R.string.refreshlistview_pulldown, DEFAULT_PULLDOWN));
		currentState = State.PULLDOWN;
	}

	/**
	 * Call to perform item click. Reset the position without the header
	 * 
	 * @see android.widget.AbsListView#performItemClick(android.view.View, int, long)
	 */
	@Override
	public boolean performItemClick(View view, int position, long id) {
		if (position == 0) {
			return true;
		} else {
			return super.performItemClick(view, position - getHeaderViewsCount(), id);
		}
	}

	/**
	 * Set up first touch to later calculations.
	 * 
	 * @see android.widget.AbsListView#onInterceptTouchEvent(android.view.MotionEvent )
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			currentY = ev.getY();
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * Handle what to do when the user release the touch.
	 * 
	 * @see android.widget.AbsListView#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			if (currentState != State.UPDATING) {
				if (currentState == State.RELEASE) {
					header.startAnimation(new ResizeHeaderAnimation(headerHeight));
					startRefreshing();
				} else {
					header.startAnimation(new ResizeHeaderAnimation(0));
				}
			} else {
				header.startAnimation(new ResizeHeaderAnimation(headerHeight));
			}
		case MotionEvent.ACTION_MOVE:
			ev.setAction(MotionEvent.ACTION_CANCEL);
		}
		return true;
	}

	/**
	 * Used to show header when scrolling down.
	 * 
	 * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(final MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_MOVE && getFirstVisiblePosition() == 0) {
			if (isAllowedToShowHeader(ev.getY())) {
				changeHeaderHeight(getHeightWithScrollResistance(ev.getY()));
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * Call to update UI when the refresh started.
	 */
	private void startRefreshing() {
		arrow.clearAnimation();
		arrow.setVisibility(View.INVISIBLE);
		progress.setVisibility(View.VISIBLE);
		comment.setText(getResourceString(R.string.refreshlistview_updating, DEFAULT_UPDATING));

		if (refreshListener != null) {
			refreshListener.onRefresh(this);
		}

		currentState = State.UPDATING;
	}

	/**
	 * Call when refreshing task is done. Must be called by the developer.
	 */
	public void finishRefreshing() {
		finishRefreshing(null);
	}

	/**
	 * Call when refreshing task is done. Must be called by the developer.
	 * 
	 * @param updateDate
	 *            allow developer to set the last updateDate
	 */
	public void finishRefreshing(Date updateDate) {
		header.startAnimation(new ResizeHeaderAnimation(0));
		progress.setVisibility(View.INVISIBLE);
		arrow.setVisibility(View.VISIBLE);
		if (updateDate == null)
			lastUpdateDate = new Date();
		else
			lastUpdateDate = updateDate;
		date.setText(getFormattedDate(lastUpdateDate));
		comment.setText(getResourceString(R.string.refreshlistview_pulldown, DEFAULT_PULLDOWN));
		currentState = State.PULLDOWN;
		invalidate();
	}

	/**
	 * Change the header height while scrolling down by making it visible and increasing
	 * topMargin of the header.
	 * 
	 * @param height
	 *            the height of the header
	 */
	private void changeHeaderHeight(int height) {
		hideOrShowHeader(height);

		LayoutParams layoutParams = (LayoutParams) container.getLayoutParams();
		layoutParams.height = height;
		container.setLayoutParams(layoutParams);

		LinearLayout.LayoutParams headerLayoutParams = (LinearLayout.LayoutParams) header.getLayoutParams();
		headerLayoutParams.topMargin = height - headerHeight;
		header.setLayoutParams(headerLayoutParams);

		if (currentState != State.UPDATING) {
			if (height > headerHeight && currentState == State.PULLDOWN) {
				arrow.startAnimation(getRotationAnimation(Rotation.ANTICLOCKWISE));
				comment.setText(getResourceString(R.string.refreshlistview_release, DEFAULT_RELEASE));
				currentState = State.RELEASE;
			} else if (height < headerHeight && currentState == State.RELEASE) {
				arrow.startAnimation(getRotationAnimation(Rotation.CLOCKWISE));
				comment.setText(getResourceString(R.string.refreshlistview_pulldown, DEFAULT_PULLDOWN));
				currentState = State.PULLDOWN;
			}
		}
	}

	/**
	 * Check whether or not the header should be shown.
	 * 
	 * @param newY
	 *            just acquired Y event
	 * @return true if it is, false otherwise
	 */
	private boolean isAllowedToShowHeader(float newY) {
		return isScrollingEnough(newY) && (currentState != State.UPDATING || (currentState == State.UPDATING && (newY - currentY) > 0));
	}

	/**
	 * Check if the scroll is enough to be taken into acccount.
	 * 
	 * @param newY
	 *            just acquired Y event
	 * @return true if it is, false otherwise
	 */
	private boolean isScrollingEnough(float newY) {
		float deltaY = Math.abs(currentY - newY);
		ViewConfiguration config = ViewConfiguration.get(getContext());
		return deltaY > config.getScaledTouchSlop();
	}

	/**
	 * Calculate height header when scrolling down.
	 * 
	 * @param newY
	 *            just acquired Y event
	 * @return the height of the header to set
	 */
	private int getHeightWithScrollResistance(float newY) {
		return Math.max((int) (newY - currentY) / RESISTANCE, 0);
	}

	/**
	 * Hide or show the header according to its height.
	 * 
	 * @param height
	 *            current height of the header
	 */
	private void hideOrShowHeader(int height) {
		if (height <= 0) {
			header.setVisibility(View.GONE);
		} else {
			header.setVisibility(View.VISIBLE);
		}
	}

	private String getResourceString(int id, String defaultString) {
		String resourceString = getContext().getString(id);
		if (resourceString == null)
			return defaultString;
		else
			return resourceString;
	}

	/**
	 * Getter to know if date is enabled
	 * 
	 * @return true if date is enabled, false otherwise
	 */
	public boolean isEnabledDate() {
		return enabledDate;
	}

	/**
	 * Set enabled date, the first date to show will just be "No past update".
	 * 
	 * @param enabledDate
	 */
	public void setEnabledDate(boolean enabledDate) {
		setEnabledDate(enabledDate, null);
	}

	public void setEnabledDate(boolean enabledDate, Date firstDate) {
		this.enabledDate = enabledDate;
		lastUpdateDate = firstDate;
		if (enabledDate) {
			date.setVisibility(View.VISIBLE);
			if (firstDate != null) {
				date.setText(getFormattedDate(firstDate));
			} else {
				date.setText(NO_UPDATE);
			}
		} else {
			date.setVisibility(View.GONE);
		}
	}

	/**
	 * Getter for last update date.
	 * 
	 * @return the last update date or null is it has never been updated yet.
	 */
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	private String getFormattedDate(Date date) {
		return formatter.format(date);
	}

	public void setRefreshListener(OnRefreshListener listener) {
		this.refreshListener = listener;
	}

	/**
	 * Callback. Call when user asks to refresh the list. Required to be implemented 
	 * by developer.
	 */
	public interface OnRefreshListener {
		public void onRefresh(RefreshListView listView);
	}

	private Animation getRotationAnimation(Rotation rotation) {
		int fromAngle = 0;
		int toAngle = 0;
		if (rotation == Rotation.ANTICLOCKWISE)
			toAngle = 180;
		else
			fromAngle = 180;
		Animation animation = new RotateAnimation(fromAngle, toAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(DURATION);
		animation.setFillAfter(true);
		return animation;
	}

	/**
	 * Animation to resize the header's height
	 */
	public class ResizeHeaderAnimation extends Animation {
		private int toHeight;

		public ResizeHeaderAnimation(int toHeight) {
			this.toHeight = toHeight;
			setDuration(DURATION);
		}

		/**
		 * Animation core, animate the height of the header to a specific value.
		 * 
		 * @see android.view.animation.Animation#applyTransformation(float, 
		 * 			android.view.animation.Transformation)
		 */
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			float height = (toHeight - container.getHeight()) * interpolatedTime + container.getHeight();
			LayoutParams lp = (LayoutParams) container.getLayoutParams();
			LinearLayout.LayoutParams headerlp = (LinearLayout.LayoutParams) header.getLayoutParams();
			headerlp.topMargin = (int) height - headerHeight;
			lp.height = (int) height;
			lp.width = (int) container.getWidth();
			container.requestLayout();
		}

		/**
		 * Used at the end of the animation to hide completely the header if it's required (toHeight == 0).
		 * 
		 * @see android.view.animation.Animation#getTransformation(long, 
		 * 			android.view.animation.Transformation)
		 */
		@Override
		public boolean getTransformation(long currentTime, Transformation outTransformation) {
			hideOrShowHeader(toHeight);
			return super.getTransformation(currentTime, outTransformation);
		}
	}

	/**
	 * Display a toast when there is an error in refresh task
	 * 
	 * @param errorMessage
	 *            error message to display
	 */
	public void errorInRefresh(String errorMessage) {
		Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
	}
}
