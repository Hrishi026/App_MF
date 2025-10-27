package com.example.mutual_fund_comparison;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Fund> selected = new ArrayList<>();
    private FundAdapter adapter;
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextInputEditText etFund = findViewById(R.id.etFund);
        MaterialButton btnAdd = findViewById(R.id.btnAdd);
        MaterialButton btnFetch = findViewById(R.id.btnFetch);
        RecyclerView rv = findViewById(R.id.rvSelected);
        lineChart = findViewById(R.id.lineChart);
        RadioGroup rgChart = findViewById(R.id.rgChart);
        ChipGroup chipGroup = findViewById(R.id.chipGroup);

        // Recycler setup
        adapter = new FundAdapter(selected, position -> {
            if (position >= 0 && position < selected.size()) {
                selected.remove(position);
                adapter.notifyItemRemoved(position);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String name = etFund.getText() != null ? etFund.getText().toString().trim() : "";
            if (!TextUtils.isEmpty(name)) {
                selected.add(new Fund(name));
                adapter.notifyItemInserted(selected.size() - 1);
                etFund.setText("");
            }
        });

        btnFetch.setOnClickListener(v -> {
            // chart type: (we have only two in UI) -> decide dataset by radio group or chip
            int selectedChart = rgChart.getCheckedRadioButtonId();
            int chipId = chipGroup.getCheckedChipId();
            // For demo, we generate random sample data for each fund and overlay
            showSampleChart();
        });

        // initial demo
        selected.add(new Fund("Axis Income Plus Arbitrage"));
        adapter.notifyDataSetChanged();
        showSampleChart();
    }

    private void showSampleChart() {
        // Generate time-series sample data (single line)
        List<Entry> entries = new ArrayList<>();
        Random rnd = new Random();
        float value = 5f;
        for (int i = 0; i < 120; i++) { // 120 sample points (months)
            value += (rnd.nextFloat() - 0.45f) * 0.6f;
            entries.add(new Entry(i, value));
        }

        LineDataSet ds = new LineDataSet(entries, selected.isEmpty() ? "Demo fund" : selected.get(0).getName());
        ds.setLineWidth(2f);
        ds.setDrawCircles(false);
        ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ds.setDrawFilled(true);
        ds.setFillAlpha(80);

        LineData ld = new LineData(ds);
        lineChart.setData(ld);

        // styling
        lineChart.getDescription().setEnabled(false);
        XAxis x = lineChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawGridLines(true);
        Legend l = lineChart.getLegend();
        l.setForm(Legend.LegendForm.LINE);

        lineChart.invalidate();
    }
}
