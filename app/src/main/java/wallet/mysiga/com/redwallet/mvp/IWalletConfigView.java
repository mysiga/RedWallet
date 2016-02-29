package wallet.mysiga.com.redwallet.mvp;

import android.content.Context;

/**
 * 配置接口类
 * @author Wilson milin411@163.com
 */
public interface IWalletConfigView {
    /**
     * 更新服务器状态
     */
    void updateWalletServiceState();

    /**
     *  获取上下文
     * @return
     */
    Context getContext();
}
