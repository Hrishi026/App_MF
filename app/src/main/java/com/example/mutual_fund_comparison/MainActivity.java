package com.example.mutual_fund_comparison;

import android.graphics.Color;
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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.example.mutual_fund_comparison.model.Fund;
import com.example.mutual_fund_comparison.adapters.FundAdapter;
import com.example.mutual_fund_comparison.model.MockData;

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
				showSampleChart();
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
				showSampleChart();
            }
        });

        btnFetch.setOnClickListener(v -> {
            // chart type: (we have only two in UI) -> decide dataset by radio group or chip
            int selectedChart = rgChart.getCheckedRadioButtonId();
            int chipId = chipGroup.getCheckedChipId();
            // For demo, we generate random sample data for each fund and overlay
            showSampleChart();
        });

        // initial demo: pre-populate with a few mock funds
        selected.clear();
        selected.addAll(MockData.getSampleFunds());
        adapter.notifyDataSetChanged();
        showSampleChart();
    }

	private void showSampleChart() {
		// For each selected fund, generate a synthetic time-series and add as a dataset
		List<ILineDataSet> dataSets = new ArrayList<>();

		if (selected.isEmpty()) {
			lineChart.setData(null);
			lineChart.invalidate();
			return;
		}

		int points = 120; // 120 months
		for (Fund fund : selected) {
			// Deterministic color and randomness per fund name
			int seed = fund.getName() != null ? fund.getName().hashCode() : 0;
			Random rnd = new Random(seed);
			float value = 5f + (seed % 300) / 100f; // small offset between funds

			List<Entry> entries = new ArrayList<>(points);
			for (int i = 0; i < points; i++) {
				value += (rnd.nextFloat() - 0.45f) * 0.6f;
				entries.add(new Entry(i, value));
			}

			LineDataSet ds = new LineDataSet(entries, fund.getName());
			ds.setLineWidth(2f);
			ds.setDrawCircles(false);
			ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
			ds.setDrawFilled(false);
			// Distinct color based on fund name
			int color = Color.HSVToColor(new float[]{Math.abs(seed % 360), 0.7f, 0.9f});
			ds.setColor(color);
			ds.setHighLightColor(color);
			ds.setValueTextColor(color);

			dataSets.add(ds);
		}

		LineData ld = new LineData(dataSets);
		ld.setDrawValues(false);
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
		l.setWordWrapEnabled(true);

		lineChart.invalidate();
	}
}
