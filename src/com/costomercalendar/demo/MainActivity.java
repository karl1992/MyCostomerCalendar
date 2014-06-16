package com.costomercalendar.demo;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.costomercalendar.view.CalendarView;
import com.costomercalendar.view.CalendarView.OnItemClickListener;

import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private CalendarView calendarView;
	private TextView top_Date;
	private LinearLayout linear_pre_month;
	private LinearLayout linear_next_month;
	private TextView txt_date;
	private List<Date> listdate=new ArrayList<Date>();
	String[] strArray=new String[]{"2014-06-28","2014-06-19","2014-06-20","2014-06-27"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setListHaveCourseDate();
		//获取日历控件对象
		calendarView=(CalendarView)findViewById(R.id.myCalendarView);
		calendarView.setBackgroundResource(R.drawable.user_background);
		calendarView.setListHaveCourseDate(listdate);
		//获取日历中年月ya[0]为年，ya[1]为月（格式大家可以自行在日历控件中改）
		linear_pre_month=(LinearLayout)findViewById(R.id.linear_pre_month);
		linear_pre_month.setOnClickListener(this);
		linear_next_month=(LinearLayout)findViewById(R.id.linear_next_month);
		txt_date=(TextView)findViewById(R.id.txt_date);
		linear_next_month.setOnClickListener(this);
	    top_Date=(TextView)findViewById(R.id.Top_Date);
	    top_Date.setText(calendarView.getYearAndmonth());
	    calendarView.setOnItemClickListener(new calendarItemClickListener());
	    calendarView.invalidate();
	}
	
	//设置有课日期
	private Date setHaveCourseDate(String dateStr){
		DateFormat dd=new SimpleDateFormat("yyyy-MM-dd");
		Date date=null;
		try {
			date = dd.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	private void setListHaveCourseDate(){
		for(int i=0;i<strArray.length;i++){
			listdate.add(setHaveCourseDate(strArray[i]));
		}
	}
	
	
	
	class calendarItemClickListener implements OnItemClickListener{

		@Override
		public void OnItemClick(Date date) {
			// TODO Auto-generated method stub
			 SimpleDateFormat dateformat2=new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒 E ");   
		     String a2=dateformat2.format(date);
		     txt_date.setText(a2);
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.linear_pre_month:
			  top_Date.setText(calendarView.clickLeftMonth());
			break;
		case R.id.linear_next_month:
			  top_Date.setText(calendarView.clickRightMonth());
			break;
		}
	}



}
