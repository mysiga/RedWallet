package wallet.mysiga.com.redwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

/**
 * 主页
 *
 * @author Wilson
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.common_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        findViewById(R.id.open_red_wallet_service).setOnClickListener(this);
        findViewById(R.id.notification_mode).setOnClickListener(this);
        findViewById(R.id.windows_mode).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open_red_wallet_service:
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    Toast.makeText(this, "找到抢红包辅助服务，然后开启服务即可", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.notification_mode:
                sendBroadcast(new Intent(WalletService.ACTION_NOTIFICATION_OPEN_RED));
                break;
            case R.id.windows_mode:
                sendBroadcast(new Intent(WalletService.ACTION_WINDOWS_OPEN_RED));
                break;
        }
    }
}
