package com.home.honor.home;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Exchanger;

/**
 * Created by honor on 2017/2/19.
 */
public class weather_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    static Context context;
    static LineData lineData;
    static JSONObject Data;
    static LineData Hour_Linedata;
    static LineData Day_Linedata;
    static LineData Week_Linedata;
    static int chartnum = 0;

    private LineData parseLineData(String days){
        ArrayList<Entry> arrEntry = new ArrayList<>();
        ArrayList<Entry> arrEntry2 = new ArrayList<>();
        try {
            JSONArray temp_array = Data.getJSONArray(days + "_temp");
            JSONArray humid_array = Data.getJSONArray(days + "_humid");

            for (int i = 0; i < 12; i++) {
                Entry entry = new Entry(i, temp_array.getInt(i));
                arrEntry.add(entry);
            }

            for (int i = 0; i < 12; i++) {
                Entry entry = new Entry(i, humid_array.getInt(i));
                arrEntry2.add(entry);
            }
            LineDataSet lds1 = new LineDataSet(arrEntry, "Temperature");
            lds1.setColor(Color.WHITE, 50);
            lds1.setCircleColor(Color.WHITE);
            lds1.setLineWidth(4);

            LineDataSet lds2 = new LineDataSet(arrEntry2, "Humidity");
            lds2.setColor(Color.parseColor("#03F4EB"), 50);
            lds2.setCircleColor(Color.parseColor("#03F4EB"));
            lds2.setDrawCircleHole(false);
            lds2.setLineWidth(4);
            lds1.setDrawValues(false);
            lds2.setDrawValues(false);

            return new LineData(lds1, lds2);
        }catch (Exception e){
            e.getMessage();
            return null;
        }


    }

    public void selChartData(TextView clicked, LineData seled){
        clicked.setTextColor(Color.parseColor("#ffffffff"));
        setLineData(seled);
    }


    public void setData(JSONObject jsonObject){
        Data = jsonObject;

        if(Data!=null) {
            Hour_Linedata = parseLineData("hour");
            Day_Linedata = parseLineData("day");
            Week_Linedata = parseLineData("week");
            switch (chartnum){
                case 0:
                    setLineData(Hour_Linedata);
                    break;
                case 1:
                    setLineData(Day_Linedata);
                    break;
                case 2:
                    setLineData(Week_Linedata);
                    break;
            }
        }else {
            Log.e("weather_adapter","data is null!");
        }
    }


    public weather_adapter(Context context) {this.context = context;}
    public void setLineData(LineData lineData){this.lineData=lineData;notifyDataSetChanged();}
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh =null;
        switch (viewType) {
            case 1:
                View viewOne = LayoutInflater.from(context).inflate(R.layout.weather_1, parent, false);
                vh = new One_viewholder(viewOne);
                break;
            case 2:
                View viewTwo =LayoutInflater.from(context).inflate(R.layout.weather_2, parent, false);
                vh = new Two_viewholder(viewTwo);
                break;
            case 3:
                View viewThere =LayoutInflater.from(context).inflate(R.layout.weather_3, parent, false);
                vh = new There_viewholder(viewThere);
        }
        return vh;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (position){
            case 0:
                try{
                    if (Data != null) {
                        One_viewholder vhold = (One_viewholder) holder;
                        vhold.temp.setText(Data.get("tigandu").toString());
                        vhold.location.setText(Data.get("city").toString());
                        vhold.description.setText(Data.get("ganmaozhishu").toString());
                        vhold.wind.setText(Data.get("wind_direction").toString());
                        vhold.tigan.setText(Data.get("tiganzhishu").toString());
                        vhold.air.setText(Data.get("pollution").toString());
                    }
                }catch (Exception e){
                    Log.e("weather_adapter:one",e.getMessage());
                }
                break;
            case 1:
                try {
                    if (Data != null) {
                        Two_viewholder tvh = (Two_viewholder) holder;
                        tvh.status_today.setText(Data.get("status_today").toString());
                        tvh.temp_today.setText(Data.get("temp_today").toString());
                        tvh.status_tomorrow.setText(Data.get("status_tomorrow").toString());
                        tvh.temp_tomorrow.setText(Data.get("temp_tomorrow").toString());
                        tvh.status_afterday.setText(Data.get("status_afterday").toString());
                        tvh.temp_afterday.setText(Data.get("temp_afterday").toString());
                    }
                }catch (Exception e){
                    Log.e("weather_adapter:two",e.getMessage());
                }
                break;
            case 2:
                There_viewholder vhold = (There_viewholder)holder;
                vhold.chart.setData(lineData);
                vhold.chart.setTouchEnabled(false);
                vhold.chart.setBorderColor(Color.WHITE);
                Legend l = vhold.chart.getLegend();
                l.setTextColor(Color.WHITE);
                XAxis xAxis = vhold.chart.getXAxis();
                xAxis.setTextColor(Color.WHITE);
                YAxis leftAxis = vhold.chart.getAxisLeft();
                leftAxis.setTextColor(Color.WHITE);
                leftAxis.setDrawGridLines(false);
                YAxis rightAxis = vhold.chart.getAxisRight();
                rightAxis.setTextColor(Color.WHITE);
                vhold.chart.getDescription().setEnabled(false);
        }
    }
    @Override
    public int getItemViewType(int position) {
        int p = position+1;
        return p;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class One_viewholder  extends RecyclerView.ViewHolder {
        public TextView temp;
        public TextView location;
        public TextView description;
        public TextView wind;
        public TextView tigan;
        public TextView air;
        public One_viewholder(View itemView) {
            super(itemView);
            temp = (TextView)itemView.findViewById(R.id.temperature);
            location = (TextView)itemView.findViewById(R.id.weather_loc);
            description = (TextView)itemView.findViewById(R.id.weather_description);
            wind = (TextView)itemView.findViewById(R.id.wind_direction);
            tigan = (TextView)itemView.findViewById(R.id.weather_tiganzhishu);
            air = (TextView)itemView.findViewById(R.id.weather_air);
        }
    }
    class Two_viewholder  extends RecyclerView.ViewHolder {
        public TextView status_today;
        public TextView temp_today;
        public TextView status_tomorrow;
        public TextView temp_tomorrow;
        public TextView status_afterday;
        public TextView temp_afterday;
        public Two_viewholder(View itemView) {
            super(itemView);
            status_today = (TextView)itemView.findViewById(R.id.status_today);
            temp_today = (TextView)itemView.findViewById(R.id.temp_today);
            status_tomorrow = (TextView)itemView.findViewById(R.id.status_tomorrow);
            temp_tomorrow = (TextView)itemView.findViewById(R.id.temp_tomorrow);
            status_afterday = (TextView)itemView.findViewById(R.id.status_afterday);
            temp_afterday = (TextView)itemView.findViewById(R.id.temp_afterday);
        }
    }
    class There_viewholder  extends RecyclerView.ViewHolder {
        public LineChart chart;
        public TextView hour_tv;
        public TextView day_tv;
        public TextView week_tv;
        public There_viewholder(View itemView) {
            super(itemView);

            chart = (LineChart)itemView.findViewById(R.id.chart);
            hour_tv = (TextView)itemView.findViewById(R.id.weather_hour);
            day_tv = (TextView)itemView.findViewById(R.id.weather_day);
            week_tv = (TextView)itemView.findViewById(R.id.weather_week);

            hour_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hour_tv.setTextColor(Color.parseColor("#99ffffff"));
                    day_tv.setTextColor(Color.parseColor("#99ffffff"));
                    week_tv.setTextColor(Color.parseColor("#99ffffff"));
                    chartnum = 0;
                    selChartData(hour_tv,Hour_Linedata);
                }
            });

            day_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hour_tv.setTextColor(Color.parseColor("#99ffffff"));
                    day_tv.setTextColor(Color.parseColor("#99ffffff"));
                    week_tv.setTextColor(Color.parseColor("#99ffffff"));
                    chartnum = 1;
                    selChartData(day_tv,Day_Linedata);
                }
            });

            week_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hour_tv.setTextColor(Color.parseColor("#99ffffff"));
                    day_tv.setTextColor(Color.parseColor("#99ffffff"));
                    week_tv.setTextColor(Color.parseColor("#99ffffff"));
                    chartnum = 2;
                    selChartData(week_tv,Week_Linedata);
                }
            });
        }
    }
}
