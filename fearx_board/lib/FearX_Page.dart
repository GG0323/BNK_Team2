import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class CommunityJoinScreen extends StatefulWidget {
  const CommunityJoinScreen({super.key});

  @override
  State<CommunityJoinScreen> createState() => _CommunityJoinScreenState();
}
// 회원가입
class _CommunityJoinScreenState extends State<CommunityJoinScreen> {
  final TextEditingController _nicknameController = TextEditingController();
  bool _isLoading = false;
  final String _serverUrl = "http://192.168.0.184:8080/api/community/register";

  Future<void> _registerNicknameWithDB() async {
    final String nickname = _nicknameController.text.trim();

    if (nickname.isEmpty) {
      _showSnackBar('사용할 닉네임을 입력해 주세요.');
      return;
    }

    setState(() { _isLoading = true; });

    try {
      final SharedPreferences prefs = await SharedPreferences.getInstance();
      int memberNo = prefs.getInt("bank_member") ?? 0;

      if (memberNo == 0) {
        _showSnackBar('부산은행 계정 인증 정보가 없습니다. 앱을 다시 실행해 주세요.');
        return;
      }

      final Map<String, dynamic> requestBody = {
        'member_no': memberNo,
        'nickname': nickname,
        'account_role': 'MEMBER',
        'community_status': 'ACTIVE',
      };

      final response = await http.post(
        Uri.parse(_serverUrl),
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(requestBody),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        await prefs.setString('user_nickname', nickname);
        await prefs.setBool('has_community_account', true);
        await prefs.setInt('bank_member_no', memberNo);

        if (!mounted) return;
        _showSnackBar('커뮤니티 회원 등록이 완료되었습니다!');
        Navigator.pop(context, true);
      } else {
        String errorMsg = "회원 등록에 실패했습니다. (코드: ${response.statusCode})";
        try {
          final responseData = jsonDecode(response.body);
          if (responseData['message'] != null) {
            errorMsg = responseData['message'];
          }
        } catch (_) {}
        _showSnackBar(errorMsg);
      }
    } catch (e) {
      _showSnackBar('서버와 연결할 수 없습니다. 백엔드 구동 상태를 확인해 주세요.');
      print("네트워크 에러 로그: $e");
    } finally {
      if (mounted) setState(() { _isLoading = false; });
    }
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  void dispose() {
    _nicknameController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text('커뮤니티 가입', style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.black),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text("닉네임 등록", style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
            const SizedBox(height: 10),
            Text(
              "부산은행 회원 계정 연동 상태입니다.\n커뮤니티 게시판에서 사용할 닉네임을 설정해 주세요.",
              style: TextStyle(color: Colors.grey[600], height: 1.4),
            ),
            const SizedBox(height: 30),

            TextField(
              controller: _nicknameController,
              maxLength: 30,
              decoration: InputDecoration(
                labelText: '커뮤니티 닉네임',
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              ),
            ),
            const SizedBox(height: 24),

            ElevatedButton(
              onPressed: _isLoading ? null : _registerNicknameWithDB,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFE8282F),
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              child: _isLoading
                  ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                  : const Text('회원가입하기', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
          ],
        ),
      ),
    );
  }
}