package com.mysiga.wallet.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mysiga.wallet.R;
import com.mysiga.wallet.interfaces.IWalletConfigView;
import com.mysiga.wallet.model.WalletModeModel;
import com.mysiga.wallet.service.WalletService;

import java.util.ArrayList;

/**
 * @author Wilson milin411@163.com
 */
public class WalletPresenter {
    private final ArrayList<WalletModeModel> modes = new ArrayList<>();
    private final ArrayList<String> modeName = new ArrayList<>();
    private IWalletConfigView mWalletView;

    public WalletPresenter(IWalletConfigView walletView) {
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
        addModeData(new WalletModeModel(context.getString(R.string.mode_notification), WalletService.INTENT_ACTION_CHANGE_MODE));
        mWalletView.updateWalletServiceState();
    }

    public void initWindowServiceState() {
        modes.clear();
        modeName.clear();
        Context context = mWalletView.getContext();
        addModeData(new WalletModeModel(context.getString(R.string.mode_notification), WalletService.INTENT_ACTION_CHANGE_MODE));
        mWalletView.updateWalletServiceState();
    }

    private void addModeData(@NonNull WalletModeModel model) {
        modes.add(model);
        modeName.add(model.name);
    }
}
