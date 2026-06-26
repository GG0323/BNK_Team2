import 'dart:async';
import 'dart:convert';
import 'package:fearx_board/FearX_community.dart';
import 'package:fearx_board/FearX_home.dart';
import 'package:fearx_board/FearX_match.dart';
import 'package:fearx_board/FearX_moreMenu.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:http/http.dart' as http;

// 1. 로고를 1~2초간 띄워줄 화면
class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

// FearX_AllLink.dart의 _SplashScreenState 내부 수정
class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    // 화면이 켜지자마자 배경에서 조용히 서버와 통신을 시작합니다.
    _checkLoginAndNavigate();
  }

  // 💡 비동기(async)로 로그인 체크와 화면 이동을 함께 처리합니다.
  Future<void> _checkLoginAndNavigate() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance(); //[cite: 4]

    // 테스트용 회원 고유번호 저장[cite: 4]
    int member_no = 82; //[cite: 4]
    await prefs.setInt('bank_member', member_no); //[cite: 4]

    final String checkLoginUrl = "http://192.168.0.184:8080/api/community/check-login/$member_no"; //[cite: 4]

    try {
      // 🚀 서버에 요청을 보내되, 최대 3초까지만 기다리도록 타임아웃 설정
      final response = await http.get(Uri.parse(checkLoginUrl))
          .timeout(const Duration(seconds: 3)); //[cite: 4]

      if (response.statusCode == 200) { //[cite: 4]
        final Map<String, dynamic> result = jsonDecode(response.body); //[cite: 4]
        bool isMember = result['isMember'] ?? false; //[cite: 4]

        if (isMember) { //[cite: 4]
          String serverNickname = result['nickname']; //[cite: 4]
          await prefs.setBool('has_community_account', true); //[cite: 4]
          await prefs.setString('user_nickname', serverNickname); //[cite: 4]
          print("🎉 자동 로그인 성공: $serverNickname"); //[cite: 4]
        } else {
          await prefs.setBool('has_community_account', false); //[cite: 4]
          await prefs.remove('user_nickname'); //[cite: 4]
          print("👀 미가입 유저 -> 비회원 진입"); //[cite: 4]
        }
      } else {
        await prefs.setBool('has_community_account', false); //[cite: 4]
      }
    } catch (e) {
      // 인터넷이 안 되거나 자바 서버가 꺼져있을 때 튕기지 않고 로컬 데이터로 처리[cite: 4]
      print("🌐 서버 연결 실패 ($e) -> 로컬 데이터로 판별"); //[cite: 4]
      String? localNickname = prefs.getString('user_nickname'); //[cite: 4]
      await prefs.setBool('has_community_account', localNickname != null); //[cite: 4]
    }

    // 모든 네트워크 판별이 끝나면 메인 화면으로 안전하게 이동합니다![cite: 1, 4]
    if (!mounted) return;
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (context) => const BnkFearXPage()), //
    );
  }

  // 2초간 띄어주는 화면의 구성
  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      backgroundColor: Color(0xFFE8282F),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image(
              image: AssetImage(
                  'assets/FearX_logo.png'
              ),
              width: 300,
              height: 300,
            ),
            SizedBox(height: 20),
            Text(
              'FearX',
              style: TextStyle(
                color: Colors.white,
                fontSize: 28,
                fontWeight: FontWeight.bold,
                letterSpacing: 1.2,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// 메인 페이지 (모든 페이지를 이동하는게 아니라 화면에 띄움)
class BnkFearXPage extends StatefulWidget {
  const BnkFearXPage({super.key});

  @override
  State<BnkFearXPage> createState() => _BnkFearXPageState();
}

class _BnkFearXPageState extends State<BnkFearXPage> {
  // 어떤 화면이 선택되었는지 저장하는 변수
  int _selectedIndex = 0;

  // ❌ 기존에 여기에 있던 final List<Widget> _pages 리스트는 지우거나 주석 처리합니다!

  @override
  Widget build(BuildContext context) {
    // 💡 [수정 포인트] build 함수가 실행될 때 실시간으로 페이지 리스트를 생성합니다.
    // 이렇게 해야 FearXHomeContent에게 탭을 바꿀 수 있는 리모컨(함수)을 넘겨줄 수 있어요!
    final List<Widget> pages = [
      FearXHomeContent(
        onTabChanged: (index) {
          setState(() {
            _selectedIndex = index; // 홈에서 호출하면 여기 부모의 탭 번호가 바뀝니다.
          });
        },
      ),
      const FearXMatchContent(),
      const FearXComuContent(),
      const FearXMoreMenu(),
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        title: Text.rich(
          TextSpan(
            children: [
              const TextSpan(
                text: 'BNK ',
                style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold, fontSize: 20),
              ),
              const TextSpan(
                text: 'FearX',
                style: TextStyle(color: Colors.black, fontWeight: FontWeight.bold, fontSize: 20),
              ),
            ],
          ),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.close, color: Colors.black),
            onPressed: () {},
          ),
        ],
      ),
      // 💡 [수정 포인트] 기존의 _pages 대신 새로 정의한 pages[_selectedIndex]를 사용합니다.
      body: pages[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _selectedIndex,

        onTap: (index) {
          if (index == 3) {
            _showMoreMenu(context);
          } else {
            setState(() {
              _selectedIndex = index;
            });
          }
        },

        type: BottomNavigationBarType.fixed,
        selectedItemColor: Colors.red,
        items: const [
          BottomNavigationBarItem(icon: Icon(Icons.home), label: '홈'),
          BottomNavigationBarItem(icon: Icon(Icons.calendar_today), label: '경기'),
          BottomNavigationBarItem(icon: Icon(Icons.chat_bubble), label: '커뮤니티'),
          BottomNavigationBarItem(icon: Icon(Icons.more_horiz), label: '더보기'),
        ],
      ),
    );
  }

  void _showMoreMenu(BuildContext context) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (context) {
        return const FearXMoreMenu();
      },
    );
  }
}