package com.laserfountain.framework.implementation;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.laserfountain.circly.ArcButton;
import com.laserfountain.circly.BonusNGon;
import com.laserfountain.circly.Building;
import com.laserfountain.circly.Button;
import com.laserfountain.circly.BuyButton;
import com.laserfountain.circly.ColorPalette;
import com.laserfountain.circly.ImageButton;
import com.laserfountain.circly.NumberFormatter;
import com.laserfountain.framework.Graphics;
import com.laserfountain.framework.Image;

import java.util.ArrayList;
import java.util.List;

public class AndroidGraphics implements Graphics {
    AssetManager assets;
    Bitmap frameBuffer;
    Canvas canvas;
    Paint paint;
    Paint darkTextPaint;
    Paint lightTextPaint;
    Paint smallLightTextPaint;
    Paint mediumLightTextPaint;
    Paint bonusPaint;

    Rect srcRect = new Rect();
    Rect dstRect = new Rect();

    private float xScale;
    private float yScale;

    public AndroidGraphics(AssetManager assets, Bitmap frameBuffer) {
        this.assets = assets;
        this.frameBuffer = frameBuffer;
        this.canvas = new Canvas(frameBuffer);
        this.paint = new Paint();
        this.darkTextPaint = new Paint();

        xScale = getWidth()/1080f;
        yScale = getHeight()/1920f;

        float fontSize = scale(100);

        darkTextPaint.setTextSize(fontSize);
        darkTextPaint.setTextAlign(Paint.Align.CENTER);
        darkTextPaint.setAntiAlias(true);
        darkTextPaint.setColor(ColorPalette.darkText);
        darkTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        this.lightTextPaint = new Paint();
        lightTextPaint.set(darkTextPaint);
        lightTextPaint.setColor(ColorPalette.lightText);

        this.smallLightTextPaint = new Paint();
        smallLightTextPaint.set(lightTextPaint);
        smallLightTextPaint.setTextSize(scale(30));

        this.mediumLightTextPaint = new Paint();
        mediumLightTextPaint.set(lightTextPaint);
        mediumLightTextPaint.setTextSize(scale(40));

        bonusPaint = new Paint();
        bonusPaint.setAntiAlias(true);
        bonusPaint.setColor(ColorPalette.bonus);
        bonusPaint.setShadowLayer(scale(10.0f), scale(2.0f), scale(2.0f), ColorPalette.buttonShadow);
    }

    @Override
    public int scaleX(int in) {
        return Math.round(in * xScale);
    }

    @Override
    public int scaleY(int in) {
        return Math.round(in * yScale);
    }

    @Override
    public int scale(int in) {
        return Math.min(scaleX(in), scaleY(in));
    }

    @Override
    public float scaleX(float in) {
        return in * xScale;
    }

    @Override
    public float scaleY(float in) {
        return in * yScale;
    }

    @Override
    public float scale(float in) {
        return Math.min(scaleX(in), scaleY(in));
    }

    @Override
    public void clearScreen(int color) {
        canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8, (color & 0xff));
    }

    @Override
    public void drawLine(double x, double y, double x2, double y2, int color) {
        paint.setColor(color);
        canvas.drawLine(Math.round(x), Math.round(y), Math.round(x2), Math.round(y2), paint);
    }

    @Override
    public void drawLine(double x, double y, double x2, double y2, Paint paint) {
        canvas.drawLine(Math.round(x), Math.round(y), Math.round(x2), Math.round(y2), paint);
    }

    @Override
    public void drawCircle(double x, double y, float radius, Paint painter) {
        canvas.drawCircle(Math.round(x), Math.round(y), radius, painter);
    }

    @Override
    public void drawArc(RectF rect, float percent, Paint painter) {
        canvas.drawArc(rect, -90, 360 * percent, false, painter);
    }

    @Override
    public void drawPartialArc(RectF rect, float startPercent, float sweepPercent, Paint painter) {
        canvas.drawArc(rect, -90 + 360 * startPercent, 360 * sweepPercent, false, painter);
    }

    @Override
    public void drawRectNoFill(int x, int y, int width, int height, int color) {
        paint.setColor(color);
        paint.setStyle(Style.STROKE);
        canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
    }

    @Override
    public void drawRect(int x, int y, int width, int height, int color) {
        paint.setColor(color);
        paint.setStyle(Style.FILL);
        canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
    }

    @Override
    public void drawRectWithShadow(int x, int y, int width, int height, int color) {
        Paint rectanglePainter = new Paint();
        rectanglePainter.setColor(color);
        rectanglePainter.setStyle(Style.FILL);
        rectanglePainter.setShadowLayer(scale(10.0f), scale(2.0f), scale(2.0f), ColorPalette.rectangleShadow);
        canvas.drawRect(x, y, x + width - 1, y + height - 1, rectanglePainter);
    }

    @Override
    public void drawNgon(double x, double y, double radius, int n, Paint paint, float rotation) {
        if (n == 1) {
            canvas.save();
            canvas.rotate(rotation, (float) x, (float) y);
            drawCircle(x + radius / 2, y, Math.round(radius / 4), paint);
            canvas.restore();
            return;
        } else if (n == 2) {
            canvas.save();
            canvas.rotate(rotation, (float) x, (float) y);
            paint.setStrokeWidth(Math.round(radius / 4));
            drawLine(x - radius, y, x + radius, y, paint);
            canvas.restore();
            return;
        }

        double angle = 2.0 * Math.PI / n;

        List<Point> points = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            points.add(new Point(
                    (int) Math.round(x + radius * Math.cos(angle * i)),
                    (int) Math.round(y + radius * Math.sin(angle * i))
            ));
        }

        Path path = new Path();
        path.moveTo(points.get(0).x, points.get(0).y);
        for (Point p : points.subList(1, points.size())) {
            path.lineTo(p.x, p.y);
        }
        path.lineTo(points.get(0).x, points.get(0).y);
        path.close();

        canvas.save();
        canvas.rotate(rotation, (float) x, (float) y);
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    @Override
    public void drawBonusNGon(BonusNGon bonusNGon, int corners) {
        drawNgon(bonusNGon.x, bonusNGon.y, bonusNGon.radius, corners, bonusPaint, 0);
    }

    @Override
    public void drawRect(double x, double y, double radius, Paint paint, float rotation) {
        double b = radius / Math.sqrt(2);
        canvas.save();
        canvas.rotate(rotation, (float) x, (float) y);
        canvas.drawRect(
                Math.round(x - b),
                Math.round(y - b),
                Math.round(x + b),
                Math.round(y + b),
                paint
        );
        canvas.restore();
    }

    @Override
    public void drawARGB(int a, int r, int g, int b) {
        paint.setStyle(Style.FILL);
        canvas.drawARGB(a, r, g, b);
    }

    @Override
    public void drawString(String text, double x, double y) {
        drawString(text, x, y, darkTextPaint);
    }

    @Override
    public void drawString(String text, double x, double y, Paint painter) {
        canvas.drawText(text, Math.round(x), Math.round(y) + painter.getFontMetrics().bottom, painter);
    }

    @Override
    public void drawStringCentered(String text) {
        drawStringCentered(text, darkTextPaint);
    }

    @Override
    public void drawStringCentered(String text, Paint painter) {
        drawString(text, getWidth() / 2, getHeight() / 2.5, painter);
    }

    @Override
    public void drawButton(String text, int x0, int y0, int x1, int y1) {
        Paint rectanglePainter = new Paint();
        rectanglePainter.setColor(ColorPalette.button);
        rectanglePainter.setShadowLayer(scale(10.0f), scale(2.0f), scale(2.0f), ColorPalette.buttonShadow);
        canvas.drawRect(x0, y0, x1, y1, rectanglePainter);
        drawString(text, x0 + (x1 - x0) / 2, y0 + (y1 - y0) / 2, lightTextPaint);
    }

    @Override
    public void drawButton(String text, int x0, int y0, int x1, int y1, int number, double cost, boolean enabled) {
        Paint rectanglePainter = new Paint();
        if (enabled) {
            rectanglePainter.setColor(ColorPalette.button);
        } else {
            rectanglePainter.setColor(ColorPalette.disabledButton);
        }
        canvas.drawRect(x0, y0, x1, y1, rectanglePainter);

        drawString(NumberFormatter.formatInt(number), x0 + (x1 - x0) / 2, y0 + (y1 - y0) / 2, lightTextPaint);
        String costString;
        if (cost > 0) {
            costString = NumberFormatter.formatDouble(cost);
        } else {
            costString = "?";
        }
        drawString(costString, x0 + (x1 - x0) / 2, y0 + (y1 - y0) * 9 / 10, mediumLightTextPaint);
        drawString(text, x0 + (x1 - x0) / 2, y0 + (y1 - y0) / 10, smallLightTextPaint);
    }

    @Override
    public void drawButton(Button b) {
        drawButton(b.text, b.x0, b.y0, b.x1, b.y1);
    }

    @Override
    public void drawBuyButton(BuyButton b, double clicks) {
        drawButton(b.getText(), b.x0, b.y0, b.x1, b.y1, b.getOwned(), b.getCost(), b.getCost() < clicks);
    }

    @Override
    public void drawBuildingButton(Building b, double clicks) {
        drawButton(b.getText(), b.x0, b.y0, b.x1, b.y1, b.getOwned(), b.getCost(), b.getCost() < clicks);
        if (b.upgradeAllowed()) {
            drawButton("Upgrade", b.ux0, b.uy0, b.ux1, b.uy1, b.getUpgrades(), b.getUpgradeCost(), b.upgradePossible(clicks));
        } else {
            drawButton("Upgrade", b.ux0, b.uy0, b.ux1, b.uy1, b.getUpgrades(), -1, false);
        }
    }

    @Override
    public void drawArcButton(ArcButton b) {
        Paint arcPainter = new Paint();
        arcPainter.setColor(ColorPalette.button);
        arcPainter.setShadowLayer(scale(10.0f), scale(2.0f), scale(2.0f), ColorPalette.buttonShadow);
        drawPartialArc(new RectF(b.x - b.xradius, b.y - b.yradius, b.x + b.xradius, b.y + b.yradius), -0.25f, 0.5f, arcPainter);
        drawString(b.text, b.x, b.y - b.yradius/2, lightTextPaint);
    }

    public void drawImage(Image Image, int x, int y, int srcX, int srcY, int srcWidth, int srcHeight) {
        srcRect.left = srcX;
        srcRect.top = srcY;
        srcRect.right = srcX + srcWidth;
        srcRect.bottom = srcY + srcHeight;

        dstRect.left = x;
        dstRect.top = y;
        dstRect.right = x + srcWidth;
        dstRect.bottom = y + srcHeight;

        canvas.drawBitmap(((AndroidImage) Image).bitmap, srcRect, dstRect, null);
    }

    @Override
    public void drawImage(Image Image, int x, int y) {
        canvas.drawBitmap(((AndroidImage) Image).bitmap, x, y, null);
    }

    @Override
    public void drawImageButton(ImageButton button, int overlay) {
        Bitmap bitmap = button.img.bitmap;

        dstRect.left = button.x0;
        dstRect.top = button.y0;
        dstRect.right = button.x1;
        dstRect.bottom = button.y1;

        canvas.drawBitmap(bitmap, null, dstRect, null);
        drawString(
                Integer.toString(overlay),
                button.x0 + (button.x1 - button.x0) / 2,
                button.y0 + (button.y1 - button.y0) / 2,
                lightTextPaint
        );
    }

    @Override
    public int getWidth() {
        return frameBuffer.getWidth();
    }

    @Override
    public int getHeight() {
        return frameBuffer.getHeight();
    }

    @Override
    public void drawPoints(float[] drawingPoints, Paint paint) {
        canvas.drawPoints(drawingPoints, paint);
    }

    @Override
    public void drawPath(Path path, Paint paint) {
        canvas.drawPath(path, paint);
    }
}
