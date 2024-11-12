package com.example.qr_scanner;

import static com.example.qr_scanner.MainActivity.getImageBitmap;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.print.PrintHelper;

public class ProcessImage1 extends AppCompatActivity {
    Button btnReturn;

    private static final Bitmap rawBitmap = getImageBitmap();
    protected static int[][] binaryMatrix;
    private static int top = 999;
    private static int bot = 0;
    private static int left = 999;
    private static int right = 0;
    protected static int[] imageHistogram;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 250, 250, false);
        PointCropExcessBitmap(resizedBitmap, 128);
        Log.i("testing", "start crop");
        resizedBitmap = CropBitmap(resizedBitmap);
        Bitmap tmp = toGrayscale(resizedBitmap);
        resizedBitmap = tmp;

        Log.i("testing", "done here 1");
        binaryMatrix = convertBitmapToBinaryMatrix(resizedBitmap, 128);

        Log.i("testing", "done here 2");
        setContentView(R.layout.activity_process_image1);
        btnReturn = findViewById(R.id.buttonReturn);

        Log.i("testing", "done here 3");
        Log.i("debugging", "width= " + resizedBitmap.getWidth() + ", height= " + resizedBitmap.getHeight());
        int debuging = findHorizontalFinderPattern(binaryMatrix, resizedBitmap.getHeight(), resizedBitmap.getWidth());
        Log.i("testing", "line = " + debuging);

        View mImg = findViewById(R.id.imageView);
        ((ImageView) mImg).setImageBitmap(resizedBitmap);
        btnReturn.setOnClickListener(view -> finish());
    }

    public Bitmap CropBitmap(Bitmap input) {
        Log.i("testing", "start crop");
        Bitmap output = input;
        Log.i("testing", "margin done");
        Log.i("testing", "top= " + top
                + ", bot= " + bot
                + ", left= " + left
                + ", right= " + right
        );
        Log.i("testing", "start crop");
        output = Bitmap.createBitmap(output, top, left, output.getWidth() - right, output.getHeight() - bot);
        Log.i("testing", "rescaling");
        output = Bitmap.createScaledBitmap(output, 250, 250, false);
        Log.i("testing", "crop done");
        return output;
    }

    public void PointCropExcessBitmap(Bitmap input, int threshold) {
        int outputBitmapHeight = input.getHeight();
        int outputBitmapWidth = input.getWidth();
        int blackCount = 0;
        Log.i("testing", "done here 1");
        for (int y = 0; y < outputBitmapHeight; y++) {
            blackCount = 0;
            for (int x = 0; x < outputBitmapWidth; x++) {
                int pixel = input.getPixel(x, y);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                if (gray < threshold) {
                    blackCount++;
                }
            }
            if (blackCount != 0) {
                top = Math.min(top, y);
            }

        }
        Log.i("testing", "done here 2");
        for (int x = 0; x < outputBitmapWidth; x++) {
            blackCount = 0;
            for (int y = 0; y < outputBitmapHeight; y++) {
                int pixel = input.getPixel(x, y);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                if (gray < threshold) {
                    blackCount++;
                }
            }
            if (blackCount != 0) {
                left = Math.min(left, x);
            }
        }
        Log.i("testing", "done here 3");
        for (int x = outputBitmapWidth - 1; x >= 0; x--) {
            blackCount = 0;
            for (int y = 0; y < outputBitmapHeight; y++) {
                int pixel = input.getPixel(x, y);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                if (gray < threshold) {
                    blackCount++;
                }
            }
            if (blackCount != 0) {
                right = Math.max(right, x);
            }
        }
        Log.i("testing", "done here 4");
        for (int y = outputBitmapHeight - 1; y >= 0; y--) {
            blackCount = 0;
            for (int x = 0; x < outputBitmapWidth; x++) {
                int pixel = input.getPixel(x, y);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                if (gray < threshold) {
                    blackCount++;
                }
            }
            if (blackCount != 0) {
                bot = Math.max(bot, y);
            }
        }
        bot = outputBitmapHeight - bot;
        right = outputBitmapWidth - right;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static int[][] convertBitmapToBinaryMatrix(Bitmap bitmap, float threshold) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        binaryMatrix = new int[width][height];
        imageHistogram = new int[256];
        float kr = 0.299F;
        float kb = 0.114f;
        float kg = 1 - kr - kb;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                float gray = (float) Color.red(pixel) * kr + (float) Color.green(pixel) * kg + (float) Color.blue(pixel) * kb;
                imageHistogram[Math.round(gray)]++;
                //Log.i("testing","x= "+x+",y= "+y+",gray= "+gray);
                /* White = 1 Black = 0 */
                binaryMatrix[x][y] = (gray < threshold) ? 0 : 1;
            }
        }
        return binaryMatrix;
    }

    public int findVerticalFinderPattern(int[][] input, int height, int width) {
        int columnFinderPattern = 0;
        int blackOccurred = 0;
        int whiteOccurred = 0;
        Log.i("testing", "done init");
        for (int x = 0; x < width; x++) {
            blackOccurred = 0;
            whiteOccurred = 0;
            int[] blackPixel = new int[100];
            int[] whitePixel = new int[100];
            int startPoint = 0;
            boolean startBlack = false;
            if (input[0][x] == 0)
                startBlack = true;
            for (int y = 0; y < height; y++) {
                //Log.i("testing", "x= " + x + ",y= " + y);
                if (startBlack) {
                    if (input[x][y] == 0) {
                        blackPixel[blackOccurred]++;
                    } else {
                        blackOccurred++;
                        startBlack = false;
                        whitePixel[whiteOccurred]++;
                    }
                }
                if (!startBlack) {
                    if (input[x][y] == 1) {
                        whitePixel[whiteOccurred]++;
                    } else {
                        whiteOccurred++;
                        startBlack = true;
                        blackPixel[blackOccurred]++;
                    }
                }
            }
            Log.i("testing", "x= " + x);
            int sumAll = blackPixel[0] + blackPixel[1] + blackPixel[2] + whitePixel[0] + whitePixel[1];
            int sumBlack = blackPixel[0] + blackPixel[1] + blackPixel[2];
            int sumWhite = whitePixel[0] + whitePixel[1];
            float unitSize = sumAll / 7.0f;
            boolean isPattern = false;
            Log.i("testing", "sumAll=" + sumAll + ", sumblack= " + sumBlack + ", sumwhite= " + sumWhite);
            if (Math.abs(blackPixel[0] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (Math.abs(blackPixel[1] - unitSize * 3) < 0.2f)
                isPattern = true;
            if (Math.abs(blackPixel[2] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (Math.abs(whitePixel[0] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (Math.abs(whitePixel[1] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (isPattern) {
                columnFinderPattern = x;
            }
        }
        return columnFinderPattern;
    }


    public int findHorizontalFinderPattern(int[][] input, int height, int width) {
        int rowFinderPattern = 0;
        int blackOccurred = 0;
        int whiteOccurred = 0;
        Log.i("testing", "done init");
        for (int y = 0; y < height; y++) {
            blackOccurred = 0;
            whiteOccurred = 0;
            int[] blackPixel = new int[100];
            int[] whitePixel = new int[100];
            int startPoint = 0;
            boolean startBlack = false;
            if (input[0][y] == 0)
                startBlack = true;
            for (int x = 0; x < width; x++) {
                //Log.i("testing", "x= " + x + ",y= " + y);
                if (startBlack) {
                    if (input[x][y] == 0) {
                        blackPixel[blackOccurred]++;
                    } else {
                        blackOccurred++;
                        startBlack = false;
                        whitePixel[whiteOccurred]++;
                    }
                }
                if (!startBlack) {
                    if (input[x][y] == 1) {
                        whitePixel[whiteOccurred]++;
                    } else {
                        whiteOccurred++;
                        startBlack = true;
                        blackPixel[blackOccurred]++;
                    }
                }
            }
            Log.i("testing", "y= " + y);
            int sumAll = blackPixel[0] + blackPixel[1] + blackPixel[2] + whitePixel[0] + whitePixel[1];
            int sumBlack = blackPixel[0] + blackPixel[1] + blackPixel[2];
            int sumWhite = whitePixel[0] + whitePixel[1];
            float unitSize = sumAll / 7.0f;
            boolean isPattern = false;
            Log.i("testing", "sumAll=" + sumAll + ", sumblack= " + sumBlack + ", sumwhite= " + sumWhite);
            if (Math.abs(blackPixel[0] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (Math.abs(blackPixel[1] - unitSize * 3) < 0.2f)
                isPattern = true;
            if (Math.abs(blackPixel[2] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (Math.abs(whitePixel[0] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (Math.abs(whitePixel[1] - unitSize * 1) < 0.2f)
                isPattern = true;
            if (isPattern) {
                rowFinderPattern = y;
            }
        }
        return rowFinderPattern;
    }
}