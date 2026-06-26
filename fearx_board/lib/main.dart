import 'dart:convert';
import 'package:fearx_board/FearX_AllLink.dart'; //[cite: 4]
import 'package:flutter/material.dart'; //[cite: 4]
import 'package:shared_preferences/shared_preferences.dart'; //[cite: 4]

void main() async {
  // 🌟 1. 플러터 엔진 초기화 보장 (여기선 가벼운 로컬 설정만 하고 바로 앱을 켭니다)
  WidgetsFlutterBinding.ensureInitialized(); //[cite: 4]

  // 🏁 무조건 앱부터 빠르게 가동시켜서 튕김을 방지합니다!
  runApp(const MyApp()); //[cite: 4]
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false, //[cite: 4]
      title: 'FearX Community', //[cite: 4]
      theme: ThemeData(primaryColor: const Color(0xFFE8282F)), //[cite: 4]
      // 💡 시작 화면을 메인이 아닌 '로딩 화면(SplashScreen)'으로 지정합니다.
      home: const SplashScreen(),
    );
  }
}