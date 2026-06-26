import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';

import '../../core/constants/app_colors.dart';
import '../../data/services/account_opening_api.dart';

class AccountOpeningScreen extends StatefulWidget {
  const AccountOpeningScreen({super.key});

  @override
  State<AccountOpeningScreen> createState() => _AccountOpeningScreenState();
}

class _AccountOpeningScreenState extends State<AccountOpeningScreen> {
  final AccountOpeningApi _api = AccountOpeningApi();
  final ImagePicker _picker = ImagePicker();
  final TextEditingController _frontAnswerController = TextEditingController();
  final TextEditingController _backAnswerController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();

  Map<String, dynamic> _status = <String, dynamic>{};
  Map<String, dynamic> _ocr = <String, dynamic>{};
  Map<String, dynamic>? _challenge;
  bool _loading = true;
  bool _completed = false;
  String _purpose = 'ETC';

  static const List<Map<String, String>> _purposes = [
    {'label': '급여 수령', 'code': 'SALARY'},
    {'label': '아르바이트 급여', 'code': 'PART_TIME_SALARY'},
    {'label': '연금 수령', 'code': 'PENSION'},
    {'label': '사업용 계좌', 'code': 'BUSINESS'},
    {'label': '모임 계좌', 'code': 'GROUP'},
    {'label': '공과금 납부', 'code': 'UTILITY_PAYMENT'},
    {'label': '생활비 관리', 'code': 'LIVING_EXPENSE'},
    {'label': '기타', 'code': 'ETC'},
  ];

  @override
  void initState() {
    super.initState();
    _loadStatus();
  }

  @override
  void dispose() {
    _frontAnswerController.dispose();
    _backAnswerController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _loadStatus() async {
    await _run(() async {
      final status = await _api.status();
      _applyStatus(status);
    });
  }

  Future<void> _run(Future<void> Function() action) async {
    if (mounted) setState(() => _loading = true);
    try {
      await action();
    } catch (error) {
      _showMessage(error.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _applyStatus(Map<String, dynamic> status) {
    setState(() {
      _status = status;
      _purpose = (status['purpose'] ?? _purpose).toString();
    });
  }

  Future<void> _pickAndUpload({
    required ImageSource source,
    required Future<Map<String, dynamic>> Function(String path) upload,
  }) async {
    final image = await _picker.pickImage(
      source: source,
      imageQuality: 82,
      maxWidth: 1600,
    );

    if (image == null) return;

    await _run(() async {
      _applyStatus(await upload(image.path));
    });
  }

  Future<void> _loadOcr() async {
    await _run(() async {
      final data = await _api.getOcr();
      final ocr = data['ocr'];
      setState(() {
        _ocr = ocr is Map<String, dynamic> ? Map<String, dynamic>.from(ocr) : {};
      });
    });
  }

  Future<void> _confirmOcr() async {
    await _run(() async {
      _applyStatus(await _api.updateOcr(_ocr));
    });
  }

  Future<void> _createSecurityChallenge() async {
    await _run(() async {
      final challenge = await _api.securityChallenge();
      setState(() => _challenge = challenge);
    });
  }

  Future<void> _verifySecurityCard() async {
    await _run(() async {
      _applyStatus(await _api.verifySecurityCard(
        frontAnswer: _frontAnswerController.text,
        backAnswer: _backAnswerController.text,
      ));
    });
  }

  Future<void> _openAccount() async {
    await _run(() async {
      await _api.open();
      _completed = true;
      if (mounted) {
        Navigator.of(context).pop(true);
      }
    });
  }

  Future<void> _cancelOpening() async {
    await _run(() async {
      await _api.cancel();
      if (mounted) Navigator.of(context).pop(false);
    });
  }

  void _showMessage(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), behavior: SnackBarBehavior.floating),
    );
  }

  String get _nextStep => (_status['nextStep'] ?? 'PRIVACY_CONSENT').toString();

  @override
  Widget build(BuildContext context) {
    return PopScope(
      onPopInvokedWithResult: (didPop, result) {
        if (didPop && !_completed) {
          _api.cancel();
        }
      },
      child: Scaffold(
        backgroundColor: AppColors.background,
        appBar: AppBar(
          title: const Text('입출금 계좌 개설'),
          backgroundColor: AppColors.background,
          foregroundColor: AppColors.textPrimary,
          actions: [
            TextButton(
              onPressed: _loading ? null : _cancelOpening,
              child: const Text('취소'),
            ),
          ],
        ),
        body: Stack(
          children: [
            ListView(
              padding: const EdgeInsets.fromLTRB(18, 12, 18, 28),
              children: [
                _progressHeader(),
                const SizedBox(height: 14),
                _buildCurrentStep(),
              ],
            ),
            if (_loading)
              Container(
                color: Colors.black.withValues(alpha: 0.08),
                child: const Center(child: CircularProgressIndicator()),
              ),
          ],
        ),
      ),
    );
  }

  Widget _progressHeader() {
    final ready = _status['readyToOpen'] == true;
    return _panel(
      title: ready ? '모든 인증이 완료되었습니다' : '계좌 개설 인증 진행',
      child: Text(
        ready ? '마지막으로 계좌 개설을 완료해 주세요.' : _stepDescription(_nextStep),
        style: const TextStyle(
          color: AppColors.textSecondary,
          fontWeight: FontWeight.w700,
          height: 1.45,
        ),
      ),
    );
  }

  Widget _buildCurrentStep() {
    switch (_nextStep) {
      case 'PRIVACY_CONSENT':
        return _privacyConsentStep();
      case 'ID_CARD':
        return _imageUploadStep(
          title: '신분증 촬영',
          message: '주민등록증 또는 운전면허증을 화면에 맞춰 촬영해 주세요.',
          onCamera: () => _pickAndUpload(
            source: ImageSource.camera,
            upload: _api.uploadIdCard,
          ),
          onGallery: () => _pickAndUpload(
            source: ImageSource.gallery,
            upload: _api.uploadIdCard,
          ),
        );
      case 'OCR_WAIT':
      case 'OCR_CONFIRM':
        return _ocrStep();
      case 'FACE':
        return _imageUploadStep(
          title: '얼굴 촬영',
          message: '신분증 사진과 비교할 얼굴 사진을 촬영해 주세요.',
          onCamera: () => _pickAndUpload(
            source: ImageSource.camera,
            upload: _api.uploadFace,
          ),
          onGallery: () => _pickAndUpload(
            source: ImageSource.gallery,
            upload: _api.uploadFace,
          ),
        );
      case 'SECURITY_CARD':
        return _securityCardStep();
      case 'ACCOUNT_CONSENT':
        return _simpleAgreeStep(
          title: '계좌 개설 약관 동의',
          message: '입출금 계좌 개설 약관을 확인했고 필수 항목에 동의합니다.',
          label: '약관 동의',
          onPressed: () => _run(() async {
            _applyStatus(await _api.agreeAccountTerms());
          }),
        );
      case 'PASSWORD':
        return _passwordStep();
      case 'PURPOSE':
        return _purposeStep();
      case 'READY_TO_OPEN':
        return _readyStep();
      default:
        return _privacyConsentStep();
    }
  }

  Widget _privacyConsentStep() {
    return _simpleAgreeStep(
      title: '개인정보 수집·이용 동의',
      message: '실명인증과 계좌 개설을 위해 신분증, 얼굴 이미지, OCR 결과를 암호화하여 처리합니다.',
      label: '동의하고 시작하기',
      onPressed: () => _run(() async {
        _applyStatus(await _api.agreePrivacy());
      }),
    );
  }

  Widget _ocrStep() {
    return _panel(
      title: 'OCR 결과 확인',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '신분증에서 인식한 정보를 확인하고 필요한 경우 수정해 주세요.',
            style: TextStyle(color: AppColors.textSecondary, height: 1.45),
          ),
          const SizedBox(height: 12),
          if (_ocr.isEmpty)
            _secondaryButton(label: 'OCR 결과 불러오기', onPressed: _loadOcr)
          else ...[
            ..._ocr.entries.map((entry) => Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: TextFormField(
                    initialValue: entry.value?.toString() ?? '',
                    decoration: _inputDecoration(entry.key),
                    onChanged: (value) => _ocr[entry.key] = value,
                  ),
                )),
            _primaryButton(label: '확인 완료', onPressed: _confirmOcr),
          ],
        ],
      ),
    );
  }

  Widget _securityCardStep() {
    return _panel(
      title: '보안카드 인증',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            (_challenge?['message'] ?? '보안카드 인증번호를 요청해 주세요.').toString(),
            style: const TextStyle(color: AppColors.textSecondary, height: 1.45),
          ),
          const SizedBox(height: 12),
          if (_challenge == null)
            _secondaryButton(label: '인증번호 요청', onPressed: _createSecurityChallenge)
          else ...[
            TextField(
              controller: _frontAnswerController,
              keyboardType: TextInputType.number,
              maxLength: 2,
              decoration: _inputDecoration('앞 번호'),
            ),
            TextField(
              controller: _backAnswerController,
              keyboardType: TextInputType.number,
              maxLength: 2,
              decoration: _inputDecoration('뒤 번호'),
            ),
            _primaryButton(label: '보안카드 인증', onPressed: _verifySecurityCard),
          ],
        ],
      ),
    );
  }

  Widget _passwordStep() {
    return _panel(
      title: '계좌 비밀번호 설정',
      child: Column(
        children: [
          TextField(
            controller: _passwordController,
            keyboardType: TextInputType.number,
            obscureText: true,
            maxLength: 6,
            decoration: _inputDecoration('숫자 6자리'),
          ),
          _primaryButton(
            label: '비밀번호 저장',
            onPressed: () => _run(() async {
              _applyStatus(await _api.savePassword(_passwordController.text));
            }),
          ),
        ],
      ),
    );
  }

  Widget _purposeStep() {
    return _panel(
      title: '계좌 개설 목적',
      child: Column(
        children: [
          DropdownButtonFormField<String>(
            initialValue: _purpose,
            items: _purposes
                .map((purpose) => DropdownMenuItem(
                      value: purpose['code'],
                      child: Text(purpose['label']!),
                    ))
                .toList(),
            onChanged: (value) => setState(() => _purpose = value ?? 'ETC'),
            decoration: _inputDecoration('사용 목적'),
          ),
          const SizedBox(height: 14),
          _primaryButton(
            label: '목적 저장',
            onPressed: () => _run(() async {
              _applyStatus(await _api.savePurpose(_purpose));
            }),
          ),
        ],
      ),
    );
  }

  Widget _readyStep() {
    return _panel(
      title: '최종 계좌 생성',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '모든 인증이 완료되었습니다. 계좌를 생성하면 회원 상태가 정회원으로 전환됩니다.',
            style: TextStyle(color: AppColors.textSecondary, height: 1.45),
          ),
          const SizedBox(height: 14),
          _primaryButton(label: '계좌 개설 완료', onPressed: _openAccount),
        ],
      ),
    );
  }

  Widget _imageUploadStep({
    required String title,
    required String message,
    required VoidCallback onCamera,
    required VoidCallback onGallery,
  }) {
    return _panel(
      title: title,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            message,
            style: const TextStyle(color: AppColors.textSecondary, height: 1.45),
          ),
          const SizedBox(height: 14),
          _primaryButton(label: '카메라로 촬영', onPressed: onCamera),
          const SizedBox(height: 10),
          _secondaryButton(label: '앨범에서 선택', onPressed: onGallery),
        ],
      ),
    );
  }

  Widget _simpleAgreeStep({
    required String title,
    required String message,
    required String label,
    required VoidCallback onPressed,
  }) {
    return _panel(
      title: title,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            message,
            style: const TextStyle(color: AppColors.textSecondary, height: 1.45),
          ),
          const SizedBox(height: 14),
          _primaryButton(label: label, onPressed: onPressed),
        ],
      ),
    );
  }

  Widget _panel({required String title, required Widget child}) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.cardBackground,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w900,
              color: AppColors.textPrimary,
            ),
          ),
          const SizedBox(height: 14),
          child,
        ],
      ),
    );
  }

  Widget _primaryButton({required String label, required VoidCallback onPressed}) {
    return SizedBox(
      width: double.infinity,
      height: 48,
      child: ElevatedButton(
        onPressed: _loading ? null : onPressed,
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primaryRed,
          foregroundColor: AppColors.white,
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
        child: Text(label, style: const TextStyle(fontWeight: FontWeight.w900)),
      ),
    );
  }

  Widget _secondaryButton({required String label, required VoidCallback onPressed}) {
    return SizedBox(
      width: double.infinity,
      height: 48,
      child: OutlinedButton(
        onPressed: _loading ? null : onPressed,
        child: Text(label, style: const TextStyle(fontWeight: FontWeight.w900)),
      ),
    );
  }

  InputDecoration _inputDecoration(String label) {
    return InputDecoration(
      labelText: label,
      filled: true,
      fillColor: AppColors.inputBackground,
      border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
    );
  }

  String _stepDescription(String step) {
    switch (step) {
      case 'ID_CARD':
        return '신분증 촬영 단계입니다.';
      case 'OCR_CONFIRM':
      case 'OCR_WAIT':
        return 'OCR 결과를 확인해 주세요.';
      case 'FACE':
        return '얼굴 인증 단계입니다.';
      case 'SECURITY_CARD':
        return '보안카드 인증 단계입니다.';
      case 'ACCOUNT_CONSENT':
        return '계좌 개설 약관 동의 단계입니다.';
      case 'PASSWORD':
        return '계좌 비밀번호 설정 단계입니다.';
      case 'PURPOSE':
        return '계좌 개설 목적 선택 단계입니다.';
      case 'READY_TO_OPEN':
        return '최종 계좌 생성만 남았습니다.';
      default:
        return '개인정보 수집·이용 동의가 필요합니다.';
    }
  }
}
