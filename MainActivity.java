package com.voicecontrol.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.voicecontrol.app.activities.SettingsActivity;
import com.voicecontrol.app.activities.TutorialActivity;
import com.voicecontrol.app.services.VoiceService;
import com.voicecontrol.app.services.OverlayService;
import com.voicecontrol.app.utils.PermissionManager;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1002;
    private static final int ACCESSIBILITY_PERMISSION_REQUEST_CODE = 1003;
    
    private Button btnStartService;
    private Button btnStopService;
    private Button btnSettings;
    private Button btnTutorial;
    private TextView tvStatus;
    private TextView tvInstructions;
    
    private PermissionManager permissionManager;
    private boolean isServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupClickListeners();
        
        permissionManager = new PermissionManager(this);
        
        // Verificar permisos al iniciar
        checkAndRequestPermissions();
        
        updateUI();
    }
    
    private void initializeViews() {
        btnStartService = findViewById(R.id.btn_start_service);
        btnStopService = findViewById(R.id.btn_stop_service);
        btnSettings = findViewById(R.id.btn_settings);
        btnTutorial = findViewById(R.id.btn_tutorial);
        tvStatus = findViewById(R.id.tv_status);
        tvInstructions = findViewById(R.id.tv_instructions);
        
        // Configurar texto de instrucciones
        tvInstructions.setText(
            "INSTRUCCIONES DE USO:\n\n" +
            "1. Concede todos los permisos necesarios\n" +
            "2. Activa el servicio de accesibilidad\n" +
            "3. Inicia el servicio de voz\n" +
            "4. Di 'Activate' para activar el control por voz\n" +
            "5. Di 'Help' para ver comandos disponibles\n\n" +
            "COMANDOS DE EJEMPLO:\n" +
            "• 'Abre Gmail'\n" +
            "• 'Escribe hola mundo'\n" +
            "• 'Toca el botón enviar'\n" +
            "• 'Vuelve atrás'\n" +
            "• 'Abre configuración'"
        );
    }
    
    private void setupClickListeners() {
        btnStartService.setOnClickListener(v -> startVoiceService());
        btnStopService.setOnClickListener(v -> stopVoiceService());
        btnSettings.setOnClickListener(v -> openSettings());
        btnTutorial.setOnClickListener(v -> openTutorial());
    }
    
    private void checkAndRequestPermissions() {
        if (!permissionManager.hasAllRequiredPermissions()) {
            showPermissionDialog();
        } else if (!permissionManager.hasOverlayPermission()) {
            requestOverlayPermission();
        } else if (!permissionManager.hasAccessibilityPermission()) {
            requestAccessibilityPermission();
        }
    }
    
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Permisos Necesarios")
            .setMessage("Esta aplicación necesita varios permisos para funcionar correctamente:\n\n" +
                       "• Micrófono: Para reconocimiento de voz\n" +
                       "• Almacenamiento: Para guardar comandos\n" +
                       "• Overlay: Para mostrar controles flotantes\n" +
                       "• Accesibilidad: Para controlar otras apps\n\n" +
                       "¿Deseas conceder estos permisos?")
            .setPositiveButton("Sí", (dialog, which) -> requestBasicPermissions())
            .setNegativeButton("No", (dialog, which) -> {
                Toast.makeText(this, "La app no funcionará sin permisos", Toast.LENGTH_LONG).show();
                finish();
            })
            .setCancelable(false)
            .show();
    }
    
    private void requestBasicPermissions() {
        String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        };
        
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }
    
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            new AlertDialog.Builder(this)
                .setTitle("Permiso de Overlay")
                .setMessage("Para mostrar controles flotantes sobre otras aplicaciones, necesitamos el permiso de overlay.")
                .setPositiveButton("Conceder", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Cancelar", null)
                .show();
        }
    }
    
    private void requestAccessibilityPermission() {
        new AlertDialog.Builder(this)
            .setTitle("Servicio de Accesibilidad")
            .setMessage("Para controlar otras aplicaciones, necesitas activar el servicio de accesibilidad.\n\n" +
                       "1. Se abrirá la configuración de accesibilidad\n" +
                       "2. Busca 'Voice Control App'\n" +
                       "3. Actívalo y confirma")
            .setPositiveButton("Abrir Configuración", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, ACCESSIBILITY_PERMISSION_REQUEST_CODE);
            })
            .setNegativeButton("Más tarde", null)
            .show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "Permisos básicos concedidos", Toast.LENGTH_SHORT).show();
                if (!permissionManager.hasOverlayPermission()) {
                    requestOverlayPermission();
                }
            } else {
                Toast.makeText(this, "Algunos permisos fueron denegados", Toast.LENGTH_LONG).show();
            }
        }
        
        updateUI();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (permissionManager.hasOverlayPermission()) {
                Toast.makeText(this, "Permiso de overlay concedido", Toast.LENGTH_SHORT).show();
                if (!permissionManager.hasAccessibilityPermission()) {
                    requestAccessibilityPermission();
                }
            } else {
                Toast.makeText(this, "Permiso de overlay denegado", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == ACCESSIBILITY_PERMISSION_REQUEST_CODE) {
            if (permissionManager.hasAccessibilityPermission()) {
                Toast.makeText(this, "Servicio de accesibilidad activado", Toast.LENGTH_SHORT).show();
            }
        }
        
        updateUI();
    }
    
    private void startVoiceService() {
        if (!permissionManager.hasAllRequiredPermissions()) {
            Toast.makeText(this, "Faltan permisos necesarios", Toast.LENGTH_LONG).show();
            checkAndRequestPermissions();
            return;
        }
        
        // Iniciar servicio de voz
        Intent voiceIntent = new Intent(this, VoiceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(voiceIntent);
        } else {
            startService(voiceIntent);
        }
        
        // Iniciar servicio de overlay
        Intent overlayIntent = new Intent(this, OverlayService.class);
        startService(overlayIntent);
        
        isServiceRunning = true;
        updateUI();
        
        Toast.makeText(this, "Servicio de voz iniciado. Di 'Activate' para comenzar.", Toast.LENGTH_LONG).show();
    }
    
    private void stopVoiceService() {
        stopService(new Intent(this, VoiceService.class));
        stopService(new Intent(this, OverlayService.class));
        
        isServiceRunning = false;
        updateUI();
        
        Toast.makeText(this, "Servicio de voz detenido", Toast.LENGTH_SHORT).show();
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    private void openTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);
    }
    
    private void updateUI() {
        boolean hasPermissions = permissionManager.hasAllRequiredPermissions();
        boolean hasOverlay = permissionManager.hasOverlayPermission();
        boolean hasAccessibility = permissionManager.hasAccessibilityPermission();
        
        btnStartService.setEnabled(hasPermissions && hasOverlay && hasAccessibility && !isServiceRunning);
        btnStopService.setEnabled(isServiceRunning);
        
        String status = "Estado: ";
        if (!hasPermissions) {
            status += "Faltan permisos básicos";
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else if (!hasOverlay) {
            status += "Falta permiso de overlay";
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        } else if (!hasAccessibility) {
            status += "Falta servicio de accesibilidad";
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        } else if (isServiceRunning) {
            status += "Servicio activo - Di 'Activate'";
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else {
            status += "Listo para iniciar";
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark));
        }
        
        tvStatus.setText(status);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}