package com.intellivision.adas.demo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.intellivision.adas.IVAdasEnumConstants.IVAdasTrafficLightState;
import com.intellivision.adas.IVAdasEnumConstants.IVAdasDepartureDirection;
import com.intellivision.adas.datamodels.IVAdasPcwOutput;
import com.intellivision.adas.datamodels.IVAdasPcwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasSpeedLimitOutput;
import com.intellivision.adas.datamodels.IVAdasSpeedLimitOutputEvent;
import com.intellivision.adas.demo.R;
import com.intellivision.adas.datamodels.IVAdasFcwOutput;
import com.intellivision.adas.datamodels.IVAdasFcwCertOutput;
import com.intellivision.adas.datamodels.IVAdasFcwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasLdwOutput;
import com.intellivision.adas.datamodels.IVAdasLdwOutputEvent;
import com.intellivision.adas.datamodels.IVAdasRoadMarkLine;
import com.intellivision.adas.datamodels.IVAdasSnGOutputEvent;
import com.intellivision.adas.datamodels.IVAdasTailgatingOutput;
import com.intellivision.adas.datamodels.IVAdasTailgatingOutputEvent;
import com.intellivision.adas.datamodels.IVAdasFcw2Output;
import com.intellivision.adas.datamodels.IVAdasFcw2OutputEvent;
import com.intellivision.adas.datamodels.IVAdasTrafficLightsOutput;
import com.intellivision.adas.datamodels.IVAdasTrafficLightsOutputEvent;
import com.intellivision.adas.datamodels.IVAdasRollingStopOutput;
import com.intellivision.adas.datamodels.IVAdasRollingStopOutputEvent;
import com.intellivision.adas.demo.datamodels.Settings;

import java.util.Locale;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

public class OverlayView extends ImageView {

    private static final int FCW_X = 200;
    private static final int FCW_DIST_Y = 50;
    private static final int FCW_TTC_Y = 100;
    private static final int SNG_X = 200;
    private static final int SNG_Y = 150;

    Paint p;
    private int pWidth, pHeight;

    private IVAdasFcwOutput fcwOutput = null;
    private IVAdasFcwCertOutput fcwCertOutput = null;
    private IVAdasLdwOutput ldwOutput = null;
    private IVAdasTailgatingOutput tgOutput = null;
    private IVAdasFcw2Output fcw2Output = null;
    private IVAdasTrafficLightsOutput trafficLightsOutput = null;
    private IVAdasRollingStopOutput rollingStopOutput = null;
    private IVAdasSpeedLimitOutput speedLimitOutput = null;
    private IVAdasPcwOutput pcwOutput = null;
    private IVAdasFcwOutputEvent fcwEvent = null;
    private IVAdasLdwOutputEvent ldwEvent = null;
    private IVAdasSnGOutputEvent sngEvent = null;
    private IVAdasTailgatingOutputEvent tgEvent = null;
    private IVAdasFcw2OutputEvent fcw2Event = null;
    private IVAdasTrafficLightsOutputEvent trafficLightsOutputEvent = null;
    private IVAdasRollingStopOutputEvent rollingStopOutputEvent = null;
    private IVAdasSpeedLimitOutputEvent speedLimitEvent = null;
    private IVAdasPcwOutputEvent pcwEvent = null;

    private float hoodLevel;
    private float horizonLevel;
    private float horizonPan;

    private boolean roiWasDrawed;

    public OverlayView(Context context) {
        super(context);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStrokeWidth(5);
            p.setColor(Color.GREEN);
            this.p = p;
            p.setTextSize(30);
            roiWasDrawed = false;
            drawHorizonValues(canvas);
            showLDWOutput(canvas);
            boolean fcw2Output = Settings.getBoolean(Settings.B_FCW2_OUTPUT);
            boolean trafficLightsOutput = Settings.getBoolean(Settings.B_TRAFFICLIGHTS_OUTPUT);
            boolean showStopSignsOutput = Settings.getBoolean(Settings.B_RS_ENABLED);
            boolean tgOutput = Settings.getBoolean(Settings.B_TG_OUTPUT);
            boolean showSpeedLimitOutput = Settings.getBoolean(Settings.B_SL_ENABLED);
            boolean showPcwOutput = Settings.getBoolean(Settings.B_PCW_ENABLED);
            if (fcw2Output) {
                showFcw2Output(canvas);
            }
            showFcw2Label(canvas, fcw2Output);
            if (trafficLightsOutput) {
                showTrafficLightsOutput(canvas);
            }
            if (showStopSignsOutput) {
                showStopSignsOutput(canvas);
            }
            if (tgOutput) {
                showTailgatingOutput(canvas);
            }
            if (showSpeedLimitOutput) {
                showSpeedLimitOutput(canvas);
            }
            if (showPcwOutput) {
                showPcwOutput(canvas);
            }
            showTailgatingLabel(canvas, tgOutput);
            showFcw2VehicleInfo(canvas, fcw2Output);
            showRollingStopLabel(canvas, showStopSignsOutput);
            showViolationLabel(canvas, showSpeedLimitOutput);
            showPcwLabel(canvas, showPcwOutput);
            showSnGOutput(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOutput(IVAdasLdwOutput ldwOutput,
                          IVAdasFcwOutput fcwOutput,
                          IVAdasFcwCertOutput fcwCertOutput,
                          IVAdasTailgatingOutput tgOutput,
                          IVAdasFcw2Output fcw2Output,
                          IVAdasTrafficLightsOutput trafficLightsOutput,
                          IVAdasRollingStopOutput rollingStopOutput,
                          IVAdasSpeedLimitOutput speedLimitOutput,
                          IVAdasPcwOutput pcwOutput,
                          IVAdasLdwOutputEvent ldwEvent,
                          IVAdasFcwOutputEvent fcwEvent,
                          IVAdasSnGOutputEvent sngEvent,
                          IVAdasTailgatingOutputEvent tgEvent,
                          IVAdasFcw2OutputEvent fcw2Event,
                          IVAdasTrafficLightsOutputEvent trafficLightsEvent,
                          IVAdasRollingStopOutputEvent rollingStopEvent,
                          IVAdasSpeedLimitOutputEvent speedLimitEvent,
                          IVAdasPcwOutputEvent pcwEvent) {
        this.fcwOutput = fcwOutput;
        this.fcwCertOutput = fcwCertOutput;
        this.ldwOutput = ldwOutput;
        this.tgOutput = tgOutput;
        this.fcw2Output = fcw2Output;
        this.trafficLightsOutput = trafficLightsOutput;
        this.rollingStopOutput = rollingStopOutput;
        this.speedLimitOutput = speedLimitOutput;
        this.pcwOutput = pcwOutput;
        this.ldwEvent = ldwEvent;
        this.fcwEvent = fcwEvent;
        this.sngEvent = sngEvent;
        this.tgEvent = tgEvent;
        this.fcw2Event = fcw2Event;
        this.trafficLightsOutputEvent = trafficLightsEvent;
        this.rollingStopOutputEvent = rollingStopEvent;
        this.speedLimitEvent = speedLimitEvent;
        this.pcwEvent = pcwEvent;
    }

    public void setHorizonValues(float hoodLevel, float horizonLevel, float horizonPan) {
        this.hoodLevel = hoodLevel;
        this.horizonLevel = horizonLevel;
        this.horizonPan = horizonPan;
    }

    private void showLDWOutput(Canvas canvas) {
        try {
            if (ldwOutput != null) {
                IVAdasRoadMarkLine leftLine = ldwOutput.getLeftLine();
                IVAdasRoadMarkLine rightLine = ldwOutput.getRightLine();
                IVAdasRoadMarkLine leftThresholdLine = ldwOutput.getLeftThresholdLine();
                IVAdasRoadMarkLine rightThresholdLine = ldwOutput.getRightThresholdLine();
                int leftColor = Color.GREEN;
                int rightColor = Color.GREEN;
                int thresholdColor = Color.YELLOW;
                if (ldwEvent.getIsPresent() != 0) {
                    if (ldwEvent.getDepartureDirection() == IVAdasDepartureDirection.IVDDTOWARDSLEFT) {
                        leftColor = Color.RED;
                    } else {
                        rightColor = Color.RED;
                    }
                }
                drawLine(canvas, leftLine, leftColor, 10, true);
                drawLine(canvas, rightLine, rightColor, 10, true);
                drawLine(canvas, leftThresholdLine, thresholdColor, 3, false);
                drawLine(canvas, rightThresholdLine, thresholdColor, 3, false);

                boolean ldwEventDetected = (ldwEvent.getIsPresent() != 0);
                boolean leftActive = ldwEventDetected &&
                        (ldwEvent.getDepartureDirection() == IVAdasDepartureDirection.IVDDTOWARDSLEFT);
                boolean rightActive = ldwEventDetected &&
                        (ldwEvent.getDepartureDirection() == IVAdasDepartureDirection.IVDDTOWARDSRIGHT);

                TextView leftLabel = getRootView().findViewById(R.id.tv_ldwLeft);
                leftLabel.setVisibility(leftActive ? VISIBLE : INVISIBLE);
                TextView rightLabel = getRootView().findViewById(R.id.tv_ldwRight);
                rightLabel.setVisibility(rightActive ? VISIBLE : INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawLine(Canvas canvas, IVAdasRoadMarkLine line, int color, int strokeWidth, boolean drawTypes) {
        if (line != null && line.getPointCount() > 0) {
            PointF points[] = line.getPoints();
            float x1, y1, x2, y2;
            if (points.length == 2) {
                String lineBase = "";
                p.setStrokeWidth(strokeWidth);
                p.setColor(color);

                if(drawTypes) {
                    if (line.getMarkType().getSolidity() == 0) {
                        lineBase = lineBase + "-/";
                    } else if (line.getMarkType().getSolidity() == 1) {
                        lineBase = lineBase + "S/";
                    } else if (line.getMarkType().getSolidity() == 2) {
                        lineBase = lineBase + "D/";
                    }

                    if (line.getMarkType().getColor() == 0) {
                        lineBase = lineBase + "-/";
                    } else if (line.getMarkType().getColor() == 1) {
                        lineBase = lineBase + "W/";
                    } else if (line.getMarkType().getColor() == 2) {
                        lineBase = lineBase + "Y/";
                    } else if (line.getMarkType().getColor() == 3) {
                        lineBase = lineBase + "B/";
                    }

                    if (line.getMarkType().getType() == 0) {
                        lineBase = lineBase + "-";
                    } else if (line.getMarkType().getType() == 1) {
                        lineBase = lineBase + "S";
                    } else if (line.getMarkType().getType() == 2) {
                        lineBase = lineBase + "D";
                    } else if (line.getMarkType().getType() == 3) {
                        lineBase = lineBase + "RL";
                    } else if (line.getMarkType().getType() == 4) {
                        lineBase = lineBase + "RR";
                    }
                }

                PointF p1 = points[0];
                PointF p2 = points[1];

                int width = canvas.getWidth();
                int height = canvas.getHeight();

                x1 = (p1.x * width / 100);
                y1 = (p1.y * height / 100);
                x2 = (p2.x * width / 100);
                y2 = (p2.y * height / 100);
                canvas.drawLine(x1, y1, x2, y2, p);
                if(drawTypes) {
                    canvas.drawText(lineBase, x1, (y2 + 10), p);
                }
            }
        }
    }

    private void showFCWOutput(Canvas canvas) {
        try {
            if (fcwOutput != null) {
                p.setStrokeWidth(6);
                p.setColor(Color.GREEN);
                pWidth = canvas.getWidth();
                pHeight = canvas.getHeight();
                PointF points[] = fcwOutput.getPoints();
                float x1, y1, x2, y2;
                if (points != null && points.length == 2) {
                    int textSize = (int)(getResources().getDimensionPixelSize(R.dimen.label_text_size) *
                            Settings.getFloat(Settings.F_FONT_SCALE_PERC) / 100);
                    int color = (fcwEvent.getIsPresent() != 0) ?
                            getResources().getColor(R.color.label_event, null) :
                            getResources().getColor(R.color.label_no_event, null);
                    p.setColor(color);
                    PointF p1 = points[0];
                    PointF p2 = points[1];
                    x1 = p1.x * (float) pWidth / 100f;
                    y1 = p1.y * (float) pHeight / 100f;
                    x2 = p2.x * (float) pWidth / 100f;
                    y2 = p2.y * (float) pHeight / 100f;
                    float length = (float) Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
                    float orgTop = Math.abs(y1 - length);
                    p.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(x1, orgTop, x2, y2, p);

                    p.setStyle(Paint.Style.FILL);
                    p.setTextSize(textSize);
                    float fcwY = orgTop - 0.2f * textSize;
                    //if (fcwY - textSize < 0) {
                    //    fcwY = y2 + textSize;
                    //}
                    if(fcwCertOutput != null) {
                        float vehicleDistance = fcwCertOutput.getFrontVehicleDistance();
                        float ttc = fcwCertOutput.getFrontVehicleTtc();
                        float relSpeed = fcwCertOutput.getRelativeSpeed();
                        String text = "";
                        if (vehicleDistance >= 0) {
                            text += String.format(Locale.getDefault(), "%.0f m ", vehicleDistance);
                        }
                        if (ttc >= 0) {
                            text += String.format(Locale.getDefault(), "%.1f s ", ttc);
                        }
                        if (relSpeed >= 0) {
                            text += String.format(Locale.getDefault(), "%.1f km/h", relSpeed);
                        }
                        if (text.length() > 0) {
                            try {
                                Rect bounds = new Rect();
                                p.getTextBounds(text, 0, text.length(), bounds);
                                float fcwX = Math.max(0, 0.5f* (pWidth - bounds.width()));
                                canvas.drawText(text, fcwX, fcwY, p);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showTailgatingOutput(Canvas canvas) {
        try {
            if (tgOutput != null && tgOutput.frontVehiclePoints != null &&
                    tgOutput.frontVehiclePoints.length == 2) {
                int pWidth = canvas.getWidth();
                int pHeight = canvas.getHeight();
                PointF p1 = tgOutput.frontVehiclePoints[0];
                PointF p2 = tgOutput.frontVehiclePoints[1];
                float x1 = p1.x * (float) pWidth / 100f;
                float y1 = p1.y * (float) pHeight / 100f;
                float x2 = p2.x * (float) pWidth / 100f;
                float y2 = p2.y * (float) pHeight / 100f;
                float length = (float) Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
                float orgTop = Math.abs(y1 - length);

                int color = (tgEvent.isPresent != 0) ?
                        getResources().getColor(R.color.label_event, null) :
                        getResources().getColor(R.color.label_no_event, null);
                p.setColor(color);
                p.setStrokeWidth(6);
                p.setStyle(Paint.Style.STROKE);

                canvas.drawRect(x1, orgTop, x2, y2, p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTrafficLightColor(int state){
        if(state == IVAdasTrafficLightState.IVTLSGREEN){
            return Color.GREEN;
        }
        else if(state == IVAdasTrafficLightState.IVTLSRED){
            return Color.RED;
        }
        return Color.YELLOW;
    }

    private void drawROI(Canvas canvas, float x, float y, float width, float height){
        float x1 = x * (float) pWidth;
        float y1 = y * (float) pHeight;
        float x2 = x1 + width * (float) pWidth;
        float y2 = y1 + height * (float) pHeight;
        p.setColor(Color.rgb(255, 165, 0));
        p.setStrokeWidth(6);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(x1, y1, x2, y2, p);
        roiWasDrawed = true;
    }

    private void showTrafficLightsOutput(Canvas canvas){
        try {
            if (trafficLightsOutput != null) {
                if(trafficLightsOutput.objects != null) {
                    for (int i = 0; i < trafficLightsOutput.objectsCount; i++) {
                        float x1 = trafficLightsOutput.objects[i].rect.x * (float) pWidth * 0.01f;
                        float y1 = trafficLightsOutput.objects[i].rect.y * (float) pHeight * 0.01f;
                        float x2 = trafficLightsOutput.objects[i].rect.width * (float) pWidth * 0.01f;
                        float y2 = trafficLightsOutput.objects[i].rect.height * (float) pHeight * 0.01f;

                        p.setColor(getTrafficLightColor(trafficLightsOutput.objects[i].state));
                        p.setStrokeWidth(6);
                        p.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(x1, y1, x2, y2, p);
                    }
                }

                if(!roiWasDrawed)
                    drawROI(canvas, trafficLightsOutput.roi.x, trafficLightsOutput.roi.y, trafficLightsOutput.roi.width, trafficLightsOutput.roi.height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStopSignsOutput(Canvas canvas){
        try {
            if (rollingStopOutput != null) {
                if(rollingStopOutput.objects != null) {
                    for (int i = 0; i < rollingStopOutput.objectsCount; i++) {
                        float x1 = rollingStopOutput.objects[i].rect.x * (float) pWidth * 0.01f;
                        float y1 = rollingStopOutput.objects[i].rect.y * (float) pHeight * 0.01f;
                        float x2 = x1 + rollingStopOutput.objects[i].rect.width * (float) pWidth * 0.01f;
                        float y2 = y1 + rollingStopOutput.objects[i].rect.height * (float) pHeight * 0.01f;

                        p.setColor(Color.RED);
                        p.setStrokeWidth(6);
                        p.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(x1, y1, x2, y2, p);
                    }
                }

                if(!roiWasDrawed) {
                    drawROI(canvas, rollingStopOutput.roi.x * 0.01f, rollingStopOutput.roi.y * 0.01f,
                            rollingStopOutput.roi.width * 0.01f, rollingStopOutput.roi.height * 0.01f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFcw2Output(Canvas canvas) {
        try {
            if (fcw2Output != null && fcw2Output.frontVehiclePoints != null &&
                    fcw2Output.frontVehiclePoints.length == 2) {
                int pWidth = canvas.getWidth();
                int pHeight = canvas.getHeight();
                PointF p1 = fcw2Output.frontVehiclePoints[0];
                PointF p2 = fcw2Output.frontVehiclePoints[1];
                float x1 = p1.x * (float) pWidth / 100f;
                float y1 = p1.y * (float) pHeight / 100f;
                float x2 = p2.x * (float) pWidth / 100f;
                float y2 = p2.y * (float) pHeight / 100f;
                float length = (float) Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
                float orgTop = Math.abs(y1 - length);

                int color = (fcw2Event.isPresent != 0) ?
                        getResources().getColor(R.color.label_event, null) :
                        getResources().getColor(R.color.label_no_event, null);
                p.setColor(color);
                p.setStrokeWidth(6);
                p.setStyle(Paint.Style.STROKE);

                canvas.drawRect(x1, orgTop, x2, y2, p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSnGOutput(Canvas canvas) {
        try {
            TextView go = getRootView().findViewById(R.id.tv_go);
            boolean visible = (this.sngEvent != null && this.sngEvent.getIsPresent() != 0);
            go.setVisibility(visible ? VISIBLE : INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSpeedLimitOutput(Canvas canvas) {
        try {
            if (speedLimitOutput != null) {
                if(speedLimitOutput.speedLimitSigns != null) {
                    for (int i = 0; i < speedLimitOutput.speedLimitSigns.length; i++) {
                        float x1 = speedLimitOutput.speedLimitSigns[i].rect.x * (float) pWidth * 0.01f;
                        float y1 = speedLimitOutput.speedLimitSigns[i].rect.y * (float) pHeight * 0.01f;
                        float x2 = x1 + speedLimitOutput.speedLimitSigns[i].rect.width * (float) pWidth * 0.01f;
                        float y2 = y1 + speedLimitOutput.speedLimitSigns[i].rect.height * (float) pHeight * 0.01f;

                        p.setColor(Color.RED);
                        p.setStrokeWidth(6);
                        p.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(x1, y1, x2, y2, p);
                    }
                }
                // Draw ROI
                float x1 = speedLimitOutput.roi.x * pWidth * 0.01f;
                float y1 = speedLimitOutput.roi.y * pHeight * 0.01f;
                float x2 = x1 + speedLimitOutput.roi.width * pWidth * 0.01f;
                float y2 = y1 + speedLimitOutput.roi.height * pHeight * 0.01f;
                p.setColor(Color.rgb(255, 165, 0));
                p.setStrokeWidth(6);
                p.setStyle(Paint.Style.STROKE);
                canvas.drawRect(x1, y1, x2, y2, p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPcwOutput(Canvas canvas) {
        try {
            if (pcwOutput != null) {
                if(pcwOutput.pedestrians != null) {
                    for (int i = 0; i < pcwOutput.pedestrians.length; i++) {
                        float x1 = pcwOutput.pedestrians[i].rect.x * (float) pWidth * 0.01f;
                        float y1 = pcwOutput.pedestrians[i].rect.y * (float) pHeight * 0.01f;
                        float x2 = x1 + pcwOutput.pedestrians[i].rect.width * (float) pWidth * 0.01f;
                        float y2 = y1 + pcwOutput.pedestrians[i].rect.height * (float) pHeight * 0.01f;

                        p.setColor(Color.RED);
                        p.setStrokeWidth(6);
                        p.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(x1, y1, x2, y2, p);
                    }
                }
                // Draw ROI
                float x1 = pcwOutput.roi.x * pWidth * 0.01f;
                float y1 = pcwOutput.roi.y * pHeight * 0.01f;
                float x2 = x1 + pcwOutput.roi.width * pWidth * 0.01f;
                float y2 = y1 + pcwOutput.roi.height * pHeight * 0.01f;
                p.setColor(Color.rgb(255, 165, 0));
                p.setStrokeWidth(6);
                p.setStyle(Paint.Style.STROKE);
                canvas.drawRect(x1, y1, x2, y2, p);
                // Draw Event ROI
                if (pcwOutput.eventRoi != null) {
                    for (int i = 0; i < pcwOutput.eventRoi.length; i++) {
                        int j = (i + 1) % pcwOutput.eventRoi.length;
                        PointF pt1 = pcwOutput.eventRoi[i];
                        PointF pt2 = pcwOutput.eventRoi[j];
                        if (!pt1.equals(pt2)) {
                            x1 = pt1.x * pWidth / 100.f;
                            y1 = pt1.y * pHeight / 100.f;
                            x2 = pt2.x * pWidth / 100.f;
                            y2 = pt2.y * pHeight / 100.f;
                            canvas.drawLine(x1, y1, x2, y2, p);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFcw2Label(Canvas canvas, boolean showOutput) {
        try {
            TextView tv = getRootView().findViewById(R.id.tv_fcw);
            boolean visible = (showOutput && this.fcw2Event != null && this.fcw2Event.isPresent != 0);
            tv.setVisibility(visible ? VISIBLE : INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showTailgatingLabel(Canvas canvas, boolean showOutput) {
        try {
            TextView tv = getRootView().findViewById(R.id.tv_tg);
            boolean visible = (showOutput && this.tgEvent != null && this.tgEvent.isPresent != 0);
            tv.setVisibility(visible ? VISIBLE : INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRollingStopLabel(Canvas canvas, boolean showOutput) {
        try {
            TextView tv = getRootView().findViewById(R.id.tv_rs);
            boolean visible = (showOutput && this.rollingStopOutputEvent != null &&
                    this.rollingStopOutputEvent.isPresent != 0);
            tv.setVisibility(visible ? VISIBLE : INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showViolationLabel(Canvas canvas, boolean showOutput) {
        try {
            TextView tv = getRootView().findViewById(R.id.tv_slv);
            boolean visible = (showOutput && speedLimitEvent != null &&
                    speedLimitEvent.isPresent != 0);
            if (visible) {
                String slvText = "V";
                if (speedLimitEvent.speedLimitMph > 0) {
                    slvText += Integer.toString(speedLimitEvent.speedLimitMph);
                }
                tv.setText(slvText);
            }
            tv.setVisibility(visible ? VISIBLE : INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPcwLabel(Canvas canvas, boolean showOutput) {
        try {
            TextView tv = getRootView().findViewById(R.id.tv_pcw);
            boolean visible = (showOutput && pcwEvent != null &&
                    pcwEvent.isPresent != 0);
            tv.setVisibility(visible ? VISIBLE : INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFcw2VehicleInfo(Canvas canvas, boolean showOutput) {
        try {
            String text = "";
            if (showOutput && fcw2Output != null && fcw2Output.frontVehiclePoints != null &&
                fcw2Output.frontVehiclePoints.length == 2) {
                float vehicleDistance = fcw2Output.frontVehicleDistance;
                float ttc = fcw2Output.frontVehicleHeadway > 0 ? fcw2Output.frontVehicleHeadway :
                        fcw2Output.frontVehicleTtc;
                float distanceDecrease = fcw2Output.distanceDecrease;
                if (vehicleDistance >= 0) {
                    text += String.format(Locale.getDefault(), "%.0f", vehicleDistance);
                }
                if (ttc >= 0) {
                    text += String.format(Locale.getDefault(), ", %.1f", ttc);
                }
                if (distanceDecrease >= 0) {
                    text += String.format(Locale.getDefault(), ", %.1f", distanceDecrease);
                }
            }

            boolean isTextVisible = text.length() > 0;
            TextView vehicleLabel = getRootView().findViewById(R.id.tv_vehicle);
            vehicleLabel.setVisibility(isTextVisible ? VISIBLE : INVISIBLE);
            if (isTextVisible) {
                int textSize = (int)(getResources().getDimensionPixelSize(R.dimen.medium_label_text_size) *
                        Settings.getFloat(Settings.F_FONT_SCALE_PERC) / 100);
                vehicleLabel.setTextSize(COMPLEX_UNIT_PX, textSize);
                vehicleLabel.setText(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawHorizonValues(Canvas canvas) {
        try {
            p.setColor(Color.YELLOW);
            p.setStrokeWidth(10);
            pWidth = canvas.getWidth();
            pHeight = canvas.getHeight();
            float levelY = (hoodLevel * 3.6f) * ((float) pHeight / 360f);
            float levelX = (640 * 0.1f) * ((float) pWidth / 640f);
            canvas.drawLine(0, levelY, levelX, levelY, p);

            float horizonY = (horizonLevel * 3.6f) * ((float) pHeight / 360f);
            float horizonX = (640 * 0.1f) * ((float) pWidth / 640f);
            canvas.drawLine(0, horizonY, horizonX, horizonY, p);

            float panX = (horizonPan * 6.4f) * ((float) pWidth / 640f);
            float panY = (360 * 0.1f) * ((float) pHeight / 360f);
            canvas.drawLine(panX, 0, panX, panY, p);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}