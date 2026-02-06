import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:volume_controller/volume_controller.dart';
import 'package:screen_brightness/screen_brightness.dart';
import 'package:torch_light/torch_light.dart';
import 'package:battery_plus/battery_plus.dart';
import 'package:home_widget/home_widget.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flow Widgets',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF1E88E5),
          brightness: Brightness.dark,
          surface: const Color(0xFF121212),
        ),
        useMaterial3: true,
      ),
      home: const WidgetsHomePage(),
    );
  }
}

/// Reusable tile for quick settings / notification-style controls.
class ControlTile extends StatelessWidget {
  const ControlTile({
    super.key,
    required this.icon,
    required this.label,
    this.subtitle,
    this.isOn = false,
    this.value,
    this.onTap,
    this.accentColor,
  });

  final IconData icon;
  final String label;
  final String? subtitle;
  final bool isOn;
  final double? value; // 0.0–1.0 for sliders
  final VoidCallback? onTap;
  final Color? accentColor;

  @override
  Widget build(BuildContext context) {
    final color = accentColor ?? Theme.of(context).colorScheme.primary;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surfaceContainerHighest.withValues(alpha: 0.6),
            borderRadius: BorderRadius.circular(16),
            border: Border.all(
              color: isOn ? color.withValues(alpha: 0.5) : Colors.transparent,
              width: 1.5,
            ),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                icon,
                size: 28,
                color: isOn ? color : Theme.of(context).colorScheme.onSurfaceVariant,
              ),
              const SizedBox(height: 8),
              Text(
                label,
                style: Theme.of(context).textTheme.labelMedium?.copyWith(
                      color: Theme.of(context).colorScheme.onSurface,
                      fontWeight: FontWeight.w600,
                    ),
                textAlign: TextAlign.center,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              if (subtitle != null) ...[
                const SizedBox(height: 2),
                Text(
                  subtitle!,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                  textAlign: TextAlign.center,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
              if (value != null) ...[
                const SizedBox(height: 6),
                ClipRRect(
                  borderRadius: BorderRadius.circular(2),
                  child: LinearProgressIndicator(
                    value: value!.clamp(0.0, 1.0),
                    backgroundColor: Theme.of(context).colorScheme.surfaceContainerHigh,
                    valueColor: AlwaysStoppedAnimation<Color>(color),
                    minHeight: 4,
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class WidgetsHomePage extends StatefulWidget {
  const WidgetsHomePage({super.key});

  @override
  State<WidgetsHomePage> createState() => _WidgetsHomePageState();
}

class _WidgetsHomePageState extends State<WidgetsHomePage> with WidgetsBindingObserver {
  bool _notificationsOn = true;
  bool _lockOn = true;
  bool _wifiOn = true;
  bool _bluetoothOn = false;
  bool _dndOn = false;
  bool _dataOn = true;
  bool _flashlightOn = false;
  double _volume = 0.7;
  double _brightness = 0.8;
  int _batteryLevel = 0;
  bool _torchAvailable = false;
  bool _canWriteSettings = true;
  String? _volumeError;
  String? _brightnessError;
  String? _torchError;

  final Battery _battery = Battery();
  StreamSubscription<double>? _volumeSubscription;

  static const _permissionsChannel = MethodChannel('com.chinmaya.myflowidgets/permissions');

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _loadVolume();
    _loadBrightness();
    _loadBattery();
    _checkTorch();
    _checkPermissions();
    _volumeSubscription = VolumeController.instance.addListener((volume) {
      if (mounted) setState(() => _volume = volume);
    });
    _battery.onBatteryStateChanged.listen((_) => _loadBattery());
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _checkPermissions();
  }

  Future<void> _checkPermissions() async {
    try {
      final canWrite = await _permissionsChannel.invokeMethod<bool>('canWriteSettings') ?? false;
      if (mounted) setState(() => _canWriteSettings = canWrite);
    } on PlatformException catch (_) {
      if (mounted) setState(() => _canWriteSettings = true);
    }
  }

  Future<void> _openWriteSettings() async {
    try {
      await _permissionsChannel.invokeMethod('openWriteSettings');
      await Future<void>.delayed(const Duration(milliseconds: 500));
      if (mounted) await _checkPermissions();
    } on PlatformException catch (_) {}
  }

  Future<void> _requestRelevantPermissions() async {
    try {
      final status = await Permission.notification.status;
      if (status.isDenied) await Permission.notification.request();
    } catch (_) {}
    await _checkPermissions();
    if (mounted && !_canWriteSettings) {
      _showPermissionDialog();
    }
  }

  void _showPermissionDialog() {
    showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Modify system settings'),
        content: const Text(
          'To change system brightness (including from the brightness widget), allow this app to "Modify system settings".\n\n'
          'Tap Open settings, then turn ON "Allow modify system settings" for this app.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Later'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.of(context).pop();
              _openWriteSettings();
            },
            child: const Text('Open settings'),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _volumeSubscription?.cancel();
    VolumeController.instance.removeListener();
    TorchLight.disableTorch();
    super.dispose();
  }

  Future<void> _loadVolume() async {
    try {
      final v = await VolumeController.instance.getVolume();
      if (mounted) {
        setState(() {
          _volume = v;
          _volumeError = null;
        });
        _updateHomeWidget();
      }
    } catch (e) {
      if (mounted) {
        setState(() => _volumeError = e.toString());
      }
    }
  }

  Future<void> _loadBrightness() async {
    try {
      final b = await ScreenBrightness.instance.application;
      if (mounted) {
        setState(() {
          _brightness = b.clamp(0.0, 1.0);
          _brightnessError = null;
        });
        _updateHomeWidget();
      }
    } catch (e) {
      if (mounted) {
        setState(() => _brightnessError = e.toString());
      }
    }
  }

  Future<void> _loadBattery() async {
    try {
      final level = await _battery.batteryLevel;
      if (mounted) {
        setState(() => _batteryLevel = level);
        _updateHomeWidget();
      }
    } catch (_) {
      // Ignore battery load errors
    }
  }

  Future<void> _checkTorch() async {
    try {
      final available = await TorchLight.isTorchAvailable();
      if (mounted) {
        setState(() {
          _torchAvailable = available;
          _torchError = null;
        });
        _updateHomeWidget();
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _torchAvailable = false;
          _torchError = e.toString();
        });
      }
    }
  }

  /// Pushes current tile data to each home screen widget (Volume, Brightness, Battery, etc.).
  Future<void> _updateHomeWidget() async {
    try {
      await HomeWidget.saveWidgetData<int>('battery', _batteryLevel);
      await HomeWidget.saveWidgetData<double>('volume', _volume);
      await HomeWidget.saveWidgetData<double>('brightness', _brightness);
      await HomeWidget.saveWidgetData<bool>('flash', _flashlightOn);
      await HomeWidget.saveWidgetData<bool>('notifications', _notificationsOn);
      await HomeWidget.saveWidgetData<bool>('lock', _lockOn);
      await HomeWidget.saveWidgetData<bool>('wifi', _wifiOn);
      await HomeWidget.saveWidgetData<bool>('bluetooth', _bluetoothOn);
      await HomeWidget.saveWidgetData<bool>('dnd', _dndOn);
      await HomeWidget.saveWidgetData<bool>('data', _dataOn);
      const providerNames = [
        'VolumeWidgetProvider',
        'BrightnessWidgetProvider',
        'BatteryWidgetProvider',
        'FlashWidgetProvider',
        'NotificationWidgetProvider',
        'LockWidgetProvider',
        'WifiWidgetProvider',
        'BluetoothWidgetProvider',
        'DndWidgetProvider',
        'DataWidgetProvider',
        'FullscreenWidgetProvider',
        'NetSpeedWidgetProvider',
      ];
      for (final name in providerNames) {
        await HomeWidget.updateWidget(androidName: name);
      }
    } catch (_) {
      // Widgets may not be on home screen; ignore
    }
  }

  Future<void> _setVolume(double value) async {
    try {
      await VolumeController.instance.setVolume(value);
      if (mounted) {
        setState(() {
          _volume = value;
          _volumeError = null;
        });
        _updateHomeWidget();
      }
    } catch (e) {
      if (mounted) {
        setState(() => _volumeError = e.toString());
      }
    }
  }

  Future<void> _setBrightness(double value) async {
    try {
      await ScreenBrightness.instance.setApplicationScreenBrightness(value);
      if (mounted) {
        setState(() {
          _brightness = value.clamp(0.0, 1.0);
          _brightnessError = null;
        });
        _updateHomeWidget();
      }
    } catch (e) {
      if (mounted) {
        setState(() => _brightnessError = e.toString());
      }
    }
  }

  Future<void> _toggleFlashlight() async {
    if (!_torchAvailable) return;
    try {
      if (_flashlightOn) {
        await TorchLight.disableTorch();
        if (mounted) {
          setState(() {
            _flashlightOn = false;
            _torchError = null;
          });
          _updateHomeWidget();
        }
      } else {
        await TorchLight.enableTorch();
        if (mounted) {
          setState(() {
            _flashlightOn = true;
            _torchError = null;
          });
          _updateHomeWidget();
        }
      }
    } catch (e) {
      if (mounted) {
        setState(() => _torchError = e.toString());
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 24, 20, 16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Flow Widgets',
                      style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: Theme.of(context).colorScheme.onSurface,
                          ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Add Volume, Brightness, Battery, Flash, etc. from the widget picker (each with icon and slider).',
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                            color: Theme.of(context).colorScheme.onSurfaceVariant,
                            fontSize: 12,
                          ),
                    ),
                    if (!_canWriteSettings) ...[
                      const SizedBox(height: 12),
                      Material(
                        color: Colors.orange.withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(12),
                        child: InkWell(
                          onTap: _showPermissionDialog,
                          borderRadius: BorderRadius.circular(12),
                          child: Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                            child: Row(
                              children: [
                                Icon(Icons.warning_amber_rounded, color: Colors.orange[700], size: 24),
                                const SizedBox(width: 10),
                                Expanded(
                                  child: Text(
                                    'Allow "Modify system settings" for brightness widget',
                                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                                          color: Theme.of(context).colorScheme.onSurface,
                                        ),
                                  ),
                                ),
                                Text('Open', style: TextStyle(color: Colors.orange[700], fontWeight: FontWeight.w600)),
                              ],
                            ),
                          ),
                        ),
                      ),
                    ],
                    const SizedBox(height: 8),
                    OutlinedButton.icon(
                      onPressed: _requestRelevantPermissions,
                      icon: const Icon(Icons.settings, size: 18),
                      label: const Text('Check & request permissions'),
                    ),
                  ],
                ),
              ),
            ),
            SliverPadding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              sliver: SliverGrid(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  mainAxisSpacing: 12,
                  crossAxisSpacing: 12,
                  childAspectRatio: 0.95,
                ),
                delegate: SliverChildListDelegate([
                  ControlTile(
                    icon: Icons.notifications_active,
                    label: 'Notifications',
                    subtitle: _notificationsOn ? 'On' : 'Off',
                    isOn: _notificationsOn,
                    onTap: () {
                      setState(() => _notificationsOn = !_notificationsOn);
                      _updateHomeWidget();
                    },
                    accentColor: Colors.orange,
                  ),
                  ControlTile(
                    icon: Icons.volume_up,
                    label: 'Volume',
                    subtitle: _volumeError != null ? 'Error' : '${(_volume * 100).round()}%',
                    value: _volume,
                    isOn: _volume > 0,
                    onTap: () => _showVolumeSlider(context),
                    accentColor: Colors.green,
                  ),
                  ControlTile(
                    icon: _lockOn ? Icons.lock : Icons.lock_open,
                    label: 'Screen lock',
                    subtitle: _lockOn ? 'Locked' : 'Unlocked',
                    isOn: _lockOn,
                    onTap: () {
                      setState(() => _lockOn = !_lockOn);
                      _updateHomeWidget();
                    },
                    accentColor: Colors.blue,
                  ),
                  ControlTile(
                    icon: _wifiOn ? Icons.wifi : Icons.wifi_off,
                    label: 'Wi‑Fi',
                    subtitle: _wifiOn ? 'Connected' : 'Off',
                    isOn: _wifiOn,
                    onTap: () {
                      setState(() => _wifiOn = !_wifiOn);
                      _updateHomeWidget();
                    },
                    accentColor: Colors.cyan,
                  ),
                  ControlTile(
                    icon: _bluetoothOn ? Icons.bluetooth_connected : Icons.bluetooth_disabled,
                    label: 'Bluetooth',
                    subtitle: _bluetoothOn ? 'On' : 'Off',
                    isOn: _bluetoothOn,
                    onTap: () {
                      setState(() => _bluetoothOn = !_bluetoothOn);
                      _updateHomeWidget();
                    },
                    accentColor: Colors.indigo,
                  ),
                  ControlTile(
                    icon: Icons.brightness_6,
                    label: 'Brightness',
                    subtitle: _brightnessError != null ? 'Error' : '${(_brightness * 100).round()}%',
                    value: _brightness,
                    isOn: _brightness > 0,
                    onTap: () {
                      if (!_canWriteSettings) {
                        _showPermissionDialog();
                        return;
                      }
                      _showBrightnessSlider(context);
                    },
                    accentColor: Colors.amber,
                  ),
                  ControlTile(
                    icon: Icons.battery_charging_full,
                    label: 'Battery',
                    subtitle: '$_batteryLevel%',
                    value: _batteryLevel / 100,
                    isOn: true,
                    accentColor: Colors.teal,
                  ),
                  ControlTile(
                    icon: _flashlightOn ? Icons.flash_on : Icons.flash_off,
                    label: 'Flashlight',
                    subtitle: _torchError != null ? 'N/A' : (_flashlightOn ? 'On' : 'Off'),
                    isOn: _flashlightOn,
                    onTap: _torchAvailable ? _toggleFlashlight : null,
                    accentColor: Colors.yellow,
                  ),
                  ControlTile(
                    icon: Icons.do_not_disturb_on,
                    label: 'Do not disturb',
                    subtitle: _dndOn ? 'On' : 'Off',
                    isOn: _dndOn,
                    onTap: () {
                      setState(() => _dndOn = !_dndOn);
                      _updateHomeWidget();
                    },
                    accentColor: Colors.deepPurple,
                  ),
                  ControlTile(
                    icon: Icons.data_usage,
                    label: 'Network data',
                    subtitle: 'Tap to open settings',
                    isOn: _dataOn,
                    onTap: () async {
                      try {
                        await _permissionsChannel.invokeMethod('openDataUsageSettings');
                        _updateHomeWidget();
                      } on PlatformException catch (_) {}
                    },
                    accentColor: Colors.lightGreen,
                  ),
                  ControlTile(
                    icon: Icons.fullscreen,
                    label: 'Force full screen',
                    subtitle: 'Tap to enter fullscreen',
                    isOn: false,
                    onTap: () async {
                      try {
                        await _permissionsChannel.invokeMethod('launchFullscreen');
                        _updateHomeWidget();
                      } on PlatformException catch (_) {}
                    },
                    accentColor: Colors.blueGrey,
                  ),
                  ControlTile(
                    icon: Icons.speed,
                    label: 'Net speed',
                    subtitle: 'Tap to open data usage',
                    isOn: true,
                    onTap: () async {
                      try {
                        await _permissionsChannel.invokeMethod('openDataUsageSettings');
                        _updateHomeWidget();
                      } on PlatformException catch (_) {}
                    },
                    accentColor: Colors.cyan,
                  ),
                ]),
              ),
            ),
            const SliverToBoxAdapter(child: SizedBox(height: 24)),
          ],
        ),
      ),
    );
  }

  void _showVolumeSlider(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => StatefulBuilder(
        builder: (context, setModalState) => Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surfaceContainerHigh,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Volume', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              Slider(
                value: _volume,
                onChanged: (v) {
                  setState(() => _volume = v);
                  _setVolume(v);
                  setModalState(() {});
                },
                activeColor: Colors.green,
              ),
              Text('${(_volume * 100).round()}%', style: Theme.of(context).textTheme.bodyLarge),
            ],
          ),
        ),
      ),
    );
  }

  void _showBrightnessSlider(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => StatefulBuilder(
        builder: (context, setModalState) => Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surfaceContainerHigh,
            borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text('Brightness', style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 16),
              Slider(
                value: _brightness,
                onChanged: (v) {
                  setState(() => _brightness = v.clamp(0.0, 1.0));
                  _setBrightness(v);
                  setModalState(() {});
                },
                activeColor: Colors.amber,
              ),
              Text('${(_brightness * 100).round()}%', style: Theme.of(context).textTheme.bodyLarge),
            ],
          ),
        ),
      ),
    );



  }
}
