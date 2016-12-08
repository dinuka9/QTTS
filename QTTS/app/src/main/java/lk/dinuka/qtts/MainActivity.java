package lk.dinuka.qtts;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    TextToSpeech tts;
    SeekBar speedSeekBar, pitchSeekBar;
    String utteranceId;
    String utteranceIDBelow20;
    Locale ttsLanguage;
    Spinner spinner;
    float readingSpeed = 1, readingPitch = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        toast(3000, "Initializing ... Please wait");

        spinner = (Spinner)findViewById(R.id.languageSpinner);
        speedSeekBar = (SeekBar)findViewById(R.id.speedSeekBar);
        pitchSeekBar = (SeekBar)findViewById(R.id.pitchSeekBar);

        speedSeekBar.setProgress(100);
        speedSeekBar.setMax(350);
        pitchSeekBar.setProgress(100);
        pitchSeekBar.setMax(350);

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                readingSpeed = (float) ((i/100) + 0.1);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //readingSpeed = (float) (((speedSeekBar.getProgress())/100)+0.1);
            }
        });
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                readingPitch = (float) ((i/100) + 0.1);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                List<String> langList = new ArrayList<String>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    langList = supportedLanguagesLollipop();
                } else {
                    langList = supportedLanguagesLegacy();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item, langList);
                spinner.setSelection(langList.indexOf("US"));
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        System.out.println(adapterView.getItemAtPosition(position).toString());
                        ttsLanguage = new Locale(adapterView.getItemAtPosition(position).toString());
                        tts.setLanguage(ttsLanguage);
                        toast(3000, "Selecting language. Please wait");
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                spinner.setAdapter(adapter);
            }
        });
    }
    public void readText(View view){
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(ttsLanguage);
                    tts.setSpeechRate(readingSpeed);
                    tts.setPitch(readingPitch);
                    String textToRead = ((EditText)findViewById(R.id.textToRead)).getText().toString();
                    if (!(tts.isSpeaking())){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ttsGreater21(textToRead);
                        } else {
                            ttsUnder20(textToRead);
                        }
                    }else {
                        Toast.makeText(MainActivity.this, "Plaease wait ... ", Toast.LENGTH_SHORT);
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Plaease wait ... ", Toast.LENGTH_SHORT);
                }
            }
        });
    }
    public void stopReading(View view){
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    if (tts.isSpeaking()) {
                        tts.stop();
                        tts.shutdown();
                    }
                }
            }
        });
    }

    List<String> languages = new ArrayList<String>();
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private List<String> supportedLanguagesLollipop() {
        Set<Locale> availableLocales = tts.getAvailableLanguages();
        for (Locale locale : availableLocales) {
            int res = tts.isLanguageAvailable(locale);
            if (res == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                languages.add(locale.toString());
            }
            System.out.println(locale);
        }
        return languages;
    }
    private List<String> supportedLanguagesLegacy() {
        Locale[] allLocales = Locale.getAvailableLocales();
        for (Locale locale : allLocales) {
            try {
                int res = tts.isLanguageAvailable(locale);
                boolean hasVariant = (null != locale.getVariant() && locale.getVariant().length() > 0);
                boolean hasCountry = (null != locale.getCountry() && locale.getCountry().length() > 0);
                boolean isLocaleSupported = false == hasVariant && false == hasCountry && res == TextToSpeech.LANG_AVAILABLE ||
                                false == hasVariant && true == hasCountry && res == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                                res == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
                if (true == isLocaleSupported) {
                    languages.add(locale.toString());
                }
            }
            catch (Exception ex) {
                System.out.println(ex);
            }
        }
        return languages;
    }

    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceIDBelow20);
        tts.speak(text, TextToSpeech.QUEUE_ADD, map);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        utteranceId = this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
    }
    public void toast(int millisec, String msg) {
        Handler handler = null;
        final Toast[] toasts = new Toast[1];
        for(int i = 0; i < millisec; i+=2000) {
            toasts[0] = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
            toasts[0].show();
            if(handler == null) {
                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toasts[0].cancel();
                    }
                }, millisec);
            }
        }
    }
}
