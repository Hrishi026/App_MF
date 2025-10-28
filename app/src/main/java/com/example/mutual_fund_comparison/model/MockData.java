package com.example.mutual_fund_comparison.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides a few mock funds to showcase the UI without network calls.
 */
public final class MockData {
    private MockData() {}

    public static List<Fund> getSampleFunds() {
        return new ArrayList<>(Arrays.asList(
                new Fund("Axis Income Plus Arbitrage"),
                new Fund("HDFC Flexi Cap Fund"),
                new Fund("SBI Small Cap Fund"),
                new Fund("Nippon India Index Nifty 50"),
                new Fund("ICICI Prudential Bluechip"),
                new Fund("Kotak Emerging Equity")
        ));
    }
}


