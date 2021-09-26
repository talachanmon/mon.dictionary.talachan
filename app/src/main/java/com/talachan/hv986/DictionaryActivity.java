package com.talachan.hv986;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.talachan.hv986.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class DictionaryActivity extends Activity implements View.OnClickListener {
    public static Date date = new Date();
    public static String today;
    public static String query;
    public static SQLiteDatabase database;
    public static Typeface typeFacePadauk;
    public static Typeface typeFaceOpenSans;

    public SharedPreferences recentQueries;
    public SharedPreferences storedFavourites;
    public Set favourites;
    public String meaning;
    public String randomWord;
    public AutoCompleteTextView searchview;
    public ImageButton favouriteButton;
    public ArrayAdapter<String> adapter;
    public TextView pageTitleComponent;
    public TextView wordComponent;
    public TextView meaningOfWordComponent;
    public AssetManager assetmanager;
    public Context context;

    public Dictionary dictionary;

    public DictionaryActivity() {
//		DataBaseHelper helper = new DataBaseHelper(context, "dictionary.sqlite", "com.talachan.hv986");
//		database = helper.openDataBase();
//		dictionary = new Dictionary(database);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBaseHelper helper = new DataBaseHelper(getApplicationContext(), "dictionary.sqlite", "com.talachan.hv986");
        database = helper.openDataBase();
        dictionary = new Dictionary(database);
        setContentView(R.layout.activity_main);
        getWidgets();
        showWordOfTheDay();
        enableDeviceSearchButton();

        // giving functionality for autocomplete
        searchview.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                pageTitleComponent.setVisibility(View.INVISIBLE);
                meaningOfWordComponent.setText("");
                String query = searchview.getText().toString();

                if (!query.equals("")) {
                    favouriteButton.setVisibility(View.VISIBLE);
                    meaning = dictionary.getMeaning(query);
                    if (meaning != null) {
                        renderWord(query, meaning);
                        SharedPreferences storedRecents = getStoredPreferences("recent");
                        dictionary.storeRecentWord(storedRecents, query);
                    }
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
    }

    private void enableDeviceSearchButton() {
        // TODO Auto-generated method stub
        searchview.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    query = searchview.getText().toString();

                    if (query.equals("")) {
                        meaningOfWordComponent.setText("Please Enter A Word");
                    } else {
                        SharedPreferences storedRecents = getSharedPreferences("recent", 0);
                        dictionary.storeRecentWord(storedRecents, query);
                    }
                    renderWord(query, meaning);
                    return true;
                }
                return false;
            }
        });

    }

    // Giving Functionality to Search Button
    @Override
    public void onClick(View mainView) {
        switch (mainView.getId()) {
            case R.id.favourite:
                storeFavouriteWord(searchview.getText().toString());
                break;
        }
    }

    public SharedPreferences getStoredPreferences(String key) {
        return getSharedPreferences(key, 0);
    }

    public void storeFavouriteWord(String word) {
        storedFavourites = getStoredPreferences("favourites");
        favourites = storedFavourites.getStringSet("favourites", new LinkedHashSet<String>());
        favourites.add(word);

        SharedPreferences.Editor favouriteEditor = storedFavourites.edit();
        favouriteEditor.putStringSet("favourites", favourites);
        favouriteEditor.commit();

        Toast.makeText(getApplicationContext(), "Added to favourites", Toast.LENGTH_SHORT).show();
    }

    // Converting given input according to Database Format
    // public String titleize(String word) {
    // char array[] = word.toCharArray();
    // array[0] = Character.toUpperCase(word.charAt(0));
    //
    // for (int i = 1; i < array.length; i++) {
    // array[i] = Character.toLowerCase(word.charAt(i));
    // }
    //
    // String newWord = new String(array);
    // Log.d("Modified Word", newWord);
    // return newWord;
    // }

    // Getting query Of The Day
    public void showWordOfTheDay() {
        searchview.setVisibility(View.VISIBLE);
        pageTitleComponent.setVisibility(View.VISIBLE);
        pageTitleComponent.setText("Word Of The Day");
        favouriteButton.setVisibility(View.INVISIBLE);
        wordComponent.setVisibility(View.INVISIBLE);
        meaningOfWordComponent.setText("");
        today = date.getDate() + "";
        Log.d("Todays Date", today);

        SharedPreferences storedWordOfTheDay = getStoredPreferences("WORDOFDAY");
        String wordOfTheDay = storedWordOfTheDay.getString(today, "");

        if (wordOfTheDay.equals("")) {
            randomWord = dictionary.getRandomWord();
            SharedPreferences.Editor editor = storedWordOfTheDay.edit();
            editor.putString(today, randomWord);
            editor.commit();
            meaningOfWordComponent.setText(randomWord + "\n\n" + dictionary.getMeaning(randomWord));
        } else {
            meaningOfWordComponent.setText(wordOfTheDay + "\n\n" + dictionary.getMeaning(wordOfTheDay));
        }
    }

    // Test method to log all the recent querys searched
    public Set getRecentWords() {
        recentQueries = getSharedPreferences("recent", 0);
        Set recents = recentQueries.getStringSet("recentValues", null);
        return recents;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuitems, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.home:
                showWordOfTheDay();
                break;
            case R.id.favourites:
                showFavourites();
                break;
            case R.id.aboutus:
                showAboutUs();
                break;
            case R.id.recent:
                showRecents();
                break;
            case R.id.random:
                showRandom();
                break;
        }
        return true;
    }

    private void showRandom() {
        wordComponent.setVisibility(View.INVISIBLE);
        randomWord = dictionary.getRandomWord();
        searchview.setVisibility(View.VISIBLE);
        favouriteButton.setVisibility(View.INVISIBLE);
        pageTitleComponent.setVisibility(View.VISIBLE);
        pageTitleComponent.setText("Random Word");
        searchview.clearComposingText();
        meaningOfWordComponent.setText(randomWord + "\n\n" + dictionary.getMeaning(randomWord));
    }

    private void showRecents() {
        pageTitleComponent.setVisibility(View.VISIBLE);
        pageTitleComponent.setText("Recently Searched querys");
        favouriteButton.setVisibility(View.INVISIBLE);
        meaningOfWordComponent.setVisibility(View.VISIBLE);
        searchview.setVisibility(View.VISIBLE);
        wordComponent.setVisibility(View.INVISIBLE);
        meaningOfWordComponent.setText("");
        searchview.clearComposingText();

        Set recents = getRecentWords();

        if (recents != null) {
            Iterator<String> recentWords = recents.iterator();
            String recentlyViewedQueries = "";

            while (recentWords.hasNext()) {
                String nextWord = recentWords.next();
                Log.d("values", nextWord + recents.size());
                String word = nextWord + "\n" + dictionary.getMeaning(nextWord) + "\n\n";
                meaningOfWordComponent.append(word);
            }
        } else {
            meaningOfWordComponent.setText("No Recents Found");
        }
    }

    private void showFavourites() {
        searchview.setVisibility(View.VISIBLE);
        pageTitleComponent.setVisibility(View.VISIBLE);
        meaningOfWordComponent.setVisibility(View.VISIBLE);
        pageTitleComponent.setText("Your Favourites");
        favouriteButton.setVisibility(View.INVISIBLE);
        storedFavourites = getStoredPreferences("favourites");
        favourites = storedFavourites.getStringSet("favourites", null);
        wordComponent.setVisibility(View.INVISIBLE);

        if (favourites != null) {
            Iterator<String> favouriteIterator = favourites.iterator();
            String viewFavouriteWords = "";

            while (favouriteIterator.hasNext()) {
                String favouriteWord = favouriteIterator.next();
                Log.d("values", favouriteWord + favourites.size());
                viewFavouriteWords = viewFavouriteWords + "\n\n" + favouriteWord + "\n" + dictionary.getMeaning(favouriteWord);
            }
            meaningOfWordComponent.setText(viewFavouriteWords);
        } else {
            meaningOfWordComponent.setText("No Favourites Found");
        }
    }

    private void showAboutUs() {
        searchview.setVisibility(View.GONE);
        wordComponent.setVisibility(View.GONE);
        pageTitleComponent.setText("About Us");
        String aboutus = " အဘိဓါန်ဝွံ ဒုၚ်လဝ်သ္ဇိုၚ်လ္တူ လိက်အုပ် နာဲထောန်ဝဵုမဒှ်ရ။ Mon Dictionary was Base on Nai Htun Wai and complie by HV-986. Any Suggestion? please visite  www.soelinthet.blogspot.com";
        meaningOfWordComponent.setText(aboutus);
    }

    public void getWidgets() {
        favouriteButton = (ImageButton) findViewById(R.id.favourite);
        pageTitleComponent = (TextView) findViewById(R.id.wordoftheday);
        favouriteButton.setVisibility(View.INVISIBLE);
        favouriteButton.setOnClickListener(this);
        meaningOfWordComponent = (TextView) findViewById(R.id.meaning);
        wordComponent = (TextView) findViewById(R.id.word);
        meaningOfWordComponent.setMovementMethod(new ScrollingMovementMethod());

        context = getBaseContext();
        assetmanager = getAssets();

        typeFacePadauk = Typeface.createFromAsset(assetmanager, "Padauk.ttf");
        typeFaceOpenSans = Typeface.createFromAsset(assetmanager, "Padauk.ttf");

        meaningOfWordComponent.setTypeface(typeFacePadauk);
        pageTitleComponent.setTypeface(typeFacePadauk);

        searchview = (AutoCompleteTextView) findViewById(R.id.searchView1);

        // searchview
        // .setAdapter(new ArrayAdapter<String>(this,
        // android.R.layout.simple_dropdown_item_1line, dictionaryData()));
        searchview.setPadding(10, 0, 0, 0);
        searchview.setThreshold(1);
        searchview.setHint("English query");
    }

    public ArrayList dictionaryData() {
        ArrayList<String> words = new ArrayList<String>();

        Cursor data = database.rawQuery("Select * from eng2mon order by eng_word", null);

        while (data.moveToNext()) {
            words.add(data.getString(data.getColumnIndex("eng_word")));
        }

        return words;
    }

    private void renderWord(String query, String meaning) {
        // TODO Auto-generated method stub
        wordComponent.setText(query);
        meaningOfWordComponent.setText(meaning);
    }
}
