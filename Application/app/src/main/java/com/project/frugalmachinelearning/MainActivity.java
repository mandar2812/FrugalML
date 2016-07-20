package com.project.frugalmachinelearning;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.project.frugalmachinelearning.classifiers.ActivityType;
import com.project.frugalmachinelearning.classifiers.ActivityWindow;
import com.project.frugalmachinelearning.classifiers.FactoryClassifiers;
import com.project.frugalmachinelearning.external.MathStuff;
import com.project.frugalmachinelearning.tools.ApplicationStates;
import com.project.frugalmachinelearning.tools.FileOperations;
import com.project.frugalmachinelearning.tools.FloatingActionButtonFlexibleActions;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.AveragedNDependenceEstimators.A1DE;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int AMOUNT_OF_ATTRIBUTES = 23;

    private static final int UPDATES_PER_SECOND = 16;

    private static final int FRAME_SIZE = UPDATES_PER_SECOND * 2;

    private SensorManager mSensorManager;

    private float accelX;
    private float accelY;
    private float accelZ;

    private float gyroX;
    private float gyroY;
    private float gyroZ;

    private float gravityX;
    private float gravityY;
    private float gravityZ;

    private float linAccelX;
    private float linAccelY;
    private float linAccelZ;

    private float rotVecX;
    private float rotVecY;
    private float rotVecZ;
    private float rotVecS;

    private float stDetVal;

    private float aiPreVal;

    private float magFielX;
    private float magFielY;
    private float magFielZ;

    private float heartRateVal;


    private Thread t;
    private Thread tClassifyActivity;

    private boolean needTitle = true;

    private AbstractClassifier selectedClassifier;

    private DenseInstance[] instances = new DenseInstance[FRAME_SIZE];
    private int posInstance;
    private boolean warmingUp;
    private int performingActivity;

    private TextView mTheoreticalActivity;
    private TextView mGenericActivity;

    private ImageButton bPause;

    private ImageButton bStop;

    private RelativeLayout fBackground;

    private PrintWriter pw;
    private DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS dd/MM/yyyy");

    private boolean firstRun = true;

    private long timeChangeActivityUpdateMs;

    private float[] accelerometerFilter = new float[3];

    private double[] accelXArray = new double[FRAME_SIZE];
    private double[] accelYArray = new double[FRAME_SIZE];
    private double[] accelZArray = new double[FRAME_SIZE];
    private double[] accelXMin = new double[FRAME_SIZE];
    private double[] accelYMin = new double[FRAME_SIZE];
    private double[] accelZMin = new double[FRAME_SIZE];
    private double[] accelXMax = new double[FRAME_SIZE];
    private double[] accelYMax = new double[FRAME_SIZE];
    private double[] accelZMax = new double[FRAME_SIZE];
    private double[] accelXMean = new double[FRAME_SIZE];
    private double[] accelYMean = new double[FRAME_SIZE];
    private double[] accelZMean = new double[FRAME_SIZE];
    private double[] accelXStd = new double[FRAME_SIZE];
    private double[] accelYStd = new double[FRAME_SIZE];
    private double[] accelZStd = new double[FRAME_SIZE];

    private double[] gyroXArray = new double[FRAME_SIZE];
    private double[] gyroYArray = new double[FRAME_SIZE];
    private double[] gyroZArray = new double[FRAME_SIZE];
    private double[] gyroXMin = new double[FRAME_SIZE];
    private double[] gyroYMin = new double[FRAME_SIZE];
    private double[] gyroZMin = new double[FRAME_SIZE];
    private double[] gyroXMax = new double[FRAME_SIZE];
    private double[] gyroYMax = new double[FRAME_SIZE];
    private double[] gyroZMax = new double[FRAME_SIZE];
    private double[] gyroXMean = new double[FRAME_SIZE];
    private double[] gyroYMean = new double[FRAME_SIZE];
    private double[] gyroZMean = new double[FRAME_SIZE];
    private double[] gyroXStd = new double[FRAME_SIZE];
    private double[] gyroYStd = new double[FRAME_SIZE];
    private double[] gyroZStd = new double[FRAME_SIZE];

    private double[] gravityXArray = new double[FRAME_SIZE];
    private double[] gravityYArray = new double[FRAME_SIZE];
    private double[] gravityZArray = new double[FRAME_SIZE];
    private double[] gravityXMin = new double[FRAME_SIZE];
    private double[] gravityYMin = new double[FRAME_SIZE];
    private double[] gravityZMin = new double[FRAME_SIZE];
    private double[] gravityXMax = new double[FRAME_SIZE];
    private double[] gravityYMax = new double[FRAME_SIZE];
    private double[] gravityZMax = new double[FRAME_SIZE];
    private double[] gravityXMean = new double[FRAME_SIZE];
    private double[] gravityYMean = new double[FRAME_SIZE];
    private double[] gravityZMean = new double[FRAME_SIZE];
    private double[] gravityXStd = new double[FRAME_SIZE];
    private double[] gravityYStd = new double[FRAME_SIZE];
    private double[] gravityZStd = new double[FRAME_SIZE];

    private double[] linAccelXArray = new double[FRAME_SIZE];
    private double[] linAccelYArray = new double[FRAME_SIZE];
    private double[] linAccelZArray = new double[FRAME_SIZE];
    private double[] linAccelXMin = new double[FRAME_SIZE];
    private double[] linAccelYMin = new double[FRAME_SIZE];
    private double[] linAccelZMin = new double[FRAME_SIZE];
    private double[] linAccelXMax = new double[FRAME_SIZE];
    private double[] linAccelYMax = new double[FRAME_SIZE];
    private double[] linAccelZMax = new double[FRAME_SIZE];
    private double[] linAccelXMean = new double[FRAME_SIZE];
    private double[] linAccelYMean = new double[FRAME_SIZE];
    private double[] linAccelZMean = new double[FRAME_SIZE];
    private double[] linAccelXStd = new double[FRAME_SIZE];
    private double[] linAccelYStd = new double[FRAME_SIZE];
    private double[] linAccelZStd = new double[FRAME_SIZE];

    private double[] rotVecXArray = new double[FRAME_SIZE];
    private double[] rotVecYArray = new double[FRAME_SIZE];
    private double[] rotVecZArray = new double[FRAME_SIZE];
    private double[] rotVecSArray = new double[FRAME_SIZE];
    private double[] rotVecXMin = new double[FRAME_SIZE];
    private double[] rotVecYMin = new double[FRAME_SIZE];
    private double[] rotVecZMin = new double[FRAME_SIZE];
    private double[] rotVecSMin = new double[FRAME_SIZE];
    private double[] rotVecXMax = new double[FRAME_SIZE];
    private double[] rotVecYMax = new double[FRAME_SIZE];
    private double[] rotVecZMax = new double[FRAME_SIZE];
    private double[] rotVecSMax = new double[FRAME_SIZE];
    private double[] rotVecXMean = new double[FRAME_SIZE];
    private double[] rotVecYMean = new double[FRAME_SIZE];
    private double[] rotVecZMean = new double[FRAME_SIZE];
    private double[] rotVecSMean = new double[FRAME_SIZE];
    private double[] rotVecXStd = new double[FRAME_SIZE];
    private double[] rotVecYStd = new double[FRAME_SIZE];
    private double[] rotVecZStd = new double[FRAME_SIZE];
    private double[] rotVecSStd = new double[FRAME_SIZE];

    private double[] stDetValArray = new double[FRAME_SIZE];

    private double[] aiPreValArray = new double[FRAME_SIZE];
    private double[] aiPreValMin = new double[FRAME_SIZE];
    private double[] aiPreValMax = new double[FRAME_SIZE];
    private double[] aiPreValMean = new double[FRAME_SIZE];
    private double[] aiPreValStd = new double[FRAME_SIZE];

    private double[] magFielXArray = new double[FRAME_SIZE];
    private double[] magFielYArray = new double[FRAME_SIZE];
    private double[] magFielZArray = new double[FRAME_SIZE];
    private double[] magFielXMin = new double[FRAME_SIZE];
    private double[] magFielYMin = new double[FRAME_SIZE];
    private double[] magFielZMin = new double[FRAME_SIZE];
    private double[] magFielXMax = new double[FRAME_SIZE];
    private double[] magFielYMax = new double[FRAME_SIZE];
    private double[] magFielZMax = new double[FRAME_SIZE];
    private double[] magFielXMean = new double[FRAME_SIZE];
    private double[] magFielYMean = new double[FRAME_SIZE];
    private double[] magFielZMean = new double[FRAME_SIZE];
    private double[] magFielXStd = new double[FRAME_SIZE];
    private double[] magFielYStd = new double[FRAME_SIZE];
    private double[] magFielZStd = new double[FRAME_SIZE];

    private double[] heartRateValArray = new double[FRAME_SIZE];
    private double[] heartRateValMin = new double[FRAME_SIZE];
    private double[] heartRateValMax = new double[FRAME_SIZE];
    private double[] heartRateValMean = new double[FRAME_SIZE];
    private double[] heartRateValStd = new double[FRAME_SIZE];

    private long previousBatteryUpdate;

    StandardDeviation stDev = new StandardDeviation();
    Mean mean = new Mean();

    private StringBuilder stringOfSensors;

    private MathStuff msf;

    private long pTime;
    private long nTime;

    private FloatingActionButtonFlexibleActions leftCenterButton;

    private static int appState;
    boolean computeComplexFeatures = false;


    /**
     * Custom 'what' for Message sent to Handler.
     */
    private static final int MSG_UPDATE_SCREEN = 0;

    /**
     * Milliseconds between updates based on state.
     */
    private static final long ACTIVE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long AMBIENT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(900);

    /**
     * Tracks latest ambient details, such as burnin offsets, etc.
     */
    private Bundle mAmbientDetails;

    private final Handler mActiveModeUpdateHandler = new UpdateHandler(this);

    private volatile int mDrawCount = 0;

    private boolean discretizeData = false;

    private boolean initDiscretizeItem = true;

    Discretize discretizeItems;

    private AlarmManager mAmbientStateAlarmManager;
    private PendingIntent mAmbientStatePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTheoreticalActivity = (TextView) stub.findViewById(R.id.mTheoreticalActivity);
                mGenericActivity = (TextView) stub.findViewById(R.id.mGenericActivity);

                bPause = (ImageButton) stub.findViewById(R.id.pauseActivities);

                bStop = (ImageButton) stub.findViewById(R.id.finishActivities);

                fBackground = (RelativeLayout) stub.findViewById(R.id.mBackRelativeLayout);

                refreshDisplayAndSetNextUpdate();
            }
        });

        // activate constant visibility for the activity
        setAmbientEnabled();

        // screen on for the whole run of the application
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mAmbientStateAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent ambientStateIntent = new Intent(getApplicationContext(), MainActivity.class);

        mAmbientStatePendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0 /* requestCode */,
                ambientStateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Log.i(TAG, "onCreate()");
    }

    private void launchCollectingInformation() {
        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000 / UPDATES_PER_SECOND);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (isExternalStorageWritable()) {

                                        if (needTitle && appState == 0) {
                                            pw.println(createTitle());
                                            needTitle = false;
                                        }

                                        // computing features
                                        sensorsAndComplexFeaturesToArrays(posInstance);

                                        if (appState == 0) {
                                            if (computeComplexFeatures && performingActivity != 6) {

                                                // pause data collection after activity change for a short period and wait for computed values
                                                long currentTimeMs = System.currentTimeMillis();
                                                if (currentTimeMs - timeChangeActivityUpdateMs >= 5000) {
                                                    StringBuilder newInfo = arraysToString(posInstance);
                                                    pw.println(newInfo);
                                                }
                                            }
                                        } else {
                                            DenseInstance instance = getDenseInstances(AMOUNT_OF_ATTRIBUTES * 5 - 8, posInstance);
                                            instances[posInstance] = instance;
                                        }

                                        posInstance++;
                                        if (posInstance == 2 * UPDATES_PER_SECOND) {
                                            posInstance = 0;
                                            computeComplexFeatures = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.i(TAG, e.toString());
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {

                }
            }
        };

        t.start();
    }

    private void launchingRecognitionActivities() {
        tClassifyActivity = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                ArrayList<Attribute> attributes = getNewAttributes();
                                Instances data = ActivityWindow.constructInstances(attributes, instances);

                                Instances discretData = null;

                                if (discretizeData && data.numInstances() == 32 && discretizeItems != null) {

                                    try {
                                        discretData = Filter.useFilter(data, discretizeItems);
                                    } catch (Exception e) {
                                        Log.i(TAG, e.toString());
                                    }
                                }

                                String activityFullName = "BEING COMPUTED";

                                if (discretData != null) {
                                    activityFullName = ActivityWindow.getActivityName(selectedClassifier, discretData);
                                } else  {
                                    if (!discretizeData) {
                                        activityFullName = ActivityWindow.getActivityName(selectedClassifier, data);
                                    }
                                }

                                long currentTimeMs = System.currentTimeMillis();
                                if (currentTimeMs - previousBatteryUpdate > 29555) {
                                    StringBuilder bInfo = new StringBuilder();
                                    bInfo.append(currentTimeMs).append(",");
                                    bInfo.append(selectedClassifier.getClass()).append(",");
                                    bInfo.append(getBatteryLevel());
                                    String bInfoString = bInfo.toString();
                                    pw.println(bInfoString);
                                    pw.flush();

                                    bInfo = null;
                                    previousBatteryUpdate = currentTimeMs;

                                    Log.i(TAG, bInfoString);

                                }

                                mTheoreticalActivity.setText("Activity is " + activityFullName);

                                Log.i(TAG, activityFullName);
                            }

                        });
                    }
                } catch (InterruptedException e) {
                    Log.i(TAG, e.toString());
                }
            }
        };

        tClassifyActivity.start();
    }

    @Override
    protected void onDestroy() {

        if (t != null) {
            t.interrupt();

        }
        if (tClassifyActivity != null) {
            tClassifyActivity.interrupt();
        }

        pw.close();

        mSensorManager = null;

        mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
        mAmbientStateAlarmManager.cancel(mAmbientStatePendingIntent);

        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        Log.i(TAG, "onAccuracyChanged()");
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);

        /**
         * In this sample, we aren't using the ambient details bundle (EXTRA_BURN_IN_PROTECTION or
         * EXTRA_LOWBIT_AMBIENT), but if you need them, you can pull them from the local variable
         * set here.
         */
        mAmbientDetails = ambientDetails;

        /** Clears Handler queue (only needed for updates in active mode). */
        mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);

        refreshDisplayAndSetNextUpdate();
        Log.d(TAG, "onEnterAmbient()");

        onExitAmbient();

    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();

        mAmbientStateAlarmManager.cancel(mAmbientStatePendingIntent);
        Log.d(TAG, "onExitAmbient()");

        refreshDisplayAndSetNextUpdate();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();

        refreshDisplayAndSetNextUpdate();
    }

    /**
     * Updates display based on Ambient state. If you need to pull data, you should do it here.
     */
    private void loadDataAndUpdateScreen() {

        mDrawCount += 1;
        long currentTimeMs = System.currentTimeMillis();
        Log.d(TAG, "loadDataAndUpdateScreen(): " + currentTimeMs + "(" + isAmbient() + ")");

        Date current = new Date();

        msf = new MathStuff();

        // mTime.setText(new SimpleDateFormat("HH:mm:ss.SSS").format(current) + " update time");

        if (firstRun) {
            try {
                timeChangeActivityUpdateMs = System.currentTimeMillis();

                performingActivity = 6;

                /*    Random random = new Random();
                int fileNumber = random.nextInt(100);   */

                Intent intent = getIntent();
                String stateFromIntent = intent.getStringExtra("APP STATE");
                appState = ApplicationStates.valueOf(stateFromIntent).ordinal();

/*                String hexColor = "#" + intent.getStringExtra("background");
                fBackground.setBackgroundColor(Color.parseColor(hexColor)); */

                SimpleDateFormat shortName = new SimpleDateFormat("dd,HHmmss");
                String fileNumber = shortName.format(new Date());

                // change an interface and prepare tools for the assignment
                if (appState == 1) {

                    final String batteryLevelName = FileOperations.getSensorStorageDir("SensorsInformation") + "/batteryInfo" + fileNumber + ".txt";
                    FileOperations.deleteFile(batteryLevelName);
                    final File batteryData = new File(batteryLevelName);

                    previousBatteryUpdate = System.currentTimeMillis();

                    pw = new PrintWriter(new BufferedWriter(new FileWriter(batteryData, true)));

                    mGenericActivity.setVisibility(View.INVISIBLE);

                    bPause.setVisibility(View.INVISIBLE);

                    Log.i(TAG, batteryLevelName);

                } else {

                    createCircleMenu();

                    final String sensorDataName = FileOperations.getSensorStorageDir("SensorsInformation") + "/measurements" + fileNumber + ".txt";
                    FileOperations.deleteFile(sensorDataName);
                    final File sensorData = new File(sensorDataName);

                    pw = new PrintWriter(new BufferedWriter(new FileWriter(sensorData, true)));

                    stringOfSensors = new StringBuilder();

                    mTheoreticalActivity.setVisibility(View.INVISIBLE);
                    mGenericActivity.setVisibility(View.INVISIBLE);

                    bPause.setVisibility(View.INVISIBLE);

                    Log.i(TAG, sensorDataName);

                }

            } catch (IOException e) {
                Log.i(TAG, e.toString());
            }

            // clean old files with random results
            FileOperations.deleteFile("/storage/emulated/0/myfile_nbp.txt");
            FileOperations.deleteFile("myfile_nbp.txt");

            // create classifier from a file
            String selectedClassifierName = "A1DE";
            if (selectedClassifierName.equals("A1DE")) {
                discretizeData = true;
                initFilter();
            }
            FactoryClassifiers fc = new FactoryClassifiers();
            String modelFileName = fc.getModelFile(selectedClassifierName);
            InputStream ins = getResources().openRawResource(getResources().getIdentifier(modelFileName, "raw", getPackageName()));

            selectedClassifier = fc.getModel(selectedClassifierName, ins);

            posInstance = 0;
            warmingUp = true;
            setSensors();

            if (appState == 0) {
                launchCollectingInformation();
            } else {
                launchCollectingInformation();
                launchingRecognitionActivities();
            }

            firstRun = false;

            Log.i(TAG, "First run is still active");
        }

        if (isAmbient()) {

        } else {

        }
    }

    private void initFilter() {
        discretizeItems = new Discretize();

        String[] options = new String[6];
        options[0] = "-B";
        options[1] = "2";
        options[2] = "-M";
        options[3] = "-1.0";
        options[4] = "-R";
        options[5] = "first-106";

        try {

//            InputStream ins = getResources().openRawResource(getResources().getIdentifier("margins", "raw", getPackageName()));

            InputStream ins = getResources().openRawResource(getResources().getIdentifier("extended", "raw", getPackageName()));

            BufferedReader br = new BufferedReader(new InputStreamReader(ins, "UTF-8"));

            Instances data = new Instances(br);
            data.setClassIndex(data.numAttributes() - 1);

            discretizeItems.setOptions(options);
            discretizeItems.setInputFormat(data);
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        Log.d(TAG, "onNewIntent(): " + intent);

        // Described in the following section
        refreshDisplayAndSetNextUpdate();
    }

    private void refreshDisplayAndSetNextUpdate() {

        loadDataAndUpdateScreen();
        long timeMs = System.currentTimeMillis();

        // this condition is true when called from ambient mode and can be used with timer value
        if (isAmbient()) {
            long delayMs = AMBIENT_INTERVAL_MS - (timeMs % AMBIENT_INTERVAL_MS);
            long triggerTimeMs = timeMs + delayMs;

            mAmbientStateAlarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    mAmbientStatePendingIntent);

        } else {
            long delayMs = ACTIVE_INTERVAL_MS - (timeMs % ACTIVE_INTERVAL_MS);

            mActiveModeUpdateHandler.removeMessages(MSG_UPDATE_SCREEN);
            mActiveModeUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SCREEN, delayMs);
        }

    }

    private void setSensors() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> mList = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);

        // print the amount of sensors and short information
        Log.i(TAG, String.valueOf(mList.size()));
        for (int i = 0; i < mList.size(); i++) {
            Log.i(TAG, "\t" + i + " " + mList.get(i).getName());
            Log.i(TAG, "\t" + i + " " + mList.get(i).toString());
        }

    }

    private void sensorsAndComplexFeaturesToArrays(int pos) {
        accelXArray[pos] = accelX;
        accelYArray[pos] = accelY;
        accelZArray[pos] = accelZ;
        if (computeComplexFeatures) {
            accelXMin[pos] = msf.getMin(accelXArray);
            accelYMin[pos] = msf.getMin(accelYArray);
            accelZMin[pos] = msf.getMin(accelZArray);
            accelXMax[pos] = msf.getMax(accelXArray);
            accelYMax[pos] = msf.getMax(accelYArray);
            accelZMax[pos] = msf.getMax(accelZArray);
            accelXMean[pos] = msf.getMean(accelXArray);
            accelYMean[pos] = msf.getMean(accelYArray);
            accelZMean[pos] = msf.getMean(accelZArray);
            accelXStd[pos] = msf.getStdDev(accelXArray);
            accelYStd[pos] = msf.getStdDev(accelYArray);
            accelZStd[pos] = msf.getStdDev(accelZArray);
        }

        gyroXArray[pos] = gyroX;
        gyroYArray[pos] = gyroY;
        gyroZArray[pos] = gyroZ;
        if (computeComplexFeatures) {
            gyroXMin[pos] = msf.getMin(gyroXArray);
            gyroYMin[pos] = msf.getMin(gyroYArray);
            gyroZMin[pos] = msf.getMin(gyroZArray);
            gyroXMax[pos] = msf.getMax(gyroXArray);
            gyroYMax[pos] = msf.getMax(gyroYArray);
            gyroZMax[pos] = msf.getMax(gyroZArray);
            gyroXMean[pos] = msf.getMean(gyroXArray);
            gyroYMean[pos] = msf.getMean(gyroYArray);
            gyroZMean[pos] = msf.getMean(gyroZArray);
            gyroXStd[pos] = msf.getStdDev(gyroXArray);
            gyroYStd[pos] = msf.getStdDev(gyroYArray);
            gyroZStd[pos] = msf.getStdDev(gyroZArray);
        }

        gravityXArray[pos] = gravityX;
        gravityYArray[pos] = gravityY;
        gravityZArray[pos] = gravityZ;
        if (computeComplexFeatures) {
            gravityXMin[pos] = msf.getMin(gravityXArray);
            gravityYMin[pos] = msf.getMin(gravityYArray);
            gravityZMin[pos] = msf.getMin(gravityZArray);
            gravityXMax[pos] = msf.getMax(gravityXArray);
            gravityYMax[pos] = msf.getMax(gravityYArray);
            gravityZMax[pos] = msf.getMax(gravityZArray);
            gravityXMean[pos] = msf.getMean(gravityXArray);
            gravityYMean[pos] = msf.getMean(gravityYArray);
            gravityZMean[pos] = msf.getMean(gravityZArray);
            gravityXStd[pos] = msf.getStdDev(gravityXArray);
            gravityYStd[pos] = msf.getStdDev(gravityYArray);
            gravityZStd[pos] = msf.getStdDev(gravityZArray);
        }

        linAccelXArray[pos] = linAccelX;
        linAccelYArray[pos] = linAccelY;
        linAccelZArray[pos] = linAccelZ;
        if (computeComplexFeatures) {
            linAccelXMin[pos] = msf.getMin(linAccelXArray);
            linAccelYMin[pos] = msf.getMin(linAccelYArray);
            linAccelZMin[pos] = msf.getMin(linAccelZArray);
            linAccelXMax[pos] = msf.getMax(linAccelXArray);
            linAccelYMax[pos] = msf.getMax(linAccelYArray);
            linAccelZMax[pos] = msf.getMax(linAccelZArray);
            linAccelXMean[pos] = msf.getMean(linAccelXArray);
            linAccelYMean[pos] = msf.getMean(linAccelYArray);
            linAccelZMean[pos] = msf.getMean(linAccelZArray);
            linAccelXStd[pos] = msf.getStdDev(linAccelXArray);
            linAccelYStd[pos] = msf.getStdDev(linAccelYArray);
            linAccelZStd[pos] = msf.getStdDev(linAccelZArray);
        }

        rotVecXArray[pos] = rotVecX;
        rotVecYArray[pos] = rotVecY;
        rotVecZArray[pos] = rotVecZ;
        rotVecSArray[pos] = rotVecS;
        if (computeComplexFeatures) {
            rotVecXMin[pos] = msf.getMin(rotVecXArray);
            rotVecYMin[pos] = msf.getMin(rotVecYArray);
            rotVecZMin[pos] = msf.getMin(rotVecZArray);
            rotVecSMin[pos] = msf.getMin(rotVecSArray);
            rotVecXMax[pos] = msf.getMax(rotVecXArray);
            rotVecYMax[pos] = msf.getMax(rotVecYArray);
            rotVecZMax[pos] = msf.getMax(rotVecZArray);
            rotVecSMax[pos] = msf.getMax(rotVecSArray);
            rotVecXMean[pos] = msf.getMean(rotVecXArray);
            rotVecYMean[pos] = msf.getMean(rotVecYArray);
            rotVecZMean[pos] = msf.getMean(rotVecZArray);
            rotVecSMean[pos] = msf.getMean(rotVecSArray);
            rotVecXStd[pos] = msf.getStdDev(rotVecXArray);
            rotVecYStd[pos] = msf.getStdDev(rotVecYArray);
            rotVecZStd[pos] = msf.getStdDev(rotVecZArray);
            rotVecSStd[pos] = msf.getStdDev(rotVecSArray);
        }

        stDetValArray[pos] = stDetVal;

        aiPreValArray[pos] = aiPreVal;
        if (computeComplexFeatures) {
            aiPreValMin[pos] = msf.getMin(aiPreValArray);
            aiPreValMax[pos] = msf.getMax(aiPreValArray);
            aiPreValMean[pos] = msf.getMean(aiPreValArray);
            aiPreValStd[pos] = msf.getStdDev(aiPreValArray);
        }

        magFielXArray[pos] = magFielX;
        magFielYArray[pos] = magFielY;
        magFielZArray[pos] = magFielZ;
        if (computeComplexFeatures) {
            magFielXMin[pos] = msf.getMin(magFielXArray);
            magFielYMin[pos] = msf.getMin(magFielYArray);
            magFielZMin[pos] = msf.getMin(magFielZArray);
            magFielXMax[pos] = msf.getMax(magFielXArray);
            magFielYMax[pos] = msf.getMax(magFielYArray);
            magFielZMax[pos] = msf.getMax(magFielZArray);
            magFielXMean[pos] = msf.getMean(magFielXArray);
            magFielYMean[pos] = msf.getMean(magFielYArray);
            magFielZMean[pos] = msf.getMean(magFielZArray);
            magFielXStd[pos] = msf.getStdDev(magFielXArray);
            magFielYStd[pos] = msf.getStdDev(magFielYArray);
            magFielZStd[pos] = msf.getStdDev(magFielZArray);
        }

        heartRateValArray[pos] = heartRateVal;
        if (computeComplexFeatures) {
            heartRateValMin[pos] = msf.getMin(heartRateValArray);
            heartRateValMax[pos] = msf.getMax(heartRateValArray);
            heartRateValMean[pos] = msf.getMean(heartRateValArray);
            heartRateValStd[pos] = msf.getStdDev(heartRateValArray);
        }

    }

    private StringBuilder arraysToString(int pos) {
        StringBuilder allSensorsData = stringOfSensors;
        long currentTime = System.currentTimeMillis();

        if (pTime == 0) {
            pTime = currentTime;
        } else {
            pTime = nTime;
            nTime = currentTime;

            long difference = nTime - pTime;

            if (difference > 100) {
                Log.i(TAG, String.valueOf(nTime - pTime));
            }
        }

        if (allSensorsData != null) {
            allSensorsData.setLength(0);
        } else {
            allSensorsData = new StringBuilder();
        }

        allSensorsData.append(currentTime).append(",")
                .append(accelXArray[pos]).append(",").append(accelYArray[pos]).append(",").append(accelZArray[pos]).append(",")
                .append(accelXMin[pos]).append(",").append(accelYMin[pos]).append(",").append(accelZMin[pos]).append(",")
                .append(accelXMax[pos]).append(",").append(accelYMax[pos]).append(",").append(accelZMax[pos]).append(",")
                .append(accelXMean[pos]).append(",").append(accelYMean[pos]).append(",").append(accelZMean[pos]).append(",")
                .append(accelXStd[pos]).append(",").append(accelYStd[pos]).append(",").append(accelZStd[pos]).append(",");

        allSensorsData.append(gyroXArray[pos]).append(",").append(gyroYArray[pos]).append(",").append(gyroZArray[pos]).append(",")
                .append(gyroXMin[pos]).append(",").append(gyroYMin[pos]).append(",").append(gyroZMin[pos]).append(",")
                .append(gyroXMax[pos]).append(",").append(gyroYMax[pos]).append(",").append(gyroZMax[pos]).append(",")
                .append(gyroXMean[pos]).append(",").append(gyroYMean[pos]).append(",").append(gyroZMean[pos]).append(",")
                .append(gyroXStd[pos]).append(",").append(gyroYStd[pos]).append(",").append(gyroZStd[pos]).append(",");

        allSensorsData.append(gravityXArray[pos]).append(",").append(gravityYArray[pos]).append(",").append(gravityZArray[pos]).append(",")
                .append(gravityXMin[pos]).append(",").append(gravityYMin[pos]).append(",").append(gravityZMin[pos]).append(",")
                .append(gravityXMax[pos]).append(",").append(gravityYMax[pos]).append(",").append(gravityZMax[pos]).append(",")
                .append(gravityXMean[pos]).append(",").append(gravityYMean[pos]).append(",").append(gravityZMean[pos]).append(",")
                .append(gravityXStd[pos]).append(",").append(gravityYStd[pos]).append(",").append(gravityZStd[pos]).append(",");

        allSensorsData.append(linAccelXArray[pos]).append(",").append(linAccelYArray[pos]).append(",").append(linAccelZArray[pos]).append(",")
                .append(linAccelXMin[pos]).append(",").append(linAccelYMin[pos]).append(",").append(linAccelZMin[pos]).append(",")
                .append(linAccelXMax[pos]).append(",").append(linAccelYMax[pos]).append(",").append(linAccelZMax[pos]).append(",")
                .append(linAccelXMean[pos]).append(",").append(linAccelYMean[pos]).append(",").append(linAccelZMean[pos]).append(",")
                .append(linAccelXStd[pos]).append(",").append(linAccelYStd[pos]).append(",").append(linAccelZStd[pos]).append(",");

        allSensorsData.append(rotVecXArray[pos]).append(",").append(rotVecYArray[pos]).append(",")
                .append(rotVecZArray[pos]).append(",").append(rotVecSArray[pos]).append(",")
                .append(rotVecXMin[pos]).append(",").append(rotVecYMin[pos]).append(",")
                .append(rotVecZMin[pos]).append(",").append(rotVecSMin[pos]).append(",")
                .append(rotVecXMax[pos]).append(",").append(rotVecYMax[pos]).append(",")
                .append(rotVecZMax[pos]).append(",").append(rotVecSMax[pos]).append(",")
                .append(rotVecXMean[pos]).append(",").append(rotVecYMean[pos]).append(",")
                .append(rotVecZMean[pos]).append(",").append(rotVecSMean[pos]).append(",")
                .append(rotVecXStd[pos]).append(",").append(rotVecYStd[pos]).append(",")
                .append(rotVecZStd[pos]).append(",").append(rotVecSStd[pos]).append(",");

        allSensorsData.append(stDetValArray[pos]).append(",");

        allSensorsData.append(aiPreValArray[pos]).append(",").append(aiPreValMin[pos]).append(",").append(aiPreValMax[pos]).append(",")
                .append(aiPreValMean[pos]).append(",").append(aiPreValStd[pos]).append(",") ;

        allSensorsData.append(magFielXArray[pos]).append(",").append(magFielYArray[pos]).append(",").append(magFielZArray[pos]).append(",")
                .append(magFielXMin[pos]).append(",").append(magFielYMin[pos]).append(",").append(magFielZMin[pos]).append(",")
                .append(magFielXMax[pos]).append(",").append(magFielYMax[pos]).append(",").append(magFielZMax[pos]).append(",")
                .append(magFielXMean[pos]).append(",").append(magFielYMean[pos]).append(",").append(magFielZMean[pos]).append(",")
                .append(magFielXStd[pos]).append(",").append(magFielYStd[pos]).append(",").append(magFielZStd[pos]).append(",");

        allSensorsData.append(heartRateValArray[pos]).append(",").append(heartRateValMin[pos]).append(",").append(heartRateValMax[pos]).append(",")
                .append(heartRateValMean[pos]).append(",").append(heartRateValStd[pos]).append(",");

        allSensorsData.append(performingActivity);

        return allSensorsData;
    }

    private String createTitle() {
        StringBuilder title = new StringBuilder();
        title.append("Timestamp ,");

        title.append("AccelX, ");
        title.append("AccelY, ");
        title.append("AccelZ, ");
        title.append("AccelXMin, ");
        title.append("AccelYMin, ");
        title.append("AccelZMin, ");
        title.append("AccelXMax, ");
        title.append("AccelYMax, ");
        title.append("AccelZMax, ");
        title.append("AccelXMean, ");
        title.append("AccelYMean, ");
        title.append("AccelZMean, ");
        title.append("AccelXStd, ");
        title.append("AccelYStd, ");
        title.append("AccelZStd, ");

        title.append("GyroX, ");
        title.append("GyroY, ");
        title.append("GyroZ, ");
        title.append("GyroXMin, ");
        title.append("GyroYMin, ");
        title.append("GyroZMin, ");
        title.append("GyroXMax, ");
        title.append("GyroYMax, ");
        title.append("GyroZMax, ");
        title.append("GyroXMean, ");
        title.append("GyroYMean, ");
        title.append("GyroZMean, ");
        title.append("GyroXStd, ");
        title.append("GyroYStd, ");
        title.append("GyroZStd, ");

        title.append("GravityX, ");
        title.append("GravityY, ");
        title.append("GravityZ, ");
        title.append("GravityXMin, ");
        title.append("GravityYMin, ");
        title.append("GravityZMin, ");
        title.append("GravityXMax, ");
        title.append("GravityYMax, ");
        title.append("GravityZMax, ");
        title.append("GravityXMean, ");
        title.append("GravityYMean, ");
        title.append("GravityZMean, ");
        title.append("GravityXStd, ");
        title.append("GravityYStd, ");
        title.append("GravityZStd, ");

        title.append("LinAccelX, ");
        title.append("LinAccelY, ");
        title.append("LinAccelZ, ");
        title.append("LinAccelXMin, ");
        title.append("LinAccelYMin, ");
        title.append("LinAccelZMin, ");
        title.append("LinAccelXMax, ");
        title.append("LinAccelYMax, ");
        title.append("LinAccelZMax, ");
        title.append("LinAccelXMean, ");
        title.append("LinAccelYMean, ");
        title.append("LinAccelZMean, ");
        title.append("LinAccelXStd, ");
        title.append("LinAccelYStd, ");
        title.append("LinAccelZStd, ");

        title.append("RotVecX, ");
        title.append("RotVecY, ");
        title.append("RotVecZ, ");
        title.append("RotVecS, ");
        title.append("RotVecXMin, ");
        title.append("RotVecYMin, ");
        title.append("RotVecZMin, ");
        title.append("RotVecSMin, ");
        title.append("RotVecXMax, ");
        title.append("RotVecYMax, ");
        title.append("RotVecZMax, ");
        title.append("RotVecSMax, ");
        title.append("RotVecXMean, ");
        title.append("RotVecYMean, ");
        title.append("RotVecZMean, ");
        title.append("RotVecSMean, ");
        title.append("RotVecXStd, ");
        title.append("RotVecYStd, ");
        title.append("RotVecZStd, ");
        title.append("RotVecSStd, ");

        title.append("StDetVal, ");

        title.append("AiPreVal, ");
        title.append("AiPreValMin, ");
        title.append("AiPreValMax, ");
        title.append("AiPreValMean, ");
        title.append("AiPreValStd, ");

        title.append("MagFielX, ");
        title.append("MagFielY, ");
        title.append("MagFielZ, ");
        title.append("MagFielXMin, ");
        title.append("MagFielYMin, ");
        title.append("MagFielZMin, ");
        title.append("MagFielXMax, ");
        title.append("MagFielYMax, ");
        title.append("MagFielZMax, ");
        title.append("MagFielXMean, ");
        title.append("MagFielYMean, ");
        title.append("MagFielZMean, ");
        title.append("MagFielXStd, ");
        title.append("MagFielYStd, ");
        title.append("MagFielZStd, ");

        title.append("HeartRateVal, ");
        title.append("HeartRateValMin, ");
        title.append("HeartRateValMax, ");
        title.append("HeartRateValMean, ");
        title.append("HeartRateValStd, ");

        title.append("Activity");

        String dataTitle = title.toString();

        title = null;

        return dataTitle;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // In this example, alpha is calculated as t / (t + dT),
            // where t is the low-pass filter's time-constant and
            // dT is the event delivery rate.

            final float alpha = 0.1f;

            // Isolate the force of gravity with the low-pass filter.
            accelerometerFilter[0] = alpha * accelerometerFilter[0] + (1 - alpha) * event.values[0];
            accelerometerFilter[1] = alpha * accelerometerFilter[1] + (1 - alpha) * event.values[1];
            accelerometerFilter[2] = alpha * accelerometerFilter[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            float ax = event.values[0] - accelerometerFilter[0];
            float ay = event.values[1] - accelerometerFilter[1];
            float az = event.values[2] - accelerometerFilter[2];

            accelX = ax;
            accelY = ay;
            accelZ = az;
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            gyroX = ax;
            gyroY = ay;
            gyroZ = az;
        }

        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            gravityX = ax;
            gravityY = ay;
            gravityZ = az;
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            linAccelX = ax;
            linAccelY = ay;
            linAccelZ = az;
        }

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];
            float as = event.values[2];

            rotVecX = ax;
            rotVecY = ay;
            rotVecZ = az;
            rotVecS = as;
        }

        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            float ax = event.values[0];

            stDetVal = ax;
        }

        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            float ax = event.values[0];

            aiPreVal = ax;
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float ax = event.values[0];
            float ay = event.values[1];
            float az = event.values[2];

            magFielX = ax;
            magFielY = ay;
            magFielZ = az;
        }

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            float ax = event.values[0];

            heartRateVal = ax;
        }

        if (warmingUp) {
            stDetVal = 0.0f;
            heartRateVal = 0.0f;
            warmingUp = false;
        }
    }

    /**
     * Handles the button press to finish this activity and take the user back to the Home.
     */
    public void onFinishActivity(View view) {
        showCustomDialog(view);
    }

    public void onWalking() {
        performingActivity = 0;                                                      // ActivityType.valueOf("WALKING").ordinal();
        mGenericActivity.setText("Walking");
        timeChangeActivityUpdateMs = System.currentTimeMillis();
        posInstance = 0;
        computeComplexFeatures = false;
    }

    public void onUpstairs() {
        performingActivity = 1;                                                      // ActivityType.valueOf("WALKING_UPSTAIRS").ordinal();
        mGenericActivity.setText("Upstairs");
        timeChangeActivityUpdateMs = System.currentTimeMillis();
        posInstance = 0;
        computeComplexFeatures = false;
    }

    public void onDownstairs() {
        performingActivity = 2;                                                      // ActivityType.valueOf("WALKING_DOWNSTAIRS").ordinal();
        mGenericActivity.setText("Downstairs");
        timeChangeActivityUpdateMs = System.currentTimeMillis();
        posInstance = 0;
        computeComplexFeatures = false;
    }

    public void onSitting() {
        performingActivity = 3;                                                      // ActivityType.valueOf("SITTING").ordinal();
        mGenericActivity.setText("Sitting");
        timeChangeActivityUpdateMs = System.currentTimeMillis();
        posInstance = 0;
        computeComplexFeatures = false;
    }

    public void onStanding() {
        performingActivity = 4;                                                      // ActivityType.valueOf("STANDING").ordinal();
        mGenericActivity.setText("Standing");
        timeChangeActivityUpdateMs = System.currentTimeMillis();
        posInstance = 0;
        computeComplexFeatures = false;
    }

    public void onLaying() {
        performingActivity = 5;                                                      // ActivityType.valueOf("LAYING").ordinal();
        mGenericActivity.setText("Laying");
        timeChangeActivityUpdateMs = System.currentTimeMillis();
        posInstance = 0;
        computeComplexFeatures = false;
    }

    public void onPause(View view) {
        performingActivity = 6;
        mGenericActivity.setText("Pause");
        timeChangeActivityUpdateMs = System.currentTimeMillis();
        posInstance = 0;
        computeComplexFeatures = false;

        leftCenterButton.setVisibility(View.VISIBLE);
        bPause.setVisibility(View.INVISIBLE);
        bStop.setVisibility(View.VISIBLE);
    }

    /**
     * Handler separated into static class to avoid memory leaks.
     */
    private static class UpdateHandler extends Handler {
        private final WeakReference<MainActivity> mMainActivityWeakReference;

        public UpdateHandler(MainActivity reference) {
            mMainActivityWeakReference = new WeakReference<MainActivity>(reference);
        }

        @Override
        public void handleMessage(Message message) {
            MainActivity mainActivity = mMainActivityWeakReference.get();

            if (mainActivity != null) {
                switch (message.what) {
                    case MSG_UPDATE_SCREEN:
                        mainActivity.refreshDisplayAndSetNextUpdate();
                        break;
                }
            }
        }
    }

    public void createCircleMenu() {

        // Set up the large red button on the center right side
        // With custom button and content sizes and margins
        int redActionButtonSize = getResources().getDimensionPixelSize(R.dimen.red_action_button_size);
        int redActionButtonMargin = getResources().getDimensionPixelOffset(R.dimen.action_button_margin);
        int redActionButtonContentSize = getResources().getDimensionPixelSize(R.dimen.red_action_button_content_size);
        int redActionButtonContentMargin = getResources().getDimensionPixelSize(R.dimen.red_action_button_content_margin);
        int redActionMenuRadius = getResources().getDimensionPixelSize(R.dimen.red_action_menu_radius);
        int blueSubActionButtonSize = getResources().getDimensionPixelSize(R.dimen.blue_sub_action_button_size);
        int blueSubActionButtonContentMargin = getResources().getDimensionPixelSize(R.dimen.blue_sub_action_button_content_margin);

        ImageView fabIconStar = new ImageView(this);
        fabIconStar.setImageDrawable(getResources().getDrawable(R.drawable.ic_touch_app_white_24dp));

        FloatingActionButton.LayoutParams starParams = new FloatingActionButton.LayoutParams(redActionButtonSize, redActionButtonSize);
        starParams.setMargins(redActionButtonMargin,
                redActionButtonMargin,
                redActionButtonMargin,
                redActionButtonMargin);
        fabIconStar.setLayoutParams(starParams);

        FloatingActionButton.LayoutParams fabIconStarParams = new FloatingActionButton.LayoutParams(redActionButtonContentSize,
                redActionButtonContentSize);
        fabIconStarParams.setMargins(redActionButtonContentMargin,
                redActionButtonContentMargin,
                redActionButtonContentMargin,
                redActionButtonContentMargin);

        leftCenterButton = new FloatingActionButtonFlexibleActions.Builder(this)
                .setContentView(fabIconStar, fabIconStarParams)
                .setBackgroundDrawable(R.drawable.button_action_red_selector)
                .setPosition(FloatingActionButtonFlexibleActions.POSITION_CENTER)
                .setLayoutParams(starParams)
                .build();

        final GestureDetector gestureDetector = new GestureDetector(this, new MyGestureDetector());

        leftCenterButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return gestureDetector.onTouchEvent(event);

            }
        });

        // Set up customized SubActionButtons for the right center menu
        SubActionButton.Builder lCSubBuilder = new SubActionButton.Builder(this);
        lCSubBuilder.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_action_blue_selector));

        FrameLayout.LayoutParams blueContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        blueContentParams.setMargins(blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin,
                blueSubActionButtonContentMargin);
        lCSubBuilder.setLayoutParams(blueContentParams);
        // Set custom layout params
        FrameLayout.LayoutParams blueParams = new FrameLayout.LayoutParams(blueSubActionButtonSize, blueSubActionButtonSize);
        lCSubBuilder.setLayoutParams(blueParams);

        ImageView lcIcon1 = new ImageView(this);
        ImageView lcIcon2 = new ImageView(this);
        ImageView lcIcon3 = new ImageView(this);
        ImageView lcIcon4 = new ImageView(this);
        ImageView lcIcon5 = new ImageView(this);
        ImageView lcIcon6 = new ImageView(this);

        lcIcon1.setImageDrawable(getResources().getDrawable(R.drawable.ic_directions_walk_white_24dp));
        lcIcon2.setImageDrawable(getResources().getDrawable(R.drawable.ic_trending_up_white_24dp));
        lcIcon3.setImageDrawable(getResources().getDrawable(R.drawable.ic_trending_down_white_24dp));
        lcIcon4.setImageDrawable(getResources().getDrawable(R.drawable.ic_airline_seat_recline_normal_white_24dp));
        lcIcon5.setImageDrawable(getResources().getDrawable(R.drawable.ic_accessibility_white_24dp));
        lcIcon6.setImageDrawable(getResources().getDrawable(R.drawable.ic_local_hotel_white_24dp));

        SubActionButton buttonOnWalking = lCSubBuilder.setContentView(lcIcon1, blueContentParams).build();
        buttonOnWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftCenterButton.performClick();

                bPause.setVisibility(View.VISIBLE);

                mGenericActivity.setVisibility(View.VISIBLE);

                leftCenterButton.setVisibility(View.INVISIBLE);

                onWalking();
                Toast toast = Toast.makeText(MainActivity.this, "Walking", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        SubActionButton buttonOnWalkingUpstairs = lCSubBuilder.setContentView(lcIcon2, blueContentParams).build();
        buttonOnWalkingUpstairs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftCenterButton.performClick();

                bPause.setVisibility(View.VISIBLE);

                mGenericActivity.setVisibility(View.VISIBLE);

                leftCenterButton.setVisibility(View.INVISIBLE);

                onUpstairs();
                Toast toast = Toast.makeText(MainActivity.this, "Walking upstairs", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        SubActionButton buttonOnWalkingDownstairs = lCSubBuilder.setContentView(lcIcon3, blueContentParams).build();
        buttonOnWalkingDownstairs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftCenterButton.performClick();

                bPause.setVisibility(View.VISIBLE);

                mGenericActivity.setVisibility(View.VISIBLE);

                leftCenterButton.setVisibility(View.INVISIBLE);

                onDownstairs();
                Toast toast = Toast.makeText(MainActivity.this, "Waking downstairs", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        SubActionButton buttonOnSitting = lCSubBuilder.setContentView(lcIcon4, blueContentParams).build();
        buttonOnSitting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftCenterButton.performClick();

                bPause.setVisibility(View.VISIBLE);

                mGenericActivity.setVisibility(View.VISIBLE);

                leftCenterButton.setVisibility(View.INVISIBLE);

                onSitting();
                Toast toast = Toast.makeText(MainActivity.this, "Sitting", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        SubActionButton buttonOnStanding = lCSubBuilder.setContentView(lcIcon5, blueContentParams).build();
        buttonOnStanding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftCenterButton.performClick();

                bPause.setVisibility(View.VISIBLE);
                bStop.setVisibility(View.VISIBLE);
                mGenericActivity.setVisibility(View.VISIBLE);

                leftCenterButton.setVisibility(View.INVISIBLE);

                onStanding();
                Toast toast = Toast.makeText(MainActivity.this, "Standing", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        SubActionButton buttonOnLaying = lCSubBuilder.setContentView(lcIcon6, blueContentParams).build();
        buttonOnLaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftCenterButton.performClick();

                bPause.setVisibility(View.VISIBLE);

                mGenericActivity.setVisibility(View.VISIBLE);

                leftCenterButton.setVisibility(View.INVISIBLE);

                onLaying();
                Toast toast = Toast.makeText(MainActivity.this, "Laying", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        // Build another menu with custom options
        final FloatingActionMenu leftCenterMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(buttonOnWalking)
                .addSubActionView(buttonOnWalkingUpstairs)
                .addSubActionView(buttonOnWalkingDownstairs)
                .addSubActionView(buttonOnSitting)
                .addSubActionView(buttonOnStanding)
                .addSubActionView(buttonOnLaying)
                .setRadius(redActionMenuRadius)
                .setStartAngle(0)
                .setEndAngle(360)
                .attachTo(leftCenterButton)
                .build();

    }

    public void showCustomDialog(View view) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.dialog);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        // set the custom dialog components - text, image and button
        TextView text = (TextView) dialog.findViewById(R.id.text);
        text.setText(getResources().getString(R.string.dialog_title));

        Button bCancel = (Button) dialog.findViewById(R.id.cancel);
        // close dialog when this button is pressed

        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        Button bExit = (Button) dialog.findViewById(R.id.exit);
        // if button is clicked, go to browser to display content

        bExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Toast toast = Toast.makeText(MainActivity.this, "Have a nice day, my wonderful friend", Toast.LENGTH_SHORT);

                // center text in toast message
                TextView tView = (TextView) toast.getView().findViewById(android.R.id.message);
                if (tView != null) tView.setGravity(Gravity.CENTER);

                toast.show();

                setResult(RESULT_OK);
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public ArrayList<Attribute> getNewAttributes() {
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        int numAttrib = 0;

        attributes.add(new Attribute("AccelX", numAttrib++));
        attributes.add(new Attribute("AccelY", numAttrib++));
        attributes.add(new Attribute("AccelZ", numAttrib++));
        attributes.add(new Attribute("AccelXMin", numAttrib++));
        attributes.add(new Attribute("AccelYMin", numAttrib++));
        attributes.add(new Attribute("AccelZMin", numAttrib++));
        attributes.add(new Attribute("AccelXMax", numAttrib++));
        attributes.add(new Attribute("AccelYMax", numAttrib++));
        attributes.add(new Attribute("AccelZMax", numAttrib++));
        attributes.add(new Attribute("AccelXMean", numAttrib++));
        attributes.add(new Attribute("AccelYMean", numAttrib++));
        attributes.add(new Attribute("AccelZMean", numAttrib++));
        attributes.add(new Attribute("AccelXStd", numAttrib++));
        attributes.add(new Attribute("AccelYStd", numAttrib++));
        attributes.add(new Attribute("AccelZStd", numAttrib++));

        attributes.add(new Attribute("GyroX", numAttrib++));
        attributes.add(new Attribute("GyroY", numAttrib++));
        attributes.add(new Attribute("GyroZ", numAttrib++));
        attributes.add(new Attribute("GyroXMin", numAttrib++));
        attributes.add(new Attribute("GyroYMin", numAttrib++));
        attributes.add(new Attribute("GyroZMin", numAttrib++));
        attributes.add(new Attribute("GyroXMax", numAttrib++));
        attributes.add(new Attribute("GyroYMax", numAttrib++));
        attributes.add(new Attribute("GyroZMax", numAttrib++));
        attributes.add(new Attribute("GyroXMean", numAttrib++));
        attributes.add(new Attribute("GyroYMean", numAttrib++));
        attributes.add(new Attribute("GyroZMean", numAttrib++));
        attributes.add(new Attribute("GyroXStd", numAttrib++));
        attributes.add(new Attribute("GyroYStd", numAttrib++));
        attributes.add(new Attribute("GyroZStd", numAttrib++));

        attributes.add(new Attribute("GravityX", numAttrib++));
        attributes.add(new Attribute("GravityY", numAttrib++));
        attributes.add(new Attribute("GravityZ", numAttrib++));
        attributes.add(new Attribute("GravityXMin", numAttrib++));
        attributes.add(new Attribute("GravityYMin", numAttrib++));
        attributes.add(new Attribute("GravityZMin", numAttrib++));
        attributes.add(new Attribute("GravityXMax", numAttrib++));
        attributes.add(new Attribute("GravityYMax", numAttrib++));
        attributes.add(new Attribute("GravityZMax", numAttrib++));
        attributes.add(new Attribute("GravityXMean", numAttrib++));
        attributes.add(new Attribute("GravityYMean", numAttrib++));
        attributes.add(new Attribute("GravityZMean", numAttrib++));
        attributes.add(new Attribute("GravityXStd", numAttrib++));
        attributes.add(new Attribute("GravityYStd", numAttrib++));
        attributes.add(new Attribute("GravityZStd", numAttrib++));

        attributes.add(new Attribute("LinAccelX", numAttrib++));
        attributes.add(new Attribute("LinAccelY", numAttrib++));
        attributes.add(new Attribute("LinAccelZ", numAttrib++));
        attributes.add(new Attribute("LinAccelXMin", numAttrib++));
        attributes.add(new Attribute("LinAccelYMin", numAttrib++));
        attributes.add(new Attribute("LinAccelZMin", numAttrib++));
        attributes.add(new Attribute("LinAccelXMax", numAttrib++));
        attributes.add(new Attribute("LinAccelYMax", numAttrib++));
        attributes.add(new Attribute("LinAccelZMax", numAttrib++));
        attributes.add(new Attribute("LinAccelXMean", numAttrib++));
        attributes.add(new Attribute("LinAccelYMean", numAttrib++));
        attributes.add(new Attribute("LinAccelZMean", numAttrib++));
        attributes.add(new Attribute("LinAccelXStd", numAttrib++));
        attributes.add(new Attribute("LinAccelYStd", numAttrib++));
        attributes.add(new Attribute("LinAccelZStd", numAttrib++));

        attributes.add(new Attribute("RotVecX", numAttrib++));
        attributes.add(new Attribute("RotVecY", numAttrib++));
        attributes.add(new Attribute("RotVecZ", numAttrib++));
        attributes.add(new Attribute("RotVecS", numAttrib++));
        attributes.add(new Attribute("RotVecXMin", numAttrib++));
        attributes.add(new Attribute("RotVecYMin", numAttrib++));
        attributes.add(new Attribute("RotVecZMin", numAttrib++));
        attributes.add(new Attribute("RotVecSMin", numAttrib++));
        attributes.add(new Attribute("RotVecXMax", numAttrib++));
        attributes.add(new Attribute("RotVecYMax", numAttrib++));
        attributes.add(new Attribute("RotVecZMax", numAttrib++));
        attributes.add(new Attribute("RotVecSMax", numAttrib++));
        attributes.add(new Attribute("RotVecXMean", numAttrib++));
        attributes.add(new Attribute("RotVecYMean", numAttrib++));
        attributes.add(new Attribute("RotVecZMean", numAttrib++));
        attributes.add(new Attribute("RotVecSMean", numAttrib++));
        attributes.add(new Attribute("RotVecXStd", numAttrib++));
        attributes.add(new Attribute("RotVecYStd", numAttrib++));
        attributes.add(new Attribute("RotVecZStd", numAttrib++));
        attributes.add(new Attribute("RotVecSStd", numAttrib++));

        attributes.add(new Attribute("StDetVal", numAttrib++));

        attributes.add(new Attribute("AiPreVal", numAttrib++));
        attributes.add(new Attribute("AiPreValMin", numAttrib++));
        attributes.add(new Attribute("AiPreValMax", numAttrib++));
        attributes.add(new Attribute("AiPreValMean", numAttrib++));
        attributes.add(new Attribute("AiPreValStd", numAttrib++));

        attributes.add(new Attribute("MagFielX", numAttrib++));
        attributes.add(new Attribute("MagFielY", numAttrib++));
        attributes.add(new Attribute("MagFielZ", numAttrib++));
        attributes.add(new Attribute("MagFielXMin", numAttrib++));
        attributes.add(new Attribute("MagFielYMin", numAttrib++));
        attributes.add(new Attribute("MagFielZMin", numAttrib++));
        attributes.add(new Attribute("MagFielXMax", numAttrib++));
        attributes.add(new Attribute("MagFielYMax", numAttrib++));
        attributes.add(new Attribute("MagFielZMax", numAttrib++));
        attributes.add(new Attribute("MagFielXMean", numAttrib++));
        attributes.add(new Attribute("MagFielYMean", numAttrib++));
        attributes.add(new Attribute("MagFielZMean", numAttrib++));
        attributes.add(new Attribute("MagFielXStd", numAttrib++));
        attributes.add(new Attribute("MagFielYStd", numAttrib++));
        attributes.add(new Attribute("MagFielZStd", numAttrib++));

        attributes.add(new Attribute("HeartRateVal", numAttrib++));
        attributes.add(new Attribute("HeartRateValMin", numAttrib++));
        attributes.add(new Attribute("HeartRateValMax", numAttrib++));
        attributes.add(new Attribute("HeartRateValMean", numAttrib++));
        attributes.add(new Attribute("HeartRateValStd", numAttrib++));

        List<String> values = getActivityValues();
        attributes.add(new Attribute("Activity", values, numAttrib));

        return attributes;
    }

    private void doExit() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        alertDialog.setNegativeButton("No", null);

        alertDialog.setMessage("Do you want to exit?");
        alertDialog.setTitle("AppTitle");
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        doExit();
    }

    public DenseInstance getDenseInstances(int numOfAttributes, int pos) {
        double[] attributeValues = new double[numOfAttributes];
        int currentAttNumber = 0;

        if (computeComplexFeatures) {
            attributeValues[currentAttNumber++] = accelXArray[pos];
            attributeValues[currentAttNumber++] = accelYArray[pos];
            attributeValues[currentAttNumber++] = accelZArray[pos];
            attributeValues[currentAttNumber++] = accelXMin[pos];
            attributeValues[currentAttNumber++] = accelYMin[pos];
            attributeValues[currentAttNumber++] = accelZMin[pos];
            attributeValues[currentAttNumber++] = accelXMax[pos];
            attributeValues[currentAttNumber++] = accelYMax[pos];
            attributeValues[currentAttNumber++] = accelZMax[pos];
            attributeValues[currentAttNumber++] = accelXMean[pos];
            attributeValues[currentAttNumber++] = accelYMean[pos];
            attributeValues[currentAttNumber++] = accelZMean[pos];
            attributeValues[currentAttNumber++] = accelXStd[pos];
            attributeValues[currentAttNumber++] = accelYStd[pos];
            attributeValues[currentAttNumber++] = accelZStd[pos];

            attributeValues[currentAttNumber++] = gyroXArray[pos];
            attributeValues[currentAttNumber++] = gyroYArray[pos];
            attributeValues[currentAttNumber++] = gyroZArray[pos];
            attributeValues[currentAttNumber++] = gyroXMin[pos];
            attributeValues[currentAttNumber++] = gyroYMin[pos];
            attributeValues[currentAttNumber++] = gyroZMin[pos];
            attributeValues[currentAttNumber++] = gyroXMax[pos];
            attributeValues[currentAttNumber++] = gyroYMax[pos];
            attributeValues[currentAttNumber++] = gyroZMax[pos];
            attributeValues[currentAttNumber++] = gyroXMean[pos];
            attributeValues[currentAttNumber++] = gyroYMean[pos];
            attributeValues[currentAttNumber++] = gyroZMean[pos];
            attributeValues[currentAttNumber++] = gyroXStd[pos];
            attributeValues[currentAttNumber++] = gyroYStd[pos];
            attributeValues[currentAttNumber++] = gyroZStd[pos];

            attributeValues[currentAttNumber++] = gravityXArray[pos];
            attributeValues[currentAttNumber++] = gravityYArray[pos];
            attributeValues[currentAttNumber++] = gravityZArray[pos];
            attributeValues[currentAttNumber++] = gravityXMin[pos];
            attributeValues[currentAttNumber++] = gravityYMin[pos];
            attributeValues[currentAttNumber++] = gravityZMin[pos];
            attributeValues[currentAttNumber++] = gravityXMax[pos];
            attributeValues[currentAttNumber++] = gravityYMax[pos];
            attributeValues[currentAttNumber++] = gravityZMax[pos];
            attributeValues[currentAttNumber++] = gravityXMean[pos];
            attributeValues[currentAttNumber++] = gravityYMean[pos];
            attributeValues[currentAttNumber++] = gravityZMean[pos];
            attributeValues[currentAttNumber++] = gravityXStd[pos];
            attributeValues[currentAttNumber++] = gravityYStd[pos];
            attributeValues[currentAttNumber++] = gravityZStd[pos];

            attributeValues[currentAttNumber++] = linAccelXArray[pos];
            attributeValues[currentAttNumber++] = linAccelYArray[pos];
            attributeValues[currentAttNumber++] = linAccelZArray[pos];
            attributeValues[currentAttNumber++] = linAccelXMin[pos];
            attributeValues[currentAttNumber++] = linAccelYMin[pos];
            attributeValues[currentAttNumber++] = linAccelZMin[pos];
            attributeValues[currentAttNumber++] = linAccelXMax[pos];
            attributeValues[currentAttNumber++] = linAccelYMax[pos];
            attributeValues[currentAttNumber++] = linAccelZMax[pos];
            attributeValues[currentAttNumber++] = linAccelXMean[pos];
            attributeValues[currentAttNumber++] = linAccelYMean[pos];
            attributeValues[currentAttNumber++] = linAccelZMean[pos];
            attributeValues[currentAttNumber++] = linAccelXStd[pos];
            attributeValues[currentAttNumber++] = linAccelYStd[pos];
            attributeValues[currentAttNumber++] = linAccelZStd[pos];

            attributeValues[currentAttNumber++] = rotVecXArray[pos];
            attributeValues[currentAttNumber++] = rotVecYArray[pos];
            attributeValues[currentAttNumber++] = rotVecZArray[pos];
            attributeValues[currentAttNumber++] = rotVecSArray[pos];
            attributeValues[currentAttNumber++] = rotVecXMin[pos];
            attributeValues[currentAttNumber++] = rotVecYMin[pos];
            attributeValues[currentAttNumber++] = rotVecZMin[pos];
            attributeValues[currentAttNumber++] = rotVecSMin[pos];
            attributeValues[currentAttNumber++] = rotVecXMax[pos];
            attributeValues[currentAttNumber++] = rotVecYMax[pos];
            attributeValues[currentAttNumber++] = rotVecZMax[pos];
            attributeValues[currentAttNumber++] = rotVecSMax[pos];
            attributeValues[currentAttNumber++] = rotVecXMean[pos];
            attributeValues[currentAttNumber++] = rotVecYMean[pos];
            attributeValues[currentAttNumber++] = rotVecZMean[pos];
            attributeValues[currentAttNumber++] = rotVecSMean[pos];
            attributeValues[currentAttNumber++] = rotVecXStd[pos];
            attributeValues[currentAttNumber++] = rotVecYStd[pos];
            attributeValues[currentAttNumber++] = rotVecZStd[pos];
            attributeValues[currentAttNumber++] = rotVecSStd[pos];

            attributeValues[currentAttNumber++] = stDetValArray[pos];

            attributeValues[currentAttNumber++] = aiPreValArray[pos];
            attributeValues[currentAttNumber++] = aiPreValMin[pos];
            attributeValues[currentAttNumber++] = aiPreValMax[pos];
            attributeValues[currentAttNumber++] = aiPreValMean[pos];
            attributeValues[currentAttNumber++] = aiPreValStd[pos];

            attributeValues[currentAttNumber++] = magFielXArray[pos];
            attributeValues[currentAttNumber++] = magFielYArray[pos];
            attributeValues[currentAttNumber++] = magFielZArray[pos];
            attributeValues[currentAttNumber++] = magFielXMin[pos];
            attributeValues[currentAttNumber++] = magFielYMin[pos];
            attributeValues[currentAttNumber++] = magFielZMin[pos];
            attributeValues[currentAttNumber++] = magFielXMax[pos];
            attributeValues[currentAttNumber++] = magFielYMax[pos];
            attributeValues[currentAttNumber++] = magFielZMax[pos];
            attributeValues[currentAttNumber++] = magFielXMean[pos];
            attributeValues[currentAttNumber++] = magFielYMean[pos];
            attributeValues[currentAttNumber++] = magFielZMean[pos];
            attributeValues[currentAttNumber++] = magFielXStd[pos];
            attributeValues[currentAttNumber++] = magFielYStd[pos];
            attributeValues[currentAttNumber++] = magFielZStd[pos];

            attributeValues[currentAttNumber++] = heartRateValArray[pos];
            attributeValues[currentAttNumber++] = heartRateValMin[pos];
            attributeValues[currentAttNumber++] = heartRateValMax[pos];
            attributeValues[currentAttNumber++] = heartRateValMean[pos];
            attributeValues[currentAttNumber++] = heartRateValStd[pos];

            List<String> activityValues = getActivityValues();
            if (appState == 1) {
                // any value for a place of activity
                attributeValues[currentAttNumber] = activityValues.indexOf(activityValues.get(0));
            } else {
                attributeValues[currentAttNumber] = activityValues.indexOf(activityValues.get(performingActivity));
            }
        }

        return new DenseInstance(1.0, attributeValues);
    }

    public float getBatteryLevel() {
        Intent batteryIntent = getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    private List<String> getActivityValues() {
        List<String> values = new ArrayList<String>();
        values.add("0");
        values.add("1");
        values.add("2");
        values.add("3");
        values.add("4");
        values.add("5");
        values.add("NA");

        return values;
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                if (bStop.getVisibility() == View.VISIBLE) {
                    mGenericActivity.setVisibility(View.INVISIBLE);
                    bStop.setVisibility(View.INVISIBLE);
                } else {
                    if (!(mGenericActivity.getText().toString().equals(getResources().getString(R.string.new_activity)))) {
                        mGenericActivity.setVisibility(View.VISIBLE);
                    }
                    bStop.setVisibility(View.VISIBLE);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            showCustomDialog(bStop);
        }

    }

}
