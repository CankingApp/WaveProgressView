package com.canking.waveview;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import net.canking.waveview.R;

public class MainActivity extends FragmentActivity {
  private WaveProgressView mWaterWave;
  int progress = 50;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mWaterWave = (WaveProgressView) findViewById(R.id.waterWaveView);
  }

  public void start(View view) {
    mWaterWave.startWave();
  }

  public void stop(View view) {
    mWaterWave.stopWave();
  }

  public void progressUp(View view) {
    mWaterWave.setProgress((progress++));
  }

  public void progressDown(View view) {
    mWaterWave.setProgress((progress--));
  }

  public void progressSet(View view) {

  }

  public void colorTran(View view) {

  }

  public void colorChange(View view) {

  }
}
