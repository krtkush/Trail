package krtkush.github.io.trailproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import krtkush.github.io.trail.Trail;

public class MainActivity extends AppCompatActivity {

    private Trail trail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        ArrayList<String> listValues = new ArrayList<>();

        for (int i = 0; i < 30; i++)
            listValues.add(i, String.valueOf(i));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        Adapter adapter = new Adapter(this, listValues);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        trail = new Trail.Builder()
                .setRecyclerView(recyclerView)
                .setMinimumViewingTimeThreshold(2000)
                .setMinimumVisibleHeightThreshold(60)
                .setDataDumpInterval(30000)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        trail.startTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();

        trail.getTrackingData(true);
    }
}
