import 'package:fearx_board/FearX_Page.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FearXMoreMenu extends StatefulWidget {
  const FearXMoreMenu({super.key});

  @override
  State<FearXMoreMenu> createState() => _FearXMoreMenuState();
}

class _FearXMoreMenuState extends State<FearXMoreMenu> {
  bool _isMember = false;
  String _nickname = "비회원";

  @override
  void initState() {
    super.initState();
    _loadUserInfo();
  }

  // 로컬 세션 정보 로드 및 화면 갱신
  Future<void> _loadUserInfo() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    setState(() {
      _isMember = prefs.getBool('has_community_account') ?? false;
      _nickname = prefs.getString('user_nickname') ?? "비회원";
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.only(topLeft: Radius.circular(24), topRight: Radius.circular(24)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Center(
            child: Container(
              width: 40,
              height: 4,
              margin: const EdgeInsets.only(bottom: 20),
              decoration: BoxDecoration(
                color: Colors.grey[300],
                borderRadius: BorderRadius.circular(10),
              ),
            ),
          ),

          // 사용자 정보 헤더 구역
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(color: Colors.grey[100], borderRadius: BorderRadius.circular(16)),
            child: Row(
              children: [
                CircleAvatar(
                  backgroundColor: _isMember ? const Color(0xFFE8282F) : Colors.grey,
                  child: const Icon(Icons.person, color: Colors.white),
                ),
                const SizedBox(width: 16),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(_nickname, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 4),
                    Text(_isMember ? "BNK FearX 커뮤니티 회원" : "구경 중인 비회원", style: TextStyle(fontSize: 13, color: Colors.grey[600])),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // 회원 상태에 따른 조건부 가입/수정 메뉴
          if (!_isMember) ...[
            _buildMenuItem(Icons.app_registration, "커뮤니티 회원가입", Colors.blue, () async {
              // 가입 창으로 이동 후 결과 수신 대기
              final bool? isRegistered = await Navigator.push<bool>(
                context,
                MaterialPageRoute(builder: (context) => const CommunityJoinScreen()),
              );

              // 가입 완료 신호(true)를 받으면 UI 즉시 새로고침
              if (isRegistered == true) {
                _loadUserInfo();
              }
            }),
          ] else ...[
            _buildMenuItem(Icons.edit, "내 닉네임 수정", Colors.black, () async {
              print("닉네임 수정 창 이동 예정 (오라클 UPDATE 문 실행 타겟)");
            }),
          ],

          const Divider(height: 30),
          _buildMenuItem(Icons.help_outline, "고객센터 / 1:1 문의", Colors.black, () {}),
          _buildMenuItem(Icons.info_outline, "서비스 이용약관", Colors.black, () {}),
          const SizedBox(height: 10),
        ],
      ),
    );
  }

  Widget _buildMenuItem(IconData icon, String title, Color color, VoidCallback onTap) {
    return ListTile(
      leading: Icon(icon, color: color),
      title: Text(title, style: TextStyle(fontWeight: FontWeight.w500, color: color)),
      trailing: const Icon(Icons.arrow_forward_ios, size: 14),
      onTap: onTap,
    );
  }
}