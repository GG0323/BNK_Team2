package com.example.bnk.dto.community;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data @ToString
@NoArgsConstructor
@AllArgsConstructor						// 댓글
public class CommunityReply {
	private long reply_no;				// 댓글 PK
	private long board_no;				// 게시글 FK
	private long community_account_no;	// 댓글 작성자 FK
	private String reply_content;		// 댓글 내용
	private String reply_status;		// 댓글 상태 ('ACTIVE','DELETED','HIDDEN')
	private LocalDate created_at;		// 작성일시
	private LocalDate updated_at;		// 수정일시
}
