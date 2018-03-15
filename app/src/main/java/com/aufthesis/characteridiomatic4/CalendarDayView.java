package com.aufthesis.characteridiomatic4;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
/ * Created by yoichi75jp2 on 2018/03/05.
 */

public class CalendarDayView extends LinearLayout
{
    private Context m_context;
    private View m_layout;

    private TextView m_date;
    private TextView m_today_count;

    // ////////////////////////////////////////////////////////////
    // コンストラクタ
    public CalendarDayView(Context context, AttributeSet attr) {
        super(context, attr);

        m_context = context;
        m_layout = LayoutInflater.from(context).inflate(R.layout.day_of_calendar, this);
        m_date = m_layout.findViewById(R.id.day_value);
        m_today_count = m_layout.findViewById(R.id.achievement);


    }

    public void initDateView(int id)
    {
        m_date.setText("07".toCharArray(),0,2);
        m_today_count.setText(m_context.getString(R.string.cal_date_count, 10));
        m_layout.setId(id);
    }
}
