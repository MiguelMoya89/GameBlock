package com.example.gameview;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Definición de variables
    private ImageView smileyFace;
    private MediaPlayer reboteSound;
    private MediaPlayer outSound; // Nuevo objeto MediaPlayer para el sonido de salida
    private int smileyImageIndex = 0;
    private int fondoImageIndex = 0;
    private View paddle;
    private int smileyX, smileyY;
    private int smileySpeedX, smileySpeedY;
    private int paddleX;
    private int screenWidth, screenHeight;
    private boolean gameRunning = false;
    private LinearLayout container;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();

    private View block;

    private int blockHitCount = 0;
    private TextView blockHitCounterTextView;

    private int[] smileyImages = {
            R.drawable.imagen1,
            R.drawable.imagen2,
            R.drawable.imagen3,
            R.drawable.imagen4,
            R.drawable.imagen5,
            R.drawable.imagen6,
            R.drawable.imagen7,
            R.drawable.imagen8,
            R.drawable.imagen10,
            R.drawable.imagen11,
            R.drawable.imagen12,
            R.drawable.imagen13
    };

    private int[] fondoImages = {
            R.drawable.fondo1,
            R.drawable.fondo2,
            R.drawable.fondo4
    };

    private static final int REBOUND_RANDOMNESS = 3;
    private static final int BLOCK_EDGE_ADJUSTMENT = 10;
    private static final int BLOCK_RESPAWN_DELAY = 200;
    private static final int SPEED_INCREMENT = 2;
    private static final int INITIAL_SPEED = 8;
    private Random random = new Random();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de vistas y objetos
        block = findViewById(R.id.block);
        smileyFace = findViewById(R.id.ball);
        paddle = findViewById(R.id.paddle);
        container = findViewById(R.id.container);
        blockHitCounterTextView = findViewById(R.id.block_hit_counter);

        Button resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("https://fluxfm.streamabc.net/flx-chillhop-mp3-320-1595440?sABC=657812so%230%232qqpnss01895rqr0s8oq129o03s183o0%23fgernzf.syhksz.qr&aw_0_1st.playerid=streams.fluxfm.de&amsparams=playerid:streams.fluxfm.de;skey:1702367995");
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        reboteSound = new MediaPlayer();
        try {
            reboteSound.setDataSource("https://www.wavsource.com/snds_2020-10-01_3728627494378403/sfx/pluck.wav");
            reboteSound.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Inicialización del nuevo objeto MediaPlayer para el sonido de salida
        outSound = new MediaPlayer();
        try {
            outSound.setDataSource("https://www.wavsource.com/snds_2020-10-01_3728627494378403/video_games/duke/game_over.wav"); // Reemplaza "URL_DEL_SONIDO_DE_SALIDA" con la URL de tu sonido de salida
            outSound.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        container.post(new Runnable() {
            @Override
            public void run() {
                screenWidth = container.getWidth();
                screenHeight = container.getHeight();
                resetGame();
            }
        });

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    paddleX = (int) event.getX() - (paddle.getWidth() / 2);
                    if (paddleX < 0) paddleX = 0;
                    if (paddleX + paddle.getWidth() > screenWidth) paddleX = screenWidth - paddle.getWidth();
                    paddle.setX(paddleX);
                }
                return true;
            }
        });

        startGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (reboteSound != null) {
            reboteSound.release();
            reboteSound = null;
        }
        if (outSound != null) {
            outSound.release();
            outSound = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void resetGame() {
        // Restablecimiento del juego
        smileyFace.setImageResource(smileyImages[smileyImageIndex]);
        smileyImageIndex = (smileyImageIndex + 1) % smileyImages.length;

        container.setBackgroundResource(fondoImages[fondoImageIndex]);
        fondoImageIndex = (fondoImageIndex + 1) % fondoImages.length;

        smileyX = screenWidth / 2;
        smileyY = screenHeight / 2;
        smileySpeedX = INITIAL_SPEED;
        smileySpeedY = INITIAL_SPEED;
        paddleX = (screenWidth - paddle.getWidth()) / 2;
        smileyFace.setX(smileyX);
        smileyFace.setY(smileyY);
        paddle.setX(paddleX);

        blockHitCount = 0;
        blockHitCounterTextView.setText("Bloques: " + blockHitCount);
    }

    private void startGame() {
        // Inicio del juego
        gameRunning = true;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (gameRunning) {
                    smileyX += smileySpeedX;
                    smileyY += smileySpeedY;

                    checkCollision();

                    smileyFace.setX(smileyX);
                    smileyFace.setY(smileyY);
                    handler.postDelayed(this, 5);
                }
            }
        });
    }

    private void checkCollision() {
        // Comprobación de colisiones
        if (smileyX < 0 || smileyX + smileyFace.getWidth() > screenWidth) {
            smileySpeedX = -smileySpeedX;
        }
        if (smileyY < 0) {
            smileySpeedY = -smileySpeedY;
        } else if (smileyY + smileyFace.getHeight() > screenHeight) {
            if (outSound != null) {
                outSound.start();
            }
            resetGame();
        }

        if (smileyX + smileyFace.getWidth() >= paddleX &&
                smileyX <= paddleX + paddle.getWidth() &&
                smileyY + smileyFace.getHeight() >= screenHeight - paddle.getHeight()) {
            smileySpeedY = -smileySpeedY;
            smileySpeedX += random.nextInt(REBOUND_RANDOMNESS) - 1;
            if (reboteSound != null) {
                reboteSound.start();
            }
        }

        if (block.getVisibility() == View.VISIBLE &&
                smileyX + smileyFace.getWidth() >= block.getX() + BLOCK_EDGE_ADJUSTMENT &&
                smileyX <= block.getX() + block.getWidth() - BLOCK_EDGE_ADJUSTMENT &&
                smileyY + smileyFace.getHeight() >= block.getY() + BLOCK_EDGE_ADJUSTMENT &&
                smileyY <= block.getY() + block.getHeight() - BLOCK_EDGE_ADJUSTMENT) {
            smileySpeedX = -smileySpeedX;
            smileySpeedY = -smileySpeedY;

            smileySpeedX += random.nextInt(REBOUND_RANDOMNESS) - 1;
            smileySpeedY += random.nextInt(REBOUND_RANDOMNESS) - 1;

            block.setVisibility(View.INVISIBLE);

            blockHitCount++;

            if (blockHitCount % 10 == 0) {
                smileySpeedX += SPEED_INCREMENT;
                smileySpeedY += SPEED_INCREMENT;
            }

            blockHitCounterTextView.setText("Bloques: " + blockHitCount);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    respawnBlock();
                }
            }, BLOCK_RESPAWN_DELAY);
        }
    }

    private void respawnBlock() {
        // Respawn del bloque
        int x, y;
        do {
            x = random.nextInt(screenWidth - block.getWidth());
            y = random.nextInt(screenHeight - block.getHeight());
        } while ((x >= paddleX && x <= paddleX + paddle.getWidth() && y >= screenHeight - paddle.getHeight()) ||
                (x <= 0 || x + block.getWidth() >= screenWidth || y <= 0 || y + block.getHeight() >= screenHeight));
        block.setX(x);
        block.setY(y);

        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        int color = Color.rgb(red, green, blue);

        block.setBackgroundColor(color);

        block.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!gameRunning) {
            startGame();
        }
    }
}