**Google Cloud Message(GCM)**是Google提供給開發者使用的**推播服務平台(Push Notification Service)**
其前身稱為**Android Cloud to Device Messaging(C2DM)**，此於Android 2.2(Froyo)開始支援
在2012/6/27更名為Google Cloud Message(在Android 4.1發佈前)

GCM主要功能是透過Google的連結伺服器發佈信息給Android的裝置，藉此達到通知的功能
舉例來說：A在B的FB塗鴉牆上留言，FB將藉由推播系統通知B說"有人在你塗鴉牆留言"，這時候B的手機就會跳出通知來了唷。
**不過！**這個例子其實不太正確，原因是FB並不是使用GCM達到這些功能的，Facebook使用MQTT這項技術自行架設推播服務平台，這樣一來就不會受限於Google了!
若你有安裝Facebook APP的話可以在 設定 -> 應用程式管理 -> 執行中 點擊該應用程式來看即會看到如下畫面:
**MqttPushService**即是一支背景程式負責接收來自FB的推播訊息
<img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/n73XbaxThqqRoq1i03ka_Screenshot_2014-09-23-16-54-11%5B1%5D.png" alt="Screenshot_2014-09-23-16-54-11[1].png" width="300">
<hr>
在開發上，GCM的應用主要架構如下圖所示:
<img align="center" src="https://developer.android.com/images/gcm/GCM-arch.png" alt="GCM Architecture"><br>
(Reference: [https://developer.android.com/google/gcm/gcm.html](https://developer.android.com/google/gcm/gcm.html))<br><br>
其中3rd-Party App Server是指由開發者所架設的伺服器，是訊息發布的來源，connection server只是當成轉傳中繼站。(至於Google公司有沒有把所有訊息備份下來我就不清楚了XD)

看到這裡可能會有人覺得納悶，**為何不要直接第三方主機直接對手機傳送訊息就好?**
這是由於手機有可能處於NAT網路後面，並不是固定IP，這時伺服器就無法主動找到手機發布訊息了，Connection Server就是解決連線問題的存在。
這也是許多APP應用上使用Push Notification Service的原因，至於是怎麼樣解決連線問題可以尋找一些關鍵技術: **XMPP**、**MQTT**、**HTTP Binding**。

<hr>
接下來將透過範例來說明整個GCM的運作過程，在開始前先提醒開發者，網路上可以找到許多GCM的實作案例，但是**Google官方在Android 3.1之後將實作的部分整合成API GoogleCloudMessaging，有些實作方式已經被棄用了!**

+ Get API Key

   首先，想要在第三方伺服器上使用GCM的推播服務需要先向Google申請使用API的金鑰。
   所以請開發者先到**[Google Developers Console](https://console.developers.google.com)**申請專案與開啟API Key
  * Create Project
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/d0Pe0WflR1WNGz5c0e2f_create_project.png" alt="create_project.png" width="600">
<br>創建完後即可得到所謂的**SenderID**即是下圖中的Project Number，這會在APP的程式中使用到。<br>
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/CiuYVnuFRXGXdbKUNqhJ_senderid.png" alt="senderid.png" width="600">
  * Apply API
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/3bzRBigITNqxg5NkUxBw_enableGCM.png" alt="enableGCM.png" width="600">
  * Get Server Key
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/RR2JpunwRnumpFUhzplS_getkey.png" alt="getkey.png" width="600">
<br>按下後會詢問指定伺服器IP，可以選擇不填寫，這樣任何IP都能使用這一組金鑰，如下圖所示，紅色框起來的部分就是產生的金鑰，隨後需要放到第三方伺服器的程式碼之中。<br>
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/sCWZQZEpT0aQI5JUeE2g_key.png" alt="key.png" width="600">

<hr>
+ Implement 3rd-party Server

   在實作第三方伺服器時有兩種選擇，主要是選擇Connection Server的種類：HTTP or XMPP。
   兩者的差異可參考**[Android官方說明](https://developer.android.com/google/gcm/server.html)**，本篇將介紹使用HTTP的方法，透過JSON檔將要推播的訊息進行封裝，藉由HTTP POST的方式傳送到Connection Server轉發給各推播對象。
   
   這個Server主要有三個功能：**註冊regID**、**註銷regID**、**推播訊息**。
  **regID是由Android裝置向Google取得的一串識別碼**，在選擇推播對象時即是使用此碼，為此Server需要將regID儲存起來，作為推播時的**"收件者"**。
   當然有註冊就有註銷的功能，是為了讓APP端選擇終止推播服務時不會再被推播打擾，這點很重要，是因為在**APP端並無法透過API註銷**!這點將在後面做說明。
   
   在此提供我實作的程式碼**[GCMImplement](https://github.com/jerry30546/GCMImplement)**，這是依據Google官方所提供的**[範例程式](https://code.google.com/p/gcm/)**修改而成(沒甚麼改才對XD)。
   這邊提供我實作上的步驟與問題：
   我使用的IDE是Intellij與架設Tomcat Server。
  
  + Create Project
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/Z1cAyY0MSra0m9gi3MIC_new_project.png" alt="new_project.png" width="600">
  <br>如果是第一次設定的下需要指定Tomcat Server的家目錄位址<br>
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/urlMBCLWSAClsWuhVArM_indicate_server.png" alt="indicate_server.png" width="400">
  
  + Modify API Key
<br>在[project\_home]/web/WEB-INF/classes/api.key檔案中，將在Developers Console取得的API KEY換上即可。<br>
  + Error – no json library with gcm server API
   <br>Google所提供的範例程式碼會使用到JSON的library，創建新專案實並不會導入及編譯。<br>
   可以透過下列方法解決：<br>
   **File -> Project Structure -> Libraries -> "+" -> "com.googlecode.json-simple:json-simple:1.1"**<br>
  <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/eTDC46uRQiONy5QD7OhF_json.png" alt="json.png" width="600">
  
<hr>
+ Implement Client(APP)

   由於APP要使用GCM需要用到額外的libraries，所以在實作前請確認Android SDK是否有下載必要的library。<br>
   <img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/nRhWiPkSyOugdBgrrTjg_lib.png" alt="lib.png" width="300"><br>
   在APP這邊主要歸分為三大類功能：**取得regID**、**註冊或註銷regID**、**處理推播訊息**

  + 取得regID <br>
  在取得regID的部分主要透過API GoogleCloudMessaging進行，將想要接收的SenderID進行註冊，SenderID可以是多個來源。<br> 
  主要程式碼如下： <br>
``` 
String[] SENDER_ID = {"1011748981590", "75870242865"};
GoogleCloudMessaging gcm;
gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
String regid = gcm.register(SENDER_ID);
```
<br>
<img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/B3GdyteRS2RkrKla8hPA_Register.png" alt="Register.png" width="700">
<img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/d0cpr1dKQRLLAiJc2eGj_Screenshot_2014-09-24-22-56-55.png" alt="Screenshot_2014-09-24-22-56-55.png" width="300">


  + 註冊或註銷regID<br>
  向Server註冊或註銷的功能主要都在ServerUtilities.java這支程式裡。在取得regID後，即可透過**HTTP Request(POST)**的方式將regID送至第三方伺服器進行儲存。而在用戶端如果想要終止推播功能的話，則可透過向Server註銷regID的方式達成。這時有人可能會問，**GoogleCloudMessaging有提供unregister的API使用，怎麼還要註銷在Server所儲存的regID?**<br>
  官方的解釋為unregister的功能為使用在更新取的新的regID使用，註銷後重新透過register功能再向Google索取新的ID，**經過測試確實unregister功能呼叫後，APP仍可接收到由Server推播出來的訊息**。
  
  + 處理推播訊息<br>
  在處理推播訊息上，由前面介紹的內容可知，GCM機制透過**內建於Android系統內的Gcm service發送廣播(Broadcast)**給各APP，所以在APP的部分就由**BroadcastReceiver**進行接收處理。
  <br>由於應用程式常不在前景運作，故BroadcastReceiver會啟動Service去處理相關的事情，在程式碼中的GcmIntentService.java即是處理訊息的Service，將由GcmService傳來的**Intent擷取出Bundle找出夾帶訊息**，所要得出的值(value)需要一把鑰匙(key)，這個鑰匙及是在第三方伺服器傳送時使用Message API實所封裝的，如下列程式碼中的**"message"**
```
Message message = new Message.Builder().addData("message", userMessage).build();
```
<img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/V0JlRolYTwPlw1sW0C3O_sendnotification.png" alt="sendnotification.png" width="700">
<br>至於Connection Server到手機的這個步驟可以透過adb shell command看到手機一直使用HTTP Binding的方式連結著Server，如下圖所示。<br>
<img class="center" src="http://user-image.logdown.io/user/756/blog/753/post/234580/aGIXYncVQlEPlLFihfsD_binding.png" alt="binding.png" width="700">
