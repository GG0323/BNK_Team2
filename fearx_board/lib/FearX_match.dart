import 'package:flutter/material.dart';

// 나중에 DB에서 받아올 경기 데이터 구조를 미리 정의합니다.
class MatchData {
  final String date;       // 경기 날짜 (예: "2026.05.27")
  final String time;       // 경기 시간 (예: "19:00")
  final String opponent;   // 상대 팀명 (예: "KRX")
  final String status;     // 경기 상태 (예: "UPCOMING", "LIVE", "FINISHED")
  final String? outCome;   // 경기 결과 (예: "2:1 승리" - FINISHED일 때만 사용)

  MatchData({
    required this.date,
    required this.time,
    required this.opponent,
    required this.status,
    this.outCome,
  });
}

class FearXMatchContent extends StatelessWidget {
  const FearXMatchContent({super.key});

  @override
  Widget build(BuildContext context) {
    // 💾 [가짜 데이터] 나중에 이 부분을 Java API 통신(http.get) 데이터로 교체하게 됩니다!
    final List<MatchData> mockMatches = [
      MatchData(date: "2026.05.27", time: "19:00", opponent: "KRX", status: "FINISHED", outCome: "2:1 승리"),
      MatchData(date: "2026.05.30", time: "17:00", opponent: "T1", status: "FINISHED", outCome: "0:2 패배"),
      MatchData(date: "2026.05.24", time: "15:00", opponent: "GEN", status: "FINISHED", outCome: "0:2 패배"),
      MatchData(date: "2026.05.20", time: "20:00", opponent: "DK", status: "FINISHED", outCome: "1:2 패배"),
    ];

    return Scaffold(
      backgroundColor: const Color(0xFFF5F5F5), // 기존 앱과 일치하는 연회색 배경
      body: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
        itemCount: mockMatches.length,
        itemBuilder: (context, index) {
          final match = mockMatches[index];
          return _buildMatchCard(context, match);
        },
      ),
    );
  }

  // 📇 경기 정보 한 칸을 그려주는 카드 위젯
  Widget _buildMatchCard(BuildContext context, MatchData match) {
    // 경기 상태에 따른 스타일 분기 처리
    Color statusColor = Colors.grey;
    String statusText = "경기 종료";

    if (match.status == "UPCOMING") {
      statusColor = Colors.blue;
      statusText = "경기 예정";
    } else if (match.status == "LIVE") {
      statusColor = Colors.red;
      statusText = "LIVE";
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: const [
          BoxShadow(color: Colors.black12, blurRadius: 8, offset: Offset(0, 2)),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Left: 날짜 및 시간 정보
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: statusColor.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  statusText,
                  style: TextStyle(color: statusColor, fontWeight: FontWeight.bold, fontSize: 12),
                ),
              ),
              const SizedBox(height: 10),
              Text(
                match.date,
                style: const TextStyle(color: Colors.grey, fontSize: 13),
              ),
              Text(
                match.time,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
              ),
            ],
          ),

          // Center: 대진 정보 (FearX vs 상대팀)
          Column(
            children: [
              const Text(
                "BNK FearX",
                style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
              ),
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 2),
                child: Text("VS", style: TextStyle(color: Colors.red, fontWeight: FontWeight.bold)),
              ),
              Text(
                match.opponent,
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16, color: Colors.black),
              ),
            ],
          ),

          // Right: 경기 상태에 따른 버튼 또는 결과창
          Column(
            children: [
              if (match.status == "UPCOMING")
                ElevatedButton(
                  onPressed: () {
                    // 응원하기 액션
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(content: Text('${match.opponent}전 응원 완료! 🔥')),
                    );
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.yellow,
                    foregroundColor: Colors.black,
                    elevation: 0,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  ),
                  child: const Text("응원하기"),
                )
              else if (match.status == "FINISHED")
                Text(
                  match.outCome ?? "결과 없음",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 14,
                    color: match.outCome!.contains("승리") ? Colors.red : Colors.blue,
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}