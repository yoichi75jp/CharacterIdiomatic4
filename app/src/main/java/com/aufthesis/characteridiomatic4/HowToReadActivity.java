package com.aufthesis.characteridiomatic4;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Created by yoichi75jp2 on 2017/03/04.
public class HowToReadActivity extends AppCompatActivity implements View.OnClickListener {

    private Context m_context = null;

    private Map<Integer, Button> m_mapButton = new ConcurrentHashMap<>();
    private List<Integer> m_listID = new ArrayList<>();
    //private List<Pair<String,String>> m_listIdiom = new ArrayList<>();
    private List<Map<String,String>> m_listQuestion = new ArrayList<>();
    private List<Button> m_listClickButton = new ArrayList<>();
    private List<Button> m_listAnswerButton = new ArrayList<>();

    private ArrayList<String> m_answeredList = new ArrayList<>();

    private TextView m_record;

    private TextView m_txtIdiom;
    private TextView m_answer;

    final private int m_tolerance = 50;

    private AdView m_adView;
    private static InterstitialAd m_InterstitialAd;

    private class Idiom
    {
        String m_idiom;
        String m_read;
        int m_level;

        Idiom(String idiom, String read, int level)
        {
            m_idiom = idiom;
            m_read = read;
            m_level = level;
        }
    }
    private List<Idiom> m_listIdiom = new ArrayList<>();
    private List<String> m_listReading = new ArrayList<>();
    //private List<Idiom> m_listLowLevelIdiom = new ArrayList<>();
    private int m_sizeLevel1 = 0;
    private int m_sizeLevel2 = 0;

    private String m_mode;

    //private Context m_context;
    //private DBOpenHelper m_DbHelper;
    private SQLiteDatabase m_db;

    private Integer m_correctCount;

    private final int m_defaultColor = Color.parseColor("#fff482"); //
    private final int m_onClickColor = Color.parseColor("#E0FFFF"); // LightCyan
    //private final int m_correctColor = Color.parseColor("#00FF00"); // Lime

    private final List<Integer> m_listCorrectColor = new ArrayList<>(Arrays.asList(
            Color.parseColor("#B0FFB0"),
            Color.parseColor("#42FF42"),
            Color.parseColor("#00E100"),
            Color.parseColor("#00B400")
    ));

    // 効果音用
    final int SOUND_POOL_MAX = 6;
    private SoundPool m_soundPool;
    private int m_correctSound;
    private int m_incorrectSound;
    private int m_clearSoundID;
    private int m_levelUpID;
    private float m_volume;

    private SharedPreferences m_prefs;
    private DateFormat m_format;

    private boolean m_lookedAnswer = false;

    //private FirebaseAnalytics m_FirebaseAnalytics;
    private List<Integer> m_listNum = new ArrayList<>(Arrays.asList(1,2,3));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_howtoread);

        m_context = this;

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // Obtain the FirebaseAnalytics instance.
        //m_FirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle fireLogBundle = new Bundle();
        fireLogBundle.putString("TEST", "MyApp MainActivity.onCreate() is called.");
        MyApp.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.APP_OPEN, fireLogBundle);

        m_prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        m_format = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        m_volume = m_prefs.getInt(getString(R.string.seek_volume), 100)/100.f;
        m_mode = m_prefs.getString(getString(R.string.level), getString(R.string.level_normal));

        // スマートフォンの液晶のサイズを取得を開始
        // ウィンドウマネージャのインスタンス取得
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        // ディスプレイのインスタンス生成
        Display disp = wm.getDefaultDisplay();
        // スマートフォンの画面のサイズ
        Point point = new Point();
        disp.getSize(point);
        //int swsize = point.x;

        int textSize1 = 25;
        int textSize2 = 13;
        int textSize3 = 30;
        if(this.getResources().getConfiguration().smallestScreenWidthDp >= 600)
        {
            //Tabletの場合
            textSize1 = 60;
            textSize2 = 20;
            textSize3 = 80;
        }

        //フォントを変える場合
        //Typeface typefaceOriginal = Typeface.createFromAsset(getAssets(), "fonts/hkgyoprokk.ttf");

        m_listID.clear();
        m_listID.add(R.id.char1);
        m_listID.add(R.id.char2);
        m_listID.add(R.id.char3);
        m_listID.add(R.id.char4);
        m_listID.add(R.id.char5);
        m_listID.add(R.id.char6);
        m_listID.add(R.id.char7);
        m_listID.add(R.id.char8);
        m_listID.add(R.id.char9);
        m_listID.add(R.id.char10);
        m_listID.add(R.id.char11);
        m_listID.add(R.id.char12);
        m_listID.add(R.id.char13);
        m_listID.add(R.id.char14);
        m_listID.add(R.id.char15);
        m_listID.add(R.id.char16);
        m_listID.add(R.id.renew);
        m_listID.add(R.id.back);
        m_listID.add(R.id.answer_btn);
        m_listID.add(R.id.look_answer_btn);
        for(int i = 0; i < m_listID.size(); i++)
        {
            Button button = findViewById(m_listID.get(i));
            button.setOnClickListener(this);
            if(i < 16)
            {
                button.setTextSize(textSize1);
                //button.setTypeface(typefaceOriginal);
            }
            m_mapButton.put(m_listID.get(i),button);
        }

        TextView instruction2 = findViewById(R.id.instruction2);
        m_record = findViewById(R.id.record);

        instruction2.setTextSize(textSize2);
        m_record.setTextSize(textSize2);

        m_txtIdiom = findViewById(R.id.idiom);
        m_txtIdiom.setTextSize(textSize3);

        m_answer = findViewById(R.id.text_answer);
        m_answer.setTextSize(textSize1);

        DBOpenHelper dbHelper = new DBOpenHelper(this);
        m_db = dbHelper.getDataBase();

        this.setCharacterSet();

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, "ca-app-pub-1485554329820885~3571541058");

        //バナー広告
        m_adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        if(!MainActivity.g_isDebug)
            m_adView.loadAd(adRequest);

        // AdMobインターステイシャル
        m_InterstitialAd = new InterstitialAd(this);
        m_InterstitialAd.setAdUnitId(getString(R.string.adUnitInterId));
        m_InterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (m_lookedAnswer && m_InterstitialAd.isLoaded()) {
                    m_InterstitialAd.show();
                }
            }
        });
    }

    //Button押下時処理
    public void onClick(View view)
    {
        Intent intent;
        int id = view.getId();
        Button button = m_mapButton.get(id);
        String answer;
        switch(id)
        {
            case R.id.char1:
            case R.id.char2:
            case R.id.char3:
            case R.id.char4:
            case R.id.char5:
            case R.id.char6:
            case R.id.char7:
            case R.id.char8:
            case R.id.char9:
            case R.id.char10:
            case R.id.char11:
            case R.id.char12:
            case R.id.char13:
            case R.id.char14:
            case R.id.char15:
            case R.id.char16:
                if(m_listClickButton.indexOf(button) >= 0) break;
                if(m_listAnswerButton.indexOf(button) >= 0) break;
                //if(m_listClickButton.size() == 4) break;
                if(m_correctCount == 1) break;

                String character = button.getText().toString();
                answer = m_answer.getText().toString();
                answer += character;
                m_answer.setText(answer);

                m_listClickButton.add(button);
                button.setBackgroundColor(m_onClickColor);
                break;

            case R.id.renew:
                m_answer.setText(getString(R.string.blank));
                intent = new Intent(this, DummyActivity.class);
                startActivityForResult(intent, 1);
                //this.setCharacterSet();
                break;
            case R.id.back:
                if(m_correctCount >= 1) break;

                answer = m_answer.getText().toString();
                if(!answer.equals(""))
                {
                    answer = answer.substring(0, answer.length()-1);
                    m_answer.setText(answer);
                }

                if(m_listClickButton.size() > 0)
                {
                    m_listClickButton.get(m_listClickButton.size()-1).setBackgroundColor(m_defaultColor);
                    m_listClickButton.remove(m_listClickButton.size()-1);
                }
                break;
            case R.id.answer_btn:
                if(m_correctCount == 1)
                    this.showAnswer();
                else if (!m_answer.getText().toString().equals(""))
                {
                    String idiom = m_txtIdiom.getText().toString().replace(getString(R.string.blank), "");
                    answer = m_answer.getText().toString();

                    // 正解したとき
                    if (m_listReading.indexOf(answer) >= 0) {
                        for (int i = 0; i < m_listClickButton.size(); i++) {
                            Button btn = m_listClickButton.get(i);
                            m_listAnswerButton.add(btn);
                            btn.setBackgroundColor(m_listCorrectColor.get(m_correctCount+2));
                        }
                        if (!m_lookedAnswer) {
                            m_answeredList.add(idiom);
                            saveList(getString(R.string.answered_list2), m_answeredList);
                        }
                        m_record.setText(getString(R.string.record, m_answeredList.size()));
                        m_listClickButton.clear();
                        m_correctCount++;
                        if (DashboardActivity.m_listMax.indexOf(m_answeredList.size()) < 0)
                            m_soundPool.play(m_correctSound, m_volume, m_volume, 0, 0, 1.0F);
                        else {
                            m_soundPool.play(m_levelUpID, m_volume, m_volume, 0, 0, 1.0F);
                            LayoutInflater inflater = getLayoutInflater();
                            view = inflater.inflate(R.layout.toast_layout, null);
                            TextView text = view.findViewById(R.id.toast_text);
                            text.setText(getString(R.string.goal_achievement));
                            text.setGravity(Gravity.CENTER);
                            Toast toast = Toast.makeText(this, getString(R.string.goal_achievement), Toast.LENGTH_LONG);
                            toast.setView(view);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            this.induceReview();
                        }
                        button.setBackgroundResource(R.drawable.circle2);
                        button.setText(getString(R.string.look_answer));
                        Button look_answer_btn = m_mapButton.get(R.id.look_answer_btn);
                        look_answer_btn.setEnabled(false);

                        if(!MainActivity.g_doneReview)
                        {
                            int count = m_prefs.getInt(getString(R.string.count_induce), 0);
                            count++;
                            SharedPreferences.Editor editor = m_prefs.edit();
                            editor.putInt(getString(R.string.count_induce), count);
                            editor.apply();
                        }
                    }
                    else //間違えたとき
                    {
                        for (int i = 0; i < m_listClickButton.size(); i++) {
                            Button btn = m_listClickButton.get(i);
                            btn.setBackgroundColor(m_defaultColor);
                        }
                        m_answer.setText("");
                        m_listClickButton.clear();
                        m_soundPool.play(m_incorrectSound, m_volume, m_volume, 0, 0, 1.0F);
                    }
                }
                break;
            case R.id.look_answer_btn:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.confirm)
                        .setMessage(R.string.confirm_message1)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showAnswer();
                                m_lookedAnswer = true;
                                //Intent intent = new Intent(m_context, DummyActivity.class);
                                //startActivityForResult(intent, 1);
                                int count = m_prefs.getInt(getString(R.string.look_count), 0);
                                count++;
                                if(count >= 4)
                                {
                                    Collections.shuffle(m_listNum);
                                    if(m_listNum.get(0) == 1) {
                                        count = 0;
                                        m_InterstitialAd.loadAd(new AdRequest.Builder().build());
                                    }
                                }
                                SharedPreferences.Editor editor = m_prefs.edit();
                                editor.putInt(getString(R.string.look_count), count);
                                editor.apply();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                break;
        }
    }

    // 答えを見せてWeb検索もできる
    @SuppressWarnings("unchecked")
    private void showAnswer()
    {
        BaseAdapter adapter = new SimpleAdapter(this,
                m_listQuestion,
                android.R.layout.simple_list_item_2,
                new String[]{"idiom", "read"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        ListView listView = new ListView(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //@SuppressWarnings("unchecked")
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Map<String, String> conMap = (Map<String, String>)arg0.getItemAtPosition(arg2);
                String idiom = conMap.get("idiom");
                String read = conMap.get("read");
                Intent intent = new Intent(m_context, WebBrowserActivity.class);
                intent.putExtra(SearchManager.QUERY, getString(R.string.search_word2, idiom, read));
                startActivity(intent);
            }
        });
        listView.setAdapter(adapter);
        AlertDialog.Builder listDlg = new AlertDialog.Builder(this);
        listDlg.setTitle(getString(R.string.search_disc));
        listDlg.setView(listView);
        listDlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        listDlg.setCancelable(false);
        listDlg.show();
    }

    // 漢字をバラして設定する
    private void setCharacterSet() {
        //既に本日解答した熟語を取得する

        boolean isJP = Locale.getDefault().toString().equals(Locale.JAPAN.toString());

        m_lookedAnswer = false;
        m_answeredList = loadList(getString(R.string.answered_list2));
        int lastSize = m_answeredList.size();
        if(lastSize > 0)
            MainActivity.g_isInduceReviewTarget = false;
        String saveDay = m_prefs.getString(getString(R.string.save_day), "");
        //saveDay = "2016/11/06";
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        if(isJP)
            sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);

        Date formatSaveDate = new Date();
        try {
            // 文字列→Date型変換
            if (saveDay.equals("")) saveDay = m_format.format(new Date());
            formatSaveDate = sdf.parse(saveDay);
        } catch (ParseException exp) {
            Toast toast = Toast.makeText(this, exp.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
        //int diffDay = differenceDays(new Date(), formatSaveDate);

        Button look_answer_btn = m_mapButton.get(R.id.look_answer_btn);
        look_answer_btn.setEnabled(true);

        Calendar calendar = Calendar.getInstance();
        //int date = calender.get(Calendar.DATE);
        int thisMonth = calendar.get(Calendar.MONTH);
        calendar.setTime(formatSaveDate);
        int saveMonth = calendar.get(Calendar.MONTH);
        /*
        if(lastSize > 0 &&
                (diffDay >= 32 ||
                        (date == 1 && diffDay >= 28) ||
                        (date != 1 && diffDay >= date - 1)))
        {*/
        if (lastSize > 0 && thisMonth != saveMonth)
        {
            int maxScore = m_prefs.getInt(getString(R.string.max_score2), 0);
            showMaxScoreMessage(maxScore, lastSize);
            if(maxScore < lastSize)
            {
                SharedPreferences.Editor editor = m_prefs.edit();
                editor.putInt(getString(R.string.max_score2), lastSize);
                editor.apply();
            }
            m_answeredList.clear();
            saveList(getString(R.string.answered_list2), m_answeredList);
        }
        m_record.setText(getString(R.string.record, m_answeredList.size()));
        m_answer.setText("");

        m_correctCount = 0;
        m_listQuestion.clear();
        m_listReading.clear();
        if(m_listIdiom.size() <= 0)
        {
            try
            {
                String sql  = "select idiom, "
                        + "\"read-ja\""
                        + " , level"
                        + " from CharacterIdiom4 where not "
                        + "\"read-ja\""
                        + " is null";
                Cursor cursor = m_db.rawQuery(sql, null);
                cursor.moveToFirst();
                if (cursor.getCount() != 0) {

                    for (int i = 0; i < cursor.getCount(); i++) {
                        String idiom = (cursor.getString(0));
                        String read = (cursor.getString(1));
                        int level = 0;
                        if(!cursor.isNull(2)) {
                            level = (cursor.getInt(2));
                            if(level == 1) m_sizeLevel1++;
                            if(level == 2) m_sizeLevel2++;
                        }
                        //Pair<String,String> keyValue = new Pair<>(idiom, read);
                        //m_listIdiom.add(keyValue);
                        m_listIdiom.add(new Idiom(idiom, read, level));
                        //if(level == 1 || level == 2)
                        //    m_listLowLevelIdiom.add(new Idiom(idiom, read, level));
                        cursor.moveToNext();
                    }
                }
                cursor.close();
            }
            catch(Exception exp)
            {
                Toast toast = Toast.makeText(this, String.valueOf(exp.getMessage()),Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
        if(m_listIdiom.size() > 0)
        {
            m_txtIdiom.setText("");
            Collections.shuffle(m_listIdiom);
            List<String> listCharacter = new ArrayList<>();
            for(int i = 0; i < m_listIdiom.size(); i++)
            {
                boolean isExist = false;
                String idiom = m_listIdiom.get(i).m_idiom;
                String read = m_listIdiom.get(i).m_read;
                int level = m_listIdiom.get(i).m_level;
                if(read.equals("")) continue;
                if(m_answeredList.indexOf(idiom) >= 0) continue;  // 既に解答した熟語を省く
                if(m_mode.equals(getString(R.string.level_normal)))
                {
                    if(m_sizeLevel1 + m_sizeLevel2 - m_tolerance > m_answeredList.size() && level == 0 || level >= 3) continue;
                    if(m_sizeLevel1 - m_tolerance > m_answeredList.size() && level == 2) continue;
                }
                else if (level == 1 || level == 2) continue;

                if(m_txtIdiom.getText().equals(""))
                {
                    StringBuilder sbIdiom = new StringBuilder();
                    for(int j = 0; j < 4; j++)
                    {
                        sbIdiom.append(idiom.substring(j, j+1));
                        if(j < 3)
                            sbIdiom.append(getString(R.string.blank));
                    }
                    m_txtIdiom.setText(sbIdiom.toString());
                }
                else
                    idiom = m_txtIdiom.getText().toString();

                for(int j = 0; j < read.length(); j++)
                {
                    if(listCharacter.indexOf(read.substring(j, j+1)) >= 0) isExist = true;
                }
                if(isExist) continue;

                if(m_listQuestion.size() == 0)
                {
                    for(int j = 0; j < m_listIdiom.size(); j++)
                    {
                        String idiom2 = m_listIdiom.get(j).m_idiom;
                        String read2 = m_listIdiom.get(j).m_read;
                        if(idiom.equals(idiom2))
                        {
                            Map<String,String> mapQuestion = new HashMap<>();
                            mapQuestion.put("idiom",idiom2);
                            mapQuestion.put("read",read2);
                            m_listQuestion.add(mapQuestion);
                            setReading(read2, listCharacter, true);
                            m_listReading.add(read2);
                        }
                    }
                }
                if(setReading(read, listCharacter, false)) break;
            }
            Collections.shuffle(listCharacter);
            for(int i = 0; i < listCharacter.size(); i++)
            {
                Button button = m_mapButton.get(m_listID.get(i));
                button.setText(listCharacter.get(i));
                button.setBackgroundColor(m_defaultColor);
            }
            Button button = m_mapButton.get(R.id.answer_btn);
            button.setText(getString(R.string.to_answer));
            button.setBackgroundResource(R.drawable.circle);

            m_listClickButton.clear();
            m_listAnswerButton.clear();
        }
    }

/*
    //日付の差（日数）を算出する
    public int differenceDays(Date date1, Date date2) {
        long datetime1 = date1.getTime();
        long datetime2 = date2.getTime();
        long one_date_time = 1000 * 60 * 60 * 24;
        long diffDays = (datetime1 - datetime2) / one_date_time;
        return (int)diffDays;
    }
*/
private boolean setReading(String read, List<String> listCharacter, boolean isDuplicate)
{
    boolean is1st = listCharacter.size() == 0;
    for(int i = 0; i < read.length(); i++)
    {
        String chr = read.substring(i, i+1);
        boolean isExits = listCharacter.indexOf(chr) >= 0;
        int count = 0;
        if(isDuplicate)
        {
            for(int j = 0; j < read.length(); j++)
            {
                String chr2 = read.substring(j, j+1);
                if(chr.equals(chr2)) count++;
            }
        }
        boolean isDuplicate2 = count > 1;
        if(!is1st && isExits && !isDuplicate2)
            continue;
        listCharacter.add(chr);
        if(listCharacter.size() >= 16) break;
    }
    return listCharacter.size() >= 16;
}

    //１週間のスコアがこれまで最高スコアであるときに表示する
    public void showMaxScoreMessage(int score1, int score2)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.next_title));

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.max_score_layout, null);
        TextView highScoreText = view.findViewById(R.id.high_score);
        TextView lastWeeksScoreText = view.findViewById(R.id.last_week_score);
        TextView messageText = view.findViewById(R.id.message);

        highScoreText.setText(String.valueOf(score1));
        lastWeeksScoreText.setText(String.valueOf(score2));

        if(score1 > score2)
        {
            highScoreText.setTextSize(30);
            highScoreText.setTextColor(Color.RED);
            messageText.setText(getString(R.string.next_message1));
            messageText.setTextColor(Color.BLUE);
        }
        else if(score1 <= score2)
        {
            lastWeeksScoreText.setTextSize(30);
            lastWeeksScoreText.setTextColor(Color.RED);
            messageText.setText(getString(R.string.next_message2));
            messageText.setTextColor(Color.RED);
        }
        messageText.setGravity(Gravity.CENTER);

        dialog.setView(view);
        dialog.setPositiveButton(getString(R.string.next_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            /*
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.final_title));
            dialog.setMessage(getString(R.string.final_message));
            dialog.setPositiveButton(getString(R.string.final_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    //moveTaskToBack(true);
                }
            });
            dialog.setNegativeButton(getString(R.string.final_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
            */
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 設定値 ArrayList<String> を保存（Context は Activity や Application や Service）
    private void saveList(String key, ArrayList<String> list) {
        JSONArray jsonAry = new JSONArray();
        for(int i = 0; i < list.size(); i++) {
            jsonAry.put(list.get(i));
        }
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putString(key, jsonAry.toString());
        editor.putString(getString(R.string.save_day), m_format.format(new Date()));
        editor.apply();
    }

    // 設定値 ArrayList<String> を取得（Context は Activity や Application や Service）
    private ArrayList<String> loadList(String key) {
        ArrayList<String> list = new ArrayList<>();
        String strJson = m_prefs.getString(key, ""); // 第２引数はkeyが存在しない時に返す初期値
        if(!strJson.equals("")) {
            try {
                JSONArray jsonAry = new JSONArray(strJson);
                for(int i = 0; i < jsonAry.length(); i++) {
                    list.add(jsonAry.getString(i));
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        // 予め音声データを読み込む
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            m_soundPool = new SoundPool(SOUND_POOL_MAX, AudioManager.STREAM_MUSIC, 0);
        }
        else
        {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            m_soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(SOUND_POOL_MAX)
                    .build();
        }
        m_correctSound = m_soundPool.load(getApplicationContext(), R.raw.correct2, 1);
        m_incorrectSound = m_soundPool.load(getApplicationContext(), R.raw.incorrect1, 1);
        m_clearSoundID = m_soundPool.load(getApplicationContext(), R.raw.cheer, 1);
        m_levelUpID = m_soundPool.load(getApplicationContext(), R.raw.ji_023, 1);
        if (m_adView != null) {
            m_adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (m_adView != null) {
            m_adView.pause();
        }
        super.onPause();
        m_soundPool.release();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play, menu);
        //m_menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if(id == R.id.restore)
        {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm)
                    .setMessage(getString(R.string.confirm_message2))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int lastSize = m_answeredList.size();
                            if (lastSize > 0)
                            {
                                int maxScore = m_prefs.getInt(getString(R.string.max_score2), 0);
                                showMaxScoreMessage(maxScore, lastSize);
                                if(maxScore < lastSize)
                                {
                                    SharedPreferences.Editor editor = m_prefs.edit();
                                    editor.putInt(getString(R.string.max_score2), lastSize);
                                    editor.apply();
                                }
                            }
                            m_answeredList.clear();
                            saveList(getString(R.string.answered_list2), m_answeredList);
                            m_record.setText(getString(R.string.record, m_answeredList.size()));
                            Intent intent = new Intent(m_context, DummyActivity.class);
                            startActivityForResult(intent, 1);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
        if(id == R.id.close)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.final_title));
            dialog.setMessage(getString(R.string.final_message));
            dialog.setPositiveButton(getString(R.string.final_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                   moveTaskToBack(true);
                }
            });
            dialog.setNegativeButton(getString(R.string.final_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
        if(id == R.id.dashboard)
        {
            //既に本日解答した熟語を取得する
            ArrayList<String> answerList = loadList(getString(R.string.answered_list2));
            ArrayList<String> readList = new ArrayList<>();
            for(int i = 0; i < answerList.size(); i++)
            {
                for(int j = 0; j < m_listIdiom.size(); j++)
                {
                    if(m_listIdiom.get(j).m_idiom.equals(answerList.get(i)))
                    {
                        readList.add(m_listIdiom.get(j).m_read);
                        break;
                    }
                }
            }
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putStringArrayListExtra("idiom", answerList);
            intent.putStringArrayListExtra("read", readList);
            int requestCode = 1;
            startActivityForResult(intent, requestCode);
            //startActivity(intent);
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
            return true;
        }

        if(id == R.id.setting)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            int requestCode = 2;
            startActivityForResult(intent, requestCode);
            //startActivity(intent);
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
            return true;
        }

        if(id == android.R.id.home)
        {
            finish();
            // アニメーションの設定
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                this.setCharacterSet();
                break;
            case 2:
                m_volume = m_prefs.getInt(getString(R.string.seek_volume), 100)/100.f;
                break;
            case 3:
                break;
            case 4:
                break;
            default:break;
        }
    }

    private void induceReview()
    {
        if(MainActivity.g_doneReview) return;
        int count = m_prefs.getInt(getString(R.string.count_induce), 0);
        if(count >= 300)
        {
            try
            {
                Thread.sleep(500);
            }
            catch(Exception e){}
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.induce_title));
            dialog.setMessage(getString(R.string.induce_message));
            dialog.setPositiveButton(getString(R.string.induce_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = m_prefs.edit();
                    editor.putInt(getString(R.string.count_induce), 0);
                    editor.putBoolean(getString(R.string.review_done), true);
                    editor.apply();
                    Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
                    googlePlayIntent.setData(Uri.parse("market://details?id=com.aufthesis.characteridiomatic4"));
                    startActivity(googlePlayIntent);
                }
            });
            dialog.setNegativeButton(getString(R.string.induce_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.setCancelable(false);
            dialog.show();
        }
    }

}
