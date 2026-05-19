package com.example.bnk.dto.community;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor						// 게시판
public class CommunityBoard {
	private long board_no;				// 게시글 PK
	private long community_account_no;	// 작성자 FK
	private String board_title;			// 게시글 제목
	private String board_content;		// 게시글 내용
	private long like_count;			// 좋아요 수
	private long view_count;			// 조회수
	private String board_status;		// 게시글 상태 ('ACTIVE','DELETED','HIDDEN')
	private LocalDate created_at;		// 작성일시
	private LocalDate updated_at;		// 수정일시
}
