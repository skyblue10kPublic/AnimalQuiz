package com.deitel.flagquiz;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

import static java.security.AccessController.getContext;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnPlay;

    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";
    public static final String SOUNDON = "pref_soundOn";
    private boolean preferencesChanged = true; // did preferences change?
    //Fragment quizFragment;

    boolean soundOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnPlay = (Button) findViewById(R.id.btnPlay);

        btnPlay.setOnClickListener(btnCloseWelcomeView);

        soundOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_soundOn",true);

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);



        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);

    }

    private final View.OnClickListener btnCloseWelcomeView = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent resultData = new Intent();
            setResult(Activity.RESULT_OK,resultData);

            if (soundOn) {

                MediaPlayer player = null;
                try {
                    player = MediaPlayer.create(getApplicationContext(), R.raw.welcomegame);

                    final MediaPlayer finalPlayer = player;
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            finalPlayer.release();
                            finish();
                        }
                    });

                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                }
                catch (Exception e) {

                    if (player!=null)
                    {
                        player.release();
                    }
                }

                //player.start();
            }
            else
            {
                finish();
            }

        }
    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        // display the app's menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // inflate the menu
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        else
            return false;
    }

    // displays the SettingsActivity when running on a phone
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    // listener for changes to the app's SharedPreferences
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                // called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    preferencesChanged = true; // user changed app setting


                    MainActivityFragment quizFragment = (MainActivityFragment)
                            getSupportFragmentManager().findFragmentById(
                                    R.id.quizFragment);

                    if (key.equals(SOUNDON)) {
                        soundOn = sharedPreferences.getBoolean("pref_soundOn",true);
                    }
                    else {
                        if (key.equals(CHOICES)) { // # of choices to display changed
                            quizFragment.updateGuessRows(sharedPreferences);
                            //quizFragment.resetQuiz();
                        } else if (key.equals(REGIONS)) { // regions to include changed
                            Set<String> regions =
                                    sharedPreferences.getStringSet(REGIONS, null);

                            if (regions != null && regions.size() > 0) {
                                quizFragment.updateRegions(sharedPreferences);
                                quizFragment.resetQuiz();
                            } else {
                                // must select one region--set North America as default
                                SharedPreferences.Editor editor =
                                        sharedPreferences.edit();
                                regions.add(getString(R.string.default_region));
                                editor.putStringSet(REGIONS, regions);
                                editor.apply();

                                Toast.makeText(WelcomeActivity.this,
                                        R.string.default_region_message,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    Toast.makeText(WelcomeActivity.this,
                            R.string.restarting_quiz,
                            Toast.LENGTH_SHORT).show();

                }
            };

}
