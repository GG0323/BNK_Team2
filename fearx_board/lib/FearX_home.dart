import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

class FearXHomeContent extends StatelessWidget {
  final Function(int)? onTabChanged;

  const FearXHomeContent({super.key, this.onTabChanged});

  // 1. 함수를 클래스 바로 아래, build 함수 위에 넣습니다.
  // URL을 여는 로직 (빨간줄 방지를 위해 Future/async 사용)
  Future<void> _launchURL() async {
    final Uri url = Uri.parse('https://fearxstore.com/');
    // 웹 환경에서도 잘 작동하도록 mode 설정을 추가합니다.
    if (!await launchUrl(url, mode: LaunchMode.externalApplication)) {
      throw Exception('Could not launch $url');
    }
  }

  @override
  Widget build(BuildContext context) {
    // LayoutBuilder: 기기마다 다른 화면 높이를 실시간으로 계산한대용~
    return LayoutBuilder(
      builder: (context, constraints) {
        return SingleChildScrollView(
          child: ConstrainedBox(
            // 컨텐츠의 최소 높이를 기기 전체 화면 높이(constraints.maxHeight)로 고정
            constraints: BoxConstraints(minHeight: constraints.maxHeight),
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 24.0),
              child: Column(
                // 위(배너), 중간(카드), 아래(공지)를 수직으로 끝과 끝에 배치하여 공간을 꽉 채움
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [

                  GestureDetector(
                    onTap: _launchURL,
                    child: _buildBanner(),
                  ),

                  // 영역 2 중앙 인기글 & 다음 경기
                  _buildCenterSection(),

                  // 영역 3 하단 공지사항 바
                  _buildNoticeCard(),

                  // 하단 바와 너무 딱 붙지 않게 살짝 띄워주는 여백
                  const SizedBox(height: 10),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

// 1. 배너 위젯 생성 함수 (이미지 넘침 방지 추가)
  Widget _buildBanner() {
    return Container(
        margin: const EdgeInsets.symmetric(horizontal: 16),
        height: 200,
        width: double.infinity,
        child: Column(
          children: [
            const Text('FearX 굿즈 샵 이동', style: TextStyle(color: Colors.black)),
            const SizedBox(height: 10),
            const Expanded(
              child: Image(
                image: AssetImage('assets/goods_logo.png'),
                fit: BoxFit.contain,
              ),
            ),
          ],
        )
    );
  }

  // 2. 중앙 레이아웃 (인기글 + 경기정보 수직 배치 완료)
  Widget _buildCenterSection() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _buildCard(
              "🔥 인기글",
              _buildPostList()
          ),
          const SizedBox(height: 15),
          _buildCard(
              "✨ 다음 경기",
              _buildMatchInfo()
          ),
        ],
      ),
    );
  }


// 공통 카드 틀 (디자인 통일성을 위해 함수화)
  Widget _buildCard(String title, Widget content) {
    return Container(
      padding: const EdgeInsets.all(14),
      height: 160,  // -> 중앙 컨텐츠 높이
      decoration: BoxDecoration(
        color: Colors.white,  // 배경색
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(  // 그림자도 넣어주고
              color: Colors.black12,
              blurRadius: 10
          )
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
              title,
              style: const TextStyle(
                  fontWeight: FontWeight.bold
              )
          ),
          const SizedBox(height: 10),
          content, // 실제 들어갈 내용 (인기글 리스트나 경기정보)
        ],
      ),
    );
  }

  // 인기글 리스트 생성 위젯
  Widget _buildPostList() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      // java로 DB에서 API 받아오고, 여기다가 리스트로 넣어야 함.
      children: const [
        Text(
            "• 제목 1 받아오기",
            overflow: TextOverflow.ellipsis
        ),
        Divider(), // 글 사이 구분선.
        Text(
            "• 제목 2 받아오기",
            overflow: TextOverflow.ellipsis
        ),
        Divider(),
        Text(
            "• 제목 3 받아오기",
            overflow: TextOverflow.ellipsis
        ),
      ],
    );
  }

  // 다음 경기 정보 생성 위젯
  // FearX_home.dart 하단에 있는 다음 경기 정보 위젯
  Widget _buildMatchInfo() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text("05/27 19:00", style: TextStyle(fontWeight: FontWeight.bold)),
        const Text("vs KRX", style: TextStyle(color: Colors.blue, fontWeight: FontWeight.bold, fontSize: 16)),
        const SizedBox(height: 10),
        ElevatedButton(
          onPressed: () {
            // 👇 생성자로 받은 리모컨을 작동시킵니다. (1번 인덱스 = 경기 메뉴)
            if (onTabChanged != null) {
              onTabChanged!(1);
            }
          },
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.yellow,
            foregroundColor: Colors.black,
            minimumSize: Size.zero,
            padding: const EdgeInsets.symmetric(horizontal: 30, vertical: 6),
            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
          ),
          child: const Text("응원 하기 ➔"),
        )
      ],
    );
  }

  // 3. 하단 공지사항 위젯
  Widget _buildNoticeCard() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.yellow),
      ),
      child: Row(
        children: [
          const Icon(Icons.campaign, color: Colors.orange, size: 30),
          const SizedBox(width: 15),
          const Expanded(
            child: Text(
                "커뮤니티 클린 캠페인 안내 (욕설을 줄여주세요!)",
                style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          const Icon(
              Icons.arrow_forward_ios,
              size: 16,
              color: Colors.grey
          ),
        ],
      ),
    );
  }

}
