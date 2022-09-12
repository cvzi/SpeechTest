# SpeechTest

This app requests voice recognition from the current voice input app on Android.

It can be used to trigger a bug in Android 11 and lower that occures when a third party assistant app does't provide a valid `RecognitionService`:

> Android Developer Partner Outreach <app-compat-developer-outreach@google.com> wrote:
> 
> Dear Partner,
> 
> We recently uncovered a highly visible user-facing issue where your app causes the Android RecognitionService to break, which is potentially affecting all of your users on Android 11 or earlier.
> Specifically, when a user on Android 11 or earlier sets your assistant application as the default assistant for the phone, it is expected to provide RecognitionService (speech to text) functionality to other applications on the same device as well. However, if another app requests the RecognitionService functionality and does not receive it from your assistant app, the requesting app will immediately crash. The Android RecognitionService functionality is a widely used service. To fix the issue as soon as possible, please review the below recommendation.
> To address this issue, you could either:
> 1. Build & implement a RecognitionService of your own that can serve these queries when they arrive. OR2.Implement a “trampoline” service that will redirect RecognitionService requests sent to your assistant app, to the Google app, which has a RecognitionService available in all of these Android versions. 
> **OR** 
> 2. Implement a “trampoline” service that will redirect RecognitionService requests sent to your assistant app, to the Google app, which has a RecognitionService available in all of these Android versions.
> 
> Please note, option 2 is provided for you only as an expedient to resolving this highly visible user-facing bug. Sample code written in java + xml is provided at the end of this email purely for your convenience.
> We have no preference which approach you take, only that you provide us with a confirmation you received this email along with your choice of approach, and an estimated date of completion for the fix.
> Thank you for your help, and please do not hesitate to reach out if you have questions!
> 
>  
> Best,
> 
> Android Team
> 
> 
> Example:
> Add a RecognitionService entry in the app’s AndroidManifest.xml file. Replace class name with your class name.
> 
> ```xml
> <service android:name="com.example.recognitiontrampoline.RecognitionServiceTrampoline"
>       android:label="RecognitionServiceTrampoline"
>       android:exported="true">
>       <intent-filter>
>           <action android:name="android.speech.RecognitionService" />
>           <category android:name="android.intent.category.DEFAULT" >
>       </intent-filter>
> </service>
> ```
> 
> Make sure your app must have **Microphone Permission**. If it is not, please request users to grant Microphone Permission. If your app doesn’t have microphone permissions, client of the RecognitionService will receive [ERROR_INSUFFICIENT_PERMISSIONS](https://developer.android.com/reference/android/speech/SpeechRecognizer#ERROR_INSUFFICIENT_PERMISSIONS).
> 
> Here is the sample code which trampoline RecognitionService requests to Google app. Please change the package name and class name as per your requirements.
> 
> ```java
> package com.example.recognitiontrampoline;
> import android.content.ComponentName;
> import android.content.Intent;
> import android.os.Build;
> import android.os.Bundle;
> import android.os.RemoteException;
> import android.speech.RecognitionListener;
> import android.speech.RecognitionService;
> import android.speech.SpeechRecognizer;
> 
> import java.util.concurrent.ConcurrentHashMap;
> 
> public class RecognitionServiceTrampoline extends RecognitionService {
> private static final String TAG = "RSTrampoline";
> public final ConcurrentHashMap recognizerMap =
> new ConcurrentHashMap<>();
> 
> public RecognitionServiceTrampoline() {
> }
> 
> @Override
>  protected void onStartListening(Intent intent, Callback callback) {
>   if (!recognizerMap.containsKey(callback) || recognizerMap.get(callback) == null) {
>     SpeechRecognizer speechRecognizer =
>       SpeechRecognizer.createSpeechRecognizer(
>         getApplicationContext(),
>         new ComponentName(getRSPackageName(), getRecognitionServiceName()));
>     speechRecognizer.setRecognitionListener(createRecognitionListener(callback));
>     recognizerMap.put(callback, speechRecognizer);
>    }
>    recognizerMap.get(callback).startListening(intent);
>  }
> 
>  private String getRSPackageName() {
>   if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
>     return "com.google.android.tts";
>   } else {
>     return "com.google.android.googlequicksearchbox";
>   }
>  }
> 
>  private String getRecognitionServiceName() {
>   if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
>     return "com.google.android.apps.speech.tts.googletts.service.GoogleTTSRecognitionService";
>   } else {
>     return "com.google.android.voicesearch.serviceapi.GoogleRecognitionService";
>   }
>  }
> 
>  @Override
>  protected void onCancel(Callback callback) {
>   SpeechRecognizer speechRecognizer = recognizerMap.remove(callback);
>   if (speechRecognizer != null) {
>     speechRecognizer.cancel();
>   }
>  }
> 
>  @Override
>  protected void onStopListening(Callback callback) {
>   SpeechRecognizer speechRecognizer = recognizerMap.get(callback);
>   if (speechRecognizer != null) {
>     speechRecognizer.stopListening();
>   }
>  }
> 
> private RecognitionListener createRecognitionListener(RecognitionService.Callback callback) {
>  return new RecognitionListener() {
>   @Override
>   public void onReadyForSpeech(Bundle params) {
>     logIfThrows(() -> callback.readyForSpeech(params));
>   }
> 
>   @Override
>   public void onBeginningOfSpeech() {
>     logIfThrows(callback::beginningOfSpeech);
>   }
> 
>   @Override
>   public void onRmsChanged(float rmsdB) {
>     logIfThrows(() -> callback.rmsChanged(rmsdB));
>   }
> 
>   @Override
>   public void onBufferReceived(byte[] buffer) {
>     logIfThrows(() -> callback.bufferReceived(buffer));
>   }
> 
>   @Override
>   public void onEndOfSpeech() {
>     logIfThrows(callback::endOfSpeech);
>   }
> 
>   @Override
>   public void onError(int error) {
>     logIfThrows(() -> callback.error(error));
>   }
> 
>   @Override
>   public void onResults(Bundle results) {
>     logIfThrows(() -> callback.results(results));
>   }
> 
>   @Override
>   public void onPartialResults(Bundle partialResults) {
>     logIfThrows(() -> callback.partialResults(partialResults));
>   }
> 
>   @Override
>   public void onEvent(int eventType, Bundle params) {
>   }
> 
>   private void logIfThrows(RemoteExceptionRunnable runnable) {
>     try {
>       runnable.run();
>     } catch (RemoteException e) {
>     }
>    }
>   }
>  }
> 
>  private interface RemoteExceptionRunnable {
>   void run() throws RemoteException;
>  }
> }
> ```
