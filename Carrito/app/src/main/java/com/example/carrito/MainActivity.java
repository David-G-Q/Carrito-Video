package com.example.carrito;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Declarar variables y objetos necesarios

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> deviceList;

    private TextView txtX;
    private TextView txtY;
    private TextView txtZ;
    String msg;
    private Button btnConectarBluetooth;
    private Button btnArriba;
    private Button btnAbajo;
    private Button btnIzquierda;
    private Button btnDerecha;
    private Button btnDetener;

    private OutputStream outputStream;
    private BluetoothSocket bluetoothSocket;



    private Handler repeatHandler;
    private boolean repeatFlag = false;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 2;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // UUID para SPP (Serial Port Profile)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtener referencias a los elementos del diseño

        btnConectarBluetooth = findViewById(R.id.btnConectarBluetooth);
        btnArriba = findViewById(R.id.btnArriba);
        btnAbajo = findViewById(R.id.btnAbajo);
        btnIzquierda = findViewById(R.id.btnIzquierda);
        btnDerecha = findViewById(R.id.btnDerecha);
        btnDetener = findViewById(R.id.btnDetener);
        txtX = findViewById(R.id.txtX);
        txtY = findViewById(R.id.txtY);
        txtZ = findViewById(R.id.txtZ);
        // Configurar los colores iniciales de los botones
        btnArriba.setBackgroundColor(Color.BLACK);
        btnAbajo.setBackgroundColor(Color.BLACK);
        btnIzquierda.setBackgroundColor(Color.BLACK);
        btnDerecha.setBackgroundColor(Color.BLACK);
        // Configurar el adaptador Bluetooth

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // El dispositivo no tiene Bluetooth
        }
// Solicitar permisos Bluetooth si no están concedidos
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_PERMISSION);
        }
//Solicitar al usuario habilitar el Bluetooth si está desactivado
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        // Configurar los botones y establecer sus acciones
        btnConectarBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectBluetooth();
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            // El dispositivo no tiene acelerómetro
        }

        btnIzquierda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });

        btnDerecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });

        btnDetener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand("5");
            }
        });

        btnArriba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });

        btnAbajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBluetoothCommand(msg);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Registrar el listener del acelerómetro
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Obtener los valores de los ejes x, y, z del sensor
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        // Redondear los valores a dos decimales
        x = Math.round(x * 100) / 100.0f;
        y = Math.round(y * 100) / 100.0f;
        z = Math.round(z * 100) / 100.0f;

        // Actualizar los valores de los ejes en los TextView correspondientes
        txtX.setText("X: " + String.valueOf(x));
        txtY.setText("Y: " + String.valueOf(y));
        txtZ.setText("Z: " + String.valueOf(z));

        // Actualizar los TextView con los valores de los ejes
        if (y > 5) {
            btnAbajo.setBackgroundColor(Color.GREEN);
            msg = "3";
            btnAbajo.performClick();
        } else {
            btnAbajo.setBackgroundColor(Color.BLACK);
        }

        // Detectar el movimiento hacia arriba
        if (y < -5) {
            btnArriba.setBackgroundColor(Color.GREEN);
            msg = "1";
            btnArriba.performClick();
        } else {
            btnArriba.setBackgroundColor(Color.BLACK);
        }

        // Detectar el movimiento hacia la izquierda
        if (x > 5) {
            btnIzquierda.setBackgroundColor(Color.GREEN);
            btnIzquierda.performClick();
            msg = "2";
        } else {
            btnIzquierda.setBackgroundColor(Color.BLACK);
        }

        // Detectar el movimiento hacia la derecha
        if (x < -5) {
            btnDerecha.setBackgroundColor(Color.GREEN);
            btnDerecha.performClick();
            msg = "4";
        } else {
            btnDerecha.setBackgroundColor(Color.BLACK);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendBluetoothCommand(String command) {
        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error ", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No hay conexión ", Toast.LENGTH_SHORT).show();
        }
    }

    private void connectBluetooth() {
        // Obtener los dispositivos Bluetooth emparejados
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        pairedDevices = bluetoothAdapter.getBondedDevices();
        deviceList = new ArrayList<>();


        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
            }

            // Mostrar lista de dispositivos emparejados
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Selecciona el dispositivo");
            builder.setCancelable(true);


            CharSequence[] deviceNames = new CharSequence[deviceList.size()];
            for (int i = 0; i < deviceList.size(); i++) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                deviceNames[i] = deviceList.get(i).getName();
            }


            builder.setItems(deviceNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    BluetoothDevice selectedDevice = deviceList.get(which);


                    try {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothSocket.connect();
                        outputStream = bluetoothSocket.getOutputStream();
                        Toast.makeText(MainActivity.this, "Conexión exitosa", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error al establecer la conexión", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(this, "No hay dispositivos ", Toast.LENGTH_SHORT).show();
        }
    }


}
