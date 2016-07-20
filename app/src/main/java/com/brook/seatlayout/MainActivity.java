package com.brook.seatlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.brook.seatlayout.view.seatview.SeatRootView;
import com.brook.seatlayout.view.seatview.seatmodel.SeatRow;
import com.brook.seatlayout.view.seatview.seatmodel.SeatRowModel;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private SeatRootView seatView;
    private SeatRowModel seatRowModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seatView = (SeatRootView) findViewById(R.id.seat);
        seatRowModel = new SeatRowModel();
        seatRowModel.maxRowCount = 12;
        seatRowModel.maxColumnCount = 22;
        seatRowModel.seatRows = getSeat(seatRowModel.maxRowCount);
        seatView.setData(seatRowModel);
    }

    private ArrayList<SeatRow> getSeat(int row) {
        ArrayList<SeatRow> seatRows = new ArrayList<>(row);
        int i = 0;
        while (i++ < row) {
            SeatRow seatRow = new SeatRow();
            seatRow.columnIds = "01|02|03|04|ZL|05|06|07|08|09|10|11|12|13|14|15|16";
            seatRow.rowNum = i;
            if (2 == i || 6 == i) {
                seatRow.rowNum = -1;
            }
            seatRows.add(seatRow);
        }
        return seatRows;
    }
}
