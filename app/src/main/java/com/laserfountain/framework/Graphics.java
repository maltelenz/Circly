package com.laserfountain.framework;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.List;

public interface Graphics {

    enum ImageFormat {
        ARGB8888, ARGB4444, RGB565
    }

    int scaleX(int in);

    int scaleY(int in);

    int scale(int in);

    float scaleX(float in);

    float scaleY(float in);

    float scale(float in);

    void clearScreen(int color);

    void drawLine(double x, double y, double x2, double y2, int color);

    void drawLine(double x, double y, double x2, double y2, Paint paint);

    void drawRect(int x, int y, int width, int height, int color);

    void drawRectNoFill(int x, int y, int width, int height, int color);

    void drawRectWithShadow(int x, int y, int width, int height, int color);

    void drawImage(Image image, int x, int y, int srcX, int srcY,
                   int srcWidth, int srcHeight);

    void drawImage(Image Image, int x, int y);

    void drawString(String text, int x, int y);

    void drawString(String text, double x, double y, Paint painter);

    void drawStringCentered(String text);

    void drawStringCentered(String string, Paint largePainter);

    int getWidth();

    int getHeight();

    void drawARGB(int i, int j, int k, int l);

    void drawCircle(double x, double y, float f, Paint painter);

    void drawArc(RectF rect, float percent, Paint painter);

    void drawPartialArc(RectF rect, float startPercent, float sweepPercent, Paint painter);

    void drawPoints(float[] drawingPoints, Paint paint);

    void drawPath(Path path, Paint paint);

}