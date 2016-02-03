package wallet.mysiga.com.redwallet.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import wallet.mysiga.com.redwallet.R;
import wallet.mysiga.com.redwallet.WalletService;
import wallet.mysiga.com.redwallet.model.WalletModeModel;
import wallet.mysiga.com.redwallet.view.IWalletView;

/**
 * @author Wilson milin411@163.com
 */
public class WalletPresenter {
    private final ArrayList<WalletModeModel> modes = new ArrayList<>();
    private final ArrayList<String> modeName = new ArrayList<>();
    private IWalletView mWalletView;

    public WalletPresenter(IWalletView walletView) {
        this.mWalletView = walletView;
    }

    public ArrayList<WalletModeModel> getModes() {
        return modes;
    }

    public ArrayList<String> getModeName() {
        return modeName;
    }

    public void noStartService() {
        modes.clear();
        modeName.clear();
        mWalletView.updateWalletServiceState();
    }

    public void initNotificationServiceState() {
        modes.clear();
        modeName.clear();
        Context context = mWalletView.getContext();
        addModeData(new WalletModeModel(context.getString(R.string.mode_notification), WalletService.INTENT_ACTION_NOTIFICATION_OPEN_RED));
        addModeData(new WalletModeModel(context.getString(R.string.mode_window), WalletService.INTENT_ACTION_WINDOWS_OPEN_RED));
        mWalletView.updateWalletServiceState();
    }

    public void initWindowServiceState() {
        modes.clear();
        modeName.clear();
        Context context = mWalletView.getContext();
        addModeData(new WalletModeModel(context.getString(R.string.mode_window), WalletService.INTENT_ACTION_WINDOWS_OPEN_RED));
        addModeData(new WalletModeModel(context.getString(R.string.mode_notification), WalletService.INTENT_ACTION_NOTIFICATION_OPEN_RED));
        mWalletView.updateWalletServiceState();
    }

    private void addModeData(@NonNull WalletModeModel model) {
        modes.add(model);
        modeName.add(model.name);
    }
}
