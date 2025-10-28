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
    private RadioGroup rgChart;
    private ChipGroup chipGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextInputEditText etFund = findViewById(R.id.etFund);
        MaterialButton btnAdd = findViewById(R.id.btnAdd);
        MaterialButton btnFetch = findViewById(R.id.btnFetch);
        RecyclerView rv = findViewById(R.id.rvSelected);
        lineChart = findViewById(R.id.lineChart);
        rgChart = findViewById(R.id.rgChart);
        chipGroup = findViewById(R.id.chipGroup);

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

        btnFetch.setOnClickListener(v -> showSampleChart());

        rgChart.setOnCheckedChangeListener((group, id) -> showSampleChart());
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> showSampleChart());

        // initial demo: pre-populate with a few mock funds
        selected.clear();
        selected.addAll(MockData.getSampleFunds());
        adapter.notifyDataSetChanged();
        showSampleChart();
    }

    private void showSampleChart() {
        // For each selected fund, generate a synthetic NAV time-series and add as a dataset
		List<ILineDataSet> dataSets = new ArrayList<>();

		if (selected.isEmpty()) {
			lineChart.setData(null);
			lineChart.invalidate();
			return;
		}

        int years = getSelectedYears();
        int points = years * 12; // months
        boolean showCagr = rgChart.getCheckedRadioButtonId() == R.id.rbCagr;

		for (Fund fund : selected) {
			// Deterministic color and randomness per fund name
			int seed = fund.getName() != null ? fund.getName().hashCode() : 0;
			Random rnd = new Random(seed);
            float value = 10f + (seed % 300) / 50f; // starting NAV offset

            // Build a long base NAV history to slice from (10 years)
            int maxPoints = 10 * 12;
            float[] nav = new float[maxPoints];
            for (int i = 0; i < maxPoints; i++) {
                value *= 1f + ((rnd.nextFloat() - 0.45f) * 0.02f); // gentle monthly drift
                nav[i] = value;
            }

            int start = Math.max(0, maxPoints - points);
            List<Entry> entries = new ArrayList<>(points);
            if (!showCagr) {
                // NAV line (indexed to 0 at left for X axis)
                for (int i = 0; i < points; i++) {
                    entries.add(new Entry(i, nav[start + i]));
                }
            } else {
                // Rolling CAGR for selected window (years), plotted monthly
                // CAGR = (end/start)^(1/years) - 1 ; convert to percent
                int window = years * 12;
                for (int i = start; i < maxPoints; i++) {
                    int idx = i - start;
                    if (i - window >= 0) {
                        float end = nav[i];
                        float begin = nav[i - window];
                        float cagr = (float)(Math.pow(end / begin, 1.0 / years) - 1.0) * 100f;
                        entries.add(new Entry(idx, cagr));
                    } else {
                        entries.add(new Entry(idx, Float.NaN));
                    }
                }
            }

            LineDataSet ds = new LineDataSet(entries, labelFor(fund.getName(), showCagr, years));
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
        // Axis units
        if (rgChart.getCheckedRadioButtonId() == R.id.rbCagr) {
            lineChart.getAxisLeft().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return String.format("%.0f%%", value);
                }
            });
        } else {
            lineChart.getAxisLeft().setValueFormatter(null);
        }
		Legend l = lineChart.getLegend();
		l.setForm(Legend.LegendForm.LINE);
		l.setWordWrapEnabled(true);

		lineChart.invalidate();
	}

    private int getSelectedYears() {
        int id = chipGroup.getCheckedChipId();
        if (id == R.id.chip3) return 3;
        if (id == R.id.chip5) return 5;
        if (id == R.id.chip10) return 10;
        return 1;
    }

    private String labelFor(String fundName, boolean cagr, int years) {
        return cagr ? fundName + " (CAGR " + years + "y)" : fundName + " (NAV)";
    }
}
