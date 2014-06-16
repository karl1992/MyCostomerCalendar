package com.costomercalendar.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.MotionEvent;
import android.view.View;

public class CalendarView extends View implements View.OnTouchListener {
	private final static String TAG = "anCalendar";
	private Date selectedStartDate;
	private Date selectedEndDate;
	private Date curDate;// 当前日历显示的月
	private Date today;// 今天的日期文字显示红色
	private Date downDate;// 手指按下状态时临时日期
	private Date showFirstDate, showLastDate;// 日历显示的第一个日期和最后一个日期
	private int downIndex;// 按下的格子的索引
	private Calendar calendar;
	private Surface surface;
	private DateCell[] date = new DateCell[42];// 日历显示数字
	private int curStartIndex, curEndIndex;// 当前显示的日历起始的索引
	private List<Date> listHaveCourseDate=new ArrayList<Date>();
	// 给控件设置监听事件
	private OnItemClickListener onItemClickListener;

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public CalendarView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}
    
	public void setListHaveCourseDate(List<Date> listDate){
		this.listHaveCourseDate=listDate;
	}
	
	private void init() {
		curDate = selectedStartDate = selectedEndDate = today = new Date();
		calendar = Calendar.getInstance();
		calendar.setTime(curDate);
		surface = new Surface();
		surface.density = getResources().getDisplayMetrics().density;
		setBackgroundColor(surface.bgColor);
		setOnTouchListener(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		surface.width = (int) (getResources().getDisplayMetrics().widthPixels);
		surface.height = (int) (getResources().getDisplayMetrics().heightPixels * 2 / 5);
		widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.width,
				View.MeasureSpec.EXACTLY);
		heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(surface.height,
				View.MeasureSpec.EXACTLY);
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		Log.d(TAG, "[onLayout] changed:"
				+ (changed ? "new size" : "not change") + "left:" + left
				+ " top:" + top + " right:" + right + " bottom:" + bottom);
		if (changed) {
			surface.init();
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// 画框
		canvas.drawPath(surface.boxPath, surface.borderPaint);
		// 星期
		float weekTextY = surface.monthHeight + surface.weekHeight * 3 / 4f;
		for (int i = 0; i < surface.weekText.length; i++) {
			float weekTextX = i
					* surface.cellWidth
					+ (surface.cellWidth - surface.weekPaint
							.measureText(surface.weekText[i])) / 2f;
			canvas.drawText(surface.weekText[i], weekTextX, weekTextY,
					surface.weekPaint);
		}
		// 计算日期
		calculateDate();
		// 按下状态，选择状态背景颜色
		drawDownOrSelectedBg(canvas);
		
		//选中的文字范围
		  int[] selectDateText = new int[] { -1, -1 };  
		 if (!selectedEndDate.before(showFirstDate)  
	                && !selectedStartDate.after(showLastDate)) {  
	            calendar.setTime(curDate);  
	            calendar.add(Calendar.MONTH, -1);  
	            findSelectedIndex(0, curStartIndex, calendar, selectDateText);  
	            if (selectDateText[1] == -1) {  
	                calendar.setTime(curDate);  
	                findSelectedIndex(curStartIndex, curEndIndex, calendar, selectDateText);  
	            }  
	            if (selectDateText[1] == -1) {  
	                calendar.setTime(curDate);  
	                calendar.add(Calendar.MONTH, 1);  
	                findSelectedIndex(curEndIndex, 42, calendar, selectDateText);  
	            }  
	            if (selectDateText[0] == -1) {  
	            	selectDateText[0] = 0;  
	            }  
	            if (selectDateText[1] == -1) {  
	            	selectDateText[1] = 41;  
	            }  
	        }  
		// write date number
		// today index
		int todayIndex = -1;
		int tommorrowIndex=-1;
		int afterTommorrowIndex=-1;
		calendar.setTime(curDate);
		String curYearAndMonth = calendar.get(Calendar.YEAR) + ""
				+ calendar.get(Calendar.MONTH);
		calendar.setTime(today);
		String todayYearAndMonth = calendar.get(Calendar.YEAR) + ""
				+ calendar.get(Calendar.MONTH);
		if (curYearAndMonth.equals(todayYearAndMonth)) {
			int todayNumber = calendar.get(Calendar.DAY_OF_MONTH);
			todayIndex = curStartIndex + todayNumber - 1;
			tommorrowIndex=todayIndex+1;
			afterTommorrowIndex=tommorrowIndex+1;
		}
		//设置有课程的日期
		  List<Integer> listIntHaveCourseDate=new ArrayList<Integer>(); 
		if(listHaveCourseDate!=null&&listHaveCourseDate.size()>0){
			for(int i=0;i<listHaveCourseDate.size();i++){
				if(listHaveCourseDate.get(i)!=null){
				calendar.setTime(((Date)listHaveCourseDate.get(i)));
				String haveYearAndMonth = calendar.get(Calendar.YEAR) + ""
						+ calendar.get(Calendar.MONTH);
				if (curYearAndMonth.equals(haveYearAndMonth)) {
					int haveCourseNumber = calendar.get(Calendar.DAY_OF_MONTH);
					int haveCourseIndex = curStartIndex + haveCourseNumber - 1;
					listIntHaveCourseDate.add(haveCourseIndex);
				}
				}
			}
		}
		for (int i = 0; i < 42; i++) {
			int color = surface.textColor;
			if (isLastMonth(i)) {
				color = surface.borderColor;
			} else if (isNextMonth(i)) {
				color = surface.borderColor;
			}
			if (todayIndex != -1 && i == todayIndex) {
				color = surface.nearDayNumberColor;
				date[todayIndex].setIsNearDayValue("今天");
				date[todayIndex].setNearDay(true);
			} 
			if(tommorrowIndex!=-1&&i==tommorrowIndex){
				color = surface.nearDayNumberColor;
				date[tommorrowIndex].setIsNearDayValue("明天");
				date[tommorrowIndex].setNearDay(true);
			}
			if(afterTommorrowIndex!=-1&&i==afterTommorrowIndex){
				color = surface.nearDayNumberColor;
				date[afterTommorrowIndex].setIsNearDayValue("后天");
				date[afterTommorrowIndex].setNearDay(true);
			}
			if(listIntHaveCourseDate!=null&&listIntHaveCourseDate.size()>0){
				for(int j=0;j<listIntHaveCourseDate.size();j++){
					if(i==listIntHaveCourseDate.get(j)){
						color = surface.haveCourseDayColor;
						date[i].setIsHavaCourseDayValue("有课");
						date[i].setHaveCourseDay(true);
					}
				}
			}
				if(i>=selectDateText[0]&&i<=selectDateText[0]){//被选中了
					color = surface.seleteDateColor;
				}
			if(date[i].isNearDay){
				drawCellText(canvas, i, new String[]{date[i].getIsNearDayValue()} , color);
			}else if(date[i].isHaveCourseDay){
				drawCellText(canvas, i, new String[]{date[i].getDay()+"",date[i].getIsHavaCourseDayValue()} , color);
			}else{
			drawCellText(canvas, i,new String[]{ date[i].getDay() + ""}, color);
			}

		}
	}

	private void drawCellBg(Canvas canvas, int index, int color) {
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		surface.cellBgPaint.setColor(color);
		float left = surface.cellWidth * (x - 1) + surface.borderWidth;
		float top = surface.monthHeight + surface.weekHeight + (y - 1)
				* surface.cellHeight + surface.borderWidth;
		canvas.drawRect(left, top, left + surface.cellWidth
				- surface.borderWidth, top + surface.cellHeight
				- surface.borderWidth, surface.cellBgPaint);

	}

	private void drawDownOrSelectedBg(Canvas canvas) {
		// down and not up
		if (downDate != null) {
			drawCellBg(canvas, downIndex, surface.cellDownColor);
		}
		// selected bg color
		 if (!selectedEndDate.before(showFirstDate)  
	                && !selectedStartDate.after(showLastDate)) {  
	            int[] section = new int[] { -1, -1 };  
	            calendar.setTime(curDate);  
	            calendar.add(Calendar.MONTH, -1);  
	            findSelectedIndex(0, curStartIndex, calendar, section);  
	            if (section[1] == -1) {  
	                calendar.setTime(curDate);  
	                findSelectedIndex(curStartIndex, curEndIndex, calendar, section);  
	            }  
	            if (section[1] == -1) {  
	                calendar.setTime(curDate);  
	                calendar.add(Calendar.MONTH, 1);  
	                findSelectedIndex(curEndIndex, 42, calendar, section);  
	            }  
	            if (section[0] == -1) {  
	                section[0] = 0;  
	            }  
	            if (section[1] == -1) {  
	                section[1] = 41;  
	            }  
	            for (int i = section[0]; i <= section[1]; i++) {  
	                drawCellBg(canvas, i, surface.cellSelectedColor);  
	            }  
	        }  
	}

	private void findSelectedIndex(int startIndex, int endIndex,
			Calendar calendar, int[] section) {
		for (int i = startIndex; i < endIndex; i++) {
			calendar.set(Calendar.DAY_OF_MONTH, date[i].getDay());
			Date temp = calendar.getTime();
			if (temp.compareTo(selectedStartDate) == 0) {
				section[0] = i;
			}
			if (temp.compareTo(selectedEndDate) == 0) {
				section[1] = i;
				return;
			}
		}
	}

	private void drawCellText(Canvas canvas, int index, String[] text, int color) {
		if(text.length==1){
		int x = getXByIndex(index);
		int y = getYByIndex(index);
		surface.datePaint.setColor(color);
		float cellY = surface.monthHeight + surface.weekHeight + (y - 1)
				* surface.cellHeight + surface.cellHeight * 2 / 3f;
		float cellX = (surface.cellWidth * (x - 1))
				+ (surface.cellWidth - surface.datePaint.measureText(text[0]))
				/ 2f;
		canvas.drawText(text[0], cellX, cellY, surface.datePaint);
		}else if(text.length==2){
			int x = getXByIndex(index);
			int y = getYByIndex(index);
			surface.datePaint.setColor(color);
			float cellY = surface.monthHeight + surface.weekHeight + (y - 1)
					* surface.cellHeight + surface.cellHeight * 3 / 7f;
			float cellX = (surface.cellWidth * (x - 1))
					+ (surface.cellWidth - surface.datePaint.measureText(text[0]))
					/ 2f;
			canvas.drawText(text[0], cellX, cellY, surface.datePaint);
			cellY=cellY+surface.datePaint.getTextSize()+3.0f;
			cellX = (surface.cellWidth * (x - 1))
					+ (surface.cellWidth - surface.datePaint.measureText(text[1]))
					/ 2f;
			canvas.drawText(text[1], cellX, cellY, surface.datePaint);
		}

	}

	private int getXByIndex(int i) {
		return i % 7 + 1;// 1 2 3 4 5 6 7
	}

	private int getYByIndex(int i) {
		return i / 7 + 1;
	}

	private boolean isLastMonth(int i) {
		if (i < curStartIndex) {
			return true;
		}
		return false;
	}

	private boolean isNextMonth(int i) {
		if (i >= curEndIndex) {
			return true;
		}
		return false;
	}

	private void calculateDate() {
		calendar.setTime(curDate);
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		int dayInWeek = calendar.get(Calendar.DAY_OF_WEEK);
		Log.d(TAG, "day in week：" + dayInWeek);
		int monthStart = dayInWeek;
		if (monthStart == 1) {
			monthStart = 8;
		}
		monthStart -= 1;// 以日为开头 -1，以星期一为开头-2
		curStartIndex = monthStart;
		date[monthStart]=new DateCell(1);
		// last month
		if (monthStart > 0) {
			calendar.set(Calendar.DAY_OF_MONTH, 0);
			Log.v(TAG, "Calculate YEAR:" + calendar.get(Calendar.YEAR)
					+ " MONTH:" + calendar.get(Calendar.MONTH) + " DAY:"
					+ calendar.get(Calendar.DAY_OF_MONTH));
			int dayInmonth = calendar.get(Calendar.DAY_OF_MONTH);
			for (int i = monthStart - 1; i >= 0; i--) {
				date[i]=new DateCell(dayInmonth);
				dayInmonth--;
			}
			calendar.set(Calendar.DAY_OF_MONTH, date[0].getDay());
		}
		showFirstDate = calendar.getTime();
		// this month
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 0);
		int monthDay = calendar.get(Calendar.DAY_OF_MONTH);
		for (int i = 1; i < monthDay; i++) {
			date[monthStart + i]=new DateCell(i+1);
		}
		curEndIndex = monthStart + monthDay;
		// next month
		for (int i = monthStart + monthDay; i < 42; i++) {
			date[i]=new DateCell(i - (monthStart + monthDay) + 1);
		}
		if (curEndIndex < 42) {
			// 显示了下一月的
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		calendar.set(Calendar.DAY_OF_MONTH, date[41].getDay());
		showLastDate = calendar.getTime();

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setSelectedDateByColor(event.getX(), event.getY());

			break;
		case MotionEvent.ACTION_UP:
			if (downDate != null) {
				selectedStartDate = selectedEndDate = downDate;
				// 响应监听事件
				onItemClickListener.OnItemClick(selectedStartDate);
				downDate = null;
				invalidate();
			}
			break;
		}
		return true;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public interface OnItemClickListener {
		void OnItemClick(Date date);
	}

	private void setSelectedDateByColor(float x, float y) {
		if (y > surface.monthHeight + surface.weekHeight) {
			int m = (int) (Math.floor(x / surface.cellWidth) + 1);
			int n = (int) (Math
					.floor((y - (surface.monthHeight + surface.weekHeight))
							/ Float.valueOf(surface.cellHeight)) + 1);
			downIndex = (n - 1) * 7 + m - 1;
			Log.d(TAG, "downIndex:" + downIndex);
			calendar.setTime(curDate);
			if (isLastMonth(downIndex)) {
				calendar.add(Calendar.MONTH, -1);
			} else if (isNextMonth(downIndex)) {
				calendar.add(Calendar.MONTH, 1);
			}
			calendar.set(Calendar.DAY_OF_MONTH, date[downIndex].getDay());
			downDate = calendar.getTime();
		}
		invalidate();
	}

	// 获得当前应该显示的年月
	public String getYearAndmonth() {
		calendar.setTime(curDate);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		return year + "-" + surface.monthText[month];
	}

	// 上一月
	public String clickLeftMonth() {
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, -1);
		curDate = calendar.getTime();
		invalidate();
		return getYearAndmonth();
	}

	// 下一月
	public String clickRightMonth() {
		calendar.setTime(curDate);
		calendar.add(Calendar.MONTH, 1);
		curDate = calendar.getTime();
		invalidate();
		return getYearAndmonth();
	}

	/**
	 * 
	 * @ClassName: Surface
	 * @Description: TODO(1、布局尺寸 2、文字颜色，大小 3、当前日期的颜色，选择的日期颜色)
	 * @author lipengbo
	 * @date 2014-6-11 上午9:22:51
	 * 
	 */
	private class Surface {
		public float density;
		public int width;// 整个控件的宽度
		public int height;// 整个控件的高度
		public float monthHeight;// 显示月的高度
		public float weekHeight;// 显示星期的高度
		public float cellWidth;// 日期方框宽度
		public float cellHeight;// 日期方框高度
		public float borderWidth;
		public int bgColor = Color.parseColor("#FFFFFF");
		private int textColor = Color.BLACK;
		private int btnColor = Color.parseColor("#666666");
		private int borderColor = Color.parseColor("#CCCCCC");
		public int nearDayNumberColor = Color.parseColor("#985a1d");
		public int haveCourseDayColor = Color.parseColor("#985a1d");
		public int seleteDateColor=Color.WHITE;
		public int cellDownColor = Color.parseColor("#CCFFFF");
		public int cellSelectedColor = Color.parseColor("#32bc9e");
		public Paint borderPaint;
		public Paint monthPaint;
		public Paint weekPaint;
		public Paint datePaint;
		public Paint monthChangeBtnPaint;
		public Paint cellBgPaint;
		public Path boxPath;
		public String[] weekText = { "周日", "周一", "周二", "周三", "周四", "周五", "周六" };
		public String[] monthText = { "一月", "二月", "三月", "四月", "五月", "六月", "七月",
				"八月", "九月", "十月", "十一月", "十二月" };

		public void init() {
			float temp = height / 7f;
			monthHeight = 0;
			weekHeight = (float) ((temp + temp * 0.3f) * 0.7);
			cellHeight = (height - monthHeight - weekHeight) / 6f;
			cellWidth = width / 7f;
			borderPaint = new Paint();
			borderPaint.setColor(borderColor);
			borderPaint.setStyle(Paint.Style.STROKE);
			borderWidth = (float) (0.5 * density);
			borderWidth = borderWidth < 1 ? 1 : borderWidth;
			borderPaint.setStrokeWidth(borderWidth);
			monthPaint = new Paint();
			monthPaint.setColor(textColor);
			monthPaint.setAntiAlias(true);
			float textSize = cellHeight * 0.4f;
			Log.d(TAG, "text size:" + textSize);
			monthPaint.setTextSize(textSize);
			monthPaint.setTypeface(Typeface.DEFAULT_BOLD);
			weekPaint = new Paint();
			weekPaint.setColor(textColor);
			weekPaint.setAntiAlias(true);
			float weekTextSize = weekHeight * 0.35f;
			weekPaint.setTextSize(weekTextSize);
			weekPaint.setTypeface(Typeface.DEFAULT);
			datePaint = new Paint();
			datePaint.setColor(textColor);
			datePaint.setAntiAlias(true);
			float cellTextSize = cellHeight * 0.35f;
			datePaint.setTextSize(cellTextSize);
			datePaint.setTypeface(Typeface.DEFAULT);
			boxPath = new Path();
			boxPath.moveTo(0, monthHeight + weekHeight);
			boxPath.rLineTo(width, 0);
			boxPath.moveTo(0, monthHeight + weekHeight + 6 * cellHeight);
			boxPath.rLineTo(width, 0);
			monthChangeBtnPaint = new Paint();
			monthChangeBtnPaint.setAntiAlias(true);
			monthChangeBtnPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			monthChangeBtnPaint.setColor(btnColor);
			cellBgPaint = new Paint();
			cellBgPaint.setAntiAlias(true);
			cellBgPaint.setStyle(Paint.Style.FILL);
			cellBgPaint.setColor(cellSelectedColor);

		}
	}
     
	private class DateCell{
		private int day;
		private boolean isNearDay=false;
		private String isNearDayValue;
		private boolean isHaveCourseDay=false;
		private String isHavaCourseDayValue;
		
		public DateCell(int dayNumber){
			this.day=dayNumber;
		}
		public int getDay() {
			return day;
		}
		public void setDay(int day) {
			this.day = day;
		}
		public boolean isNearDay() {
			return isNearDay;
		}
		public void setNearDay(boolean isNearDay) {
			this.isNearDay = isNearDay;
		}
		public String getIsNearDayValue() {
			return isNearDayValue;
		}
		public void setIsNearDayValue(String isNearDayValue) {
			this.isNearDayValue = isNearDayValue;
		}
		public boolean isHaveCourseDay() {
			return isHaveCourseDay;
		}
		public void setHaveCourseDay(boolean isHaveCourseDay) {
			this.isHaveCourseDay = isHaveCourseDay;
			this.isNearDay=false;
		}
		public String getIsHavaCourseDayValue() {
			return isHavaCourseDayValue;
		}
		public void setIsHavaCourseDayValue(String isHavaCourseDayValue) {
			this.isHavaCourseDayValue = isHavaCourseDayValue;
		}
	}
	
	
}
