package com.example.bnk.dto.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogTransitionStatDto {
	private String from_url;
    private String to_url;
    private long transition_cnt;

}
