package com.aufthesis.characteridiomatic4;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
/ * Created by yoichi75jp2 on 2018/03/05.
 */

public class OneDayView extends LinearLayout
{
    private Context m_context;
    private View m_layout;

    private TextView m_level;
    private TextView m_date;
    private TextView m_today_count;
    private TextView m_accumulation_count;

    // ////////////////////////////////////////////////////////////
    // コンストラクタ
    public OneDayView(Context context, AttributeSet attr) {
        super(context, attr);

        m_context = context;
        m_layout = LayoutInflater.from(context).inflate(R.layout.date_oneday, this);
        m_level = m_layout.findViewById(R.id.txt_level);
        m_date = m_layout.findViewById(R.id.txt_date);
        m_today_count = m_layout.findViewById(R.id.txt_today);
        m_accumulation_count = m_layout.findViewById(R.id.txt_accumulation);


    }

    public void initDateView(int id)
    {
        m_level.setText(m_context.getString(R.string.cal_level, 2));
        m_date.setText("07".toCharArray(),0,2);
        m_today_count.setText(m_context.getString(R.string.cal_date_count, 10));
        m_accumulation_count.setText(m_context.getString(R.string.cal_accumulation_count, 100));
        m_layout.setId(id);
    }
}
