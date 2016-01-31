package wallet.mysiga.com.redwallet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

import wallet.mysiga.com.redwallet.model.WalletModeModel;

/**
 * 主页
 *
 * @author Wilson milin411@163.com
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String INTENT_ACTION_CONNECTED = "com.redwallet.action_connected";
    public static final String INTENT_ACTION_END = "com.redwallet.action_end";
    public static final String NO_START = "未启动服务";


    private final ArrayList<WalletModeModel> modes = new ArrayList<>();
    private final ArrayList<String> modeName = new ArrayList<>();
    private ArrayAdapter mModeAdapter;
    private ModeBroadcastReceiver modeBroadcastReceiver;

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

        Spinner modeView = (Spinner) findViewById(R.id.red_wallet_mode);
        findViewById(R.id.open_red_wallet_service).setOnClickListener(this);

        addModeData(new WalletModeModel(NO_START, null));
        mModeAdapter = new ArrayAdapter(this, R.layout.item_common_title_spinner, modeName);
        modeView.setAdapter(mModeAdapter);
        modeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!modes.isEmpty()) {
                    WalletModeModel mode = modes.get(position);
                    if (mode != null && !TextUtils.isEmpty(mode.mode)) {
                        sendBroadcast(new Intent(mode.mode));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        modeBroadcastReceiver = new ModeBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_CONNECTED);
        filter.addAction(INTENT_ACTION_END);
        registerReceiver(modeBroadcastReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        if (modeBroadcastReceiver != null) {
            unregisterReceiver(modeBroadcastReceiver);
        }
        super.onDestroy();
    }

    private void initModeView() {
        clear();
        addModeData(new WalletModeModel("外挂抢红包模式", WalletService.INTENT_ACTION_NOTIFICATION_OPEN_RED));
        addModeData(new WalletModeModel("当前聊天窗口抢红包模式", WalletService.INTENT_ACTION_WINDOWS_OPEN_RED));
        mModeAdapter.notifyDataSetChanged();
    }

    private void clearModeData() {
        clear();
        addModeData(new WalletModeModel(NO_START, null));
        mModeAdapter.notifyDataSetChanged();
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
        }
    }

    class ModeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case INTENT_ACTION_CONNECTED:
                    initModeView();
                    break;
                case INTENT_ACTION_END:
                    clearModeData();
                    break;
            }
        }
    }

    private void addModeData(@NonNull WalletModeModel model) {
        modes.add(model);
        modeName.add(model.name);
    }

    private void clear() {
        modes.clear();
        modeName.clear();
    }
}
