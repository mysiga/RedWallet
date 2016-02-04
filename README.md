# RedWallet 微信抢红包

 - 当前抢红包有两种模式
 	-  外挂模式
 	![Flipboard playing multiple GIFs](http://7xn0ue.com1.z0.glb.clouddn.com/%E5%A4%96%E6%8C%82%E6%A8%A1%E5%BC%8F%E5%A4%96%E6%8C%82%E6%A8%A1%E5%BC%8F.gif)
 	-  窗口模式
 	![Flipboard playing multiple GIFs](http://7xn0ue.com1.z0.glb.clouddn.com/%E7%AA%97%E5%8F%A3%E6%A8%A1%E5%BC%8F%E7%AA%97%E5%8F%A3%E6%A8%A1%E5%BC%8F.gif)
 -  外挂模式
 	- 开启服务默认就开启了"外挂抢红包模式"
 	- 外挂模式就是不在当前聊天界面聊天都可以抢红包
 	- 添加屏幕在没有设置锁屏下可以在关闭屏幕下唤醒屏幕后台抢红包
 -  窗口模式
 	- 就是只能抢当前聊天界面的模式，
 	- bug:抢完每个当前聊天界面的红包，必须要把当前聊天界面的红包删掉。自己发的红包要自己点击抢红包才能抢到，不能自动帮你抢。
 - 使用心得
 	- 速度
 		- 网络:一定要保证手机网络或者本身手机的无线是好的
 		- 手机:这也是影响抢到红包的重要的，在当前聊天窗口抢红包最能体现,首先手机处理速度快。
 		- iPhone:这里提到下这个外挂都是基于Android手机写的，但是实际在与iPhone6s抢红包发现问题,在网络和手机都相当好的情况下才能抢的过iPhone6s(测试手机:Nexus6,mx3Nexus6与iPhone6s基本：1：1，mx3与iPhone6s基本：0：n)
 	- 时间
 		- 外挂模式:3s(正常)
 		- 当前聊天窗口抢红包模式：1-2s(正常)
 - 代码:可以先参考下[抢红包的鼻祖](https://github.com/lendylongli/qianghongbao.git),我也是基于他的代码修改,优化的
 	- 代码优化
 		- switch:jdk使用1.7，如果有if-elseif都采用switch

 		````
 		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void switchClickRedWallet(AccessibilityEvent event) {
        String eventName = String.valueOf(event.getClassName());
        switch (eventName) {
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI":
                //拆红包
                openRedWalletView();
                break;
            case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                //拆完红包后看详细的纪录界面
                break;
            case "com.tencent.mm.ui.LauncherUI":
                //点中领取红包
                clickRedWalletView();
                break;
            default:
                break;
        }
    }
 		````
 		- synchronized:在使用中发现有多线程安全问题

 		````
 		public synchronized void onAccessibilityEvent(AccessibilityEvent event) {
 		````
 		- 去掉部分for循环:

 		````
 		case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> messages = event.getText();
                if (!messages.isEmpty()) {
                    String message = String.valueOf(messages.get(0));
                    if (!message.contains(WECHAT_RED_TEXT_KEY)) {
                        return;
                    }
                    openNotification(event);
                }
                break;
 		````
 		在抢红包通知中“[微信红包]”判断就取第一个，其他点击'微信红包'，点击'抢红包'都取一条。
 		- 简化逻辑：每次只做一件事
 			- 外挂抢红包模式：用红包通知-->点击通知-->触发 AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED-->点击微信红包-->触发 AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED-点击抢红包-->触发 AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED-跳转抢红包详情页面，鼻祖的代码中有个一问题，点击完通知就立即获取当前页面是否有“微信红包”这个按钮。实际要触发了AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED这个事件才能有"微信红包"按钮
 		- 通知延迟都设置为0

 		````
 		<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged|typeNotificationStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:canRetrieveWindowContent="true"
    android:description="@string/server_description"
    android:notificationTimeout="0"
    android:packageNames="com.tencent.mm" />
 		````
 - 当前聊天窗口抢红包模式:重点说下这个吧这也是别人没有我却有的功能
 	- 地球人知道:微信当前页面聊天没有通知
 	- 当前窗口有消息只有三种状态改变：具体tag可以设置这个
 	````
 	android:accessibilityEventTypes="typeAllMask"
 	````
 		- AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
 			- 微信每个页面跳转都会触发这个
 		- AccessibilityEvent.TYPE_VIEW_SCROLLED:
 			- 当前页面有消息发过来都会触发这个，要是自己发信息就不会触发这个，而且一次短信就触发一次，触发对象ListView
 		- AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
 			- 有消息就会触发，每次都触发4次
 		- 最后选择AccessibilityEvent.TYPE_VIEW_SCROLLED这个事件来监听是不是这个页面有消息发送过来的标志，主要发生一次，