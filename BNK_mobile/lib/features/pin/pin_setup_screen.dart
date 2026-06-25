import 'dart:math';

import 'package:flutter/material.dart';

import '../../core/constants/app_colors.dart';
import '../../core/storage/secure_storage.dart';
import '../home/home_screen.dart';

class PinSetupScreen extends StatefulWidget {
  final String memberName;

  const PinSetupScreen({
    super.key,
    required this.memberName,
  });

  @override
  State<PinSetupScreen> createState() => _PinSetupScreenState();
}

class _PinSetupScreenState extends State<PinSetupScreen> {
  String _firstPin = '';
  String _currentPin = '';
  bool _isConfirmStep = false;

  List<String> _keypadNumbers = [];

  @override
  void initState() {
    super.initState();
    _shuffleKeypad();
  }

  void _shuffleKeypad() {
    final numbers = List<String>.generate(10, (index) => index.toString());
    numbers.shuffle(Random());

    setState(() {
      _keypadNumbers = numbers;
    });
  }

  void _onNumberTap(String number) {
    if (_currentPin.length >= 6) return;

    setState(() {
      _currentPin += number;
    });

    if (_currentPin.length == 6) {
      Future.delayed(const Duration(milliseconds: 250), () {
        if (!mounted) return;
        _handlePinComplete();
      });
    }
  }

  void _onDeleteTap() {
    if (_currentPin.isEmpty) return;

    setState(() {
      _currentPin = _currentPin.substring(0, _currentPin.length - 1);
    });
  }

  Future<void> _handlePinComplete() async {
    if (!_isConfirmStep) {
      setState(() {
        _firstPin = _currentPin;
        _currentPin = '';
        _isConfirmStep = true;
      });

      _shuffleKeypad();
      return;
    }

    if (_firstPin == _currentPin) {
      await SecureStorage.savePin(_currentPin);

      if (!mounted) return;

      Navigator.pushReplacement(
        context,
        MaterialPageRoute(
          builder: (_) => HomeScreen(
            memberName: widget.memberName,
          ),
        ),
      );
    } else {
      setState(() {
        _firstPin = '';
        _currentPin = '';
        _isConfirmStep = false;
      });

      _shuffleKeypad();

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('간편비밀번호가 일치하지 않습니다. 다시 등록해주세요.'),
          behavior: SnackBarBehavior.floating,
        ),
      );
    }
  }

  Widget _buildPinDots() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: List.generate(6, (index) {
        final bool filled = index < _currentPin.length;

        return Container(
          width: 15,
          height: 15,
          margin: const EdgeInsets.symmetric(horizontal: 6),
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: filled ? AppColors.textPrimary : Colors.transparent,
            border: Border.all(
              color: AppColors.textPrimary,
              width: 2,
            ),
          ),
        );
      }),
    );
  }

  Widget _buildNumberButton(String text) {
    return SizedBox(
      width: 72,
      height: 56,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.white,
          foregroundColor: AppColors.textPrimary,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(18),
            side: const BorderSide(
              color: AppColors.border,
            ),
          ),
        ),
        onPressed: () => _onNumberTap(text),
        child: Text(
          text,
          style: const TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.w800,
          ),
        ),
      ),
    );
  }

  Widget _buildTextButton({
    required String text,
    required VoidCallback onPressed,
  }) {
    return SizedBox(
      width: 72,
      height: 56,
      child: TextButton(
        onPressed: onPressed,
        child: Text(
          text,
          style: const TextStyle(
            color: AppColors.textSecondary,
            fontWeight: FontWeight.w700,
          ),
        ),
      ),
    );
  }

  Widget _buildKeypad() {
    if (_keypadNumbers.length < 10) {
      return const SizedBox.shrink();
    }

    final row1 = _keypadNumbers.sublist(0, 3);
    final row2 = _keypadNumbers.sublist(3, 6);
    final row3 = _keypadNumbers.sublist(6, 9);
    final lastNumber = _keypadNumbers[9];

    return Column(
      children: [
        _buildKeypadRow(row1),
        const SizedBox(height: 12),
        _buildKeypadRow(row2),
        const SizedBox(height: 12),
        _buildKeypadRow(row3),
        const SizedBox(height: 12),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            _buildTextButton(
              text: '삭제',
              onPressed: _onDeleteTap,
            ),
            _buildNumberButton(lastNumber),
            _buildTextButton(
              text: '재배열',
              onPressed: _shuffleKeypad,
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildKeypadRow(List<String> numbers) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: numbers.map(_buildNumberButton).toList(),
    );
  }

  @override
  Widget build(BuildContext context) {
    final title = _isConfirmStep
        ? '간편비밀번호를 다시 입력하세요'
        : '간편비밀번호를 등록하세요';

    final subtitle = _isConfirmStep
        ? '확인을 위해 한 번 더 입력해주세요'
        : '앞으로 앱 로그인 시 사용할 6자리 비밀번호';

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: SingleChildScrollView(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 18),
            child: Column(
              children: [
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 20,
                    vertical: 20,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.cardBackground,
                    borderRadius: BorderRadius.circular(28),
                  ),
                  child: const Row(
                    children: [
                      Text(
                        'BNK',
                        style: TextStyle(
                          fontSize: 26,
                          fontWeight: FontWeight.w800,
                          color: AppColors.primaryRed,
                        ),
                      ),
                      SizedBox(width: 18),
                      Text(
                        '간편비밀번호 등록',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                          color: AppColors.textPrimary,
                        ),
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 28),

                Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    title,
                    style: const TextStyle(
                      fontSize: 21,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ),

                const SizedBox(height: 10),

                Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    subtitle,
                    style: const TextStyle(
                      fontSize: 14,
                      color: AppColors.textSecondary,
                    ),
                  ),
                ),

                const SizedBox(height: 34),

                _buildPinDots(),

                const SizedBox(height: 34),

                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 20,
                    vertical: 16,
                  ),
                  decoration: BoxDecoration(
                    color: AppColors.white,
                    borderRadius: BorderRadius.circular(22),
                    border: Border.all(color: AppColors.border),
                  ),
                  child: Text(
                    _isConfirmStep
                        ? '등록 단계\n2차 입력 → 일치 여부 확인'
                        : '등록 단계\n1차 입력 → 2차 재입력 확인',
                    style: const TextStyle(
                      fontSize: 14,
                      height: 1.6,
                      fontWeight: FontWeight.w600,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ),

                const SizedBox(height: 24),

                _buildKeypad(),

                const SizedBox(height: 24),

                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 18,
                    vertical: 16,
                  ),
                  decoration: BoxDecoration(
                    color: const Color(0xFFFFF8EE),
                    borderRadius: BorderRadius.circular(18),
                    border: Border.all(
                      color: const Color(0xFFFFB75E),
                    ),
                  ),
                  child: const Text(
                    '얼굴인식 등록 안내\n얼굴인식은 별도 인증 파트와 연동 예정',
                    style: TextStyle(
                      fontSize: 13,
                      height: 1.5,
                      color: Color(0xFF8A5A13),
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),

                const SizedBox(height: 18),
              ],
            ),
          ),
        ),
      ),
    );
  }
}