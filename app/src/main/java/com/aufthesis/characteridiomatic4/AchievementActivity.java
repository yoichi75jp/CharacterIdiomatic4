package com.aufthesis.characteridiomatic4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
/ * Created by yoichi75jp2 on 2018/02/26.
 */

public class AchievementActivity extends Activity implements View.OnClickListener
{
    private Context m_context;

    private RadioButton m_idiomRadBtn;
    private RadioButton m_readRadBtn;
    private LinearLayout m_matrixLayout;
    private TextView m_displayText;
    private Button m_preButton;
    private Button m_nextButton;

    final private List<String> m_weekList = new ArrayList<>();

    private List<Pair<Button,Date>> m_pairButtonDate = new ArrayList<>();
    private Map<Integer, Button> m_mapButtonID = new ConcurrentHashMap<>();

    final boolean m_isJP = Locale.getDefault().toString().equals(Locale.JAPAN.toString());
    final Date m_today = new Date(System.currentTimeMillis());

    private int m_textSize1 = 15;
    private int m_textSize2 = 20;
    private int m_textSize3 = 30;

    private int m_mode = 0;
    private Integer m_buttonCount = 0;

    private AdView m_adView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        m_context = this;

        m_matrixLayout = findViewById(R.id.view_matrix);

        m_idiomRadBtn = findViewById(R.id.idiom_rad);
        m_readRadBtn = findViewById(R.id.read_rad);
        m_idiomRadBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //TODO:ラジオボタン切替時処理
            }
        });
        m_displayText = findViewById(R.id.now_display);
        m_displayText.setOnClickListener(this);
        m_preButton = findViewById(R.id.pre_button);
        m_preButton.setOnClickListener(this);
        m_nextButton = findViewById(R.id.next_button);
        m_nextButton.setOnClickListener(this);

        //曜日リスト化
        m_weekList.add(getString(R.string.week_sun));
        m_weekList.add(getString(R.string.week_mon));
        m_weekList.add(getString(R.string.week_tue));
        m_weekList.add(getString(R.string.week_wed));
        m_weekList.add(getString(R.string.week_thu));
        m_weekList.add(getString(R.string.week_fri));
        m_weekList.add(getString(R.string.week_sat));

        if(this.getResources().getConfiguration().smallestScreenWidthDp >= 600)
        {
            //Tabletの場合
            m_textSize1 = 25;
            m_textSize2 = 70;
            m_textSize3 = 80;
        }
        m_displayText.setTextSize(m_textSize2);
        setData();

        //バナー広告
        m_adView = findViewById(R.id.adView3);
        AdRequest adRequest = new AdRequest.Builder().build();
        if(!MainActivity.g_isDebug)
            m_adView.loadAd(adRequest);

    }

    //@param mode 0:1Month(7x6), 1:1Year(3x4), 2:12Years(3x4)
    private void setData()
    {
        int maxRow = 0, maxColumn = 0;
        switch(m_mode)
        {
            case 0:
                maxRow = 7;
                maxColumn = 7;
                break;
            case 1:
            case 2:
                maxRow = 4;
                maxColumn = 3;
                break;
            default:break;
        }
        m_mapButtonID.clear();
        m_matrixLayout.removeAllViews();
        m_buttonCount = 0;

        Calendar firstFieldDate = get1stFieldDate();

        for(int row = 1; row <= maxRow; row++)
        {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParam.weight = 1.0f;
            rowLayout.setLayoutParams(rowParam);
            rowLayout.setGravity(Gravity.CENTER);
            for(int column = 1; column <= maxColumn; column++)
            {
                LinearLayout.LayoutParams colParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                colParam.weight = 1.0f;
                if(m_mode == 0 && row == 1)
                {
                    colParam.gravity = Gravity.BOTTOM;
                    TextView text = new TextView(this);
                    text.setLayoutParams(colParam);
                    text.setGravity(Gravity.CENTER);
                    text.setText(m_weekList.get(column-1));
                    text.setTextSize(m_textSize2);
                    switch (column)
                    {
                        case 1:
                            text.setTextColor(Color.RED);
                            break;
                        case 7:
                            text.setTextColor(Color.BLUE);
                            break;
                        default:
                            text.setTextColor(Color.BLACK);
                            break;
                    }
                    rowLayout.addView(text);
                }
                else
                {
                    m_buttonCount++;
                    Button button = new Button(this);
                    button.setLayoutParams(colParam);
                    //button.setTextColor(Color.WHITE);
                    String date = "";
                    switch (m_mode)
                    {
                        case 0:
                            date = getString(R.string.cal_date, firstFieldDate.get(Calendar.DATE));
                            firstFieldDate.add(Calendar.DATE,1);
                            break;
                        case 1:
                            date = getString(R.string.cal_month, firstFieldDate.get(Calendar.MONTH));
                            firstFieldDate.add(Calendar.MONTH,1);
                            break;
                        case 2:
                            date = getString(R.string.cal_year, firstFieldDate.get(Calendar.YEAR));
                            firstFieldDate.add(Calendar.YEAR,1);
                            break;
                    }
                    button.setText(date);
                    if(m_mode == 0)
                        button.setTextSize(m_textSize1);
                    else
                        button.setTextSize(m_textSize3);
                    button.setId(m_buttonCount);
                    rowLayout.addView(button);
                    button.setOnClickListener(this);
                    button.setBackgroundColor(Color.parseColor("#00000000"));
                    m_mapButtonID.put(button.getId(), button);
                }
            }
            m_matrixLayout.addView(rowLayout);
        }
    }

    //カレンダーの左上に対応する日付を取得する
    private Calendar get1stFieldDate()
    {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (m_mode)
        {
            case 0:
                boolean is1stDayOfMonth = false;
                do {
                    if(calendar.get(Calendar.DATE) == 1)
                        is1stDayOfMonth = true;

                    if(is1stDayOfMonth)
                    {
                        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                            break;
                    }
                    calendar.add(Calendar.DATE, -1);
                }while(true);
                break;
            case 1:
                do {
                    if(calendar.get(Calendar.DAY_OF_MONTH) == 1)
                        break;
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                }while(true);
                break;
            case 2:
                do {
                    if(calendar.get(Calendar.DAY_OF_YEAR) % 10 == 0)
                        break;
                    calendar.add(Calendar.DAY_OF_YEAR, -1);
                }while(true);
                break;
        }
        return calendar;
    }

    public void onClick(View view)
    {
        int id = view.getId();
        switch (id)
        {
            case R.id.now_display:
                m_mode++;
                if(m_mode > 2) m_mode = 0;
                setData();
                break;
            case R.id.pre_button:
                break;
            case R.id.next_button:
                break;
            default:
                for(int i = 1; i <= m_mapButtonID.size(); i++)
                {
                    m_mapButtonID.get(i).setBackgroundColor(Color.parseColor("#00000000"));
                }
                Button button = (Button)view;
                button.setBackgroundColor(Color.MAGENTA);
                break;
        }
    }

    @Override
    public void onPause() {
        if (m_adView != null) {
            m_adView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (m_adView != null) {
            m_adView.resume();
        }
    }
    @Override
    public void onDestroy()
    {
        if (m_adView != null) {
            m_adView.destroy();
        }
        super.onDestroy();
        setResult(RESULT_OK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            default:break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
