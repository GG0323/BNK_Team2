import 'package:camera/camera.dart';
import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';

class IdCardCameraScreen extends StatefulWidget {
  const IdCardCameraScreen({super.key});

  @override
  State<IdCardCameraScreen> createState() => _IdCardCameraScreenState();
}

class _IdCardCameraScreenState extends State<IdCardCameraScreen> {
  CameraController? _controller;
  Future<void>? _initializeCameraFuture;
  bool _capturing = false;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _initializeCameraFuture = _initializeCamera();
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  Future<void> _initializeCamera() async {
    try {
      final cameras = await availableCameras();
      if (cameras.isEmpty) {
        throw CameraException('no_camera', '사용 가능한 카메라가 없습니다.');
      }

      final camera = cameras.firstWhere(
        (camera) => camera.lensDirection == CameraLensDirection.back,
        orElse: () => cameras.first,
      );

      final controller = CameraController(
        camera,
        ResolutionPreset.high,
        enableAudio: false,
        imageFormatGroup: ImageFormatGroup.jpeg,
      );

      await controller.initialize();
      await controller.setFlashMode(FlashMode.off);

      if (!mounted) {
        await controller.dispose();
        return;
      }

      setState(() => _controller = controller);
    } on CameraException catch (error) {
      if (mounted) {
        setState(() => _errorMessage = error.description ?? error.code);
      }
    } catch (error) {
      if (mounted) {
        setState(() => _errorMessage = error.toString());
      }
    }
  }

  Future<void> _takePicture() async {
    final controller = _controller;
    if (controller == null || !controller.value.isInitialized || _capturing) {
      return;
    }

    setState(() => _capturing = true);
    try {
      final image = await controller.takePicture();
      if (mounted) {
        Navigator.of(context).pop(image.path);
      }
    } on CameraException catch (error) {
      if (mounted) {
        setState(() {
          _capturing = false;
          _errorMessage = error.description ?? error.code;
        });
      }
    } catch (error) {
      if (mounted) {
        setState(() {
          _capturing = false;
          _errorMessage = error.toString();
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: FutureBuilder<void>(
        future: _initializeCameraFuture,
        builder: (context, snapshot) {
          if (_errorMessage != null) {
            return _CameraErrorView(
              message: _errorMessage!,
              onClose: () => Navigator.of(context).pop(),
            );
          }

          final controller = _controller;
          if (snapshot.connectionState != ConnectionState.done ||
              controller == null ||
              !controller.value.isInitialized) {
            return const Center(child: CircularProgressIndicator());
          }

          return Stack(
            fit: StackFit.expand,
            children: [
              _CameraPreview(controller: controller),
              const _IdCardGuideOverlay(),
              SafeArea(
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(18, 10, 18, 22),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          IconButton(
                            onPressed: () => Navigator.of(context).pop(),
                            icon: const Icon(Icons.close),
                            color: Colors.white,
                            tooltip: '닫기',
                          ),
                          const Spacer(),
                        ],
                      ),
                      const Spacer(),
                      const Text(
                        '신분증을 가이드 안에 맞춰 촬영해 주세요.',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                          shadows: [
                            Shadow(
                              blurRadius: 8,
                              color: Colors.black54,
                              offset: Offset(0, 1),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 18),
                      _CaptureButton(
                        capturing: _capturing,
                        onPressed: _takePicture,
                      ),
                    ],
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

class _CameraPreview extends StatelessWidget {
  const _CameraPreview({required this.controller});

  final CameraController controller;

  @override
  Widget build(BuildContext context) {
    final previewSize = controller.value.previewSize;
    if (previewSize == null) {
      return CameraPreview(controller);
    }

    return ClipRect(
      child: FittedBox(
        fit: BoxFit.cover,
        child: SizedBox(
          width: previewSize.height,
          height: previewSize.width,
          child: CameraPreview(controller),
        ),
      ),
    );
  }
}

class _IdCardGuideOverlay extends StatelessWidget {
  const _IdCardGuideOverlay();

  @override
  Widget build(BuildContext context) {
    return IgnorePointer(
      child: CustomPaint(
        painter: _IdCardGuidePainter(),
        child: const SizedBox.expand(),
      ),
    );
  }
}

class _IdCardGuidePainter extends CustomPainter {
  static const double _cardAspectRatio = 85.6 / 53.98;
  static const double _cornerLength = 34;
  static const double _cornerStroke = 5;

  @override
  void paint(Canvas canvas, Size size) {
    final horizontalPadding = size.width * 0.08;
    final guideWidth = (size.width - horizontalPadding * 2).clamp(260.0, 420.0);
    final guideHeight = guideWidth / _cardAspectRatio;
    final center = Offset(size.width / 2, size.height * 0.45);
    final guideRect = Rect.fromCenter(
      center: center,
      width: guideWidth,
      height: guideHeight,
    );
    final guideRRect = RRect.fromRectAndRadius(
      guideRect,
      const Radius.circular(18),
    );

    final overlayPath = Path()
      ..addRect(Offset.zero & size)
      ..addRRect(guideRRect)
      ..fillType = PathFillType.evenOdd;

    canvas.drawPath(
      overlayPath,
      Paint()..color = Colors.black.withValues(alpha: 0.52),
    );

    canvas.drawRRect(
      guideRRect,
      Paint()
        ..color = Colors.white.withValues(alpha: 0.94)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.6,
    );

    final cornerPaint = Paint()
      ..color = AppColors.primaryRed
      ..style = PaintingStyle.stroke
      ..strokeWidth = _cornerStroke
      ..strokeCap = StrokeCap.round;

    _drawCorners(canvas, guideRect, cornerPaint);
  }

  void _drawCorners(Canvas canvas, Rect rect, Paint paint) {
    canvas.drawLine(
      rect.topLeft,
      rect.topLeft + const Offset(_cornerLength, 0),
      paint,
    );
    canvas.drawLine(
      rect.topLeft,
      rect.topLeft + const Offset(0, _cornerLength),
      paint,
    );

    canvas.drawLine(
      rect.topRight,
      rect.topRight - const Offset(_cornerLength, 0),
      paint,
    );
    canvas.drawLine(
      rect.topRight,
      rect.topRight + const Offset(0, _cornerLength),
      paint,
    );

    canvas.drawLine(
      rect.bottomLeft,
      rect.bottomLeft + const Offset(_cornerLength, 0),
      paint,
    );
    canvas.drawLine(
      rect.bottomLeft,
      rect.bottomLeft - const Offset(0, _cornerLength),
      paint,
    );

    canvas.drawLine(
      rect.bottomRight,
      rect.bottomRight - const Offset(_cornerLength, 0),
      paint,
    );
    canvas.drawLine(
      rect.bottomRight,
      rect.bottomRight - const Offset(0, _cornerLength),
      paint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _CaptureButton extends StatelessWidget {
  const _CaptureButton({required this.capturing, required this.onPressed});

  final bool capturing;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return SizedBox.square(
      dimension: 74,
      child: DecoratedBox(
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          border: Border.all(color: Colors.white, width: 4),
        ),
        child: Padding(
          padding: const EdgeInsets.all(7),
          child: ElevatedButton(
            onPressed: capturing ? null : onPressed,
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.white,
              disabledBackgroundColor: Colors.white70,
              shape: const CircleBorder(),
              padding: EdgeInsets.zero,
              elevation: 0,
            ),
            child: capturing
                ? const SizedBox.square(
                    dimension: 24,
                    child: CircularProgressIndicator(strokeWidth: 3),
                  )
                : const SizedBox.shrink(),
          ),
        ),
      ),
    );
  }
}

class _CameraErrorView extends StatelessWidget {
  const _CameraErrorView({required this.message, required this.onClose});

  final String message;
  final VoidCallback onClose;

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Icon(
              Icons.no_photography_outlined,
              color: Colors.white,
              size: 52,
            ),
            const SizedBox(height: 16),
            const Text(
              '카메라를 열 수 없습니다.',
              textAlign: TextAlign.center,
              style: TextStyle(
                color: Colors.white,
                fontSize: 18,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              message,
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.white70, height: 1.45),
            ),
            const SizedBox(height: 22),
            ElevatedButton(
              onPressed: onClose,
              style: ElevatedButton.styleFrom(
                backgroundColor: AppColors.primaryRed,
                foregroundColor: Colors.white,
              ),
              child: const Text('확인'),
            ),
          ],
        ),
      ),
    );
  }
}
