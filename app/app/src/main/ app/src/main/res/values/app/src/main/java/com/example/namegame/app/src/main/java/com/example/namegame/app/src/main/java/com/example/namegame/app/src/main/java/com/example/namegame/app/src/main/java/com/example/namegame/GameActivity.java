package com.example.namegame;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private TextView tvCategory, tvLetter, tvTimer, tvScore;
    private GridLayout gridAnswers;
    private EditText etAnswer;
    private Button btnSubmit;

    private String[] categories = {"نام", "شهر", "کشور", "حیوان", "غذا", "اشیا"};
    private String currentCategory;
    private char currentLetter;
    private int currentPlayer = 0;
    private int timeLeft = 30;
    private int[] scores = {0, 0, 0, 0};

    private Handler gameHandler = new Handler();
    private boolean isHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvCategory = findViewById(R.id.tvCategory);
        tvLetter = findViewById(R.id.tvLetter);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        gridAnswers = findViewById(R.id.gridAnswers);
        etAnswer = findViewById(R.id.etAnswer);
        btnSubmit = findViewById(R.id.btnSubmit);

        String[] players = getIntent().getStringArrayExtra("players");
        isHost = getIntent().getBooleanExtra("isHost", false);

        setupGame();
        startTurn();

        btnSubmit.setOnClickListener(v -> submitAnswer());
    }

    private void setupGame() {
        currentCategory = categories[new Random().nextInt(categories.length)];
        currentLetter = getRandomLetter();
        
        tvCategory.setText("دسته: " + currentCategory);
        tvLetter.setText("حرف: " + currentLetter);
        updateScore();
    }

    private char getRandomLetter() {
        String letters = "ابپتثجچحخدذرزژسشصضطظعغفقکگلمنوهی";
        return letters.charAt(new Random().nextInt(letters.length()));
    }

    private void startTurn() {
        timeLeft = 30;
        updateTimer();
        gameHandler.postDelayed(timerRunnable, 1000);
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timeLeft--;
            updateTimer();
            if (timeLeft > 0) {
                gameHandler.postDelayed(this, 1000);
            } else {
                endTurn();
            }
        }
    };

    private void updateTimer() {
        tvTimer.setText("زمان: " + timeLeft + " ثانیه");
    }

    private void updateScore() {
        StringBuilder scoreText = new StringBuilder("امتیازها: ");
        for (int i = 0; i < scores.length; i++) {
            if (i < scores.length) {
                scoreText.append("بازیکن ").append(i + 1).append(": ").append(scores[i]).append(" ");
            }
        }
        tvScore.setText(scoreText.toString());
    }

    private void submitAnswer() {
        String answer = etAnswer.getText().toString().trim();
        if (!answer.isEmpty() && answer.charAt(0) == currentLetter) {
            // Add answer to grid
            TextView answerView = new TextView(this);
            answerView.setText(answer);
            answerView.setTextSize(16);
            answerView.setPadding(8, 8, 8, 8);
            gridAnswers.addView(answerView);
            
            etAnswer.setText("");
            scores[currentPlayer] += 10;
            updateScore();
        } else {
            Toast.makeText(this, "پاسخ باید با حرف " + currentLetter + " شروع شود", Toast.LENGTH_SHORT).show();
        }
    }

    private void endTurn() {
        gameHandler.removeCallbacks(timerRunnable);
        currentPlayer = (currentPlayer + 1) % 4;
        setupGame();
        startTurn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHandler.removeCallbacks(timerRunnable);
    }
}
